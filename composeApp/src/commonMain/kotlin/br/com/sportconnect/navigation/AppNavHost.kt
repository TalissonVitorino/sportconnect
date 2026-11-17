package br.com.sportconnect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.sportconnect.feature.home.HomeActions
import br.com.sportconnect.feature.home.HomeRoute

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.Home> { HomeRoute(HomeActions()) }
    }
}
