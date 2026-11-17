package br.com.sportconnect

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import br.com.sportconnect.di.initKoin

fun main() {
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
