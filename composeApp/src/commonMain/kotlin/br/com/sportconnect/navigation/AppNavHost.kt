@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.navigation

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.Channel
import androidx.navigation.NavController
import br.com.sportconnect.feature.common.ReminderSync
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Column
import br.com.sportconnect.feature.common.OfflineBanner
import br.com.sportconnect.feature.common.LocalOnline
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.platform.ConnectivityMonitor
import br.com.sportconnect.resources.error_upload_failed_title
import br.com.sportconnect.resources.error_upload_failed_body
import br.com.sportconnect.feature.common.ProfilePhotoHost
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.lifecycle.Lifecycle
import br.com.sportconnect.core.designsystem.component.LocalSCNavAnimatedScope
import br.com.sportconnect.core.designsystem.component.LocalSCSharedTransitionScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.SharedTransitionLayout
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.navigation.NavUri
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
import br.com.sportconnect.feature.account.DeleteAccountRoute
import br.com.sportconnect.feature.account.EditProfileRoute
import br.com.sportconnect.feature.account.PrivacyRoute
import br.com.sportconnect.feature.account.SavedCardsRoute
import br.com.sportconnect.feature.account.SportsRoute
import br.com.sportconnect.feature.account.SupportScreen
import br.com.sportconnect.feature.auth.AuthRoute
import br.com.sportconnect.feature.auth.LegalDoc
import br.com.sportconnect.feature.auth.LegalScreen
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
    hasRoute<Route.Profile>() || hasRoute<Route.ProfilePayments>() || hasRoute<Route.Receipt>() ||
        hasRoute<Route.ProfilePrivacy>() || hasRoute<Route.ProfileSports>() ||
        hasRoute<Route.SavedCards>() || hasRoute<Route.Support>() -> AthleteTab.Profile
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
    /** Web (W1): liga o historico do navegador ao NavController. */
    onNavHostReady: suspend (NavController) -> Unit = {},
) {
    val session: UiSessionStore = koinInject()
    val startAuth: AuthRepository = koinInject()
    // B3: com sessao de arena, abre no Painel. Na Home de atleta a barra fica oculta para gestores e a pessoa
    // ficava presa sem navegacao.
    val start = remember {
        startDestination ?: when {
            !session.onboardingDone.value -> Route.Onboarding
            startAuth.currentUser.value?.papeis?.any { it == PapelUsuario.GESTOR_ARENA || it == PapelUsuario.OPERADOR_ARENA } == true ->
                ArenaAdminRoute.Dashboard
            else -> Route.Home
        }
    }
    val shell: ShellViewModel = koinViewModel()
    val shellState by shell.state.collectAsStateWithLifecycle()
    val backStack by navController.currentBackStackEntryAsState()
    val tab = backStack?.destination?.athleteTab()
    val showNav = tab != null && !shellState.arenaUser
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val nav = remember(navController) { AppNavigator(navController) }
    // Links externos (convite, jogo, arena) com o app ja aberto ou vindos do iOS.
    // So limpa o listener se ainda for o desta casca: num link a frio o Android recria a Activity e a casca
    // antiga e descartada depois da nova ja ter se registrado.
    // W1: o link pode chegar antes de o NavHost ter grafo (abertura a frio na web); entra numa fila e so
    // navega depois que existe a primeira tela.
    val links = remember(navController) { Channel<String>(Channel.UNLIMITED) }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.first()
        for (uri in links) {
            runCatching { navController.navigate(NavUri(uri)) }.onFailure { println("Link nao aberto: $uri -> $it") }
        }
    }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.first()
        onNavHostReady(navController)
    }
    DisposableEffect(navController) {
        val listener: (String) -> Unit = { uri -> links.trySend(uri) }
        ExternalUriHandler.listener = listener
        onDispose { if (ExternalUriHandler.listener === listener) ExternalUriHandler.listener = null }
    }
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
    val authRepo: AuthRepository = koinInject()

    // Login so quando precisa (09 ficha 1): visitante explora a vontade; criar jogo e pagar pedem o celular
    // e, depois do codigo, a pessoa volta exatamente para onde ia (09 §2.5).
    // B9: o login pedido no meio de um fluxo abre como sheet sobre a tela atual (09 ficha 2), sem perder o
    // contexto (o convite/arena continua atras). Depois do codigo segue para onde a pessoa ia.
    var pendingLogin by remember { mutableStateOf<AfterLogin?>(null) }
    var loginSheetNonce by remember { mutableStateOf(0) }
    fun requireLogin(next: AfterLogin) {
        loginSheetNonce++
        pendingLogin = next
    }

    fun goCreate(slotId: String? = null) {
        if (authRepo.currentUser.value == null) requireLogin(AfterLogin.Create(slotId)) else nav.go(Route.GameCreate(slotId))
    }

    fun goPay(gameId: String, afterCreate: Boolean = false) {
        if (authRepo.currentUser.value == null) requireLogin(AfterLogin.Pay(gameId)) else nav.go(Route.GamePay(gameId, afterCreate))
    }

    val photoErrorMsg = stringResource(Res.string.error_upload_failed_title) + ". " + stringResource(Res.string.error_upload_failed_body)
    // E7: conexao. Ao voltar, sincroniza os dados (o que mudou enquanto estava offline).
    val connectivity: ConnectivityMonitor = koinInject()
    val online by connectivity.online.collectAsStateWithLifecycle()
    val gamesRepo: GameRepository = koinInject()
    LaunchedEffect(online) { if (online) runCatching { gamesRepo.sincronizar() } }
    // E8: lembretes locais acompanham os jogos enquanto o app esta aberto (e reagendam a cada abertura).
    val reminders: ReminderSync = koinInject()
    LaunchedEffect(reminders) { reminders.run() }
    CompositionLocalProvider(LocalOnline provides online) {
    Column(Modifier.fillMaxSize()) {
    var bannerVisible by remember { mutableStateOf(false) }
    OfflineBanner(online, onVisibleChange = { bannerVisible = it })
    // Com o banner visivel ele ja ocupa a status bar: as telas abaixo nao descontam de novo.
    Box(Modifier.weight(1f).then(if (bannerVisible) Modifier.consumeWindowInsets(WindowInsets.statusBars) else Modifier)) {
    ProfilePhotoHost(onError = { scope.launch { snackbar.showSC(photoErrorMsg, SCSnackbarKind.Error) } }) {
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
                onClick = { goCreate() },
                containerColor = SportConnectTheme.colors.primaryContainer,
                contentColor = SportConnectTheme.colors.onPrimaryContainer,
                modifier = Modifier.testTag("nav_fab_create_game"),
            ) { Icon(Icons.Rounded.Add, stringResource(Res.string.home_cta_create_game)) }
        },
        showNavigation = showNav,
        drawerBreakpoint = Dp.Infinity,
    ) { contentModifier ->
        Box(contentModifier.fillMaxSize()) {
            SharedTransitionLayout {
            CompositionLocalProvider(LocalSCSharedTransitionScope provides this) {
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
                            when {
                                pay != null -> nav.replace(Route.GamePay(pay))
                                r.createGame -> nav.replace(Route.GameCreate(r.slotId))
                                else -> nav.resetTo(Route.Home)
                            }
                        },
                        onOpenLegal = { nav.go(Route.Legal(it.name)) },
                    )
                }
                composable<Route.Legal> { entry ->
                    val doc = LegalDoc.entries.firstOrNull { it.name == entry.toRoute<Route.Legal>().doc } ?: LegalDoc.Terms
                    LegalScreen(doc, onBack = nav::back)
                }
                composable<Route.Home> {
                    CompositionLocalProvider(LocalSCNavAnimatedScope provides this) {
                    HomeRoute(
                        HomeActions(
                            openGame = { nav.go(Route.GameDetail(it)) },
                            pay = { id, afterCreate -> goPay(id, afterCreate) },
                            resolve = { nav.go(Route.GameResolve(it)) },
                            createGame = { goCreate() },
                            findCourt = { nav.openTab(Route.Explore) },
                            notifications = { nav.go(Route.Notifications) },
                        ),
                    )
                    }
                }
                composable<Route.Explore> {
                    ExploreRoute(ExploreActions(openArena = { nav.go(Route.ArenaDetail(it)) }, pickSlot = { goCreate(it) }))
                }
                composable<Route.ArenaDetail>(
                    deepLinks = listOf(navDeepLink<Route.ArenaDetail>(basePath = "https://sportconnect.app/a")),
                ) { entry ->
                    ArenaDetailRoute(
                        entry.toRoute(),
                        ArenaDetailActions(
                            back = nav::back,
                            openSlots = { court, date -> nav.go(Route.SlotPicker(court, date)) },
                            createGame = { goCreate(it) },
                        ),
                    )
                }
                composable<Route.SlotPicker> { entry ->
                    SlotPickerRoute(entry.toRoute(), onBack = nav::back, onContinue = { goCreate(it) })
                }
                composable<Route.GameCreate> { entry ->
                    CreateGameRoute(
                        entry.toRoute(),
                        CreateGameActions(
                            close = nav::back,
                            pickSlot = { nav.openTab(Route.Explore) },
                            goToPay = { id -> nav.replace(Route.GamePay(id, afterCreate = true)) },
                            login = { requireLogin(AfterLogin.Create(null)) },
                        ),
                    )
                }
                composable<Route.GameDetail>(
                    deepLinks = listOf(navDeepLink<Route.GameDetail>(basePath = "https://sportconnect.app/g")),
                ) { entry ->
                    CompositionLocalProvider(LocalSCNavAnimatedScope provides this) {
                    GameDetailRoute(
                        entry.toRoute(),
                        GameDetailActions(
                            back = { if (!navController.popBackStack()) nav.resetTo(Route.Home) },
                            pay = { goPay(it) },
                            invite = { nav.go(Route.GameInvite(it)) },
                            resolve = { nav.go(Route.GameResolve(it)) },
                            receipt = { nav.go(Route.Receipt(it)) },
                        ),
                    )
                    }
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
                            login = { requireLogin(AfterLogin.Pay(it)) },
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
                            pay = { goPay(it) },
                            login = { requireLogin(AfterLogin.Pay(it)) },
                            seeGame = { nav.replace(Route.GameDetail(it)) },
                            close = { nav.resetTo(Route.Home) },
                        ),
                    )
                }
                composable<Route.MyGames> {
                    CompositionLocalProvider(LocalSCNavAnimatedScope provides this) {
                    MyGamesRoute(
                        MyGamesActions(
                            openGame = { nav.go(Route.GameDetail(it)) },
                            createGame = { goCreate() },
                            repeat = { nav.go(Route.ArenaDetail(it.arenaId)) },
                        ),
                    )
                    }
                }
                composable<Route.Profile> {
                    ProfileRoute(
                        ProfileActions(
                            myGames = { nav.openTab(Route.MyGames) },
                            payments = { nav.go(Route.ProfilePayments) },
                            notifications = { nav.go(Route.Notifications) },
                            switchMode = ::switchMode,
                            loggedOut = { nav.resetTo(Route.Auth()) },
                            signIn = { nav.go(Route.Auth()) },
                            editProfile = { nav.go(Route.ProfileEdit) },
                            sports = { nav.go(Route.ProfileSports) },
                            savedCards = { nav.go(Route.SavedCards) },
                            privacy = { nav.go(Route.ProfilePrivacy) },
                            support = { nav.go(Route.Support) },
                        ),
                        vm = profileVm,
                    )
                }
                composable<Route.ProfilePrivacy> {
                    PrivacyRoute(
                        onBack = nav::back,
                        onOpenLegal = { nav.go(Route.Legal(it.name)) },
                        onDeleteAccount = { nav.go(Route.DeleteAccount) },
                    )
                }
                composable<Route.DeleteAccount> {
                    // Conta excluida: volta para a Home como visitante (pilha limpa).
                    DeleteAccountRoute(onBack = nav::back, onFinished = { nav.resetTo(Route.Home) })
                }
                composable<Route.ProfileEdit> { EditProfileRoute(onBack = nav::back) }
                composable<Route.ProfileSports> { SportsRoute(onBack = nav::back) }
                composable<Route.SavedCards> { SavedCardsRoute(onBack = nav::back) }
                composable<Route.Support> { SupportScreen(onBack = nav::back) }
                composable<Route.ProfilePayments> { PaymentsRoute(onBack = nav::back, onReceipt = { nav.go(Route.Receipt(it)) }) }
                composable<Route.Receipt> { entry -> ReceiptRoute(entry.toRoute(), onBack = nav::back, onHelp = { nav.go(Route.Support) }) }
                composable<Route.Notifications> { NotificationsRoute(onBack = nav::back, onOpenGame = { nav.go(Route.GameDetail(it)) }) }

                // Modo "Minha Arena" (feature/arenaadmin). "Perfil" da arena abre o Perfil, que tem o seletor de modo.
                arenaAdminGraph(navController, onExitArenaMode = { nav.go(Route.Profile) })
            }
            }
            }
            SCSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
        }
    }

    pendingLogin?.let { target ->
        ModalBottomSheet(
            onDismissRequest = { pendingLogin = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SportConnectTheme.colors.surface,
            modifier = Modifier.testTag("auth_sheet"),
        ) {
            Box(Modifier.fillMaxWidth().fillMaxHeight(0.92f)) {
                AuthRoute(
                    onClose = { pendingLogin = null },
                    onDone = {
                        pendingLogin = null
                        when (target) {
                            is AfterLogin.Pay -> nav.go(Route.GamePay(target.gameId))
                            is AfterLogin.Create -> nav.go(Route.GameCreate(target.slotId))
                        }
                    },
                    onOpenLegal = { pendingLogin = null; nav.go(Route.Legal(it.name)) },
                    // Chave nova a cada abertura: o ViewModel anterior ja terminou (done = true).
                    vm = koinViewModel(key = "auth-sheet-$loginSheetNonce"),
                )
            }
        }
    }
    }
    }
    }
    }
}

/** Para onde seguir depois do login pedido no meio de um fluxo (09 §2.5). */
private sealed interface AfterLogin {
    data class Pay(val gameId: String) : AfterLogin
    data class Create(val slotId: String?) : AfterLogin
}

/** Acoes de navegacao (encapsula o NavHostController; facilita previews/testes). */
class AppNavigator(private val nc: NavHostController) {
    /**
     * A11: ignora o toque enquanto a tela atual nao esta RESUMED (ex.: durante a transicao). Evita abrir a
     * mesma tela duas vezes com toque duplo, mesmo com argumentos diferentes (launchSingleTop nao cobre).
     */
    fun go(route: Any) {
        if (nc.currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == false) return
        nc.navigate(route) { launchSingleTop = true }
    }

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
