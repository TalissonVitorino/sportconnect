package br.com.sportconnect.feature.arenaadmin.payouts

import br.com.sportconnect.core.designsystem.theme.SCSelection
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.GameCardSkeleton
import br.com.sportconnect.core.designsystem.component.PriceLine
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdown
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdownVariant
import br.com.sportconnect.core.designsystem.component.SCPriceEmphasis
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.formatBrl
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.component.spokenBrl
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.StatusRepasse
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.ArenaTab
import br.com.sportconnect.feature.arenaadmin.common.ArenaAdminScaffold
import br.com.sportconnect.feature.arenaadmin.common.ArenaErrorState
import br.com.sportconnect.feature.arenaadmin.common.ArenaNoArenaState
import br.com.sportconnect.feature.arenaadmin.common.ArenaPanelCard
import br.com.sportconnect.feature.arenaadmin.common.ArenaSectionTitle
import br.com.sportconnect.feature.arenaadmin.common.ArenaSidePanel
import br.com.sportconnect.feature.arenaadmin.common.ArenaWidth
import br.com.sportconnect.feature.arenaadmin.common.ChoiceChips
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_payout_account_value
import br.com.sportconnect.resources.aadm_payout_col_date
import br.com.sportconnect.resources.aadm_payout_col_fee
import br.com.sportconnect.resources.aadm_payout_col_game
import br.com.sportconnect.resources.aadm_payout_col_gross
import br.com.sportconnect.resources.aadm_payout_col_net
import br.com.sportconnect.resources.aadm_payout_col_status
import br.com.sportconnect.resources.aadm_payout_detail_title
import br.com.sportconnect.resources.aadm_payout_held_banner
import br.com.sportconnect.resources.aadm_payout_none_next
import br.com.sportconnect.resources.aadm_payout_on_date
import br.com.sportconnect.resources.aadm_payout_open_booking
import br.com.sportconnect.resources.aadm_payout_paid_total
import br.com.sportconnect.resources.aadm_payout_period_30
import br.com.sportconnect.resources.aadm_payout_projected
import br.com.sportconnect.resources.aadm_payout_period_7
import br.com.sportconnect.resources.aadm_payout_period_90
import br.com.sportconnect.resources.aadm_payout_period_all
import br.com.sportconnect.resources.aadm_payout_scheduled_total
import br.com.sportconnect.resources.aadm_select_booking_hint
import br.com.sportconnect.resources.arena_tab_payouts
import br.com.sportconnect.resources.empty_payouts_body
import br.com.sportconnect.resources.empty_payouts_title
import br.com.sportconnect.resources.payout_bank_account
import br.com.sportconnect.resources.payout_expected_date
import br.com.sportconnect.resources.payout_export
import br.com.sportconnect.resources.payout_export_cd
import br.com.sportconnect.resources.payout_exported
import br.com.sportconnect.resources.payout_fix_account
import br.com.sportconnect.resources.payout_gross
import br.com.sportconnect.resources.payout_net
import br.com.sportconnect.resources.payout_next_amount
import br.com.sportconnect.resources.payout_next_title
import br.com.sportconnect.resources.payout_paid_date
import br.com.sportconnect.resources.payout_period_filter
import br.com.sportconnect.resources.payout_platform_fee
import br.com.sportconnect.resources.payout_rule_explainer
import br.com.sportconnect.resources.payout_status_held
import br.com.sportconnect.resources.payout_status_paid
import br.com.sportconnect.resources.payout_status_scheduled
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

val PayoutPeriod.labelRes: StringResource
    get() = when (this) {
        PayoutPeriod.Last7 -> Res.string.aadm_payout_period_7
        PayoutPeriod.Last30 -> Res.string.aadm_payout_period_30
        PayoutPeriod.Last90 -> Res.string.aadm_payout_period_90
        PayoutPeriod.All -> Res.string.aadm_payout_period_all
    }

private data class PayoutStatusVisual(val label: StringResource, val icon: ImageVector, val container: Color, val content: Color)

@Composable
private fun StatusRepasse.visual(): PayoutStatusVisual {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    return when (this) {
        StatusRepasse.AGENDADO -> PayoutStatusVisual(Res.string.payout_status_scheduled, Icons.Rounded.Schedule, c.tertiaryContainer, c.onTertiaryContainer)
        StatusRepasse.LIQUIDADO -> PayoutStatusVisual(Res.string.payout_status_paid, Icons.Rounded.CheckCircle, ext.successContainer, ext.onSuccessContainer)
        StatusRepasse.RETIDO -> PayoutStatusVisual(Res.string.payout_status_held, Icons.Rounded.PauseCircle, ext.warningContainer, ext.onWarningContainer)
    }
}

@Composable
private fun PayoutStatusChip(status: StatusRepasse) {
    val v = status.visual()
    Row(
        Modifier.clip(SCShape.badge).background(v.container).padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(v.icon, null, tint = v.content, modifier = Modifier.size(16.dp))
        Text(stringResource(v.label), style = MaterialTheme.typography.labelMedium, color = v.content)
    }
}

/** `arena-admin/payouts` — Repasses (10 §2.21). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaPayoutsScreen(
    nav: ArenaAdminNavigator,
    viewModel: ArenaPayoutsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    LaunchedEffect(state.exportCsv) {
        val csv = state.exportCsv ?: return@LaunchedEffect
        // No common nao ha "salvar arquivo": o extrato vai para a area de transferencia.
        runCatching { clipboard.setText(AnnotatedString(csv)) }
        viewModel.exportHandled()
        snackbar.showSC(getString(Res.string.payout_exported), SCSnackbarKind.Success)
    }
    ArenaAdminScaffold(
        selected = ArenaTab.Payouts,
        nav = nav,
        snackbarHostState = snackbar,
        topBar = {
            SCTopBar(
                title = stringResource(Res.string.arena_tab_payouts),
                navigation = null,
                actions = {
                    IconButton(onClick = viewModel::export, modifier = Modifier.testTag("payouts_export"), enabled = state.load == LoadState.Ready) {
                        Icon(Icons.Rounded.Download, stringResource(Res.string.payout_export_cd))
                    }
                },
            )
        },
    ) { padding, width ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.load) {
                LoadState.Loading -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { repeat(3) { GameCardSkeleton() } }
                LoadState.Error -> ArenaErrorState(viewModel::retry)
                LoadState.NoArena -> ArenaNoArenaState(nav::openOnboarding)
                LoadState.Ready -> if (width == ArenaWidth.Compact) PayoutsCompact(state, viewModel) else PayoutsWide(state, viewModel, nav, width)
            }
        }
    }
    if (state.selected != null) {
        // Compact/medium: detalhe em sheet. Expanded usa o painel lateral.
        PayoutDetailSheet(state, viewModel, nav)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayoutDetailSheet(state: ArenaPayoutsUiState, viewModel: ArenaPayoutsViewModel, nav: ArenaAdminNavigator) {
    val item = state.selected ?: return
    androidx.compose.foundation.layout.BoxWithConstraints {
        if (ArenaWidth.of(maxWidth) == ArenaWidth.Expanded) return@BoxWithConstraints
        ModalBottomSheet(
            onDismissRequest = { viewModel.select(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = SCShape.sheet,
            containerColor = SportConnectTheme.colors.surfaceContainerLow,
        ) {
            PayoutDetail(item, nav, Modifier.padding(20.dp))
        }
    }
}

@Composable
private fun NextPayoutHero(state: ArenaPayoutsUiState, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val next = state.next
    val dateLabel = next?.let { br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.dayLabel(it.date) }
    val cd = if (next != null) stringResource(Res.string.payout_next_amount, spokenBrl(state.nextAmount.cents), dateLabel!!) else stringResource(Res.string.aadm_payout_none_next)
    Column(
        modifier.fillMaxWidth().clip(SCShape.card).background(c.primaryContainer).padding(20.dp)
            .testTag("payouts_next").semantics(mergeDescendants = true) { contentDescription = cd },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(Res.string.payout_next_title), style = t.titleSmall, color = c.onPrimaryContainer)
        if (next != null) {
            Text(state.nextAmount.formatBrl(), style = t.displaySmall, color = c.onPrimaryContainer)
            Text(stringResource(Res.string.aadm_payout_on_date, dateLabel!!), style = t.bodyMedium, color = c.onPrimaryContainer)
            if (next.projected) Text(stringResource(Res.string.aadm_payout_projected), style = t.bodySmall, color = c.onPrimaryContainer)
        } else {
            Text(stringResource(Res.string.aadm_payout_none_next), style = t.bodyLarge, color = c.onPrimaryContainer)
        }
    }
}

@Composable
private fun Totals(state: ArenaPayoutsUiState, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(Res.string.aadm_payout_scheduled_total to state.totalScheduled, Res.string.aadm_payout_paid_total to state.totalPaid).forEach { (label, value) ->
            Column(Modifier.weight(1f).clip(SCShape.small).background(c.surfaceContainerLow).padding(16.dp)) {
                Text(stringResource(label), style = t.labelLarge, color = c.onSurfaceVariant)
                Text(value.formatBrl(), style = SportConnectTheme.typographyExt.numericPriceSmall, color = c.onSurface)
            }
        }
    }
}

@Composable
private fun HeldBanner(count: Int) {
    if (count == 0) return
    val c = SportConnectTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(SCShape.small).background(c.errorContainer).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.Warning, null, tint = c.onErrorContainer)
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.aadm_payout_held_banner, count), style = MaterialTheme.typography.bodyMedium, color = c.onErrorContainer)
            TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                Text(stringResource(Res.string.payout_fix_account), color = c.onErrorContainer, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PeriodFilter(state: ArenaPayoutsUiState, viewModel: ArenaPayoutsViewModel) {
    Column(Modifier.testTag("payouts_period"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArenaSectionTitle(stringResource(Res.string.payout_period_filter))
        ChoiceChips(PayoutPeriod.entries, state.period, { stringResource(it.labelRes) }, viewModel::setPeriod, tag = { "payouts_period_${it.name}" })
    }
}

@Composable
private fun BankAccount(doc: String) {
    val c = SportConnectTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("payouts_bank_account"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.AccountBalance, null, tint = c.onSurfaceVariant)
        Column {
            Text(stringResource(Res.string.payout_bank_account), style = MaterialTheme.typography.titleSmall, color = c.onSurface)
            Text(stringResource(Res.string.aadm_payout_account_value, doc), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
        }
    }
}

@Composable
private fun PayoutRow(item: PayoutItemUi, onClick: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp).clickable(role = Role.Button, onClick = onClick)
            .testTag("payouts_item_${item.id}").padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.description, style = t.titleSmall, color = c.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.dateLabel, style = t.bodySmall, color = c.onSurfaceVariant)
                PayoutStatusChip(item.status)
            }
        }
        Text(item.net.formatBrl(), style = t.titleMedium, color = c.onSurface)
    }
}

@Composable
private fun PayoutsCompact(state: ArenaPayoutsUiState, viewModel: ArenaPayoutsViewModel) {
    LazyColumn(Modifier.fillMaxSize().testTag("payouts_list"), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NextPayoutHero(state)
                Text(stringResource(Res.string.payout_rule_explainer), style = MaterialTheme.typography.bodyMedium, color = SportConnectTheme.colors.onSurfaceVariant)
                Totals(state)
                HeldBanner(state.heldCount)
                PeriodFilter(state, viewModel)
            }
        }
        if (state.items.isEmpty()) {
            item { SCEmptyState(stringResource(Res.string.empty_payouts_title), stringResource(Res.string.empty_payouts_body), illustration = SCIllustration.NoResults) }
        }
        items(state.items, key = { it.id }) { item ->
            PayoutRow(item) { viewModel.select(item.id) }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = SportConnectTheme.colors.outlineVariant)
        }
        item { Box(Modifier.padding(16.dp)) { BankAccount(state.bankDocument) } }
    }
}

@Composable
private fun PayoutsWide(state: ArenaPayoutsUiState, viewModel: ArenaPayoutsViewModel, nav: ArenaAdminNavigator, width: ArenaWidth) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Row(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).testTag("payouts_list"), contentPadding = PaddingValues(24.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                    NextPayoutHero(state, Modifier.weight(1f))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Totals(state)
                        Text(stringResource(Res.string.payout_rule_explainer), style = t.bodyMedium, color = c.onSurfaceVariant)
                    }
                }
            }
            item {
                Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeldBanner(state.heldCount)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Box(Modifier.weight(1f)) { PeriodFilter(state, viewModel) }
                        SCButton(stringResource(Res.string.payout_export), viewModel::export, variant = SCButtonVariant.Secondary, leadingIcon = Icons.Rounded.Download)
                    }
                }
            }
            item { TableHeader() }
            if (state.items.isEmpty()) {
                item { SCEmptyState(stringResource(Res.string.empty_payouts_title), stringResource(Res.string.empty_payouts_body), illustration = SCIllustration.NoResults) }
            }
            items(state.items, key = { it.id }) { item ->
                TableRow(item, selected = item.id == state.selected?.id) { viewModel.select(item.id) }
                HorizontalDivider(color = c.outlineVariant)
            }
            item { Box(Modifier.padding(top = 16.dp)) { BankAccount(state.bankDocument) } }
        }
        if (width == ArenaWidth.Expanded) {
            ArenaSidePanel {
                ArenaPanelCard {
                    val sel = state.selected
                    if (sel == null) Text(stringResource(Res.string.aadm_select_booking_hint), style = t.bodyMedium, color = c.onSurfaceVariant)
                    else PayoutDetail(sel, nav)
                }
            }
        }
    }
}

private val colWeights = listOf(1f, 2.4f, 1.1f, 1.1f, 1.2f, 1.4f)

@Composable
private fun TableHeader() {
    val c = SportConnectTheme.colors
    val labels = listOf(
        Res.string.aadm_payout_col_date, Res.string.aadm_payout_col_game, Res.string.aadm_payout_col_gross,
        Res.string.aadm_payout_col_fee, Res.string.aadm_payout_col_net, Res.string.aadm_payout_col_status,
    )
    Row(Modifier.fillMaxWidth().heightIn(min = 40.dp).background(c.surfaceContainerLow).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { i, l ->
            Text(
                stringResource(l), style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant,
                textAlign = if (i in 2..4) TextAlign.End else TextAlign.Start,
                modifier = Modifier.weight(colWeights[i]).padding(horizontal = 4.dp).semantics { heading() },
            )
        }
    }
}

@Composable
private fun TableRow(item: PayoutItemUi, selected: Boolean, onClick: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .background(if (selected) c.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("payouts_item_${item.id}")
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(item.dateLabel, style = t.bodyMedium, color = c.onSurface, modifier = Modifier.weight(colWeights[0]).padding(horizontal = 4.dp))
        Text(item.description, style = t.bodyMedium, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(colWeights[1]).padding(horizontal = 4.dp))
        Text(item.gross.formatBrl(), style = t.bodyMedium, color = c.onSurface, textAlign = TextAlign.End, modifier = Modifier.weight(colWeights[2]).padding(horizontal = 4.dp))
        Text("− ${item.fee.formatBrl()}", style = t.bodyMedium, color = c.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(colWeights[3]).padding(horizontal = 4.dp))
        Text(item.net.formatBrl(), style = t.titleSmall, color = c.onSurface, textAlign = TextAlign.End, modifier = Modifier.weight(colWeights[4]).padding(horizontal = 4.dp))
        Box(Modifier.weight(colWeights[5]).padding(start = 12.dp)) { PayoutStatusChip(item.status) }
    }
}

@Composable
private fun PayoutDetail(item: PayoutItemUi, nav: ArenaAdminNavigator, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.aadm_payout_detail_title), style = t.titleSmall, color = c.onSurfaceVariant, modifier = Modifier.semantics { heading() })
        Text(item.description, style = t.titleMedium, color = c.onSurface)
        PayoutStatusChip(item.status)
        SCPriceBreakdown(
            lines = listOf(
                PriceLine(stringResource(Res.string.payout_gross), item.gross),
                PriceLine(stringResource(Res.string.payout_platform_fee), valueText = "− ${item.fee.formatBrl()}", emphasis = SCPriceEmphasis.Muted),
            ),
            total = PriceLine(stringResource(Res.string.payout_net), item.net, emphasis = SCPriceEmphasis.Strong),
            variant = SCPriceBreakdownVariant.Arena,
        )
        Text(
            if (item.paidLabel != null) stringResource(Res.string.payout_paid_date, item.paidLabel) else stringResource(Res.string.payout_expected_date, item.expectedLabel),
            style = t.bodyMedium, color = c.onSurfaceVariant,
        )
        SCButton(stringResource(Res.string.aadm_payout_open_booking), { nav.openBooking(item.jogoId) }, variant = SCButtonVariant.Text)
    }
}
