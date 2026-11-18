package br.com.sportconnect.core

import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SplitCostRn04Test {
    private val split = SplitCostUseCase()

    @Test
    fun cem_reais_por_tres_sobra_um_centavo_no_organizador() {
        val r = split.calcular(Money(10_000), 3)
        assertEquals(Money(3_333), r.cotaBase)
        assertEquals(Money(1), r.sobra)
        assertEquals(Money(3_334), r.cotaOrganizador)
        assertEquals(Money(3_333), r.cotaConvidado)
        assertEquals(listOf(Money(3_334), Money(3_333), Money(3_333)), r.cotas)
    }

    @Test
    fun duzentos_e_quarenta_por_doze_divide_exato() {
        val r = split.calcular(Money(24_000), 12)
        assertEquals(Money(2_000), r.cotaOrganizador)
        assertEquals(Money.ZERO, r.sobra)
        assertTrue(r.cotas.all { it == Money(2_000) })
    }

    @Test
    fun exemplos_da_tabela_rn04() {
        split.calcular(Money(25_000), 12).let {
            assertEquals(Money(2_083), it.cotaConvidado); assertEquals(Money(4), it.sobra); assertEquals(Money(2_087), it.cotaOrganizador)
        }
        split.calcular(Money(19_999), 4).let {
            assertEquals(Money(4_999), it.cotaConvidado); assertEquals(Money(3), it.sobra); assertEquals(Money(5_002), it.cotaOrganizador)
        }
    }

    @Test
    fun uma_vaga_organizador_paga_tudo() {
        val r = split.calcular(Money(15_050), 1)
        assertEquals(listOf(Money(15_050)), r.cotas)
        assertEquals(Money.ZERO, r.sobra)
    }

    @Test
    fun invariante_soma_igual_ao_total_para_varias_combinacoes() {
        for (v in listOf(1L, 99L, 10_000L, 12_345L, 24_000L, 39_999L, 40_000L)) for (n in 1..20) {
            val r = split.calcular(Money(v), n)
            assertEquals(v, r.cotas.sumOf { it.cents }, "V=$v N=$n")
            assertTrue(r.sobra.cents in 0 until n)
        }
    }

    @Test
    fun taxa_de_conveniencia_por_metodo_fica_fora_do_rateio() {
        val pix = split.calcular(Money(24_000), 12, MetodoPagamento.PIX)
        assertEquals(Money(150), pix.taxaConveniencia)
        assertEquals(Money(2_150), pix.totalConvidado)
        val cartao = split.calcular(Money(24_000), 12, MetodoPagamento.CARTAO)
        assertEquals(Money(2_199), cartao.totalConvidado)
        assertEquals(24_000, cartao.cotas.sumOf { it.cents })
    }

    @Test
    fun cota_minima_rn06() {
        assertTrue(split.calcular(Money(6_000), 12).respeitaCotaMinima)
        assertFalse(split.calcular(Money(5_999), 12).respeitaCotaMinima)
        assertEquals(11, split.maxVagasParaCotaMinima(Money(5_999)))
    }

    @Test
    fun rejeita_valores_invalidos() {
        assertFailsWith<IllegalArgumentException> { split.calcular(Money(1_000), 0) }
        assertFailsWith<IllegalArgumentException> { split.calcular(Money(-1), 2) }
    }

    @Test
    fun money_formata_em_pt_br() {
        assertEquals("R$\u00A01.234,56", Money(123_456).format())
        assertEquals("R$\u00A00,05", Money(5).format())
        assertEquals("-R$\u00A010,00", Money(-1_000).format())
    }
}
