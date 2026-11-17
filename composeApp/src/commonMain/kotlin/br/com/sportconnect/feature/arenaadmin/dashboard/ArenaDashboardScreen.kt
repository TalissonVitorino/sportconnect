package br.com.sportconnect.feature.arenaadmin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.CourtCardSkeleton
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.component.SCSplitProgress
import br.com.sportconnect.core.designsystem.component.SCSplitProgressVariant
import br.com.sportconnect.core.designsystem.component.TimeSlotGridSkeleton
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.ArenaTab
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellStatus
import br.com.sportconnect.feature.arenaadmin.common.AgendaDayGrid
import br.com.sportconnect.feature.arenaadmin.common.AgendaDeskHeader
import br.com.sportconnect.feature.arenaadmin.common.AgendaMode
import br.com.sportconnect.feature.arenaadmin.common.AgendaStatusChip
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
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.common.NextPayoutCard
import br.com.sportconnect.feature.arenaadmin.common.detailLine
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_cd_arena_switcher
import br.com.sportconnect.resources.aadm_cd_next_day
import br.com.sportconnect.resources.aadm_cd_prev_day
import br.com.sportconnect.resources.aadm_hold_deadline
import br.com.sportconnect.resources.aadm_holds_empty
import br.com.sportconnect.resources.aadm_now_free
import br.com.sportconnect.resources.aadm_occupancy
import br.com.sportconnect.resources.aadm_payout_none_next
import br.com.sportconnect.resources.aadm_revenue
import br.com.sportconnect.resources.aadm_select_booking_hint
import br.com.sportconnect.resources.arena_alert_no_schedule
import br.com.sportconnect.resources.arena_alert_publish
import br.com.sportconnect.resources.arena_attention_title
import br.com.sportconnect.resources.arena_dashboard_title
import br.com.sportconnect.resources.arena_game_detail_paid
import br.com.sportconnect.resources.arena_holds_title
import br.com.sportconnect.resources.arena_now_title
import br.com.sportconnect.resources.arena_schedule_block_cta
import br.com.sportconnect.resources.arena_schedule_see_all
import br.com.sportconnect.resources.arena_summary_blocked
import br.com.sportconnect.resources.arena_summary_confirmed
import br.com.sportconnect.resources.arena_summary_holds
import br.com.sportconnect.resources.arena_summary_title
import br.com.sportconnect.resources.arena_tab_schedule
import br.com.sportconnect.resources.arena_today_summary
import br.com.sportconnect.resources.cd_notifications
import br.com.sportconnect.resources.common_date_time
import br.com.sportconnect.resources.common_updated_now
import br.com.sportconnect.resources.empty_arena_schedule_body
import br.com.sportconnect.resources.empty_arena_schedule_title
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** `arena-admin/dashboard` — Painel da arena (10 §2.17, redline §4.5). */
@Composable
fun ArenaDashboardScreen(
    nav: ArenaAdminNavigator,
    viewModel: ArenaDashboardViewModel = koinViewModel(),
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
    ArenaMessageHandler(
        message = state.message,
        snackbar = snackbar,
        onConsumed = viewModel::consumeMessage,
        onUndoBlock = viewModel::undoBlock,
        onOpenBooking = nav::openBooking,
    )
    BlockSheet(
        sheet = state.block,
        onDismiss = viewModel::dismissBlock,
        onCourt = viewModel::selectBlockCourt,
        onSlot = viewModel::selectBlockSlot,
        onReason = viewModel::selectBlockReason,
        onConfirm = viewModel::confirmBlock,
    )
    ArenaAdminScaffold(
        selected = ArenaTab.Dashboard,
        nav = nav,
        snackbarHostState = snackbar,
        topBar = { w -> if (w != ArenaWidth.Expanded) DashboardTopBar(state.arenaName) },
        bottomBar = { w -> if (w == ArenaWidth.Compact && state.load == LoadState.Ready) DashboardActions(viewModel, nav, Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) },
    ) { padding, width ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.load) {
                LoadState.Loading -> DashboardSkeleton(width)
                LoadState.Error -> ArenaErrorState(viewModel::retry)
                LoadState.NoArena -> ArenaNoArenaState(nav::openOnboarding)
                LoadState.Ready -> when (width) {
                    ArenaWidth.Compact -> DashboardCompact(state, viewModel, nav)
                    ArenaWidth.Medium -> DashboardMedium(state, viewModel, nav)
                    ArenaWidth.Expanded -> DashboardExpanded(state, viewModel, nav)
                }
            }
        }
    }
}

@Composable
private fun DashboardTopBar(arenaName: String) {
    val c = SportConnectTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).background(c.surface).padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            arenaName,
            style = MaterialTheme.typography.titleLarge,
            color = c.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false).semantics { heading() },
        )
        // Troca de arena e [F3]: icone presente, sem acao no MVP.
        Icon(Icons.Rounded.ArrowDropDown, stringResource(Res.string.aadm_cd_arena_switcher), tint = c.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = {}) { Icon(Icons.Outlined.Notifications, stringResource(Res.string.cd_notifications), tint = c.onSurfaceVariant) }
    }
}

@Composable
private fun DashboardActions(viewModel: ArenaDashboardViewModel, nav: ArenaAdminNavigator, modifier: Modifier = Modifier, withIcon: Boolean = false) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SCButton(
            stringResource(Res.string.arena_schedule_block_cta), { viewModel.openBlock() },
            leadingIcon = if (withIcon) Icons.Rounded.Block else null, modifier = Modifier.weight(1f).testTag("arena_dash_block"),
        )
        SCButton(
            stringResource(Res.string.arena_schedule_see_all), { nav.openTab(ArenaTab.Schedule) },
            variant = SCButtonVariant.Secondary, modifier = Modifier.weight(1f).testTag("arena_dash_see_schedule"),
        )
    }
}

@Composable
private fun DashboardSkeleton(width: ArenaWidth) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SCSkeleton(SCSkeletonShape.Line(0.4f, 16.dp))
        SCSkeleton(SCSkeletonShape.Line(0.8f, 20.dp))
        if (width == ArenaWidth.Expanded) TimeSlotGridSkeleton(columns = 4, rows = 5)
        else repeat(3) { SCSkeleton(SCSkeletonShape.Box(64.dp, widthFraction = 1f)) }
        CourtCardSkeleton()
    }
}

// ------------------------------------------------------------------ compact

@Composable
private fun DashboardCompact(state: ArenaDashboardUiState, viewModel: ArenaDashboardViewModel, nav: ArenaAdminNavigator) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp),
    ) {
        DashboardHeadline(state)
        DashboardSections(state, nav, Modifier.padding(top = 24.dp))
    }
}

@Composable
private fun DashboardHeadline(state: ArenaDashboardUiState) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(Modifier.testTag("arena_dash_summary")) {
        Text(
            stringResource(Res.string.common_date_time, stringResource(Res.string.arena_dashboard_title), state.dateLabel),
            style = t.titleSmall, color = c.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            stringResource(Res.string.arena_today_summary, state.summary.confirmed, state.summary.holds),
            style = t.bodyLarge, color = c.onSurface, modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Secoes Agora · Pre-reservas · Atencao · Proximo repasse (compact e coluna do medium). */
@Composable
private fun DashboardSections(state: ArenaDashboardUiState, nav: ArenaAdminNavigator, modifier: Modifier = Modifier, includePayout: Boolean = true) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        NowSection(state, nav)
        HoldsSection(state, nav)
        AlertsSection(state, nav)
        if (includePayout && state.showFinancials) {
            NextPayoutCard(
                state.nextPayout?.amount, state.nextPayout?.dateLabel, state.nextPayout?.projected == true,
                emptyLabel = stringResource(Res.string.aadm_payout_none_next),
            )
        }
    }
}

@Composable
private fun NowSection(state: ArenaDashboardUiState, nav: ArenaAdminNavigator) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column {
        ArenaSectionTitle(stringResource(Res.string.arena_now_title), Modifier.padding(bottom = 8.dp))
        if (state.isEmpty) {
            SCEmptyState(
                title = stringResource(Res.string.empty_arena_schedule_title),
                description = stringResource(Res.string.empty_arena_schedule_body),
            )
        }
        state.nowRows.forEachIndexed { i, row ->
            val cell = row.cell
            Row(
                Modifier.fillMaxWidth().heightIn(min = 72.dp)
                    .testTag("arena_dash_now_${row.courtId}")
                    .then(if (cell?.slotId != null) Modifier.clickable(role = Role.Button) { nav.openBooking(cell.slotId) } else Modifier)
                    .semantics(mergeDescendants = true) {}
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(row.courtName, style = t.titleSmall, color = c.onSurface, modifier = Modifier.width(80.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (cell == null) {
                        AgendaStatusChip(br.com.sportconnect.feature.arenaadmin.common.AgendaCellUi(row.courtId, 0, AgendaCellStatus.Free))
                        Text(stringResource(Res.string.aadm_now_free), style = t.bodyMedium, color = c.onSurfaceVariant)
                    } else {
                        AgendaStatusChip(cell)
                        val parts = listOfNotNull(
                            if (cell.status == AgendaCellStatus.InProgress) cell.rangeLabel else cell.startLabel,
                            cell.sport?.label(),
                            cell.detail,
                            if (cell.jogoId != null && cell.total > 0) stringResource(Res.string.arena_game_detail_paid, cell.paid, cell.total) else null,
                        )
                        Text(parts.joinToString(" · "), style = t.bodyMedium, color = c.onSurfaceVariant)
                    }
                }
            }
            if (i < state.nowRows.lastIndex) HorizontalDivider(Modifier.padding(start = 92.dp), color = c.outlineVariant)
        }
    }
}

@Composable
private fun HoldsSection(state: ArenaDashboardUiState, nav: ArenaAdminNavigator) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArenaSectionTitle(stringResource(Res.string.arena_holds_title))
        if (state.holds.isEmpty()) {
            Text(stringResource(Res.string.aadm_holds_empty), style = t.bodyMedium, color = c.onSurfaceVariant)
        }
        state.holds.forEach { h ->
            Column(
                Modifier.fillMaxWidth().clip(SCShape.small).background(c.surfaceContainerLowest)
                    .clickable(role = Role.Button) { nav.openBooking(h.slotId) }
                    .testTag("arena_dash_hold_${h.jogoId}")
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${h.courtName} · ${h.sport.label()}", style = t.titleSmall, color = c.onSurface)
                Text(
                    listOfNotNull(stringResource(Res.string.common_date_time, h.dayLabel, h.rangeLabel), h.organizer).joinToString(" · "),
                    style = t.bodyMedium, color = c.onSurfaceVariant,
                )
                SCSplitProgress(totalShares = h.total, paid = h.paid, variant = SCSplitProgressVariant.Compact)
                h.deadlineLabel?.let { Text(stringResource(Res.string.aadm_hold_deadline, it), style = t.labelMedium, color = SportConnectTheme.extendedColors.warning) }
            }
        }
    }
}

@Composable
private fun AlertsSection(state: ArenaDashboardUiState, nav: ArenaAdminNavigator) {
    if (state.alerts.isEmpty()) return
    val ext = SportConnectTheme.extendedColors
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArenaSectionTitle(stringResource(Res.string.arena_attention_title, state.alerts.size))
        state.alerts.forEach { a ->
            val container = if (a.blocking) c.errorContainer else ext.warningContainer
            val content = if (a.blocking) c.onErrorContainer else ext.onWarningContainer
            Row(
                Modifier.fillMaxWidth().clip(SCShape.small).background(container).padding(16.dp).testTag("arena_dash_alert_${a.id}"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Rounded.Warning, null, tint = content, modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.arena_alert_no_schedule, a.courtName, a.fromDate), style = t.bodyMedium, color = content)
                    TextButton(onClick = { nav.openCourt(a.courtId) }, contentPadding = PaddingValues(0.dp)) {
                        Text(stringResource(Res.string.arena_alert_publish), style = t.labelLarge, color = content, textDecoration = TextDecoration.Underline)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ medium

@Composable
private fun DashboardMedium(state: ArenaDashboardUiState, viewModel: ArenaDashboardViewModel, nav: ArenaAdminNavigator) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardHeadline(state)
        DashboardActions(viewModel, nav, Modifier.widthIn(max = 480.dp), withIcon = true)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(5f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                NowSection(state, nav)
                HoldsSection(state, nav)
            }
            Column(Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SummaryBlock(state)
                if (state.showFinancials) NextPayoutCard(state.nextPayout?.amount, state.nextPayout?.dateLabel, state.nextPayout?.projected == true, emptyLabel = stringResource(Res.string.aadm_payout_none_next))
                AlertsSection(state, nav)
            }
        }
    }
}

// ------------------------------------------------------------------ expanded

@Composable
private fun DashboardExpanded(state: ArenaDashboardUiState, viewModel: ArenaDashboardViewModel, nav: ArenaAdminNavigator) {
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
            AgendaDeskHeader(
                title = stringResource(Res.string.arena_tab_schedule),
                mode = AgendaMode.Day,
                onMode = { if (it == AgendaMode.Week) nav.openTab(ArenaTab.Schedule) },
                dateLabel = state.dateLabel,
                prevCd = stringResource(Res.string.aadm_cd_prev_day),
                nextCd = stringResource(Res.string.aadm_cd_next_day),
                onPrev = viewModel::previousDay,
                onNext = viewModel::nextDay,
                onToday = viewModel::today,
                onBlock = { viewModel.openBlock() },
            )
            Spacer(Modifier.heightIn(min = 16.dp))
            AgendaDayGrid(
                agenda = state.agenda,
                nowMinutes = state.nowMinutes.takeIf { state.isToday },
                selectedSlotId = state.selectedSlotId,
                onCellClick = viewModel::onCellClick,
                showOrganizer = false,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
        ArenaSidePanel(Modifier.testTag("arena_side_panel")) {
            SummaryBlock(state)
            ArenaPanelCard {
                val sel = state.selected
                if (sel == null) {
                    Text(stringResource(Res.string.aadm_select_booking_hint), style = MaterialTheme.typography.bodyMedium, color = SportConnectTheme.colors.onSurfaceVariant)
                } else {
                    BookingDetailContent(
                        sel,
                        panel = true,
                        showFinancials = state.showFinancials,
                        onCancel = { nav.openBooking(sel.slotId) },
                        onRelease = { nav.openBooking(sel.slotId) },
                    )
                }
            }
            if (state.showFinancials) {
                NextPayoutCard(state.nextPayout?.amount, state.nextPayout?.dateLabel, state.nextPayout?.projected == true, emptyLabel = stringResource(Res.string.aadm_payout_none_next))
            }
        }
    }
}

@Composable
private fun SummaryBlock(state: ArenaDashboardUiState) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val s = state.summary
    Column(Modifier.testTag("arena_dash_summary_block"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ArenaSectionTitle(stringResource(Res.string.arena_summary_title))
        Text(stringResource(Res.string.arena_summary_confirmed, s.confirmed), style = t.titleMedium, color = c.onSurface)
        Text(stringResource(Res.string.arena_summary_holds, s.holds), style = t.titleMedium, color = c.onSurface)
        Text(stringResource(Res.string.arena_summary_blocked, s.blocked), style = t.titleMedium, color = c.onSurface)
        Text(stringResource(Res.string.aadm_occupancy, s.occupancyPct), style = t.bodyMedium, color = c.onSurfaceVariant)
        if (state.showFinancials) {
            Text(stringResource(Res.string.aadm_revenue, br.com.sportconnect.core.designsystem.component.formatBrl(s.revenue.cents)), style = t.bodyMedium, color = c.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).clip(SCShape.full).background(SportConnectTheme.extendedColors.success))
            Text(stringResource(Res.string.common_updated_now), style = t.labelSmall, color = c.onSurfaceVariant)
        }
    }
}
