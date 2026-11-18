package br.com.sportconnect

import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** X3: o icone da janela do desktop esta no classpath e e quadrado. */
class DesktopWindowTest {
    @Test
    fun icone_da_janela_existe() {
        val img = Thread.currentThread().contextClassLoader.getResourceAsStream("sportconnect-icon.png")?.use { ImageIO.read(it) }
        assertNotNull(img)
        assertEquals(img.width, img.height)
    }
}
