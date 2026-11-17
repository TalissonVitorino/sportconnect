package br.com.sportconnect.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import br.com.sportconnect.core.designsystem.catalog.DesignSystemCatalog
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Renderiza o catalogo do DS (claro e escuro) em PNG para revisao visual.
 * Saida: <raiz>/build-screens/ds-catalog-{light,dark}.png
 * Rodar: ./gradlew :composeApp:desktopTest --tests '*CatalogScreenshotTest*' -PdisableAndroid=true
 */
class CatalogScreenshotTest {
    private val outDir: File = run {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (!File(dir, "settings.gradle.kts").exists() && dir.parentFile != null) dir = dir.parentFile
        File(dir, "build-screens").apply { mkdirs() }
    }

    private fun render(dark: Boolean, name: String, widthDp: Int, heightDp: Int, density: Float) {
        ImageComposeScene(
            width = (widthDp * density).toInt(),
            height = (heightDp * density).toInt(),
            density = Density(density),
        ) {
            SportConnectTheme(darkTheme = dark) {
                DesignSystemCatalog(Modifier.fillMaxWidth(), scrollable = false, darkTheme = dark)
            }
        }.use { scene ->
            // alguns frames para fontes/strings (Compose Resources) carregarem
            var t = 0L
            repeat(6) {
                scene.render(t)
                Thread.sleep(300)
                t += 100_000_000L
            }
            val image = scene.render(t)
            val data = image.encodeToData(EncodedImageFormat.PNG) ?: error("encode falhou")
            val file = File(outDir, name)
            file.writeBytes(data.bytes)
            assertTrue(file.length() > 0)
            println("screenshot: ${file.absolutePath}")
        }
    }

    @Test
    fun catalogLight() = render(false, "ds-catalog-light.png", 1280, 4200, 1.25f)

    @Test
    fun catalogDark() = render(true, "ds-catalog-dark.png", 1280, 4200, 1.25f)

    @Test
    fun catalogCompactLight() = render(false, "ds-catalog-compact-light.png", 400, 12000, 1f)
}
