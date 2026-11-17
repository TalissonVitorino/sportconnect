package br.com.sportconnect.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import br.com.sportconnect.core.designsystem.component.SCAdaptiveNavigation
import br.com.sportconnect.core.designsystem.component.SCNavItem
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.LocalSCReducedMotion
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.GetMyGamesUseCase
import br.com.sportconnect.core.model.PapelUsuario
import br.com.sportconnect.feature.arena.ArenaDetailActions
import br.com.sportconnect.feature.arena.ArenaDetailRoute
import br.com.sportconnect.feature.arenaadmin.ArenaAdminRoute
import br.com.sportconnect.feature.arenaadmin.arenaAdminGraph
import br.com.sportconnect.feature.auth.AuthRoute
import br.com.sportconnect.feature.common.UiSessionStore
import br.com.sportconnect.feature.creategame.CreateGameActions
import br.com.sportconnect.feature.creategame.CreateGameRoute
import br.com.sportconnect.feature.explore.ExploreActions
import br.com.sportconnect.feature.explore.ExploreRoute
import br.com.sportconnect.feature.game.GameDetailActions
import br.com.sportconnect.feature.game.GameDetailRoute
import br.com.sportconnect.feature.home.HomeActions
import br.com.sportconnect.feature.home.HomeRoute
import br.com.sportconnect.feature.home.HomeViewModel
import br.com.sportconnect.feature.invite.GuestInviteActions
import br.com.sportconnect.feature.invite.GuestInviteRoute
import br.com.sportconnect.feature.invite.OrganizerInviteActions
import br.com.sportconnect.feature.invite.OrganizerInviteRoute
import br.com.sportconnect.feature.mygames.MyGamesActions
import br.com.sportconnect.feature.mygames.MyGamesRoute
import br.com.sportconnect.feature.onboarding.OnboardingActions
import br.com.sportconnect.feature.onboarding.OnboardingRoute
import br.com.sportconnect.feature.payment.PaymentActions
import br.com.sportconnect.feature.payment.PaymentRoute
import br.com.sportconnect.feature.profile.AppMode
import br.com.sportconnect.feature.profile.NotificationsRoute
import br.com.sportconnect.feature.profile.PaymentsRoute
import br.com.sportconnect.feature.profile.ProfileActions
import br.com.sportconnect.feature.profile.ProfileRoute
import br.com.sportconnect.feature.profile.ProfileViewModel
import br.com.sportconnect.feature.profile.ReceiptRoute
import br.com.sportconnect.feature.resolve.ResolveRoute
import br.com.sportconnect.feature.slots.SlotPickerRoute
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_nav_badge
import br.com.sportconnect.resources.home_cta_create_game
import br.com.sportconnect.resources.mode_switched_arena
import br.com.sportconnect.resources.mode_switched_athlete
import br.com.sportconnect.resources.nav_games
import br.com.sportconnect.resources.nav_home
import br.com.sportconnect.resources.nav_profile
import br.com.sportconnect.resources.nav_search
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Estado da casca: badge da aba Jogos e se o perfil atual e de arena (modo Minha Arena). */
data class ShellState(val gamesBadge: Int = 0, val arenaUser: Boolean = false)

class ShellViewModel(getMyGames: GetMyGamesUseCase, auth: AuthRepository) : ViewModel() {
    val state: StateFlow<ShellState> = combine(getMyGames(), auth.currentUser) { g, u ->
        val uid = u?.id
        ShellState(
            gamesBadge = if (uid == null) 0 else g.proximos.count { HomeViewModel.needsPayment(it, uid) || HomeViewModel.needsDecision(it, uid) },
            arenaUser = u?.papeis?.any { it == PapelUsuario.GESTOR_ARENA || it == PapelUsuario.OPERADOR_ARENA } == true,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ShellState())
}

/** Abas do modo Atleta (10 §1.2). [key] vira o testTag `nav_<key>`. */
private enum class AthleteTab(val key: String) { Home("home"), Games("games"), Search("search"), Profile("profile") }

/** Destinos com navegacao global visivel (09 §11: coluna "Nav global = Sim"). */
private fun NavDestination.athleteTab(): AthleteTab? = when {
    hasRoute<Route.Home>() || hasRoute<Route.Notifications>() -> AthleteTab.Home
    hasRoute<Route.MyGames>() || hasRoute<Route.GameDetail>() -> AthleteTab.Games
    hasRoute<Route.Explore>() || hasRoute<Route.ArenaDetail>() || hasRoute<Route.SlotPicker>() -> AthleteTab.Search
    hasRoute<Route.Profile>() || hasRoute<Route.ProfilePayments>() || hasRoute<Route.Receipt>() -> AthleteTab.Profile
    else -> null
}

/**
 * Casca adaptativa + grafo de navegacao do app (modo Atleta/Organizador + grafo da arena).
 * Bottom nav < 600dp; rail em medium/expanded/large (o drawer e so do painel da arena), com o FAB
 * "Criar jogo" no topo da rail. Fluxos transacionais escondem a navegacao (09 §2.2 regra 4).
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any? = null,
) {
    val session: UiSessionStore = koinInject()
    val start = remember { startDestination ?: if (session.onboardingDone.value) Route.Home else Route.Onboarding }
    val shell: ShellViewModel = koinViewModel()
    val shellState by shell.state.collectAsStateWithLifecycle()
    val backStack by navController.currentBackStackEntryAsState()
    val tab = backStack?.destination?.athleteTab()
    val showNav = tab != null && !shellState.arenaUser
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val nav = remember(navController) { AppNavigator(navController) }
    val reducedMotion = LocalSCReducedMotion.current
    val transitions = remember(reducedMotion) { NavTransitions(reducedMotion) }

    val gamesLabel = stringResource(Res.string.nav_games)
    val items = listOf(
        SCNavItem(AthleteTab.Home.key, stringResource(Res.string.nav_home), Icons.Outlined.Home, Icons.Rounded.Home),
        SCNavItem(
            AthleteTab.Games.key, gamesLabel, Icons.Outlined.SportsScore, Icons.Rounded.SportsScore,
            badgeCount = shellState.gamesBadge.takeIf { it > 0 },
            badgeDescription = if (shellState.gamesBadge > 0) stringResource(Res.string.cd_nav_badge, gamesLabel, shellState.gamesBadge) else null,
        ),
        SCNavItem(AthleteTab.Search.key, stringResource(Res.string.nav_search), Icons.Rounded.Search, Icons.Rounded.Search),
        SCNavItem(AthleteTab.Profile.key, stringResource(Res.string.nav_profile), Icons.Outlined.Person, Icons.Rounded.Person),
    )

    fun switchMode(mode: AppMode) {
        scope.launch {
            if (mode == AppMode.Arena) {
                nav.enterArena()
                snackbar.showSC(getString(Res.string.mode_switched_arena), SCSnackbarKind.Info)
            } else {
                nav.resetTo(Route.Home)
                snackbar.showSC(getString(Res.string.mode_switched_athlete), SCSnackbarKind.Info)
            }
        }
    }

    val profileVm: ProfileViewModel = koinViewModel()

    SCAdaptiveNavigation(
        items = items,
        selectedRoute = tab?.key.orEmpty(),
        onItemSelected = { key ->
            when (key) {
                AthleteTab.Home.key -> nav.openTab(Route.Home)
                AthleteTab.Games.key -> nav.openTab(Route.MyGames)
                AthleteTab.Search.key -> nav.openTab(Route.Explore)
                AthleteTab.Profile.key -> nav.openTab(Route.Profile)
            }
        },
        header = {
            FloatingActionButton(
                onClick = { nav.go(Route.GameCreate()) },
                containerColor = SportConnectTheme.colors.primaryContainer,
                contentColor = SportConnectTheme.colors.onPrimaryContainer,
                modifier = Modifier.testTag("nav_fab_create_game"),
            ) { Icon(Icons.Rounded.Add, stringResource(Res.string.home_cta_create_game)) }
        },
        showNavigation = showNav,
        drawerBreakpoint = Dp.Infinity,
    ) { contentModifier ->
        Box(contentModifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = start,
                modifier = Modifier.fillMaxSize(),
                enterTransition = transitions.enter,
                exitTransition = transitions.exit,
                popEnterTransition = transitions.popEnter,
                popExitTransition = transitions.popExit,
            ) {
                composable<Route.Onboarding> {
                    OnboardingRoute(
                        OnboardingActions(
                            finished = { logged -> nav.resetTo(if (logged) Route.Home else Route.Auth()) },
                            arena = { profileVm.switchMode(AppMode.Arena) { switchMode(AppMode.Arena) } },
                        ),
                    )
                }
                composable<Route.Auth> { entry ->
                    val r = entry.toRoute<Route.Auth>()
                    AuthRoute(
                        onClose = { if (!navController.popBackStack()) nav.resetTo(Route.Home) },
                        onDone = {
                            val pay = r.payGameId
                            if (pay != null) nav.replace(Route.GamePay(pay)) else nav.resetTo(Route.Home)
                        },
                    )
                }
                composable<Route.Home> {
                    HomeRoute(
                        HomeActions(
                            openGame = { nav.go(Route.GameDetail(it)) },
                            pay = { id, afterCreate -> nav.go(Route.GamePay(id, afterCreate)) },
                            resolve = { nav.go(Route.GameResolve(it)) },
                            createGame = { nav.go(Route.GameCreate()) },
                            findCourt = { nav.openTab(Route.Explore) },
                            notifications = { nav.go(Route.Notifications) },
                        ),
                    )
                }
                composable<Route.Explore> {
                    ExploreRoute(ExploreActions(openArena = { nav.go(Route.ArenaDetail(it)) }, pickSlot = { nav.go(Route.GameCreate(it)) }))
                }
                composable<Route.ArenaDetail>(
                    deepLinks = listOf(navDeepLink<Route.ArenaDetail>(basePath = "https://sportconnect.app/a")),
                ) { entry ->
                    ArenaDetailRoute(
                        entry.toRoute(),
                        ArenaDetailActions(
                            back = nav::back,
                            openSlots = { court, date -> nav.go(Route.SlotPicker(court, date)) },
                            createGame = { nav.go(Route.GameCreate(it)) },
                        ),
                    )
                }
                composable<Route.SlotPicker> { entry ->
                    SlotPickerRoute(entry.toRoute(), onBack = nav::back, onContinue = { nav.go(Route.GameCreate(it)) })
                }
                composable<Route.GameCreate> { entry ->
                    CreateGameRoute(
                        entry.toRoute(),
                        CreateGameActions(
                            close = nav::back,
                            pickSlot = { nav.openTab(Route.Explore) },
                            goToPay = { id -> nav.replace(Route.GamePay(id, afterCreate = true)) },
                        ),
                    )
                }
                composable<Route.GameDetail>(
                    deepLinks = listOf(navDeepLink<Route.GameDetail>(basePath = "https://sportconnect.app/g")),
                ) { entry ->
                    GameDetailRoute(
                        entry.toRoute(),
                        GameDetailActions(
                            back = { if (!navController.popBackStack()) nav.resetTo(Route.Home) },
                            pay = { nav.go(Route.GamePay(it)) },
                            invite = { nav.go(Route.GameInvite(it)) },
                            resolve = { nav.go(Route.GameResolve(it)) },
                            receipt = { nav.go(Route.Receipt(it)) },
                        ),
                    )
                }
                composable<Route.GameInvite> { entry ->
                    OrganizerInviteRoute(
                        entry.toRoute(),
                        OrganizerInviteActions(close = { id -> nav.replace(Route.GameDetail(id)) }, seeGame = { id -> nav.replace(Route.GameDetail(id)) }),
                    )
                }
                composable<Route.GamePay> { entry ->
                    PaymentRoute(
                        entry.toRoute(),
                        PaymentActions(
                            close = nav::back,
                            seeGame = { nav.replace(Route.GameDetail(it)) },
                            invite = { nav.replace(Route.GameInvite(it)) },
                            receipt = { nav.go(Route.Receipt(it)) },
                        ),
                    )
                }
                composable<Route.GameResolve> { entry ->
                    ResolveRoute(entry.toRoute(), onClose = nav::back, onSeeGame = { nav.replace(Route.GameDetail(it)) })
                }
                composable<Route.Invite>(
                    deepLinks = listOf(navDeepLink<Route.Invite>(basePath = "https://sportconnect.app/j")),
                ) { entry ->
                    GuestInviteRoute(
                        entry.toRoute(),
                        GuestInviteActions(
                            pay = { nav.go(Route.GamePay(it)) },
                            login = { nav.go(Route.Auth(payGameId = it)) },
                            seeGame = { nav.replace(Route.GameDetail(it)) },
                            close = { nav.resetTo(Route.Home) },
                        ),
                    )
                }
                composable<Route.MyGames> {
                    MyGamesRoute(
                        MyGamesActions(
                            openGame = { nav.go(Route.GameDetail(it)) },
                            createGame = { nav.go(Route.GameCreate()) },
                            repeat = { nav.go(Route.ArenaDetail(it.arenaId)) },
                        ),
                    )
                }
                composable<Route.Profile> {
                    ProfileRoute(
                        ProfileActions(
                            myGames = { nav.openTab(Route.MyGames) },
                            payments = { nav.go(Route.ProfilePayments) },
                            notifications = { nav.go(Route.Notifications) },
                            switchMode = ::switchMode,
                            loggedOut = { nav.resetTo(Route.Auth()) },
                        ),
                        vm = profileVm,
                    )
                }
                composable<Route.ProfilePayments> { PaymentsRoute(onBack = nav::back, onReceipt = { nav.go(Route.Receipt(it)) }) }
                composable<Route.Receipt> { entry -> ReceiptRoute(entry.toRoute(), onBack = nav::back) }
                composable<Route.Notifications> { NotificationsRoute(onBack = nav::back, onOpenGame = { nav.go(Route.GameDetail(it)) }) }

                // Modo "Minha Arena" (feature/arenaadmin). "Perfil" da arena abre o Perfil, que tem o seletor de modo.
                arenaAdminGraph(navController, onExitArenaMode = { nav.go(Route.Profile) })
            }
            SCSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
        }
    }
}

/** Acoes de navegacao (encapsula o NavHostController; facilita previews/testes). */
class AppNavigator(private val nc: NavHostController) {
    fun go(route: Any) = nc.navigate(route) { launchSingleTop = true }

    fun back() {
        if (!nc.popBackStack()) resetTo(Route.Home)
    }

    /** Troca a tela atual por [route] (ex.: pagamento -> convite; nao volta para o fluxo concluido). */
    fun replace(route: Any) {
        val current = nc.currentBackStackEntry?.destination?.id
        nc.navigate(route) {
            if (current != null) popUpTo(current) { inclusive = true }
            launchSingleTop = true
        }
    }

    /** Destino de topo: guarda/restaura a pilha de cada aba (09 §2.2 regra 5). */
    fun openTab(route: Route) {
        val homeOnStack = runCatching { nc.getBackStackEntry(Route.Home) }.isSuccess
        nc.navigate(route) {
            if (homeOnStack) {
                popUpTo(Route.Home) { saveState = true }
            } else {
                popUpTo(nc.graph.findStartDestination().id) { saveState = true }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    /** Limpa a pilha inteira e abre [route]. */
    fun resetTo(route: Any) {
        nc.navigate(route) {
            popUpTo(nc.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    /** Modo Minha Arena: abre o Painel da arena como nova raiz. */
    fun enterArena() = resetTo(ArenaAdminRoute.Dashboard)
}
