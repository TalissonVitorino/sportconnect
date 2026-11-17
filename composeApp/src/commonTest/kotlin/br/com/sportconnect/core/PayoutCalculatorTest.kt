package br.com.sportconnect.core

import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Plano
import br.com.sportconnect.core.model.StatusRepasse
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PayoutCalculatorTest {
    private val calc = PayoutCalculator()

    @Test
    fun plano_basico_desconta_8_porcento() {
        val r = calc.calcular(Money(24_000), Plano.BASICO)
        assertEquals(Money(1_920), r.comissao)
        assertEquals(Money(22_080), r.liquido)
    }

    @Test
    fun plano_pro_desconta_4_porcento() {
        val r = calc.calcular(Money(24_000), Plano.PRO)
        assertEquals(Money(960), r.comissao)
        assertEquals(Money(23_040), r.liquido)
    }

    @Test
    fun comissao_arredonda_meio_centavo_para_cima_e_fecha_a_conta() {
        assertEquals(Money(988), calc.comissao(Money(12_345), Plano.BASICO)) // 987,6
        assertEquals(Money(494), calc.comissao(Money(12_345), Plano.PRO)) // 493,8
        val r = calc.calcular(Money(19_999), Plano.BASICO)
        assertEquals(19_999, r.comissao.cents + r.liquido.cents)
    }

    @Test
    fun liberacao_em_d_mais_1_util() {
        assertEquals(LocalDate(2026, 11, 17), calc.proximoDiaUtil(LocalDate(2026, 11, 16))) // seg -> ter
        assertEquals(LocalDate(2026, 11, 23), calc.proximoDiaUtil(LocalDate(2026, 11, 20))) // sex -> seg
        assertEquals(LocalDate(2026, 11, 23), calc.proximoDiaUtil(LocalDate(2026, 11, 21))) // sab -> seg
    }

    @Test
    fun gera_repasse_agendado() {
        val r = calc.gerarRepasse("rep-1", "a1", "j1", Money(36_000), Plano.PRO, LocalDate(2026, 11, 21))
        assertEquals(StatusRepasse.AGENDADO, r.status)
        assertEquals(Money(1_440), r.comissao)
        assertEquals(Money(34_560), r.valorLiquido)
        assertEquals(LocalDate(2026, 11, 23), r.dataPrevista)
    }
}
