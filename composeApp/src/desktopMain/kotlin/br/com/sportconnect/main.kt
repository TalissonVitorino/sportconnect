package br.com.sportconnect

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import br.com.sportconnect.di.initKoin
import io.github.vinceglb.filekit.FileKit

fun main() {
    // FileKit no desktop precisa de um appId para achar a pasta de dados do app (foto de perfil).
    FileKit.init(appId = "br.com.sportconnect")
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "SportConnect",
            state = rememberWindowState(width = 420.dp, height = 860.dp),
        ) {
            App()
        }
    }
}
