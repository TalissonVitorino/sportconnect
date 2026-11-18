@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.explore

import androidx.compose.runtime.LaunchedEffect
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import br.com.sportconnect.resources.empty_open_games_body
import br.com.sportconnect.resources.empty_open_games_title
import br.com.sportconnect.resources.search_tab_games
import br.com.sportconnect.resources.search_tab_courts
import br.com.sportconnect.core.designsystem.a11y.webSelectedLabel
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.feature.common.OpenGameCard
import br.com.sportconnect.feature.common.OpenGamesViewModel
import br.com.sportconnect.core.designsystem.a11y.webCheckedLabel
import br.com.sportconnect.resources.cd_map_pin
import br.com.sportconnect.resources.search_list_view
import br.com.sportconnect.resources.search_map_view
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Map
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.saveable.rememberSaveable
import br.com.sportconnect.core.designsystem.map.rememberSCMapCamera
import br.com.sportconnect.core.designsystem.map.SCMapPin
import br.com.sportconnect.core.designsystem.map.SCMap
import br.com.sportconnect.core.designsystem.map.MapTileCache
import br.com.sportconnect.resources.search_price_up_to
import br.com.sportconnect.resources.search_price_any
import br.com.sportconnect.resources.search_period_night
import br.com.sportconnect.resources.search_period_afternoon
import br.com.sportconnect.resources.search_period_morning
import br.com.sportconnect.resources.search_period_any
import br.com.sportconnect.resources.search_filter_price
import br.com.sportconnect.resources.search_filter_time
import br.com.sportconnect.resources.search_sort_price
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import org.koin.compose.koinInject
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.designsystem.component.SCPullToRefresh
import br.com.sportconnect.core.designsystem.theme.SCSelection
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Roofing
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.CourtCardSkeleton
import br.com.sportconnect.core.designsystem.component.CourtCardUi
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCourtCard
import br.com.sportconnect.core.designsystem.component.SCCourtCardVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SlotUi
import br.com.sportconnect.core.designsystem.icon.icon
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.feature.common.LoadErrorState
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_clear_text
import br.com.sportconnect.resources.common_per_person_estimate
import br.com.sportconnect.resources.empty_search_body
import br.com.sportconnect.resources.empty_search_cta
import br.com.sportconnect.resources.empty_search_title
import br.com.sportconnect.resources.search_card_distance
import br.com.sportconnect.resources.search_card_price_from
import br.com.sportconnect.resources.search_field_placeholder
import br.com.sportconnect.resources.search_filter_covered
import br.com.sportconnect.resources.search_filter_sport
import br.com.sportconnect.resources.search_results_count
import br.com.sportconnect.resources.search_sort_distance
import br.com.sportconnect.resources.search_title
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class ExploreActions(
    val openArena: (String) -> Unit = {},
    /** T3: abre um jogo com vaga. */
    val openGame: (String) -> Unit = {},
    val pickSlot: (slotId: String) -> Unit = {},
)

@Composable
fun ExploreRoute(actions: ExploreActions, viewModel: ExploreViewModel = koinViewModel(), openVm: OpenGamesViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val openGames by openVm.games.collectAsStateWithLifecycle()
    // O filtro de esporte vale para as duas abas.
    LaunchedEffect(state.sport) { openVm.setSport(state.sport) }
    val games: GameRepository = koinInject()
    val tiles: MapTileCache = koinInject()
    ExploreScreen(
        tiles = tiles,
        openGames = openGames,
        state = state,
        actions = actions,
        onQuery = viewModel::setQuery,
        onSport = viewModel::setSport,
        onDate = viewModel::setDate,
        onCovered = viewModel::toggleCovered,
        onClear = viewModel::clearFilters,
        onPeriod = viewModel::setPeriod,
        onMaxPrice = viewModel::setMaxPrice,
        onSort = viewModel::setSort,
        onRetry = viewModel::retry,
        onRefresh = games::sincronizar,
    )
}

/** Buscar quadra (10 §2.4, mockup "Buscar"). Medium: 2 colunas; expanded: lista horizontal 2 colunas. */
@Composable
fun ExploreScreen(
    state: ExploreUiState,
    actions: ExploreActions = ExploreActions(),
    onQuery: (String) -> Unit = {},
    onSport: (Sport?) -> Unit = {},
    onDate: (LocalDate) -> Unit = {},
    onCovered: () -> Unit = {},
    onClear: () -> Unit = {},
    onRetry: () -> Unit = {},
    onRefresh: suspend () -> Unit = {},
    onPeriod: (DayPeriod?) -> Unit = {},
    onMaxPrice: (Money?) -> Unit = {},
    onSort: (ExploreSort) -> Unit = {},
    /** M2: tiles do mapa; null (screenshots) = sem mapa. */
    tiles: MapTileCache? = null,
    /** T3: jogos com vaga (aba "Jogos com vaga", US-101). */
    openGames: List<Jogo> = emptyList(),
) {
    var gamesTab by rememberSaveable { mutableStateOf(false) }
    val c = SportConnectTheme.colors
    var mapMode by rememberSaveable { mutableStateOf(false) }
    var selectedArena by rememberSaveable { mutableStateOf<String?>(null) }
    val camera = rememberSCMapCamera()
    val gridState = rememberLazyGridState()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        // M2: no expandido, lista e mapa lado a lado.
        val split = tiles != null && wc == WidthClass.Expanded
        val columns = if (wc == WidthClass.Compact || split) 1 else 2
        val hPad = if (wc == WidthClass.Compact) 16.dp else 24.dp
        Scaffold(
            containerColor = c.surface,
            // T4: titulo na mesma largura do conteudo (antes ficava colado a esquerda em telas medias).
            topBar = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    SCTopBar(
                        stringResource(Res.string.search_title), navigation = null,
                        modifier = Modifier.widthIn(max = if (split) 1600.dp else if (wc == WidthClass.Expanded) 1040.dp else 760.dp)
                            .padding(horizontal = if (wc == WidthClass.Compact) 0.dp else 8.dp),
                    )
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = if (split) 1600.dp else if (wc == WidthClass.Expanded) 1040.dp else 760.dp).fillMaxSize()) {
                    SCTextField(
                        value = state.query,
                        onValueChange = onQuery,
                        label = "",
                        type = SCTextFieldType.Search,
                        placeholder = stringResource(Res.string.search_field_placeholder),
                        leadingIcon = Icons.Rounded.Search,
                        trailing = if (state.query.isNotEmpty()) {
                            { IconButton(onClick = { onQuery("") }) { Icon(Icons.Rounded.Cancel, stringResource(Res.string.cd_clear_text)) } }
                        } else null,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = hPad).testTag("explore_search_field"),
                    )
                    // T3: abas "Quadras | Jogos com vaga" (09 ficha 4).
                    PrimaryTabRow(selectedTabIndex = if (gamesTab) 1 else 0, containerColor = c.surface, modifier = Modifier.padding(top = 8.dp)) {
                        val courtsLabel = stringResource(Res.string.search_tab_courts)
                        val gamesLabel = stringResource(Res.string.search_tab_games)
                        Tab(!gamesTab, { gamesTab = false }, text = { Text(courtsLabel) }, modifier = Modifier.webSelectedLabel(courtsLabel, !gamesTab).testTag("explore_tab_courts"))
                        Tab(gamesTab, { gamesTab = true }, text = { Text(gamesLabel) }, modifier = Modifier.webSelectedLabel(gamesLabel, gamesTab).testTag("explore_tab_games"))
                    }
                    if (gamesTab) {
                        OpenGamesList(openGames, state.sport, onSport, hPad, actions)
                        return@Column
                    }
                    FilterRow(state, onSport, onDate, onCovered, hPad, onPeriod, onMaxPrice)
                    Row(Modifier.fillMaxWidth().padding(horizontal = hPad, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.search_results_count, state.results.size, PtBrFormat.relativeDay(state.date, state.today)),
                            style = MaterialTheme.typography.titleSmall,
                            color = c.onSurface,
                            modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                        )
                        // E10: ordenar (antes o botao "Mais perto" nao fazia nada).
                        Box {
                            var sortMenu by remember { mutableStateOf(false) }
                            SCButton(
                                stringResource(if (state.sort == ExploreSort.PRICE) Res.string.search_sort_price else Res.string.search_sort_distance),
                                { sortMenu = true },
                                variant = SCButtonVariant.Text,
                                size = SCButtonSize.Small,
                                modifier = Modifier.testTag("explore_sort"),
                            )
                            DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                                DropdownMenuItem(text = { Text(stringResource(Res.string.search_sort_distance)) }, onClick = { sortMenu = false; onSort(ExploreSort.DISTANCE) })
                                DropdownMenuItem(text = { Text(stringResource(Res.string.search_sort_price)) }, onClick = { sortMenu = false; onSort(ExploreSort.PRICE) })
                            }
                        }
                        // M2: alterna lista <-> mapa (no expandido os dois aparecem juntos).
                        if (tiles != null && !split) {
                            SCButton(
                                stringResource(if (mapMode) Res.string.search_list_view else Res.string.search_map_view),
                                { mapMode = !mapMode; selectedArena = null },
                                variant = SCButtonVariant.Text,
                                size = SCButtonSize.Small,
                                leadingIcon = if (mapMode) Icons.AutoMirrored.Rounded.List else Icons.Rounded.Map,
                                modifier = Modifier.testTag("explore_map_toggle"),
                            )
                        }
                    }
                    Crossfade(Triple(state.loading, state.error, state.results.isEmpty()), label = "results") { (loading, error, empty) ->
                        when {
                            loading -> Column(Modifier.padding(horizontal = hPad), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                repeat(3) { CourtCardSkeleton() }
                            }
                            error -> LoadErrorState(onRetry)
                            empty -> SCEmptyState(
                                title = stringResource(Res.string.empty_search_title),
                                description = stringResource(Res.string.empty_search_body),
                                illustration = SCIllustration.NoResults,
                                primaryAction = SCAction(stringResource(Res.string.empty_search_cta), onClear),
                                modifier = Modifier.testTag("explore_clear_filters"),
                            )
                            else -> {
                                val list: @Composable (Modifier) -> Unit = { m ->
                                    SCPullToRefresh(onRefresh, m) {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(columns),
                                            state = gridState,
                                            contentPadding = PaddingValues(start = hPad, end = hPad, top = 8.dp, bottom = 24.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.fillMaxSize(),
                                        ) {
                                            // A10: ao filtrar, os cards entram/saem/reordenam animados.
                                            items(state.results, key = { it.quadra.id }) { r -> Box(Modifier.animateItem()) { CourtResultCard(r, actions, wc) } }
                                            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.size(8.dp)) }
                                        }
                                    }
                                }
                                val pins = arenaPins(state.results)
                                val scope = rememberCoroutineScope()
                                when {
                                    tiles != null && split -> Row(Modifier.fillMaxSize()) {
                                        list(Modifier.weight(1f).fillMaxSize())
                                        SCMap(
                                            camera, tiles, pins,
                                            modifier = Modifier.weight(1f).fillMaxSize().padding(end = hPad, top = 8.dp, bottom = 16.dp).clip(MaterialTheme.shapes.extraLarge),
                                            selectedPinId = selectedArena,
                                            onPinClick = { pin ->
                                                selectedArena = pin.id
                                                val idx = state.results.indexOfFirst { it.arena.arena.id == pin.id }
                                                if (idx >= 0) scope.launch { gridState.animateScrollToItem(idx) }
                                            },
                                            onMapClick = { selectedArena = null },
                                        )
                                    }
                                    tiles != null && mapMode -> Box(Modifier.fillMaxSize().testTag("explore_map")) {
                                        SCMap(
                                            camera, tiles, pins, Modifier.fillMaxSize(),
                                            selectedPinId = selectedArena,
                                            onPinClick = { selectedArena = it.id },
                                            onMapClick = { selectedArena = null },
                                        )
                                        // A quadra mais barata da arena: o mesmo preco do pino.
                                        val sel = state.results.filter { it.arena.arena.id == selectedArena }
                                            .minByOrNull { r -> r.freeSlots.minOfOrNull { it.preco } ?: r.quadra.precoHora }
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = sel != null,
                                            enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
                                            exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut(),
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(hPad),
                                        ) {
                                            if (sel != null) CourtResultCard(sel, actions, wc, SCCourtCardVariant.Map)
                                        }
                                    }
                                    else -> list(Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    state: ExploreUiState,
    onSport: (Sport?) -> Unit,
    onDate: (LocalDate) -> Unit,
    onCovered: () -> Unit,
    hPad: androidx.compose.ui.unit.Dp,
    onPeriod: (DayPeriod?) -> Unit = {},
    onMaxPrice: (Money?) -> Unit = {},
) {
    val c = SportConnectTheme.colors
    var sportMenu by remember { mutableStateOf(false) }
    var dateMenu by remember { mutableStateOf(false) }
    val chipColors = SCSelection.filterChipColors()
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = hPad, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            FilterChip(
                selected = state.sport != null,
                onClick = { sportMenu = true },
                label = { Text(state.sport?.label() ?: stringResource(Res.string.search_filter_sport)) },
                leadingIcon = {
                    if (state.sport != null) Icon(Icons.Rounded.Check, null, Modifier.size(18.dp))
                    else Icon(Icons.Rounded.SportsSoccer, null, Modifier.size(18.dp))
                },
                trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null, Modifier.size(18.dp)) },
                colors = chipColors,
                modifier = Modifier.testTag("explore_filter_sport"),
            )
            DropdownMenu(expanded = sportMenu, onDismissRequest = { sportMenu = false }) {
                DropdownMenuItem(text = { Text(stringResource(Res.string.search_filter_sport)) }, onClick = { sportMenu = false; onSport(null) })
                Sport.entries.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.label()) },
                        leadingIcon = { Icon(s.icon, null) },
                        onClick = { sportMenu = false; onSport(s) },
                    )
                }
            }
        }
        Box {
            FilterChip(
                selected = state.date != state.today,
                onClick = { dateMenu = true },
                label = { Text(PtBrFormat.relativeDayLabel(state.date, state.today)) },
                leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(18.dp)) },
                trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null, Modifier.size(18.dp)) },
                colors = chipColors,
                modifier = Modifier.testTag("explore_filter_date"),
            )
            DropdownMenu(expanded = dateMenu, onDismissRequest = { dateMenu = false }) {
                state.days.forEach { d ->
                    DropdownMenuItem(text = { Text(PtBrFormat.relativeDayLabel(d, state.today)) }, onClick = { dateMenu = false; onDate(d) })
                }
            }
        }
        FilterChip(
            selected = state.coveredOnly,
            onClick = onCovered,
            label = { Text(stringResource(Res.string.search_filter_covered)) },
            leadingIcon = { Icon(if (state.coveredOnly) Icons.Rounded.Check else Icons.Rounded.Roofing, null, Modifier.size(18.dp)) },
            colors = chipColors,
            modifier = Modifier.webCheckedLabel(stringResource(Res.string.search_filter_covered), state.coveredOnly).testTag("explore_filter_covered"),
        )
        // E10: faixa de horario.
        Box {
            var periodMenu by remember { mutableStateOf(false) }
            val periodLabel = when (state.period) {
                DayPeriod.MORNING -> stringResource(Res.string.search_period_morning)
                DayPeriod.AFTERNOON -> stringResource(Res.string.search_period_afternoon)
                DayPeriod.NIGHT -> stringResource(Res.string.search_period_night)
                null -> stringResource(Res.string.search_filter_time)
            }
            FilterChip(
                selected = state.period != null,
                onClick = { periodMenu = true },
                label = { Text(periodLabel) },
                leadingIcon = { Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp)) },
                trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null, Modifier.size(18.dp)) },
                colors = chipColors,
                modifier = Modifier.testTag("explore_filter_time"),
            )
            DropdownMenu(expanded = periodMenu, onDismissRequest = { periodMenu = false }) {
                DropdownMenuItem(text = { Text(stringResource(Res.string.search_period_any)) }, onClick = { periodMenu = false; onPeriod(null) })
                DropdownMenuItem(text = { Text(stringResource(Res.string.search_period_morning)) }, onClick = { periodMenu = false; onPeriod(DayPeriod.MORNING) })
                DropdownMenuItem(text = { Text(stringResource(Res.string.search_period_afternoon)) }, onClick = { periodMenu = false; onPeriod(DayPeriod.AFTERNOON) })
                DropdownMenuItem(text = { Text(stringResource(Res.string.search_period_night)) }, onClick = { periodMenu = false; onPeriod(DayPeriod.NIGHT) })
            }
        }
        // E10: preco maximo por hora.
        Box {
            var priceMenu by remember { mutableStateOf(false) }
            FilterChip(
                selected = state.maxPrice != null,
                onClick = { priceMenu = true },
                label = { Text(state.maxPrice?.let { stringResource(Res.string.search_price_up_to, it.format()) } ?: stringResource(Res.string.search_filter_price)) },
                leadingIcon = { Icon(Icons.Rounded.Payments, null, Modifier.size(18.dp)) },
                trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, null, Modifier.size(18.dp)) },
                colors = chipColors,
                modifier = Modifier.testTag("explore_filter_price"),
            )
            DropdownMenu(expanded = priceMenu, onDismissRequest = { priceMenu = false }) {
                DropdownMenuItem(text = { Text(stringResource(Res.string.search_price_any)) }, onClick = { priceMenu = false; onMaxPrice(null) })
                listOf(15_000L, 20_000L, 30_000L).forEach { cents ->
                    val m = Money(cents)
                    DropdownMenuItem(text = { Text(stringResource(Res.string.search_price_up_to, m.format())) }, onClick = { priceMenu = false; onMaxPrice(m) })
                }
            }
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun CourtResultCard(r: CourtResult, actions: ExploreActions, wc: WidthClass, variant: SCCourtCardVariant = SCCourtCardVariant.List) {
    val q = r.quadra
    val price = r.freeSlots.minOf { it.preco }
    val spots = q.esportes.first().esporte.vagasPadrao.coerceAtMost(q.esportes.first().capacidadeMax)
    val perPerson = Money(price.cents / spots)
    val details = buildList {
        add(q.piso)
        if (q.coberta) add("Coberta")
    }.joinToString(" · ")
    SCCourtCard(
        court = CourtCardUi(
            arenaName = r.arena.arena.nome,
            courtName = q.nome,
            detailsLabel = details,
            sports = q.esportes.map { it.esporte },
            priceFrom = price,
            priceFromPrefix = stringResource(Res.string.search_card_price_from, "").removeSuffix("/h").trim(),
            priceSuffix = "/h",
            distanceLabel = stringResource(Res.string.search_card_distance, r.arena.distanciaFormatada.removeSuffix(" km")),
            nextSlots = r.freeSlots.take(3).map { s ->
                SlotUi(id = s.id, startLabel = PtBrFormat.hour(s.inicio), endLabel = PtBrFormat.hour(s.fim), price = s.preco)
            },
            estimateLabel = stringResource(Res.string.common_per_person_estimate, perPerson.format(), spots),
        ),
        onClick = { actions.openArena(r.arena.arena.id) },
        onSlotClick = { actions.pickSlot(it.id) },
        variant = variant,
        modifier = Modifier.fillMaxWidth().testTag("explore_court_card_${q.id}"),
    )
}

/** M2: um pino por arena, com o menor preco/hora entre as quadras do resultado. */
@Composable
private fun arenaPins(results: List<CourtResult>): List<SCMapPin> {
    val template = stringResource(Res.string.cd_map_pin, "%1", "%2")
    val fromTemplate = stringResource(Res.string.search_card_price_from, "%3")
    return results.groupBy { it.arena.arena.id }.map { (id, rs) ->
        val a = rs.first().arena
        val min = rs.minOf { r -> r.freeSlots.minOfOrNull { it.preco } ?: r.quadra.precoHora }
        SCMapPin(
            id = id,
            latitude = a.arena.localizacao.latitude,
            longitude = a.arena.localizacao.longitude,
            label = min.format().substringBefore(","),
            description = template.replace("%1", a.arena.nome).replace("%2", a.distanciaFormatada.removeSuffix(" km")) + ", " + fromTemplate.replace("%3", min.format()),
        )
    }
}

/** T3: aba "Jogos com vaga" — filtro de esporte e lista de jogos abertos (ou estado vazio do 07). */
@Composable
private fun OpenGamesList(games: List<Jogo>, sport: Sport?, onSport: (Sport?) -> Unit, hPad: androidx.compose.ui.unit.Dp, actions: ExploreActions) {
    val now = LocalSCClock.current.now()
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = hPad, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Sport.entries.forEach { s -> SCSportChip(s, variant = SCSportChipVariant.Filter, selected = sport == s, onClick = { onSport(if (sport == s) null else s) }) }
    }
    if (games.isEmpty()) {
        SCEmptyState(
            title = stringResource(Res.string.empty_open_games_title),
            description = stringResource(Res.string.empty_open_games_body),
            illustration = SCIllustration.NoResults,
            modifier = Modifier.testTag("explore_open_games_empty"),
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = hPad, end = hPad, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().testTag("explore_open_games"),
        ) {
            items(games, key = { it.id }) { j -> OpenGameCard(j, now, onClick = { actions.openGame(j.id) }, modifier = Modifier.animateItem()) }
        }
    }
}
