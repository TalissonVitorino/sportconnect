package br.com.sportconnect.core

import br.com.sportconnect.core.domain.RefundPolicy
import br.com.sportconnect.core.domain.SituacaoReembolso
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.TipoCota
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RefundPolicyTest {
    private val policy = RefundPolicy()
    private val paga = Cota("c1", Money(2_000), taxaConveniencia = Money(150), status = StatusCota.PAGA)

    @Test
    fun situacoes_que_devolvem_cota_e_taxa() {
        for (s in listOf(
            SituacaoReembolso.EXPIRACAO, SituacaoReembolso.CANCELAMENTO_ORGANIZADOR,
            SituacaoReembolso.CANCELAMENTO_ARENA, SituacaoReembolso.PAGAMENTO_TARDIO,
        )) {
            val r = policy.calcular(paga, s)!!
            assertEquals(Money(2_000), r.valorCota, s.name)
            assertEquals(Money(150), r.taxaDevolvida, s.name)
            assertEquals(Money(2_150), r.total, s.name)
        }
    }

    @Test
    fun saida_e_substituicao_devolvem_cota_e_retem_taxa() {
        for (s in listOf(SituacaoReembolso.SAIDA_ATLETA, SituacaoReembolso.SUBSTITUICAO)) {
            val r = policy.calcular(paga, s)!!
            assertEquals(Money(2_000), r.total, s.name)
            assertEquals(Money.ZERO, r.taxaDevolvida, s.name)
        }
    }

    @Test
    fun no_show_nao_gera_reembolso() {
        assertNull(policy.calcular(paga, SituacaoReembolso.NO_SHOW))
    }

    @Test
    fun cota_nao_paga_nao_gera_reembolso() {
        assertNull(policy.calcular(paga.copy(status = StatusCota.PENDENTE), SituacaoReembolso.EXPIRACAO))
        assertNull(policy.calcular(paga.copy(status = StatusCota.REEMBOLSADA), SituacaoReembolso.CANCELAMENTO_ARENA))
    }

    @Test
    fun complemento_e_cobertura_sao_100_porcento_em_expiracao() {
        val comp = Cota("c1-comp", Money(400), tipo = TipoCota.COMPLEMENTO, status = StatusCota.PAGA)
        assertEquals(Money(400), policy.calcular(comp, SituacaoReembolso.EXPIRACAO)!!.total)
        assertEquals(2, policy.calcularTodos(listOf(paga, comp, paga.copy(id = "c2", status = StatusCota.PENDENTE)), SituacaoReembolso.EXPIRACAO).size)
    }
}
