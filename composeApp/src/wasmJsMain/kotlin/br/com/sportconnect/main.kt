package br.com.sportconnect

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import br.com.sportconnect.di.initKoin
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport(document.body!!) {
        App()
        // Primeiro frame pronto: some com o loader do index.html (fade de 220 ms) e remove do DOM.
        LaunchedEffect(Unit) {
            val loader = document.getElementById("sc-loader") ?: return@LaunchedEffect
            loader.classList.add("sc-hide")
            window.setTimeout({ loader.remove(); null }, 300)
        }
    }
}
