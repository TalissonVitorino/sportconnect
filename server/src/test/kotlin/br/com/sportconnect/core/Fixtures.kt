package br.com.sportconnect.core

import br.com.sportconnect.core.domain.EventoReserva
import br.com.sportconnect.core.domain.ReservationStateMachine
import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.Reserva
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusCota
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class TestClock(var agora: Instant) : Clock {
    override fun now(): Instant = agora
}

object Fixtures {
    /** Sexta 20/11/2026 10:00 em Sao Paulo. */
    val SEXTA_10H: Instant = Instant.parse("2026-11-20T13:00:00Z")
    /** Sabado 21/11/2026 20:00 em Sao Paulo (T dos exemplos da RN-10). */
    val SABADO_20H: Instant = Instant.parse("2026-11-21T23:00:00Z")

    const val ORG = "u-org"

    fun rascunho(
        vagas: Int = 12,
        valor: Money = Money(24_000),
        esporte: Sport = Sport.FUTEBOL_SOCIETY,
        politica: PoliticaNaoPagamento = PoliticaNaoPagamento.ORGANIZADOR_COBRE,
        garantia: GarantiaCobertura = if (politica == PoliticaNaoPagamento.ORGANIZADOR_COBRE) GarantiaCobertura.PIX else GarantiaCobertura.NAO_SE_APLICA,
        inicio: Instant = SABADO_20H,
        teto: Int = 50,
    ): Jogo {
        val cotas = SplitCostUseCase()(valor, vagas).mapIndexed { i, v ->
            if (i == 0) Cota("r-c0", v, organizador = true, atletaId = ORG, atletaNome = "Org") else Cota("r-c$i", v)
        }
        return Jogo(
            id = "j1", titulo = "Teste", organizadorId = ORG, organizadorNome = "Org", esporte = esporte,
            arenaId = "a1", arenaNome = "Arena", quadraId = "q1", quadraNome = "Quadra", slotId = "s1",
            inicio = inicio, fim = inicio + 1.hours, numeroVagas = vagas, politica = politica, garantia = garantia,
            tetoComplementoPct = teto, codigoConvite = "ABC123", reserva = Reserva(id = "r", valorTotal = valor, cotas = cotas),
            criadoEm = SEXTA_10H - 5.minutes,
        )
    }

    /** Rascunho -> cota do organizador paga -> ativado em [agora]. */
    fun ativo(
        sm: ReservationStateMachine,
        agora: Instant = SEXTA_10H,
        holdHoras: Int = 4,
        base: Jogo = rascunho(),
    ): Jogo {
        val pago = sm.aplicar(base, EventoReserva.CotaPaga("r-c0", agora, ORG, "Org", pagamentoId = "pg-org")).jogo
        return sm.aplicar(pago, EventoReserva.Ativar(agora, holdHoras)).jogo
    }

    /** Paga [n] cotas PENDENTE com atletas distintos. */
    fun pagar(sm: ReservationStateMachine, jogo: Jogo, n: Int, agora: Instant = SEXTA_10H + 30.minutes, metodo: MetodoPagamento = MetodoPagamento.PIX): Jogo {
        var j = jogo
        repeat(n) { i ->
            val cota = j.reserva.cotas.first { it.status == StatusCota.PENDENTE }
            j = sm.aplicar(j, EventoReserva.CotaPaga(cota.id, agora, "u-$i-${cota.id}", "Atleta $i", metodo, "pg-${cota.id}")).jogo
        }
        return j
    }
}
