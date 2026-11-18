@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.mygames

import br.com.sportconnect.core.designsystem.a11y.webSelectedLabel
import br.com.sportconnect.core.designsystem.component.sharedGameTitleKey
import org.koin.compose.koinInject
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.designsystem.component.SCPullToRefresh
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.designsystem.component.GameCardSkeleton
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCGameCard
import br.com.sportconnect.core.designsystem.component.SCGameCardVariant
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarVariant
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.GetMyGamesUseCase
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.feature.common.SectionHeader
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.labelRes
import br.com.sportconnect.feature.common.splitCounts
import br.com.sportconnect.feature.common.toCardUi
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.empty_games_cta
import br.com.sportconnect.resources.empty_games_title
import br.com.sportconnect.resources.guest_your_share
import br.com.sportconnect.resources.home_card_status_pending
import br.com.sportconnect.resources.home_empty_hint
import br.com.sportconnect.resources.home_role_organizer
import br.com.sportconnect.resources.my_games_repeat
import br.com.sportconnect.resources.my_games_tab_past
import br.com.sportconnect.resources.my_games_tab_upcoming
import br.com.sportconnect.resources.my_games_title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.DatePeriod
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant

data class MyGamesUiState(
    val loading: Boolean = true,
    val tab: Int = 0,
    val sport: Sport? = null,
    val userId: String? = null,
    val upcoming: List<Jogo> = emptyList(),
    val past: List<Jogo> = emptyList(),
    val now: Instant = Instant.DISTANT_PAST,
) {
    val visible: List<Jogo> get() = (if (tab == 0) upcoming else past).filter { sport == null || it.esporte == sport }
    val sports: List<Sport> get() = (upcoming + past).map { it.esporte }.distinct().sortedBy { it.ordinal }
}

class MyGamesViewModel(
    private val getMyGames: GetMyGamesUseCase,
    private val auth: AuthRepository,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyGamesUiState())
    val uiState: StateFlow<MyGamesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(getMyGames(), auth.currentUser) { g, u -> g to u }.collect { (g, u) ->
                _uiState.update {
                    it.copy(loading = false, userId = u?.id, upcoming = g.proximos.filter { j -> j.status != StatusReserva.RASCUNHO }, past = g.historico, now = clock.now())
                }
            }
        }
    }

    fun selectTab(i: Int) = _uiState.update { it.copy(tab = i) }
    fun selectSport(s: Sport) = _uiState.update { it.copy(sport = if (it.sport == s) null else s) }
}

data class MyGamesActions(
    val openGame: (String) -> Unit = {},
    val createGame: () -> Unit = {},
    val repeat: (Jogo) -> Unit = {},
)

@Composable
fun MyGamesRoute(actions: MyGamesActions, vm: MyGamesViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val games: GameRepository = koinInject()
    MyGamesScreen(state, actions, vm::selectTab, vm::selectSport, onRefresh = games::sincronizar)
}

/** Meus jogos (10 §2.13): abas Proximos/Anteriores, filtro por esporte, cabecalhos por semana. */
@Composable
fun MyGamesScreen(
    state: MyGamesUiState,
    actions: MyGamesActions = MyGamesActions(),
    onTab: (Int) -> Unit = {},
    onSport: (Sport) -> Unit = {},
    onRefresh: suspend () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        Scaffold(
            containerColor = c.surface,
            topBar = { SCTopBar(stringResource(Res.string.my_games_title), variant = SCTopBarVariant.CenterAligned, navigation = null) },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = 760.dp).fillMaxSize()) {
                    PrimaryTabRow(selectedTabIndex = state.tab, containerColor = c.surface) {
                        Tab(state.tab == 0, { onTab(0) }, text = { Text(stringResource(Res.string.my_games_tab_upcoming)) }, modifier = Modifier.webSelectedLabel(stringResource(Res.string.my_games_tab_upcoming), state.tab == 0).testTag("mygames_tab_upcoming"))
                        Tab(state.tab == 1, { onTab(1) }, text = { Text(stringResource(Res.string.my_games_tab_past)) }, modifier = Modifier.webSelectedLabel(stringResource(Res.string.my_games_tab_past), state.tab == 1).testTag("mygames_tab_past"))
                    }
                    if (state.sports.size > 1) {
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 16.dp, top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.sports.forEach { s ->
                                SCSportChip(s, variant = SCSportChipVariant.Filter, selected = state.sport == s, onClick = { onSport(s) }, modifier = Modifier.testTag("mygames_filter_${s.name.lowercase()}"))
                            }
                        }
                    }
                    when {
                        state.loading -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { repeat(3) { GameCardSkeleton() } }
                        state.visible.isEmpty() -> SCEmptyState(
                            title = stringResource(Res.string.empty_games_title),
                            description = stringResource(Res.string.home_empty_hint),
                            primaryAction = SCAction(stringResource(Res.string.empty_games_cta), actions.createGame, icon = Icons.Rounded.Add),
                        )
                        else -> {
                            val grouped = state.visible.groupBy { weekStart(br.com.sportconnect.core.model.PtBrFormat.date(it.inicio)) }
                            SCPullToRefresh(onRefresh, Modifier.fillMaxSize()) {
                                LazyColumn(
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    grouped.forEach { (week, games) ->
                                        item(key = "w$week") {
                                            SectionHeader("Semana de ${br.com.sportconnect.core.model.PtBrFormat.dayMonth(week)}", Modifier.padding(top = 12.dp).animateItem())
                                        }
                                        // A10: itens entram/saem/trocam de lugar animados (ex.: ao trocar o filtro).
                                        items(games, key = { it.id }) { j -> Box(Modifier.animateItem()) { MyGameCard(j, state, actions, wc) } }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun weekStart(d: LocalDate): LocalDate = d.minus(DatePeriod(days = (d.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal)))

@Composable
private fun MyGameCard(j: Jogo, state: MyGamesUiState, actions: MyGamesActions, @Suppress("UNUSED_PARAMETER") wc: WidthClass) {
    val organizer = j.organizadorId == state.userId
    val counts = j.splitCounts(state.now)
    val statusLabel = if (j.status == StatusReserva.AGUARDANDO_PAGAMENTOS) {
        stringResource(Res.string.home_card_status_pending, counts.paid + counts.covered, j.numeroVagas)
    } else stringResource(j.status.labelRes)
    Column {
        SCGameCard(
            game = j.toCardUi(
                state.userId, statusLabel, stringResource(Res.string.guest_your_share), null, state.now,
                roleTag = if (organizer) stringResource(Res.string.home_role_organizer) else null,
            ),
            onClick = { actions.openGame(j.id) },
            variant = if (organizer) SCGameCardVariant.Organizer else SCGameCardVariant.Participant,
            modifier = Modifier.fillMaxWidth().testTag("mygames_card_${j.id}"),
            sharedTitleKey = sharedGameTitleKey(j.id),
        )
        if (state.tab == 1 && organizer) {
            SCButton(
                stringResource(Res.string.my_games_repeat),
                { actions.repeat(j) },
                variant = SCButtonVariant.Text,
                size = SCButtonSize.Small,
                modifier = Modifier.testTag("mygames_repeat_${j.id}"),
            )
        }
    }
}
