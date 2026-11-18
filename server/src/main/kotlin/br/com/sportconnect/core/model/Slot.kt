package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Estados do slot (PRD §9.3). */
@Serializable
enum class SlotStatus { LIVRE, EM_CHECKOUT, PRE_RESERVADO, RESERVADO, BLOQUEADO }

@Serializable
data class Slot(
    val id: String,
    val arenaId: String,
    val quadraId: String,
    val inicio: Instant,
    val fim: Instant,
    /** Preco vigente; congelado na reserva ao iniciar o checkout (RN-02). */
    val preco: Money,
    val status: SlotStatus = SlotStatus.LIVRE,
    /** RN-08: fim do bloqueio exclusivo de checkout do organizador. */
    val checkoutAte: Instant? = null,
    val motivoBloqueio: String? = null,
    /** Jogo que ocupa o slot (reserva nao terminal), se houver. */
    val jogoId: String? = null,
    /** Reserva externa (balcao/telefone) — ocupa sem jogo na plataforma. */
    val reservaExterna: String? = null,
) {
    /** Livre para reservar em [agora] (checkout expirado conta como livre — RN-08). */
    fun disponivelEm(agora: Instant): Boolean = inicio > agora && when (status) {
        SlotStatus.LIVRE -> true
        SlotStatus.EM_CHECKOUT -> checkoutAte != null && checkoutAte <= agora
        else -> false
    }
}
