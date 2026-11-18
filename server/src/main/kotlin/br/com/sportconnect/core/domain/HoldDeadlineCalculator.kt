package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Prazos calculados na ativacao (RN-09): H e P, imutaveis. */
data class Prazos(val hold: Instant, val cotas: Instant) {
    val temJanelaResolucao: Boolean get() = cotas < hold
}

/**
 * RN-10: `H = min(ativacao + duracao_hold, T − 2h)`, duracao_hold em [1h, 48h] (padrao 4h).
 * RN-11: P = H − 30 min (A-Pix ou C) ou P = H (A-cartao ou B).
 * RN-12: janela de coleta (P − ativacao) >= 30 min.
 */
class HoldDeadlineCalculator {

    fun calcularHold(ativacao: Instant, inicioJogo: Instant, duracaoHoldHoras: Int = HOLD_PADRAO_HORAS): Instant {
        require(duracaoHoldHoras in HOLD_MIN_HORAS..HOLD_MAX_HORAS) {
            "A duração do hold deve estar entre $HOLD_MIN_HORAS h e $HOLD_MAX_HORAS h (recebido: $duracaoHoldHoras h)."
        }
        val porDuracao = ativacao + duracaoHoldHoras.hours
        val limite = inicioJogo - ANTECEDENCIA_LIMITE_HOLD
        return minOf(porDuracao, limite)
    }

    fun temJanelaResolucao(politica: PoliticaNaoPagamento, garantia: GarantiaCobertura): Boolean = when (politica) {
        PoliticaNaoPagamento.ORGANIZADOR_COBRE -> garantia != GarantiaCobertura.CARTAO
        PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES -> true
        PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR -> false
    }

    fun calcularPrazoCotas(hold: Instant, politica: PoliticaNaoPagamento, garantia: GarantiaCobertura): Instant =
        if (temJanelaResolucao(politica, garantia)) hold - JANELA_RESOLUCAO else hold

    fun calcular(
        ativacao: Instant,
        inicioJogo: Instant,
        politica: PoliticaNaoPagamento,
        garantia: GarantiaCobertura,
        duracaoHoldHoras: Int = HOLD_PADRAO_HORAS,
    ): Prazos {
        val h = calcularHold(ativacao, inicioJogo, duracaoHoldHoras)
        return Prazos(hold = h, cotas = calcularPrazoCotas(h, politica, garantia))
    }

    /** RN-12: a politica e viavel se a janela de coleta for >= 30 min. */
    fun politicaViavel(
        ativacao: Instant,
        inicioJogo: Instant,
        politica: PoliticaNaoPagamento,
        garantia: GarantiaCobertura,
        duracaoHoldHoras: Int = HOLD_PADRAO_HORAS,
    ): Boolean {
        val prazos = calcular(ativacao, inicioJogo, politica, garantia, duracaoHoldHoras)
        return prazos.cotas - ativacao >= JANELA_COLETA_MINIMA
    }

    /** Combinacoes (politica, garantia) oferecidas; vazio => apenas pagamento integral (RN-12). */
    fun politicasViaveis(
        ativacao: Instant,
        inicioJogo: Instant,
        duracaoHoldHoras: Int = HOLD_PADRAO_HORAS,
    ): List<Pair<PoliticaNaoPagamento, GarantiaCobertura>> = listOf(
        PoliticaNaoPagamento.ORGANIZADOR_COBRE to GarantiaCobertura.PIX,
        PoliticaNaoPagamento.ORGANIZADOR_COBRE to GarantiaCobertura.CARTAO,
        PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR to GarantiaCobertura.NAO_SE_APLICA,
        PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES to GarantiaCobertura.NAO_SE_APLICA,
    ).filter { (p, g) -> politicaViavel(ativacao, inicioJogo, p, g, duracaoHoldHoras) }

    companion object {
        const val HOLD_PADRAO_HORAS = 4
        const val HOLD_MIN_HORAS = 1
        const val HOLD_MAX_HORAS = 48
        val ANTECEDENCIA_LIMITE_HOLD: Duration = 2.hours
        val JANELA_RESOLUCAO: Duration = 30.minutes
        val JANELA_COLETA_MINIMA: Duration = 30.minutes
        /** RN-08 / RN-19. */
        val TRAVA_CHECKOUT: Duration = 10.minutes
    }
}
