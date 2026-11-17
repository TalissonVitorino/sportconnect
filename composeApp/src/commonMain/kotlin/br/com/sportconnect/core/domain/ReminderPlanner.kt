package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Lembretes locais do aparelho (E8, US-046). Os que dependem do servidor ficam para o push remoto. */
enum class ReminderKind { PAY_2H, PAY_30M, GAME_TOMORROW }

data class PlannedReminder(val id: String, val kind: ReminderKind, val gameId: String, val at: Instant)

/**
 * Decide quais lembretes valem agora para quem esta logado:
 * - cota PENDENTE minha em jogo aguardando pagamentos: 2h e 30min antes do prazo das cotas (RN-09);
 * - jogo CONFIRMADO em que estou: 24h antes do inicio ("Amanha tem jogo").
 * So entra o que ainda esta no futuro; pagar/cancelar faz o lembrete sumir no proximo replanejamento.
 */
object ReminderPlanner {
    fun plan(jogos: List<Jogo>, userId: String, now: Instant): List<PlannedReminder> = buildList {
        jogos.forEach { j ->
            val prazo = j.reserva.prazoCotas
            val minhaPendente = j.reserva.cotas.any { it.atletaId == userId && it.status == StatusCota.PENDENTE }
            if (j.status == StatusReserva.AGUARDANDO_PAGAMENTOS && minhaPendente && prazo != null) {
                add(PlannedReminder("pay2h-${j.id}", ReminderKind.PAY_2H, j.id, prazo - 2.hours))
                add(PlannedReminder("pay30-${j.id}", ReminderKind.PAY_30M, j.id, prazo - 30.minutes))
            }
            if (j.status == StatusReserva.CONFIRMADO && j.participa(userId)) {
                add(PlannedReminder("tomorrow-${j.id}", ReminderKind.GAME_TOMORROW, j.id, j.inicio - 24.hours))
            }
        }
    }.filter { it.at > now }
}
