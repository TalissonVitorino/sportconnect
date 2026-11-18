package br.com.sportconnect

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.navigation.AppNavHost

/**
 * Ponto de entrada de UI compartilhado por todas as plataformas.
 * O grafo do Koin deve ser iniciado antes (ver [br.com.sportconnect.di.initKoin]).
 */
@Composable
@Preview
fun App(onNavHostReady: suspend (NavController) -> Unit = {}) {
    SportConnectTheme {
        // X4 (doc 13): tocar numa area vazia tira o foco e fecha o teclado. No iPhone os teclados numericos
        // (celular, codigo, cartao) nao tem tecla para fechar; botoes e campos consomem o toque antes.
        val focus = LocalFocusManager.current
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { focus.clearFocus() }) }) {
            AppNavHost(onNavHostReady = onNavHostReady)
        }
    }
}
