package br.com.sportconnect

import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.ExperimentalBrowserHistoryApi
import br.com.sportconnect.navigation.ExternalUriHandler
import br.com.sportconnect.navigation.WebLinks
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import br.com.sportconnect.di.initKoin
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    initKoin()
    // W1: link aberto direto no navegador (/j/CODIGO ou #/j/CODIGO) abre a tela certa, como no app.
    WebLinks.linkFromLocation(window.location.pathname, window.location.hash)?.let(ExternalUriHandler::onNewUri)
    ComposeViewport(document.body!!) {
        App(onNavHostReady = { nav -> nav.bindToBrowserNavigation(WebLinks::fragmentFor) })
        // Primeiro frame pronto: some com o loader do index.html (fade de 220 ms) e remove do DOM.
        LaunchedEffect(Unit) {
            val loader = document.getElementById("sc-loader") ?: return@LaunchedEffect
            loader.classList.add("sc-hide")
            window.setTimeout({ loader.remove(); null }, 300)
        }
    }
}
