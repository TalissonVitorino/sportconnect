package br.com.sportconnect.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** W1: URL da pagina web -> link publico tratado pelo mesmo caminho do Android/iOS. */
class WebLinksTest {
    @Test
    fun caminho_e_fragmento_viram_link_publico() {
        assertEquals("https://sportconnect.app/j/RACHA8", WebLinks.linkFromLocation("/j/RACHA8", ""))
        assertEquals("https://sportconnect.app/g/jogo-racha", WebLinks.linkFromLocation("/", "#/g/jogo-racha"))
        assertEquals("https://sportconnect.app/a/arena-vila-madalena", WebLinks.linkFromLocation("/a/arena-vila-madalena/", ""))
    }

    @Test
    fun abas_e_lixo_abrem_o_inicio() {
        assertNull(WebLinks.linkFromLocation("/", ""))
        assertNull(WebLinks.linkFromLocation("/", "#/buscar"))
        assertNull(WebLinks.linkFromLocation("/x/../etc", "#/j/<script>"))
    }
}
