package br.com.sportconnect.feature.slots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.usecase.GetAvailableSlotsUseCase
import br.com.sportconnect.core.domain.usecase.GetGameUseCase
import br.com.sportconnect.core.model.Arena
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** Estado visual de um horario na grade. */
enum class SlotCellState { Available, Busy, OnHold }

data class SlotCell(
    val slot: Slot,
    val state: SlotCellState,
    /** Pre-reservado: quando libera (prazo do hold do jogo, ou fim da trava de checkout). */
    val releasesAt: Instant?,
    /** RN-12: dentro da antecedencia minima so cabe pagamento integral. */
    val fullPaymentOnly: Boolean,
)

data class SlotPickerUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val arena: Arena? = null,
    val quadra: Quadra? = null,
    val days: List<LocalDate> = emptyList(),
    val day: LocalDate? = null,
    val cells: List<SlotCell> = emptyList(),
    val selection: String? = null,
    val now: Instant = Instant.DISTANT_PAST,
) {
    val selected: SlotCell? get() = cells.firstOrNull { it.slot.id == selection }
}

sealed interface SlotPickerEvent {
    /** O horario selecionado acabou de ser tomado por outra pessoa (tempo real). */
    data object TakenLive : SlotPickerEvent
}

class SlotPickerViewModel(
    private val route: Route.SlotPicker,
    private val arenas: ArenaRepository,
    private val getSlots: GetAvailableSlotsUseCase,
    private val getGame: GetGameUseCase,
    private val holdCalculator: HoldDeadlineCalculator,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SlotPickerUiState())
    val uiState: StateFlow<SlotPickerUiState> = _uiState.asStateFlow()
    private val _events = Channel<SlotPickerEvent>(Channel.BUFFERED)
    val events: Flow<SlotPickerEvent> = _events.receiveAsFlow()
    private var job: Job? = null
    private val holdCache = mutableMapOf<String, Instant?>()

    init {
        viewModelScope.launch {
            val quadra = arenas.quadra(route.courtId)
            val arena = quadra?.let { arenas.arena(it.arenaId) }
            if (quadra == null || arena == null) {
                _uiState.update { it.copy(loading = false, error = true) }
                return@launch
            }
            val today = clock.now().toLocalDateTime(FusoSaoPaulo).date
            val days = (0..6).map { today.plus(DatePeriod(days = it)) }
            val initial = when (val d = route.date) {
                null -> today
                "tomorrow" -> days[1]
                else -> runCatching { LocalDate.parse(d) }.getOrNull() ?: today
            }
            _uiState.update { it.copy(arena = arena, quadra = quadra, days = days) }
            selectDay(initial)
        }
    }

    fun selectDay(day: LocalDate) {
        val s = _uiState.value
        val arena = s.arena ?: return
        _uiState.update { it.copy(day = day, loading = true, selection = null) }
        job?.cancel()
        job = viewModelScope.launch {
            getSlots(arena.id, day, incluirIndisponiveis = true).collect { list ->
                val now = clock.now()
                val cells = list.filter { it.quadra.id == route.courtId }.map { sd ->
                    val slot = sd.slot
                    val state = when {
                        sd.disponivel -> SlotCellState.Available
                        slot.status == SlotStatus.PRE_RESERVADO || slot.status == SlotStatus.EM_CHECKOUT -> SlotCellState.OnHold
                        else -> SlotCellState.Busy
                    }
                    val releases = if (state == SlotCellState.OnHold) releaseOf(slot) else null
                    val viable = holdCalculator.politicasViaveis(now, slot.inicio, arena.duracaoHoldHoras).isNotEmpty()
                    SlotCell(slot, state, releases, fullPaymentOnly = state == SlotCellState.Available && !viable)
                }
                val sel = _uiState.value.selection
                val taken = sel != null && cells.firstOrNull { it.slot.id == sel }?.state != SlotCellState.Available
                _uiState.update { it.copy(loading = false, cells = cells, now = now, selection = if (taken) null else it.selection) }
                if (taken) _events.send(SlotPickerEvent.TakenLive)
            }
        }
    }

    private suspend fun releaseOf(slot: Slot): Instant? {
        if (slot.status == SlotStatus.EM_CHECKOUT) return slot.checkoutAte
        val jogoId = slot.jogoId ?: return null
        return holdCache.getOrPut(jogoId) { getGame(jogoId).first()?.reserva?.prazoHold }
    }

    fun toggle(slotId: String) {
        val cell = _uiState.value.cells.firstOrNull { it.slot.id == slotId } ?: return
        if (cell.state != SlotCellState.Available) return
        val next = if (_uiState.value.selection == slotId) null else slotId
        _uiState.update { it.copy(selection = next) }
        if (next != null) {
            analytics.track(
                AnalyticsEvents.SLOT_SELECIONADO,
                mapOf("arena_id" to cell.slot.arenaId, "quadra_id" to cell.slot.quadraId, "slot_id" to cell.slot.id, "preco_centavos" to cell.slot.preco.cents),
            )
        }
    }
}
