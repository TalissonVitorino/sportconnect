package br.com.sportconnect.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.feature.arenaadmin.ArenaAdminRoute

/**
 * Transicoes de tela (05 §12, Material motion). Sem elas o NavHost usa um fade de 700 ms, que deixa
 * toda a navegacao com cara de lenta.
 *
 * - Entre destinos de topo (abas): fade-through curto (sai em 90 ms, entra em 210 ms com leve escala).
 * - Fluxos modais (criar jogo, pagar, convite, decisao, login): sobem de baixo e descem ao fechar.
 * - Demais telas em pilha: eixo X compartilhado (desliza 1/10 da largura + fade), invertido no voltar.
 */
internal class NavTransitions(private val reducedMotion: Boolean) {

    private val NavDestination.isTopLevel: Boolean
        get() = hasRoute<Route.Home>() || hasRoute<Route.MyGames>() || hasRoute<Route.Explore>() ||
            hasRoute<Route.Profile>() || hasRoute<ArenaAdminRoute.Dashboard>() || hasRoute<ArenaAdminRoute.Courts>() ||
            hasRoute<ArenaAdminRoute.Payouts>() || hasRoute<ArenaAdminRoute.Schedule>()

    private val NavDestination.isModal: Boolean
        get() = hasRoute<Route.GameCreate>() || hasRoute<Route.GamePay>() || hasRoute<Route.GameInvite>() ||
            hasRoute<Route.GameResolve>() || hasRoute<Route.Auth>()

    private fun AnimatedContentTransitionScope<NavBackStackEntry>.betweenTabs() =
        initialState.destination.isTopLevel && targetState.destination.isTopLevel

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        when {
            reducedMotion -> fadeIn(tween(SCMotion.Short3))
            betweenTabs() -> fadeThroughIn()
            targetState.destination.isModal -> modalIn()
            else -> slideInHorizontally(tween(SCMotion.Medium2, easing = SCMotion.EmphasizedDecelerate)) { it / 10 } +
                fadeIn(tween(SCMotion.Medium1, delayMillis = SCMotion.Short2 / 2))
        }
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        when {
            reducedMotion -> fadeOut(tween(SCMotion.Short2))
            betweenTabs() -> fadeOut(tween(90, easing = SCMotion.StandardAccelerate))
            // A tela de baixo fica parada enquanto o modal sobe por cima.
            targetState.destination.isModal -> fadeOut(tween(SCMotion.Medium2, easing = SCMotion.StandardAccelerate), targetAlpha = 0.6f)
            else -> slideOutHorizontally(tween(SCMotion.Medium2, easing = SCMotion.EmphasizedAccelerate)) { -it / 10 } +
                fadeOut(tween(SCMotion.Short3))
        }
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        when {
            reducedMotion -> fadeIn(tween(SCMotion.Short3))
            betweenTabs() -> fadeThroughIn()
            initialState.destination.isModal -> fadeIn(tween(SCMotion.Medium1), initialAlpha = 0.6f)
            else -> slideInHorizontally(tween(SCMotion.Medium2, easing = SCMotion.EmphasizedDecelerate)) { -it / 10 } +
                fadeIn(tween(SCMotion.Medium1, delayMillis = SCMotion.Short2 / 2))
        }
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        when {
            reducedMotion -> fadeOut(tween(SCMotion.Short2))
            betweenTabs() -> fadeOut(tween(90, easing = SCMotion.StandardAccelerate))
            initialState.destination.isModal -> modalOut()
            else -> slideOutHorizontally(tween(SCMotion.Medium1, easing = SCMotion.EmphasizedAccelerate)) { it / 10 } +
                fadeOut(tween(SCMotion.Short3))
        }
    }

    private fun fadeThroughIn(): EnterTransition =
        fadeIn(tween(210, delayMillis = 90, easing = SCMotion.StandardDecelerate)) +
            scaleIn(tween(210, delayMillis = 90, easing = SCMotion.StandardDecelerate), initialScale = 0.96f)

    private fun modalIn(): EnterTransition =
        slideInVertically(tween(SCMotion.Medium4, easing = SCMotion.EmphasizedDecelerate)) { it / 6 } +
            fadeIn(tween(SCMotion.Medium1))

    private fun modalOut(): ExitTransition =
        slideOutVertically(tween(SCMotion.Medium1, easing = SCMotion.EmphasizedAccelerate)) { it / 6 } +
            fadeOut(tween(SCMotion.Short4))
}
