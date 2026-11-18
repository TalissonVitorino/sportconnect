package br.com.sportconnect.feature.arenaadmin

import androidx.navigation.NavHostController

/** Destinos de topo da area da arena (bottom nav / rail). */
enum class ArenaTab { Dashboard, Schedule, Courts, Payouts, Profile }

/**
 * Acoes de navegacao usadas pelas telas da arena. Abstrai o [NavHostController] para que as telas
 * possam ser renderizadas isoladamente (screenshots/testes) com [ArenaAdminNavigator.Preview].
 */
open class ArenaAdminNavigator(
    private val navController: NavHostController?,
    private val onExitArenaMode: () -> Unit = {},
) {
    open fun openTab(tab: ArenaTab) {
        val nc = navController ?: return
        val route: ArenaAdminRoute = when (tab) {
            ArenaTab.Dashboard -> ArenaAdminRoute.Dashboard
            ArenaTab.Schedule -> ArenaAdminRoute.Schedule
            ArenaTab.Courts -> ArenaAdminRoute.Courts
            ArenaTab.Payouts -> ArenaAdminRoute.Payouts
            ArenaTab.Profile -> {
                onExitArenaMode()
                return
            }
        }
        nc.navigate(route) {
            popUpTo<ArenaAdminRoute.Dashboard> { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    open fun openBooking(id: String) {
        navController?.navigate(ArenaAdminRoute.Booking(id))
    }

    open fun openCourt(courtId: String?) {
        navController?.navigate(ArenaAdminRoute.CourtEdit(courtId))
    }

    open fun openReports() {
        navController?.navigate(ArenaAdminRoute.Reports)
    }

    open fun openSettings() {
        navController?.navigate(ArenaAdminRoute.Settings)
    }

    open fun openOnboarding() {
        navController?.navigate(ArenaAdminRoute.Onboarding)
    }

    open fun back() {
        navController?.popBackStack()
    }

    /** Conclui o cadastro e vai para o Painel, removendo o onboarding da pilha. */
    open fun finishOnboarding() {
        val nc = navController ?: return
        nc.navigate(ArenaAdminRoute.Dashboard) {
            popUpTo<ArenaAdminRoute.Onboarding> { inclusive = true }
            launchSingleTop = true
        }
    }

    companion object {
        /** Navegador inerte (previews, screenshots). */
        val Preview = ArenaAdminNavigator(null)
    }
}
