package br.com.sportconnect.feature.arenaadmin.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.ArenaDashboardUseCase
import br.com.sportconnect.core.domain.usecase.PainelArena
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellStatus
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellUi
import br.com.sportconnect.feature.arenaadmin.common.AgendaMapper
import br.com.sportconnect.feature.arenaadmin.common.AgendaMode
import br.com.sportconnect.feature.arenaadmin.common.ArenaAgenda
import br.com.sportconnect.feature.arenaadmin.common.ArenaBlockController
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.local
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.localDate
import br.com.sportconnect.feature.arenaadmin.common.ArenaMessage
import br.com.sportconnect.feature.arenaadmin.common.BlockReason
import br.com.sportconnect.feature.arenaadmin.common.BlockSheetUi
import br.com.sportconnect.feature.arenaadmin.common.BookingDetailUi
import br.com.sportconnect.feature.arenaadmin.common.BookingMapper
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.common.isManager
import br.com.sportconnect.feature.arenaadmin.common.observeArenaId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.Instant

data class WeekDayUi(val date: LocalDate, val label: String, val isToday: Boolean)

/** Semana de UMA quadra: 7 colunas (dias) x horas. [cells][dia][hora]. */
data class WeekAgendaUi(
    val courtId: String,
    val courtName: String,
    val days: List<WeekDayUi>,
    val hours: List<Int>,
    val cells: List<List<AgendaCellUi>>,
) {
    /** Reaproveita a grade do dia: cada dia vira uma "coluna". */
    fun asGrid(): ArenaAgenda = ArenaAgenda(
        hours = hours,
        courts = days.mapIndexed { i, d ->
            br.com.sportconnect.feature.arenaadmin.common.CourtAgendaUi(
                courtId = d.date.toString(), name = d.label,
                mainSport = br.com.sportconnect.core.model.Sport.FUTEBOL_SOCIETY, sports = emptyList(), cells = cells[i],
            )
        },
    )
}

data class ArenaScheduleUiState(
    val load: LoadState = LoadState.Loading,
    val mode: AgendaMode = AgendaMode.Day,
    val date: LocalDate? = null,
    val dateLabel: String = "",
    val weekLabel: String = "",
    val isToday: Boolean = true,
    val nowMinutes: Int = 0,
    val agenda: ArenaAgenda = ArenaAgenda.EMPTY,
    /** Quadra visivel no compact (abas) e na semana (seletor). */
    val courtId: String? = null,
    val week: WeekAgendaUi? = null,
    val selectedSlotId: String? = null,
    val selected: BookingDetailUi? = null,
    val showFinancials: Boolean = true,
    val block: BlockSheetUi? = null,
    /** Horario bloqueado tocado: pergunta se libera. */
    val unblock: AgendaCellUi? = null,
    val message: ArenaMessage? = null,
) {
    val courtCells: List<AgendaCellUi>
        get() = agenda.courts.firstOrNull { it.courtId == courtId }?.cells.orEmpty().filter { it.status != AgendaCellStatus.Closed }
    val isEmptyDay: Boolean get() = load == LoadState.Ready && agenda.courts.none { c -> c.cells.any { it.isBooking } }
}

/**
 * Agenda da arena (10 §2.18): dia (todas as quadras) ou semana (uma quadra), navegacao por data,
 * bloqueio com motivo e desbloqueio; detalhe lateral no expanded.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaScheduleViewModel(
    private val dashboard: ArenaDashboardUseCase,
    private val arenas: ArenaRepository,
    private val auth: AuthRepository,
    private val payout: PayoutCalculator,
    private val analytics: AnalyticsTracker?,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(ArenaScheduleUiState())
    val state: StateFlow<ArenaScheduleUiState> = _state.asStateFlow()

    private val date = MutableStateFlow(clock.now().localDate())
    private val mode = MutableStateFlow(AgendaMode.Day)
    private val courtId = MutableStateFlow<String?>(null)
    private val selectedSlot = MutableStateFlow<String?>(null)
    private val now = MutableStateFlow(clock.now())
    private val retry = MutableStateFlow(0)

    private val blocker = ArenaBlockController(arenas, analytics, viewModelScope) { msg -> _state.update { it.copy(message = msg) } }

    private data class Params(val date: LocalDate, val mode: AgendaMode, val court: String?, val sel: String?, val now: Instant)

    init {
        blocker.sheet.onEach { s -> _state.update { it.copy(block = s) } }.launchIn(viewModelScope)
        val params = combine(date, mode, courtId, selectedSlot, now) { d, m, c, s, n -> Params(d, m, c, s, n) }
        retry.flatMapLatest {
            auth.observeArenaId().flatMapLatest { arenaId ->
                if (arenaId == null) flowOf<ArenaScheduleUiState?>(null)
                else flow { emit(arenas.quadras(arenaId).filter { it.ativa }.sortedBy { it.nome }) }
                    .flatMapLatest { quadras -> params.flatMapLatest { p -> load(arenaId, quadras, p) } }
            }.catch { emit(_state.value.copy(load = LoadState.Error)) }
        }.onEach { s ->
            _state.update { old -> s?.copy(block = old.block, message = old.message, unblock = old.unblock) ?: old.copy(load = LoadState.NoArena) }
        }.launchIn(viewModelScope)
    }

    private fun weekStart(d: LocalDate): LocalDate = d.minus(DatePeriod(days = d.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal))

    private fun load(arenaId: String, quadras: List<Quadra>, p: Params): Flow<ArenaScheduleUiState> {
        val hoje = p.now.localDate()
        val court = p.court?.takeIf { id -> quadras.any { it.id == id } } ?: quadras.firstOrNull()?.id
        return if (p.mode == AgendaMode.Day) {
            dashboard(arenaId, p.date).combine(flowOf(Unit)) { painel, _ -> dayState(painel, quadras, p, court, hoje) }
        } else {
            val start = weekStart(p.date)
            val days = (0..6).map { start.plus(DatePeriod(days = it)) }
            combine(days.map { dashboard(arenaId, it) }) { painels -> weekState(painels.toList(), quadras, p, court, hoje, days) }
        }
    }

    private fun base(p: Params, court: String?, hoje: LocalDate): ArenaScheduleUiState {
        val start = weekStart(p.date)
        val local = p.now.local()
        return ArenaScheduleUiState(
            load = LoadState.Ready,
            mode = p.mode,
            date = p.date,
            dateLabel = ArenaFormat.dayLabel(p.date),
            weekLabel = "${ArenaFormat.dayMonth(start)} – ${ArenaFormat.dayMonth(start.plus(DatePeriod(days = 6)))}",
            isToday = p.date == hoje,
            nowMinutes = local.hour * 60 + local.minute,
            courtId = court,
            showFinancials = auth.isManager(),
        )
    }

    private fun dayState(painel: PainelArena, quadras: List<Quadra>, p: Params, court: String?, hoje: LocalDate): ArenaScheduleUiState {
        val agenda = AgendaMapper.agenda(quadras, painel.agenda, p.now)
        blocker.updateAgenda(agenda)
        val selected = p.sel?.let { id -> painel.agenda.firstOrNull { it.slot.id == id } }?.let {
            BookingMapper.detail(it.slot, it.quadra, it.jogo, painel.arena.plano, payout, p.now)
        }
        return base(p, court, hoje).copy(agenda = agenda, selectedSlotId = selected?.slotId, selected = selected)
    }

    private fun weekState(
        painels: List<PainelArena>,
        quadras: List<Quadra>,
        p: Params,
        court: String?,
        hoje: LocalDate,
        days: List<LocalDate>,
    ): ArenaScheduleUiState {
        val q = quadras.filter { it.id == court }
        val perDay = painels.map { AgendaMapper.agenda(q, it.agenda.filter { item -> item.quadra.id == court }, p.now) }
        val hours = perDay.flatMap { it.hours }.distinct().sorted()
        val cells = perDay.map { ag ->
            val byHour = ag.courts.firstOrNull()?.cells.orEmpty().associateBy { it.hour }
            hours.map { h -> byHour[h] ?: AgendaCellUi(court.orEmpty(), h, AgendaCellStatus.Closed) }
        }
        // O dia selecionado alimenta o sheet de bloqueio.
        val dayIdx = days.indexOf(p.date).coerceAtLeast(0)
        blocker.updateAgenda(AgendaMapper.agenda(quadras, painels[dayIdx].agenda, p.now))
        val week = WeekAgendaUi(
            courtId = court.orEmpty(),
            courtName = q.firstOrNull()?.nome.orEmpty(),
            days = days.map { WeekDayUi(it, "${ArenaFormat.weekdayShort(it.dayOfWeek)} ${it.day}", it == hoje) },
            hours = hours,
            cells = cells,
        )
        val selected = p.sel?.let { id -> painels.flatMap { it.agenda }.firstOrNull { it.slot.id == id } }?.let {
            BookingMapper.detail(it.slot, it.quadra, it.jogo, painels.first().arena.plano, payout, p.now)
        }
        return base(p, court, hoje).copy(agenda = AgendaMapper.agenda(quadras, painels[dayIdx].agenda, p.now), week = week, selectedSlotId = selected?.slotId, selected = selected)
    }

    // ------------------------------------------------------------------ acoes

    fun setMode(m: AgendaMode) { mode.value = m; selectedSlot.value = null }
    fun previous() = shift(-1)
    fun next() = shift(1)
    fun today() { date.value = clock.now().localDate(); selectedSlot.value = null }
    private fun shift(sign: Int) {
        val step = if (mode.value == AgendaMode.Week) 7 else 1
        date.value = date.value.plus(DatePeriod(days = sign * step))
        selectedSlot.value = null
    }
    fun goTo(d: LocalDate) { date.value = d }
    fun selectCourt(id: String) { courtId.value = id }
    fun refreshNow() { now.value = clock.now() }
    fun retry() {
        _state.update { it.copy(load = LoadState.Loading) }
        retry.update { it + 1 }
    }

    /**
     * Toque numa celula: reserva -> detalhe (retorna true se a UI deve navegar quando nao ha painel
     * lateral); livre -> sheet de bloqueio; bloqueado -> pergunta se libera.
     */
    fun onCellClick(cell: AgendaCellUi) {
        val id = cell.slotId ?: return
        when {
            cell.isBooking -> selectedSlot.value = id
            cell.status == AgendaCellStatus.Free -> blocker.open(slotId = id)
            cell.status == AgendaCellStatus.Blocked -> _state.update { it.copy(unblock = cell) }
        }
    }

    fun clearSelection() { selectedSlot.value = null }
    fun openBlock() = blocker.open(courtId = _state.value.courtId)
    fun selectBlockCourt(courtId: String) = blocker.selectCourt(courtId)
    fun selectBlockSlot(slotId: String) = blocker.selectSlot(slotId)
    fun selectBlockReason(reason: BlockReason) = blocker.selectReason(reason)
    fun dismissBlock() = blocker.dismiss()
    fun confirmBlock(reasonLabel: String) = blocker.confirm(reasonLabel)
    fun undoBlock(slotId: String) = blocker.unblock(slotId)
    fun dismissUnblock() = _state.update { it.copy(unblock = null) }
    fun confirmUnblock() {
        val id = _state.value.unblock?.slotId ?: return
        _state.update { it.copy(unblock = null) }
        blocker.unblock(id, announce = true)
    }
    fun consumeMessage() = _state.update { it.copy(message = null) }
}
