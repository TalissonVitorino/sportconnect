package br.com.sportconnect.core

import br.com.sportconnect.core.Fixtures.SABADO_20H
import br.com.sportconnect.core.Fixtures.SEXTA_10H
import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class HoldDeadlineCalculatorTest {
    private val calc = HoldDeadlineCalculator()

    @Test
    fun exemplo_rn10_ativado_sexta_10h_hold_ate_14h() {
        assertEquals(SEXTA_10H + 4.hours, calc.calcularHold(SEXTA_10H, SABADO_20H))
    }

    @Test
    fun exemplo_rn10_ativado_sabado_16h_limitado_a_t_menos_2h() {
        val sabado16h = SABADO_20H - 4.hours
        assertEquals(SABADO_20H - 2.hours, calc.calcularHold(sabado16h, SABADO_20H))
    }

    @Test
    fun hold_nunca_ultrapassa_t_menos_2h_mesmo_com_48h() {
        val h = calc.calcularHold(SEXTA_10H, SABADO_20H, 48)
        assertEquals(SABADO_20H - 2.hours, h)
    }

    @Test
    fun limites_1h_e_48h_aceitos() {
        val inicioLonge = SEXTA_10H + 100.hours
        assertEquals(SEXTA_10H + 1.hours, calc.calcularHold(SEXTA_10H, inicioLonge, 1))
        assertEquals(SEXTA_10H + 48.hours, calc.calcularHold(SEXTA_10H, inicioLonge, 48))
    }

    @Test
    fun fora_dos_limites_rejeitado() {
        assertFailsWith<IllegalArgumentException> { calc.calcularHold(SEXTA_10H, SABADO_20H, 0) }
        assertFailsWith<IllegalArgumentException> { calc.calcularHold(SEXTA_10H, SABADO_20H, 49) }
    }

    @Test
    fun prazo_das_cotas_por_politica_rn11() {
        val h = SEXTA_10H + 4.hours
        assertEquals(h - 30.minutes, calc.calcularPrazoCotas(h, PoliticaNaoPagamento.ORGANIZADOR_COBRE, GarantiaCobertura.PIX))
        assertEquals(h, calc.calcularPrazoCotas(h, PoliticaNaoPagamento.ORGANIZADOR_COBRE, GarantiaCobertura.CARTAO))
        assertEquals(h, calc.calcularPrazoCotas(h, PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR, GarantiaCobertura.NAO_SE_APLICA))
        assertEquals(h - 30.minutes, calc.calcularPrazoCotas(h, PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES, GarantiaCobertura.NAO_SE_APLICA))
    }

    @Test
    fun rn12_ativacao_t_menos_2h45_so_oferece_b_e_a_cartao() {
        val viaveis = calc.politicasViaveis(SABADO_20H - 2.hours - 45.minutes, SABADO_20H)
        assertEquals(
            setOf(
                PoliticaNaoPagamento.ORGANIZADOR_COBRE to GarantiaCobertura.CARTAO,
                PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR to GarantiaCobertura.NAO_SE_APLICA,
            ),
            viaveis.toSet(),
        )
    }

    @Test
    fun rn12_ativacao_t_menos_3h_oferece_todas_e_t_menos_2h10_nenhuma() {
        assertEquals(4, calc.politicasViaveis(SABADO_20H - 3.hours, SABADO_20H).size)
        assertTrue(calc.politicasViaveis(SABADO_20H - 2.hours - 10.minutes, SABADO_20H).isEmpty())
    }
}
