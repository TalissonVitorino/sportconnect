package br.com.sportconnect.feature.arenaadmin.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.component.TimeSlotGridSkeleton
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.ArenaTab
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellStatus
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellUi
import br.com.sportconnect.feature.arenaadmin.common.AgendaDayGrid
import br.com.sportconnect.feature.arenaadmin.common.AgendaDeskHeader
import br.com.sportconnect.feature.arenaadmin.common.AgendaListRow
import br.com.sportconnect.feature.arenaadmin.common.AgendaMode
import br.com.sportconnect.feature.arenaadmin.common.ArenaAdminScaffold
import br.com.sportconnect.feature.arenaadmin.common.ArenaErrorState
import br.com.sportconnect.feature.arenaadmin.common.ArenaMessageHandler
import br.com.sportconnect.feature.arenaadmin.common.ArenaNoArenaState
import br.com.sportconnect.feature.arenaadmin.common.ArenaPanelCard
import br.com.sportconnect.feature.arenaadmin.common.ArenaSectionTitle
import br.com.sportconnect.feature.arenaadmin.common.ArenaSidePanel
import br.com.sportconnect.feature.arenaadmin.common.ArenaWidth
import br.com.sportconnect.feature.arenaadmin.common.BlockSheet
import br.com.sportconnect.feature.arenaadmin.common.BookingDetailContent
import br.com.sportconnect.feature.arenaadmin.common.ChoiceChips
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_cd_next_day
import br.com.sportconnect.resources.aadm_cd_next_week
import br.com.sportconnect.resources.aadm_cd_prev_day
import br.com.sportconnect.resources.aadm_cd_prev_week
import br.com.sportconnect.resources.aadm_select_booking_hint
import br.com.sportconnect.resources.aadm_unblock_body
import br.com.sportconnect.resources.aadm_unblock_cta
import br.com.sportconnect.resources.aadm_unblock_title
import br.com.sportconnect.resources.arena_schedule_block_cta
import br.com.sportconnect.resources.arena_schedule_day
import br.com.sportconnect.resources.arena_schedule_status_free
import br.com.sportconnect.resources.arena_schedule_today
import br.com.sportconnect.resources.arena_schedule_week
import br.com.sportconnect.resources.arena_tab_schedule
import br.com.sportconnect.resources.common_cancel
import br.com.sportconnect.resources.empty_arena_schedule_body
import br.com.sportconnect.resources.empty_arena_schedule_title
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** `arena-admin/schedule` — Agenda (10 §2.18). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaScheduleScreen(
    nav: ArenaAdminNavigator,
    viewModel: ArenaScheduleViewModel = koinViewModel(),
    ticking: Boolean = true,
) {
    val state by viewModel.state.collectAsState()
    if (ticking) {
        LaunchedEffect(viewModel) {
            while (true) {
                delay(60_000)
                viewModel.refreshNow()
            }
        }
    }
    val snackbar = remember { SnackbarHostState() }
    ArenaMessageHandler(state.message, snackbar, viewModel::consumeMessage, viewModel::undoBlock, nav::openBooking)
    BlockSheet(state.block, viewModel::dismissBlock, viewModel::selectBlockCourt, viewModel::selectBlockSlot, viewModel::selectBlockReason, viewModel::confirmBlock)
    state.unblock?.let { cell ->
        SCDialog(
            title = stringResource(Res.string.aadm_unblock_title),
            body = stringResource(Res.string.aadm_unblock_body, cell.detail ?: "—"),
            onDismissRequest = viewModel::dismissUnblock,
            confirm = SCAction(stringResource(Res.string.aadm_unblock_cta), viewModel::confirmUnblock),
            dismiss = SCAction(stringResource(Res.string.common_cancel), viewModel::dismissUnblock),
        )
    }

    ArenaAdminScaffold(
        selected = ArenaTab.Schedule,
        nav = nav,
        snackbarHostState = snackbar,
        topBar = { w ->
            if (w != ArenaWidth.Expanded) {
                SCTopBar(
                    title = stringResource(Res.string.arena_tab_schedule),
                    navigation = if (w == ArenaWidth.Compact) SCTopBarNavigation.Back else null,
                    onNavigationClick = { nav.openTab(ArenaTab.Dashboard) },
                    actions = {
                        TextButton(onClick = viewModel::today, modifier = Modifier.testTag("schedule_today")) {
                            Text(stringResource(Res.string.arena_schedule_today))
                        }
                    },
                )
            }
        },
        bottomBar = { w ->
            if (w == ArenaWidth.Compact && state.load == LoadState.Ready) {
                SCButton(
                    stringResource(Res.string.arena_schedule_block_cta), viewModel::openBlock,
                    leadingIcon = Icons.Rounded.Block, fullWidth = true,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).testTag("arena_dash_block"),
                )
            }
        },
    ) { padding, width ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.load) {
                LoadState.Loading -> TimeSlotGridSkeleton(Modifier.padding(16.dp), columns = if (width == ArenaWidth.Compact) 1 else 4, rows = 6)
                LoadState.Error -> ArenaErrorState(viewModel::retry)
                LoadState.NoArena -> ArenaNoArenaState(nav::openOnboarding)
                LoadState.Ready -> if (width == ArenaWidth.Compact) ScheduleCompact(state, viewModel, nav) else ScheduleWide(state, viewModel, nav, width)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSegmented(mode: AgendaMode, onMode: (AgendaMode) -> Unit, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        AgendaMode.entries.forEachIndexed { i, m ->
            SegmentedButton(
                selected = mode == m,
                onClick = { onMode(m) },
                shape = SegmentedButtonDefaults.itemShape(i, AgendaMode.entries.size),
                modifier = Modifier.testTag(if (m == AgendaMode.Day) "schedule_mode_day" else "schedule_mode_week"),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = c.secondaryContainer, activeContentColor = c.onSecondaryContainer),
            ) { Text(stringResource(if (m == AgendaMode.Day) Res.string.arena_schedule_day else Res.string.arena_schedule_week)) }
        }
    }
}

@Composable
private fun DateNavigator(state: ArenaScheduleUiState, viewModel: ArenaScheduleViewModel) {
    val week = state.mode == AgendaMode.Week
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = viewModel::previous, modifier = Modifier.testTag("schedule_prev_day")) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, stringResource(if (week) Res.string.aadm_cd_prev_week else Res.string.aadm_cd_prev_day))
        }
        Text(
            if (week) state.weekLabel else state.dateLabel,
            style = MaterialTheme.typography.titleMedium,
            color = SportConnectTheme.colors.onSurface,
            modifier = Modifier.weight(1f).semantics { heading() },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = viewModel::next, modifier = Modifier.testTag("schedule_next_day")) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, stringResource(if (week) Res.string.aadm_cd_next_week else Res.string.aadm_cd_next_day))
        }
    }
}

@Composable
private fun CourtTabs(state: ArenaScheduleUiState, onSelect: (String) -> Unit) {
    val courts = state.agenda.courts
    if (courts.isEmpty()) return
    val idx = courts.indexOfFirst { it.courtId == state.courtId }.coerceAtLeast(0)
    PrimaryScrollableTabRow(selectedTabIndex = idx, edgePadding = 16.dp, containerColor = SportConnectTheme.colors.surface) {
        courts.forEachIndexed { i, court ->
            Tab(
                selected = i == idx,
                onClick = { onSelect(court.courtId) },
                text = { Text(court.name, style = MaterialTheme.typography.titleSmall) },
                modifier = Modifier.testTag("schedule_court_tab_${court.courtId}"),
            )
        }
    }
}

private fun cellClick(cell: AgendaCellUi, viewModel: ArenaScheduleViewModel, nav: ArenaAdminNavigator, inlineDetail: Boolean) {
    if (cell.isBooking && cell.slotId != null && !inlineDetail) nav.openBooking(cell.slotId) else viewModel.onCellClick(cell)
}

@Composable
private fun ScheduleCompact(state: ArenaScheduleUiState, viewModel: ArenaScheduleViewModel, nav: ArenaAdminNavigator) {
    val c = SportConnectTheme.colors
    val courtName = state.agenda.courts.firstOrNull { it.courtId == state.courtId }?.name.orEmpty()
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeSegmented(state.mode, viewModel::setMode)
            DateNavigator(state, viewModel)
        }
        CourtTabs(state, viewModel::selectCourt)
        if (state.mode == AgendaMode.Day) {
            if (state.isEmptyDay) {
                SCEmptyState(stringResource(Res.string.empty_arena_schedule_title), stringResource(Res.string.empty_arena_schedule_body))
            }
            // Abre perto da hora atual (hoje) para o balcao nao precisar rolar.
            val startIndex = if (state.isToday) state.courtCells.indexOfFirst { it.hour >= state.nowMinutes / 60 - 1 }.coerceAtLeast(0) else 0
            val listState = androidx.compose.runtime.key(state.date, state.courtId) { androidx.compose.foundation.lazy.rememberLazyListState(startIndex) }
            LazyColumn(Modifier.fillMaxSize().testTag("schedule_list"), state = listState) {
                items(state.courtCells, key = { it.slotId ?: "${it.courtId}-${it.hour}" }) { cell ->
                    AgendaListRow(cell, courtName, { cellClick(it, viewModel, nav, inlineDetail = false) })
                    HorizontalDivider(Modifier.padding(start = 72.dp), color = c.outlineVariant)
                }
            }
        } else {
            val week = state.week
            LazyColumn(Modifier.fillMaxSize().testTag("schedule_week_list")) {
                week?.days?.forEachIndexed { i, day ->
                    item(key = "h-${day.date}") {
                        ArenaSectionTitle(
                            if (day.isToday) "${day.label} · ${stringResource(Res.string.arena_schedule_today)}" else day.label,
                            Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    val relevant = week.cells[i].filter { it.status != AgendaCellStatus.Free && it.status != AgendaCellStatus.Closed }
                    items(relevant, key = { it.slotId ?: "${day.date}-${it.hour}" }) { cell ->
                        AgendaListRow(cell, courtName, { cellClick(it, viewModel, nav, inlineDetail = false) })
                    }
                    item(key = "f-${day.date}") {
                        val free = week.cells[i].count { it.status == AgendaCellStatus.Free }
                        Text(
                            "${free}× ${stringResource(Res.string.arena_schedule_status_free)}",
                            style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant,
                            modifier = Modifier.padding(start = 72.dp, bottom = 8.dp),
                        )
                        HorizontalDivider(color = c.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleWide(state: ArenaScheduleUiState, viewModel: ArenaScheduleViewModel, nav: ArenaAdminNavigator, width: ArenaWidth) {
    val expanded = width == ArenaWidth.Expanded
    val week = state.mode == AgendaMode.Week
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
            if (expanded) {
                AgendaDeskHeader(
                    title = stringResource(Res.string.arena_tab_schedule),
                    mode = state.mode,
                    onMode = viewModel::setMode,
                    dateLabel = if (week) state.weekLabel else state.dateLabel,
                    prevCd = stringResource(if (week) Res.string.aadm_cd_prev_week else Res.string.aadm_cd_prev_day),
                    nextCd = stringResource(if (week) Res.string.aadm_cd_next_week else Res.string.aadm_cd_next_day),
                    onPrev = viewModel::previous,
                    onNext = viewModel::next,
                    onToday = viewModel::today,
                    onBlock = viewModel::openBlock,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModeSegmented(state.mode, viewModel::setMode, Modifier.weight(1f))
                    SCButton(stringResource(Res.string.arena_schedule_block_cta), viewModel::openBlock, leadingIcon = Icons.Rounded.Block)
                }
                DateNavigator(state, viewModel)
            }
            if (week) {
                ChoiceChips(
                    state.agenda.courts.map { it.courtId }, state.courtId,
                    { id -> state.agenda.courts.first { it.courtId == id }.name }, viewModel::selectCourt,
                    modifier = Modifier.padding(top = 12.dp), tag = { "schedule_court_tab_$it" },
                )
            }
            Spacer(Modifier.height(12.dp))
            val grid = if (week) state.week?.asGrid() else state.agenda
            if (grid != null) {
                AgendaDayGrid(
                    agenda = grid,
                    nowMinutes = if (!week && state.isToday) state.nowMinutes else null,
                    selectedSlotId = state.selectedSlotId,
                    onCellClick = { cellClick(it, viewModel, nav, inlineDetail = expanded) },
                    showOrganizer = week || (width == ArenaWidth.Expanded && state.agenda.courts.size <= 3),
                    showSport = !week,
                    columnTitle = { col -> if (week) col.name else "${col.name} · ${col.mainSport.label()}" },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
        if (expanded) {
            ArenaSidePanel(Modifier.testTag("arena_side_panel")) {
                ArenaPanelCard {
                    val sel = state.selected
                    if (sel == null) {
                        Text(stringResource(Res.string.aadm_select_booking_hint), style = MaterialTheme.typography.bodyMedium, color = SportConnectTheme.colors.onSurfaceVariant)
                    } else {
                        BookingDetailContent(sel, panel = true, showFinancials = state.showFinancials, onCancel = { nav.openBooking(sel.slotId) }, onRelease = { nav.openBooking(sel.slotId) })
                    }
                }
            }
        }
    }
}
