package br.com.sportconnect.core

import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.domain.ReminderKind
import br.com.sportconnect.core.domain.ReminderPlanner
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** E8: quais lembretes locais valem para os jogos da pessoa. */
class ReminderPlannerTest {
    private val clock = TestClock(Instant.parse("2026-11-17T12:00:00Z"))
    private val seed = DemoData(clock)

    private fun jogoComMinhaCotaPendente(userId: String) = seed.jogos.first { j ->
        j.status == StatusReserva.AGUARDANDO_PAGAMENTOS && j.reserva.prazoCotas != null &&
            j.reserva.cotas.any { it.status == StatusCota.PENDENTE }
    }.let { j ->
        // Atribui a 1a cota pendente a [userId].
        val idx = j.reserva.cotas.indexOfFirst { it.status == StatusCota.PENDENTE }
        val cotas = j.reserva.cotas.toMutableList().also { it[idx] = it[idx].copy(atletaId = userId) }
        j.copy(reserva = j.reserva.copy(cotas = cotas))
    }

    @Test
    fun cota_pendente_gera_lembretes_de_2h_e_30min_antes_do_prazo() {
        val j = jogoComMinhaCotaPendente("eu")
        val prazo = j.reserva.prazoCotas!!
        val plan = ReminderPlanner.plan(listOf(j), "eu", prazo - 5.hours)
        assertEquals(listOf(ReminderKind.PAY_2H, ReminderKind.PAY_30M), plan.map { it.kind })
        assertEquals(prazo - 2.hours, plan[0].at)
        assertEquals(prazo - 30.minutes, plan[1].at)
    }

    @Test
    fun lembrete_que_ja_passou_nao_e_agendado() {
        val j = jogoComMinhaCotaPendente("eu")
        val prazo = j.reserva.prazoCotas!!
        val plan = ReminderPlanner.plan(listOf(j), "eu", prazo - 1.hours)
        assertEquals(listOf(ReminderKind.PAY_30M), plan.map { it.kind })
    }

    @Test
    fun quem_ja_pagou_nao_recebe_lembrete_de_pagamento() {
        val j = jogoComMinhaCotaPendente("eu")
        val pago = j.copy(reserva = j.reserva.copy(cotas = j.reserva.cotas.map { if (it.atletaId == "eu") it.copy(status = StatusCota.PAGA) else it }))
        assertTrue(ReminderPlanner.plan(listOf(pago), "eu", clock.now()).none { it.kind != ReminderKind.GAME_TOMORROW })
    }

    @Test
    fun jogo_confirmado_lembra_na_vespera() {
        val confirmado = seed.jogos.first { it.id == DemoData.JOGO_FUTSAL_CONFIRMADO }
        val plan = ReminderPlanner.plan(listOf(confirmado), DemoData.ID_ORGANIZADOR, confirmado.inicio - 48.hours)
        assertEquals(listOf(ReminderKind.GAME_TOMORROW), plan.map { it.kind })
        assertEquals(confirmado.inicio - 24.hours, plan.single().at)
    }
}
