package br.com.sportconnect.feature.arenaadmin

import br.com.sportconnect.feature.arenaadmin.reports.ArenaSettingsScreen
import br.com.sportconnect.feature.arenaadmin.reports.ArenaReportsScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import br.com.sportconnect.feature.arenaadmin.booking.ArenaBookingScreen
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtEditScreen
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtsScreen
import br.com.sportconnect.feature.arenaadmin.dashboard.ArenaDashboardScreen
import br.com.sportconnect.feature.arenaadmin.onboarding.ArenaOnboardingScreen
import br.com.sportconnect.feature.arenaadmin.payouts.ArenaPayoutsScreen
import br.com.sportconnect.feature.arenaadmin.schedule.ArenaScheduleScreen

/**
 * Registra todas as rotas do modo "Minha Arena" no NavHost do app.
 * Cada destino de topo (Painel, Agenda, Quadras, Repasses) desenha a propria navegacao da arena
 * (bottom nav < 600dp, rail >= 600dp); "Perfil" chama [onExitArenaMode].
 */
fun NavGraphBuilder.arenaAdminGraph(
    navController: NavHostController,
    onExitArenaMode: () -> Unit = {},
) {
    val nav = ArenaAdminNavigator(navController, onExitArenaMode)
    composable<ArenaAdminRoute.Dashboard> { ArenaDashboardScreen(nav) }
    composable<ArenaAdminRoute.Schedule> { ArenaScheduleScreen(nav) }
    composable<ArenaAdminRoute.Booking> { entry ->
        val route = entry.toRoute<ArenaAdminRoute.Booking>()
        ArenaBookingScreen(bookingId = route.id, nav = nav)
    }
    composable<ArenaAdminRoute.Courts> { ArenaCourtsScreen(nav) }
    composable<ArenaAdminRoute.CourtEdit> { entry ->
        val route = entry.toRoute<ArenaAdminRoute.CourtEdit>()
        ArenaCourtEditScreen(courtId = route.courtId, nav = nav)
    }
    composable<ArenaAdminRoute.Payouts> { ArenaPayoutsScreen(nav) }
    composable<ArenaAdminRoute.Onboarding> { ArenaOnboardingScreen(nav) }
    composable<ArenaAdminRoute.Reports> { ArenaReportsScreen(nav) }
    composable<ArenaAdminRoute.Settings> { ArenaSettingsScreen(nav) }
}
