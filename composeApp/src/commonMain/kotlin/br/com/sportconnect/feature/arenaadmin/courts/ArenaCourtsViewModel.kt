package br.com.sportconnect.feature.arenaadmin.courts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.QuadraEsporte
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.TipoQuadra
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.local
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.localDate
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.common.isManager
import br.com.sportconnect.feature.arenaadmin.common.observeArenaId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Clock

// ================================================================== lista

data class CourtItemUi(
    val id: String,
    val name: String,
    val tipo: TipoQuadra,
    val surface: String,
    val covered: Boolean,
    val sports: List<Sport>,
    val priceFrom: Money,
    val occupancyPct: Int,
    val active: Boolean,
)

data class ArenaCourtsUiState(
    val load: LoadState = LoadState.Loading,
    val arenaName: String = "",
    val arenaId: String? = null,
    val courts: List<CourtItemUi> = emptyList(),
    /** Operador ve a lista sem FAB nem edicao. */
    val canEdit: Boolean = true,
)

/** Quadras da arena com ocupacao do dia (10 §2.20 lista). */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaCourtsViewModel(
    private val arenas: ArenaRepository,
    private val auth: AuthRepository,
    private val store: ArenaCourtStore,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow(ArenaCourtsUiState())
    val state: StateFlow<ArenaCourtsUiState> = _state.asStateFlow()
    private val retry = MutableStateFlow(0)

    init {
        retry.flatMapLatest {
            auth.observeArenaId().flatMapLatest { arenaId ->
                if (arenaId == null) return@flatMapLatest flowOf<ArenaCourtsUiState?>(null)
                flow { emit(arenas.arena(arenaId) to arenas.quadras(arenaId)) }.flatMapLatest { (arena, base) ->
                    combine(arenas.observeSlots(arenaId, clock.now().localDate()), store.overrides) { slots, overrides ->
                        val byCourt = slots.groupBy { it.quadraId }
                        ArenaCourtsUiState(
                            load = LoadState.Ready,
                            arenaName = arena?.nome.orEmpty(),
                            arenaId = arenaId,
                            courts = store.merge(arenaId, base, overrides).map { q ->
                                val s = byCourt[q.id].orEmpty()
                                val offered = s.count { it.status != SlotStatus.BLOQUEADO }
                                val busy = s.count { it.status == SlotStatus.RESERVADO || it.status == SlotStatus.PRE_RESERVADO || it.status == SlotStatus.EM_CHECKOUT }
                                CourtItemUi(
                                    id = q.id, name = q.nome, tipo = q.tipo, surface = q.piso, covered = q.coberta,
                                    sports = q.esportes.map { it.esporte }, priceFrom = q.precoHora,
                                    occupancyPct = if (offered == 0) 0 else busy * 100 / offered, active = q.ativa,
                                )
                            },
                            canEdit = auth.isManager(),
                        )
                    }
                }
            }.catch { emit(ArenaCourtsUiState(load = LoadState.Error)) }
        }.onEach { s -> _state.value = s ?: ArenaCourtsUiState(load = LoadState.NoArena) }.launchIn(viewModelScope)
    }

    fun retry() {
        _state.update { it.copy(load = LoadState.Loading) }
        retry.update { it + 1 }
    }
}

// ================================================================== edicao

/** Celula da grade de calor (dia x hora) com o preco vigente. */
data class HeatCell(val day: DayOfWeek, val hour: Int, val open: Boolean, val peak: Boolean, val price: Money)

data class CourtEditUiState(
    val load: LoadState = LoadState.Loading,
    val isNew: Boolean = true,
    val name: String = "",
    val tipo: TipoQuadra = TipoQuadra.SOCIETY_SINTETICO,
    val surface: String = "",
    val covered: Boolean = false,
    val sports: Set<Sport> = emptySet(),
    val schedule: CourtSchedule = CourtSchedule(),
    /** Centavos em digitos (SCTextField Money). */
    val priceDigits: String = "",
    val peakPriceDigits: String = "",
    val showErrors: Boolean = false,
    val heatmapAsList: Boolean = false,
    val saving: Boolean = false,
    val event: CourtEditEvent? = null,
) {
    val price: Money get() = Money(priceDigits.toLongOrNull() ?: 0)
    val peakPrice: Money get() = Money(peakPriceDigits.toLongOrNull() ?: 0)
    val nameError: Boolean get() = name.isBlank()
    val sportsError: Boolean get() = sports.isEmpty()
    val hoursError: Boolean get() = schedule.closeHour <= schedule.openHour
    /** Faixa de pico invalida: inicio >= fim ou fora do funcionamento (`arena_court_error_overlap`). */
    val overlapError: Boolean
        get() = schedule.peakStartHour >= schedule.peakEndHour ||
            schedule.peakStartHour < schedule.openHour || schedule.peakEndHour > schedule.closeHour
    val priceError: Boolean get() = price.cents <= 0 || peakPrice.cents <= 0
    val isValid: Boolean get() = !nameError && !sportsError && !hoursError && !overlapError && !priceError

    /** Horarios gerados nos proximos [PREVIEW_DAYS] dias (`arena_court_generated_preview`). */
    fun previewCount(today: LocalDate): Int {
        if (hoursError) return 0
        val perDay = (schedule.closeHour - schedule.openHour) * 60 / schedule.durationMinutes
        return (0 until PREVIEW_DAYS).count { today.plus(DatePeriod(days = it)).dayOfWeek in schedule.days } * perDay
    }

    fun heatmap(): List<HeatCell> = DayOfWeek.entries.flatMap { d ->
        (HEAT_FIRST..HEAT_LAST).map { h ->
            val open = d in schedule.days && h >= schedule.openHour && h < schedule.closeHour
            val peak = open && h >= schedule.peakStartHour && h < schedule.peakEndHour
            HeatCell(d, h, open, peak, if (peak) peakPrice else price)
        }
    }

    companion object {
        const val PREVIEW_DAYS = 14
        const val HEAT_FIRST = 6
        const val HEAT_LAST = 23
    }
}

sealed interface CourtEditEvent {
    data object Saved : CourtEditEvent
    data class Published(val slots: Int) : CourtEditEvent
}

/**
 * Cadastro/edicao de quadra (10 §2.20 detalhe): dados, esportes (RN-05), funcionamento, faixas de preco
 * com grade de calor e publicacao de horarios com previa.
 */
class ArenaCourtEditViewModel(
    private val courtId: String?,
    private val arenas: ArenaRepository,
    private val auth: AuthRepository,
    private val store: ArenaCourtStore,
    private val analytics: AnalyticsTracker?,
    private val clock: Clock,
) : ViewModel() {
    private val _state = MutableStateFlow(CourtEditUiState(isNew = courtId == null))
    val state: StateFlow<CourtEditUiState> = _state.asStateFlow()
    private var original: Quadra? = null
    private var arenaId: String? = null

    init {
        viewModelScope.launch {
            arenaId = auth.observeArenaId().first()
            val q = courtId?.let { id -> store.overrides.value[id] ?: arenas.quadra(id) }
            original = q
            _state.value = if (q == null) {
                CourtEditUiState(load = if (arenaId == null) LoadState.NoArena else LoadState.Ready, isNew = true)
            } else {
                CourtEditUiState(
                    load = LoadState.Ready,
                    isNew = false,
                    name = q.nome,
                    tipo = q.tipo,
                    surface = q.piso,
                    covered = q.coberta,
                    sports = q.esportes.map { it.esporte }.toSet(),
                    schedule = store.schedule(q.id) ?: CourtSchedule(),
                    priceDigits = q.precoHora.cents.toString(),
                    peakPriceDigits = q.precoHoraPico.cents.toString(),
                )
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setTipo(v: TipoQuadra) = _state.update { it.copy(tipo = v) }
    fun setSurface(v: String) = _state.update { it.copy(surface = v) }
    fun setCovered(v: Boolean) = _state.update { it.copy(covered = v) }
    fun toggleSport(s: Sport) = _state.update { it.copy(sports = if (s in it.sports) it.sports - s else it.sports + s) }
    fun toggleDay(d: DayOfWeek) = _state.update { st ->
        val days = st.schedule.days
        st.copy(schedule = st.schedule.copy(days = if (d in days) days - d else days + d))
    }
    fun setOpen(h: Int) = _state.update { it.copy(schedule = it.schedule.copy(openHour = h.coerceIn(0, 23))) }
    fun setClose(h: Int) = _state.update { it.copy(schedule = it.schedule.copy(closeHour = h.coerceIn(1, 24))) }
    fun setDuration(m: Int) = _state.update { it.copy(schedule = it.schedule.copy(durationMinutes = m)) }
    fun setPeakStart(h: Int) = _state.update { it.copy(schedule = it.schedule.copy(peakStartHour = h.coerceIn(0, 23))) }
    fun setPeakEnd(h: Int) = _state.update { it.copy(schedule = it.schedule.copy(peakEndHour = h.coerceIn(1, 24))) }
    fun setPrice(d: String) = _state.update { it.copy(priceDigits = d) }
    fun setPeakPrice(d: String) = _state.update { it.copy(peakPriceDigits = d) }
    fun toggleHeatmapView() = _state.update { it.copy(heatmapAsList = !it.heatmapAsList) }
    fun consumeEvent() = _state.update { it.copy(event = null) }

    private fun buildQuadra(s: CourtEditUiState): Quadra? {
        val arena = arenaId ?: return null
        val id = original?.id ?: store.newId(arena)
        return Quadra(
            id = id,
            arenaId = arena,
            nome = s.name.trim(),
            tipo = s.tipo,
            piso = s.surface.trim(),
            coberta = s.covered,
            esportes = s.sports.sortedBy { it.ordinal }.map { sp ->
                QuadraEsporte(sp, original?.capacidadePara(sp) ?: sp.maxVagas)
            },
            precoHora = s.price,
            precoHoraPico = s.peakPrice,
            ativa = original?.ativa ?: true,
        )
    }

    /** Salva (validando). Retorna a quadra salva ou null se invalida. */
    fun save(): Quadra? {
        val s = _state.value
        if (!s.isValid) {
            _state.update { it.copy(showErrors = true) }
            return null
        }
        val q = buildQuadra(s) ?: return null
        original = q
        store.save(q, s.schedule)
        if (s.isNew) analytics?.track(AnalyticsEvents.QUADRA_CADASTRADA, mapOf("quadra_id" to q.id, "esportes" to s.sports.joinToString { it.name }))
        _state.update { it.copy(event = CourtEditEvent.Saved, isNew = false) }
        return q
    }

    /**
     * Publica a grade: salva e aplica os precos (normal/pico) aos horarios LIVRES ja existentes dos
     * proximos dias. Horarios reservados mantem o preco (`arena_court_price_change_note`, RN-02).
     */
    fun publish() {
        val q = save() ?: return
        val s = _state.value
        _state.update { it.copy(saving = true, event = null) }
        viewModelScope.launch {
            val today = clock.now().localDate()
            var updated = 0
            for (d in 0 until CourtEditUiState.PREVIEW_DAYS) {
                val date = today.plus(DatePeriod(days = d))
                arenas.observeSlots(q.arenaId, date).first().filter { it.quadraId == q.id && it.status == SlotStatus.LIVRE }.forEach { slot ->
                    val h = slot.inicio.local().hour
                    val peak = h >= s.schedule.peakStartHour && h < s.schedule.peakEndHour
                    arenas.atualizarSlot(slot.id) { it.copy(preco = if (peak) q.precoHoraPico else q.precoHora) }
                    updated++
                }
            }
            store.markPublished(q.id, today.plus(DatePeriod(days = CourtEditUiState.PREVIEW_DAYS)))
            analytics?.track(AnalyticsEvents.GRADE_HORARIOS_PUBLICADA, mapOf("quadra_id" to q.id, "horarios" to s.previewCount(today)))
            _state.update { it.copy(saving = false, event = CourtEditEvent.Published(s.previewCount(today))) }
        }
    }

    fun today(): LocalDate = clock.now().localDate()
}
