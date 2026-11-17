package br.com.sportconnect

import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Sport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SplitCostUseCaseTest {
    private val split = SplitCostUseCase()

    @Test
    fun divideIgualmenteQuandoExato() {
        val shares = split(Money(24_000), 12)
        assertEquals(12, shares.size)
        assertTrue(shares.all { it == Money(2_000) })
    }

    @Test
    fun somaDasCotasIgualAoTotalComCentavosQuebrados() {
        val shares = split(Money(10_000), 3)
        assertEquals(listOf(Money(3_334), Money(3_333), Money(3_333)), shares)
        assertEquals(10_000, shares.sumOf { it.cents })
    }

    @Test
    fun rejeitaZeroVagas() {
        assertFailsWith<IllegalArgumentException> { split(Money(1_000), 0) }
    }

    @Test
    fun multiesporteDesdeOInicio() {
        assertEquals(10, Sport.entries.size)
    }
}
