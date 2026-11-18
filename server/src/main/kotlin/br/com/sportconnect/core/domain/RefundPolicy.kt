package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.TipoCota

/** Linhas da matriz de reembolso (RN-25). */
enum class SituacaoReembolso(val analytics: String) {
    EXPIRACAO("expiracao"),
    CANCELAMENTO_ORGANIZADOR("cancelamento_organizador"),
    CANCELAMENTO_ARENA("cancelamento_arena"),
    SAIDA_ATLETA("saida_atleta"),
    SUBSTITUICAO("substituicao"),
    NO_SHOW("no_show"),
    PAGAMENTO_TARDIO("pagamento_tardio"),
}

data class Reembolso(
    val cotaId: String,
    val valorCota: Money,
    val taxaDevolvida: Money,
    val situacao: SituacaoReembolso,
) {
    val total: Money get() = valorCota + taxaDevolvida
    val devolveTaxa: Boolean get() = !taxaDevolvida.isZero
}

/**
 * Matriz de reembolso (RN-25):
 * - Expiracao (B / falha de A ou C), cancelamento do organizador ou da arena, pagamento tardio: 100% + taxa.
 *   Complementos/coberturas tambem 100%.
 * - Saida do atleta antes da confirmacao e substituicao: 100% da cota, taxa RETIDA.
 * - No-show: 0%.
 */
class RefundPolicy {

    fun devolveTaxa(situacao: SituacaoReembolso): Boolean = when (situacao) {
        SituacaoReembolso.EXPIRACAO,
        SituacaoReembolso.CANCELAMENTO_ORGANIZADOR,
        SituacaoReembolso.CANCELAMENTO_ARENA,
        SituacaoReembolso.PAGAMENTO_TARDIO -> true
        SituacaoReembolso.SAIDA_ATLETA,
        SituacaoReembolso.SUBSTITUICAO,
        SituacaoReembolso.NO_SHOW -> false
    }

    fun percentualCota(situacao: SituacaoReembolso, tipo: TipoCota = TipoCota.PADRAO): Int = when (situacao) {
        SituacaoReembolso.NO_SHOW -> 0
        SituacaoReembolso.SAIDA_ATLETA, SituacaoReembolso.SUBSTITUICAO ->
            if (tipo == TipoCota.COMPLEMENTO) 0 else 100
        else -> 100
    }

    /** Calcula o reembolso de uma cota PAGA; retorna null se nao ha nada a devolver. */
    fun calcular(cota: Cota, situacao: SituacaoReembolso): Reembolso? {
        if (cota.status != StatusCota.PAGA) return null
        val pct = percentualCota(situacao, cota.tipo)
        val valor = Money(cota.valor.cents * pct / 100)
        val taxa = if (devolveTaxa(situacao)) cota.taxaConveniencia else Money.ZERO
        if (valor.isZero && taxa.isZero) return null
        return Reembolso(cota.id, valor, taxa, situacao)
    }

    fun calcularTodos(cotas: List<Cota>, situacao: SituacaoReembolso): List<Reembolso> =
        cotas.mapNotNull { calcular(it, situacao) }
}
