package br.com.sportconnect.feature.arenaadmin.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.domain.usecase.CancelGameUseCase
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.localDate
import br.com.sportconnect.feature.arenaadmin.common.BookingDetailUi
import br.com.sportconnect.feature.arenaadmin.common.BookingMapper
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.common.isManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

/** Motivos de cancelamento pela arena (RN-24, `arena_booking_reason_*`). */
enum class CancelReason(val id: String) { Weather("weather"), Maintenance("maintenance"), Force("force"), Other("other") }

sealed interface BookingMessage {
    data object Cancelled : BookingMessage
    data object Released : BookingMessage
    data class Failed(val text: String?) : BookingMessage
}

data class ArenaBookingUiState(
    val load: LoadState = LoadState.Loading,
    val detail: BookingDetailUi? = null,
    val showFinancials: Boolean = true,
    val cancelDialog: Boolean = false,
    val reason: CancelReason? = null,
    val cancelling: Boolean = false,
    val message: BookingMessage? = null,
)

/**
 * Detalhe da reserva (10 §2.19). [bookingId] aceita o id do slot ou do jogo. Cancelamento por forca
 * maior via [CancelGameUseCase] (Ator.ARENA, motivo obrigatorio); reserva externa pode ser liberada.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaBookingViewModel(
    private val bookingId: String,
    private val arenas: ArenaRepository,
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val payout: PayoutCalculator,
    private val cancelGame: CancelGameUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(ArenaBookingUiState())
    val state: StateFlow<ArenaBookingUiState> = _state.asStateFlow()
    private val retry = MutableStateFlow(0)

    init {
        retry.flatMapLatest {
            flow {
                val slot = arenas.slot(bookingId) ?: games.game(bookingId)?.slotId?.let { arenas.slot(it) }
                val jogoId = slot?.jogoId ?: games.game(bookingId)?.id
                emit(slot to jogoId)
            }.flatMapLatest { (slot, jogoId) ->
                if (slot == null) return@flatMapLatest flowOf<ArenaBookingUiState?>(null)
                val quadra = arenas.quadra(slot.quadraId)
                val arena = arenas.arena(slot.arenaId)
                if (quadra == null || arena == null) return@flatMapLatest flowOf(null)
                val slotFlow = arenas.observeSlots(slot.arenaId, slot.inicio.localDate()).map { l -> l.firstOrNull { it.id == slot.id } ?: slot }
                val gameFlow = if (jogoId != null) games.observeGame(jogoId) else flowOf(null)
                combine(slotFlow, gameFlow) { s, j ->
                    val phone = j?.organizadorId?.let { auth.usuario(it)?.celular }
                    ArenaBookingUiState(
                        load = LoadState.Ready,
                        detail = BookingMapper.detail(s, quadra, j, arena.plano, payout, clock.now(), phone),
                        showFinancials = auth.isManager(),
                    )
                }
            }.catch { emit(ArenaBookingUiState(load = LoadState.Error)) }
        }.onEach { s ->
            _state.update { old ->
                if (s == null) old.copy(load = LoadState.NoArena, detail = null)
                else s.copy(cancelDialog = old.cancelDialog, reason = old.reason, cancelling = old.cancelling, message = old.message)
            }
        }.launchIn(viewModelScope)
    }

    fun retry() {
        _state.update { it.copy(load = LoadState.Loading) }
        retry.update { it + 1 }
    }

    fun openCancel() = _state.update { it.copy(cancelDialog = true, reason = null) }
    fun dismissCancel() = _state.update { it.copy(cancelDialog = false, reason = null) }
    fun selectReason(r: CancelReason) = _state.update { it.copy(reason = r) }

    /** Confirma o cancelamento; [reasonLabel] e o motivo localizado gravado na reserva (RN-24/RN-34). */
    fun confirmCancel(reasonLabel: String) {
        val jogoId = _state.value.detail?.jogoId ?: return
        if (_state.value.reason == null) return
        _state.update { it.copy(cancelling = true) }
        viewModelScope.launch {
            val r = cancelGame(jogoId, reasonLabel)
            _state.update {
                it.copy(
                    cancelling = false,
                    cancelDialog = false,
                    reason = null,
                    message = if (r.isSuccess) BookingMessage.Cancelled else BookingMessage.Failed(r.exceptionOrNull()?.message),
                )
            }
        }
    }

    /** Reserva externa (balcao/mensalista): libera o horario. */
    fun release() {
        val slotId = _state.value.detail?.slotId ?: return
        viewModelScope.launch {
            arenas.atualizarSlot(slotId) { s ->
                if (s.jogoId == null && s.status == SlotStatus.RESERVADO) s.copy(status = SlotStatus.LIVRE, reservaExterna = null) else s
            }
            _state.update { it.copy(message = BookingMessage.Released) }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
}
