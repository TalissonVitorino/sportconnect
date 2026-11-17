package br.com.sportconnect.feature.arenaadmin.common

import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.model.SlotStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Motivos de bloqueio (10 §2.17, `arena_block_reason_*`). */
enum class BlockReason(val id: String) { Phone("phone"), Maintenance("maintenance"), Class("class"), Other("other") }

data class BlockOption(val slotId: String, val label: String)

/** Estado do sheet "Bloquear horario": quadra + horario pre-preenchidos e motivo obrigatorio. */
data class BlockSheetUi(
    val courts: List<Pair<String, String>>,
    val courtId: String,
    val slots: List<BlockOption>,
    val slotId: String?,
    val reason: BlockReason? = null,
    val submitting: Boolean = false,
) {
    val canConfirm: Boolean get() = slotId != null && reason != null && !submitting
}

/** Mensagens transitorias (snackbar/dialogo) emitidas pelas telas da arena. */
sealed interface ArenaMessage {
    data class Blocked(val slotId: String) : ArenaMessage
    data object Unblocked : ArenaMessage
    /** Tentou bloquear horario com reserva ativa (`error_block_active_booking_*`). */
    data class BlockHasBooking(val slotId: String) : ArenaMessage
    data object Failure : ArenaMessage
}

/**
 * Logica de bloqueio/desbloqueio compartilhada por Painel e Agenda (UX6: bloquear custa <= 2 toques).
 * So bloqueia slot LIVRE (ou checkout expirado); se outro processo reservou nesse meio-tempo,
 * devolve [ArenaMessage.BlockHasBooking].
 */
class ArenaBlockController(
    private val arenas: ArenaRepository,
    private val analytics: AnalyticsTracker?,
    private val scope: CoroutineScope,
    private val onMessage: (ArenaMessage) -> Unit,
) {
    private val _sheet = MutableStateFlow<BlockSheetUi?>(null)
    val sheet: StateFlow<BlockSheetUi?> = _sheet.asStateFlow()

    private var agenda: ArenaAgenda = ArenaAgenda.EMPTY

    fun updateAgenda(agenda: ArenaAgenda) {
        this.agenda = agenda
        _sheet.update { s -> s?.let { rebuild(it.courtId, it.slotId, it.reason) } }
    }

    private fun freeOptions(courtId: String): List<BlockOption> =
        agenda.courts.firstOrNull { it.courtId == courtId }?.cells.orEmpty()
            .filter { it.status == AgendaCellStatus.Free && it.slotId != null }
            .map { BlockOption(it.slotId!!, it.rangeLabel) }

    private fun rebuild(courtId: String, slotId: String?, reason: BlockReason?): BlockSheetUi {
        val options = freeOptions(courtId)
        return BlockSheetUi(
            courts = agenda.courts.map { it.courtId to it.name },
            courtId = courtId,
            slots = options,
            slotId = slotId?.takeIf { id -> options.any { it.slotId == id } } ?: options.firstOrNull()?.slotId,
            reason = reason,
        )
    }

    /** Abre o sheet. Com [slotId], valida antes: se o horario tem reserva, emite BlockHasBooking. */
    fun open(slotId: String? = null, courtId: String? = null) {
        if (slotId != null) {
            val cell = agenda.cell(slotId)
            if (cell != null && cell.isBooking) {
                onMessage(ArenaMessage.BlockHasBooking(slotId))
                return
            }
        }
        val court = courtId ?: slotId?.let { agenda.cell(it)?.courtId } ?: agenda.courts.firstOrNull()?.courtId ?: return
        _sheet.value = rebuild(court, slotId, null)
    }

    fun selectCourt(courtId: String) = _sheet.update { it?.let { rebuild(courtId, null, it.reason) } }
    fun selectSlot(slotId: String) = _sheet.update { it?.copy(slotId = slotId) }
    fun selectReason(reason: BlockReason) = _sheet.update { it?.copy(reason = reason) }
    fun dismiss() { _sheet.value = null }

    /** Confirma o bloqueio; [reasonLabel] e o texto do motivo ja localizado (gravado no slot). */
    fun confirm(reasonLabel: String) {
        val s = _sheet.value ?: return
        val slotId = s.slotId ?: return
        if (s.reason == null) return
        _sheet.value = s.copy(submitting = true)
        scope.launch {
            var hadBooking = false
            val result = arenas.atualizarSlot(slotId) { slot ->
                if (slot.status == SlotStatus.LIVRE || slot.status == SlotStatus.EM_CHECKOUT && slot.jogoId == null) {
                    slot.copy(status = SlotStatus.BLOQUEADO, motivoBloqueio = reasonLabel, checkoutAte = null)
                } else {
                    hadBooking = true
                    slot
                }
            }
            _sheet.value = null
            when {
                result == null -> onMessage(ArenaMessage.Failure)
                hadBooking -> onMessage(ArenaMessage.BlockHasBooking(slotId))
                else -> {
                    analytics?.track(AnalyticsEvents.HORARIO_BLOQUEADO, mapOf("slot_id" to slotId, "motivo" to s.reason.id))
                    onMessage(ArenaMessage.Blocked(slotId))
                }
            }
        }
    }

    /** Desfaz um bloqueio (snackbar "Desfazer" ou toque em horario bloqueado). */
    fun unblock(slotId: String, announce: Boolean = false) {
        scope.launch {
            arenas.atualizarSlot(slotId) { slot ->
                if (slot.status == SlotStatus.BLOQUEADO) slot.copy(status = SlotStatus.LIVRE, motivoBloqueio = null) else slot
            }
            if (announce) onMessage(ArenaMessage.Unblocked)
        }
    }
}
