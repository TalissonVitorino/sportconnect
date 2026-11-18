package br.com.sportconnect

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import br.com.sportconnect.di.initKoin
import io.github.vinceglb.filekit.FileKit
import java.awt.Dimension
import java.util.prefs.Preferences
import javax.imageio.ImageIO

/** X3 (doc 13): tamanho e posicao da janela ficam salvos entre aberturas. */
private val windowPrefs: Preferences = Preferences.userRoot().node("br/com/sportconnect/window")

private fun saveWindow(state: WindowState) {
    windowPrefs.putFloat("w", state.size.width.value)
    windowPrefs.putFloat("h", state.size.height.value)
    (state.position as? WindowPosition.Absolute)?.let {
        windowPrefs.putFloat("x", it.x.value)
        windowPrefs.putFloat("y", it.y.value)
    }
}

fun main() {
    // FileKit no desktop precisa de um appId para achar a pasta de dados do app (foto de perfil).
    FileKit.init(appId = "br.com.sportconnect")
    initKoin()
    val icon = Thread.currentThread().contextClassLoader.getResourceAsStream("sportconnect-icon.png")
        ?.use { ImageIO.read(it) }?.toComposeImageBitmap()?.let(::BitmapPainter)
    application {
        val x = windowPrefs.getFloat("x", Float.NaN)
        val y = windowPrefs.getFloat("y", Float.NaN)
        val state = rememberWindowState(
            size = DpSize(windowPrefs.getFloat("w", 420f).dp, windowPrefs.getFloat("h", 860f).dp),
            position = if (x.isNaN() || y.isNaN()) WindowPosition.PlatformDefault else WindowPosition(x.dp, y.dp),
        )
        Window(
            onCloseRequest = { saveWindow(state); exitApplication() },
            title = "SportConnect",
            icon = icon,
            state = state,
        ) {
            // Abaixo disso o layout compacto (360 dp) deixa de caber.
            LaunchedEffect(Unit) { window.minimumSize = Dimension(360, 600) }
            App()
        }
    }
}
