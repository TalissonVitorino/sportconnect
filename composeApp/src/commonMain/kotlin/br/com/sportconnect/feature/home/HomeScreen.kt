package br.com.sportconnect.feature.home

import androidx.compose.foundation.lazy.grid.items as gridItems
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import br.com.sportconnect.resources.home_section_open_games
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.feature.common.OpenGameCard
import br.com.sportconnect.feature.common.OpenGamesViewModel
import br.com.sportconnect.resources.home_greeting_guest
import androidx.compose.ui.semantics.isTraversalGroup
import br.com.sportconnect.core.designsystem.component.TopBarFirst
import br.com.sportconnect.resources.common_not_now
import br.com.sportconnect.resources.reminders_enable
import br.com.sportconnect.resources.reminders_card_body
import br.com.sportconnect.resources.reminders_card_title
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import br.com.sportconnect.core.platform.rememberNotificationPermission
import br.com.sportconnect.core.platform.localRemindersSupported
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.feature.common.ReminderSync
import br.com.sportconnect.core.designsystem.component.sharedGameTitleKey
import org.koin.compose.koinInject
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.designsystem.component.SCPullToRefresh
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.GameCardSkeleton
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCGameCard
import br.com.sportconnect.core.designsystem.component.SCGameCardVariant
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.theme.SCElevation
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.feature.common.LoadErrorState
import br.com.sportconnect.feature.common.SectionHeader
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.splitCounts
import br.com.sportconnect.feature.common.toCardUi
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_notifications
import br.com.sportconnect.resources.cd_notifications_unread
import br.com.sportconnect.resources.empty_games_cta
import br.com.sportconnect.resources.empty_games_title
import br.com.sportconnect.resources.game_resolution_open
import br.com.sportconnect.resources.guest_your_share
import br.com.sportconnect.resources.home_card_status_confirmed
import br.com.sportconnect.resources.home_card_status_pending
import br.com.sportconnect.resources.home_cta_create_game
import br.com.sportconnect.resources.home_cta_find_court
import br.com.sportconnect.resources.home_empty_hint
import br.com.sportconnect.resources.home_greeting
import br.com.sportconnect.resources.home_pay_now
import br.com.sportconnect.resources.home_role_organizer
import br.com.sportconnect.resources.home_section_decision
import br.com.sportconnect.resources.home_section_next_games
import br.com.sportconnect.resources.home_section_pending_payment
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Acoes de navegacao da Home. */
data class HomeActions(
    val openGame: (String) -> Unit = {},
    val pay: (gameId: String, afterCreate: Boolean) -> Unit = { _, _ -> },
    val resolve: (String) -> Unit = {},
    val createGame: () -> Unit = {},
    val findCourt: () -> Unit = {},
    val notifications: () -> Unit = {},
)

@Composable
fun HomeRoute(actions: HomeActions, viewModel: HomeViewModel = koinViewModel(), openVm: OpenGamesViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val openGames by openVm.games.collectAsStateWithLifecycle()
    val games: GameRepository = koinInject()
    // E8: card de lembretes na 1a vez que ha jogo (permissao pedida em contexto, nao na abertura).
    val reminders: ReminderSync = koinInject()
    val prefs: AppPreferences = koinInject()
    var showReminderCard by remember { mutableStateOf(localRemindersSupported && !prefs.remindersAsked) }
    val askNotifications = rememberNotificationPermission { granted ->
        reminders.setEnabled(granted)
        showReminderCard = false
    }
    HomeScreen(
        reminderPrompt = if (showReminderCard) ({
            ReminderCard(onEnable = askNotifications, onDismiss = { reminders.setEnabled(false); showReminderCard = false })
        }) else null,
        onRefresh = games::sincronizar,
        state = state,
        actions = actions.copy(createGame = { viewModel.onCreateGame(); actions.createGame() }),
        onRetry = viewModel::retry,
        openGames = openGames.take(3),
    )
}

/**
 * Inicio (10 §2.3): "Precisa da sua resposta" -> "Falta pagar" -> "Seus próximos jogos" -> Buscar quadra.
 * FAB estendido "Criar jogo" (oculto no estado vazio, §3.3). Expanded: 2 colunas de cards.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    actions: HomeActions,
    onRetry: () -> Unit = {},
    onRefresh: suspend () -> Unit = {},
    reminderPrompt: (@Composable () -> Unit)? = null,
    /** T3: ate 3 jogos com vaga de outras pessoas (US-101). */
    openGames: List<Jogo> = emptyList(),
) {
    val c = SportConnectTheme.colors
    val gridState = rememberLazyGridState()
    val expandedFab by remember { derivedStateOf { gridState.firstVisibleItemIndex == 0 } }
    val unread = when (state) {
        is HomeUiState.Content -> state.unread
        is HomeUiState.Empty -> state.unread
        else -> 0
    }
    val firstName = when (state) {
        is HomeUiState.Content -> state.firstName
        is HomeUiState.Empty -> state.firstName
        else -> ""
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        Scaffold(
            modifier = Modifier.semantics { isTraversalGroup = true },
            containerColor = c.surface,
            topBar = {
                val topMax = if (wc == WidthClass.Expanded) 1040.dp else 640.dp
                val hPad = if (wc == WidthClass.Compact) 0.dp else 8.dp
                // A barra de topo e propria (nao e TopAppBar), entao desconta a status bar aqui.
                Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).then(TopBarFirst), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.widthIn(max = topMax).padding(horizontal = hPad)) { HomeTopBar(firstName, unread, actions.notifications) }
                }
            },
            floatingActionButton = {
                // Medium/expanded: o FAB vira item da rail (casca de navegacao).
                if (state is HomeUiState.Content && wc == WidthClass.Compact) {
                    val createLabel = stringResource(Res.string.home_cta_create_game)
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(Res.string.home_cta_create_game), style = MaterialTheme.typography.labelLarge) },
                        icon = { Icon(Icons.Rounded.Add, null) },
                        onClick = actions.createGame,
                        expanded = expandedFab,
                        containerColor = c.primaryContainer,
                        contentColor = c.onPrimaryContainer,
                        shape = MaterialTheme.shapes.extraLarge,
                        // A4: nome acessivel explicito (com o rotulo recolhido ao rolar, a web ficava sem nome).
                        modifier = Modifier.semantics { contentDescription = createLabel }.testTag("home_fab_create_game"),
                    )
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                when (state) {
                    HomeUiState.Loading -> HomeLoading(Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(16.dp))
                    HomeUiState.Error -> LoadErrorState(onRetry)
                    is HomeUiState.Empty -> if (openGames.isEmpty()) HomeEmpty(actions) else HomeEmptyWithOpenGames(actions, openGames, wc)
                    is HomeUiState.Content -> SCPullToRefresh(onRefresh, Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) { HomeContent(state, actions, wc, gridState, reminderPrompt, openGames) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(firstName: String, unread: Int, onNotifications: () -> Unit) {
    val c = SportConnectTheme.colors
    val cd = if (unread > 0) stringResource(Res.string.cd_notifications_unread, unread) else stringResource(Res.string.cd_notifications)
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            // T10: visitante (ou conta sem nome) nao ve "E ai, !".
            if (firstName.isBlank()) stringResource(Res.string.home_greeting_guest) else stringResource(Res.string.home_greeting, firstName),
            style = MaterialTheme.typography.headlineSmall,
            color = c.onSurface,
            modifier = Modifier.weight(1f).testTag("home_greeting").semantics { heading() },
        )
        IconButton(onClick = onNotifications, modifier = Modifier.testTag("home_notifications").semantics { contentDescription = cd }) {
            if (unread > 0) {
                BadgedBox(badge = { Badge(containerColor = c.error, contentColor = c.onError) { Text(unread.toString()) } }) {
                    Icon(Icons.Rounded.Notifications, null, tint = c.onSurfaceVariant)
                }
            } else {
                Icon(Icons.Rounded.Notifications, null, tint = c.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HomeLoading(modifier: Modifier) {
    Column(modifier.semantics { contentDescription = "Carregando jogos" }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SCSkeleton(SCSkeletonShape.Line(widthFraction = 0.4f, height = 16.dp), Modifier.padding(top = 16.dp, bottom = 4.dp))
        GameCardSkeleton()
        GameCardSkeleton()
    }
}

/** T3: sem jogos meus, mas com jogos abertos por perto: o convite a criar fica no topo e os jogos abaixo. */
@Composable
private fun HomeEmptyWithOpenGames(actions: HomeActions, openGames: List<Jogo>, wc: WidthClass) {
    val now = LocalSCClock.current.now()
    val hPad = if (wc == WidthClass.Compact) 16.dp else 24.dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (wc == WidthClass.Expanded) 2 else 1),
        modifier = Modifier.widthIn(max = if (wc == WidthClass.Expanded) 1040.dp else 640.dp).fillMaxSize(),
        contentPadding = PaddingValues(start = hPad, end = hPad, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "empty") { HomeEmpty(actions) }
        header(Res.string.home_section_open_games, "home_section_open")
        gridItems(openGames, key = { "open_${it.id}" }) { j -> OpenGameCard(j, now, onClick = { actions.openGame(j.id) }) }
    }
}

/** Momento de marca §3.3: estado vazio centralizado, sem FAB. */
@Composable
private fun HomeEmpty(actions: HomeActions) {
    SCEmptyState(
        title = stringResource(Res.string.empty_games_title),
        description = stringResource(Res.string.home_empty_hint),
        primaryAction = SCAction(stringResource(Res.string.empty_games_cta), actions.createGame, icon = Icons.Rounded.Add),
        secondaryAction = SCAction(stringResource(Res.string.home_cta_find_court), actions.findCourt),
        modifier = Modifier.testTag("home_empty_state"),
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState.Content,
    actions: HomeActions,
    wc: WidthClass,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    reminderPrompt: (@Composable () -> Unit)? = null,
    openGames: List<Jogo> = emptyList(),
) {
    val columns = if (wc == WidthClass.Expanded) 2 else 1
    val maxWidth = if (wc == WidthClass.Expanded) 1040.dp else 640.dp
    val hPad = if (wc == WidthClass.Compact) 16.dp else 24.dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = Modifier.widthIn(max = maxWidth).fillMaxSize(),
        contentPadding = PaddingValues(start = hPad, end = hPad, top = 0.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (reminderPrompt != null) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "reminder_prompt") {
                Box(Modifier.padding(top = 12.dp).animateItem()) { reminderPrompt() }
            }
        }
        if (state.decisions.isNotEmpty()) {
            header(Res.string.home_section_decision.let { it }, "home_section_decision")
            items(state.decisions, state, actions, Section.Decision)
        }
        if (state.pending.isNotEmpty()) {
            header(Res.string.home_section_pending_payment, "home_section_pending")
            items(state.pending, state, actions, Section.Pending, fullWidthSingle = state.pending.size == 1)
        }
        if (state.upcoming.isNotEmpty()) {
            header(Res.string.home_section_next_games, "home_section_next")
            items(state.upcoming, state, actions, Section.Upcoming)
        }
        if (openGames.isNotEmpty()) {
            header(Res.string.home_section_open_games, "home_section_open")
            gridItems(openGames, key = { "open_${it.id}" }) { j ->
                Box(Modifier.animateItem()) { OpenGameCard(j, state.now, onClick = { actions.openGame(j.id) }) }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }, key = "find_court") {
            Box(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                SCButton(
                    stringResource(Res.string.home_cta_find_court),
                    actions.findCourt,
                    variant = SCButtonVariant.Text,
                    leadingIcon = Icons.Rounded.Search,
                    modifier = Modifier.testTag("home_find_court"),
                )
            }
        }
    }
}

private enum class Section { Decision, Pending, Upcoming }

private fun LazyGridScope.header(res: org.jetbrains.compose.resources.StringResource, tag: String) {
    item(span = { GridItemSpan(maxLineSpan) }, key = "h_$tag") {
        SectionHeader(stringResource(res), Modifier.padding(top = 12.dp).testTag(tag))
    }
}

private fun LazyGridScope.items(
    games: List<HomeGame>,
    state: HomeUiState.Content,
    actions: HomeActions,
    section: Section,
    fullWidthSingle: Boolean = false,
) {
    games.forEach { g ->
        item(
            key = "${section.name}_${g.jogo.id}",
            span = { if (fullWidthSingle) GridItemSpan(maxLineSpan) else GridItemSpan(1) },
        ) {
            // A10: quando um jogo muda de secao (ex.: pagou -> sai de "Falta pagar"), o card anima.
            Box(Modifier.animateItem()) { HomeGameCard(g, state, actions, section) }
        }
    }
}

@Composable
private fun HomeGameCard(g: HomeGame, state: HomeUiState.Content, actions: HomeActions, section: Section) {
    val j = g.jogo
    val counts = j.splitCounts(state.now)
    val statusLabel = when (j.status) {
        StatusReserva.AGUARDANDO_PAGAMENTOS, StatusReserva.RASCUNHO -> stringResource(Res.string.home_card_status_pending, counts.paid + counts.covered, j.numeroVagas)
        StatusReserva.CONFIRMADO -> stringResource(Res.string.home_card_status_confirmed)
        else -> null
    }
    val organizer = g.role == HomeRole.Organizer
    val ui = j.toCardUi(
        userId = state.userId,
        statusLabel = statusLabel,
        shareCaption = stringResource(Res.string.guest_your_share),
        countdownLabel = null,
        now = state.now,
        roleTag = if (organizer) stringResource(Res.string.home_role_organizer) else null,
    )
    val primary = when (section) {
        Section.Pending -> SCAction(
            stringResource(Res.string.home_pay_now),
            { actions.pay(j.id, j.status == StatusReserva.RASCUNHO) },
        )
        Section.Decision -> SCAction(stringResource(Res.string.game_resolution_open), { actions.resolve(j.id) })
        Section.Upcoming -> null
    }
    SCGameCard(
        game = ui,
        onClick = { actions.openGame(j.id) },
        variant = if (organizer && section == Section.Upcoming) SCGameCardVariant.Organizer else SCGameCardVariant.Participant,
        primaryAction = primary,
        modifier = Modifier.fillMaxWidth().testTag("home_game_card_${j.id}"),
        sharedTitleKey = sharedGameTitleKey(j.id),
    )
}

@Suppress("unused")
private val fabElevation = SCElevation.level3

/** E8: convite para ativar lembretes (finalidade clara antes do pedido do sistema). */
@Composable
private fun ReminderCard(onEnable: () -> Unit, onDismiss: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(
        Modifier.fillMaxWidth().background(c.primaryContainer.copy(alpha = 0.35f), MaterialTheme.shapes.large).padding(16.dp).testTag("reminder_card"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.NotificationsActive, null, tint = c.onSurface, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(stringResource(Res.string.reminders_card_title), style = t.titleSmall, color = c.onSurface)
        }
        Text(stringResource(Res.string.reminders_card_body), style = t.bodyMedium, color = c.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SCButton(stringResource(Res.string.reminders_enable), onEnable, size = SCButtonSize.Small, modifier = Modifier.testTag("reminder_enable"))
            SCButton(stringResource(Res.string.common_not_now), onDismiss, variant = SCButtonVariant.Text, size = SCButtonSize.Small)
        }
    }
}
