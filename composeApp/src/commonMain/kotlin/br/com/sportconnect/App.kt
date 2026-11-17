package br.com.sportconnect

import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
        AppNavHost(onNavHostReady = onNavHostReady)
    }
}
