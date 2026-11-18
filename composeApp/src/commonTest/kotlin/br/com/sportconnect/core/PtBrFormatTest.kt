package br.com.sportconnect.core

import br.com.sportconnect.core.model.PtBrFormat
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** T2: "hoje" dentro de frase, "Hoje" em rotulo solto (chip, titulo de secao). */
class PtBrFormatTest {
    private val hoje = LocalDate(2026, 11, 17)

    @Test
    fun dia_relativo_em_frase_e_em_rotulo() {
        assertEquals("hoje", PtBrFormat.relativeDay(hoje, hoje))
        assertEquals("Hoje", PtBrFormat.relativeDayLabel(hoje, hoje))
        assertEquals("Amanhã", PtBrFormat.relativeDayLabel(LocalDate(2026, 11, 18), hoje))
        val depois = LocalDate(2026, 11, 20)
        assertEquals(PtBrFormat.relativeDay(depois, hoje), PtBrFormat.relativeDayLabel(depois, hoje))
    }
}
