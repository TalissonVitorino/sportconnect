package br.com.sportconnect.feature.arenaadmin.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.formatBrl
import br.com.sportconnect.core.designsystem.component.spokenBrl
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_cd_now_line
import br.com.sportconnect.resources.aadm_payout_on_date
import br.com.sportconnect.resources.aadm_payout_projected
import br.com.sportconnect.resources.arena_schedule_block_cta
import br.com.sportconnect.resources.arena_schedule_day
import br.com.sportconnect.resources.arena_schedule_today
import br.com.sportconnect.resources.arena_schedule_week
import br.com.sportconnect.resources.payout_next_amount
import br.com.sportconnect.resources.payout_next_title
import org.jetbrains.compose.resources.stringResource

enum class AgendaMode { Day, Week }

/**
 * Cabecalho da agenda no expanded (10 §4.5): titulo `headlineSmall` · segmented Dia|Semana ·
 * ‹ data › `titleMedium` · [Hoje] Text · [Bloquear horario] Primary Medium.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaDeskHeader(
    title: String,
    mode: AgendaMode,
    onMode: (AgendaMode) -> Unit,
    dateLabel: String,
    prevCd: String,
    nextCd: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onBlock: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val t = MaterialTheme.typography
    val c = SportConnectTheme.colors
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = t.headlineSmall, color = c.onSurface, modifier = Modifier.semantics { heading() })
        SingleChoiceSegmentedButtonRow {
            AgendaMode.entries.forEachIndexed { i, m ->
                SegmentedButton(
                    selected = mode == m,
                    onClick = { onMode(m) },
                    shape = SegmentedButtonDefaults.itemShape(i, AgendaMode.entries.size),
                    modifier = Modifier.testTag(if (m == AgendaMode.Day) "schedule_mode_day" else "schedule_mode_week"),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = c.secondaryContainer,
                        activeContentColor = c.onSecondaryContainer,
                    ),
                ) { Text(stringResource(if (m == AgendaMode.Day) Res.string.arena_schedule_day else Res.string.arena_schedule_week)) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, modifier = Modifier.testTag("schedule_prev_day")) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, prevCd) }
            Text(dateLabel, style = t.titleMedium, color = c.onSurface)
            IconButton(onClick = onNext, modifier = Modifier.testTag("schedule_next_day")) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, nextCd) }
        }
        TextButton(onClick = onToday, modifier = Modifier.testTag("schedule_today")) {
            Text(stringResource(Res.string.arena_schedule_today), style = t.labelLarge, color = c.primary)
        }
        trailing()
        Spacer(Modifier.weight(1f))
        if (onBlock != null) {
            SCButton(stringResource(Res.string.arena_schedule_block_cta), onBlock, leadingIcon = Icons.Rounded.Block, modifier = Modifier.testTag("arena_dash_block"))
        }
    }
}

private val HourColumn = 56.dp
private val RowHeight = 64.dp
private val HeaderHeight = 48.dp

/**
 * Agenda do dia em grade (expanded): linhas = horas (64dp), colunas = quadras (cabecalho sticky),
 * linha "agora" (traco 2dp `error` + pino 8dp) quando [nowMinutes] != null.
 */
@Composable
fun AgendaDayGrid(
    agenda: ArenaAgenda,
    nowMinutes: Int?,
    selectedSlotId: String?,
    onCellClick: (AgendaCellUi) -> Unit,
    modifier: Modifier = Modifier,
    showOrganizer: Boolean = true,
    showSport: Boolean = true,
    columnTitle: @Composable (CourtAgendaUi) -> String = { "${it.name} · ${it.mainSport.label()}" },
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val first = agenda.hours.firstOrNull() ?: 0
    LaunchedEffect(agenda.hours.firstOrNull(), nowMinutes != null) {
        val targetHour = ((nowMinutes ?: (18 * 60)) / 60 - 1 - first).coerceAtLeast(0)
        scroll.scrollTo(with(density) { (RowHeight * targetHour).roundToPx() })
    }
    Column(modifier.testTag("arena_day_grid")) {
        Row(Modifier.fillMaxWidth().height(HeaderHeight), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(HourColumn))
            agenda.courts.forEach { court ->
                Text(
                    columnTitle(court),
                    style = t.titleSmall,
                    color = c.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { heading() },
                )
            }
        }
        Box(Modifier.weight(1f, fill = false).verticalScroll(scroll)) {
            Column {
                agenda.hours.forEachIndexed { i, h ->
                    Row(Modifier.fillMaxWidth().height(RowHeight)) {
                        Text(
                            ArenaFormat.hour(h),
                            style = t.titleSmall,
                            color = c.onSurfaceVariant,
                            modifier = Modifier.width(HourColumn).padding(top = 12.dp, start = 4.dp).semantics { heading() },
                        )
                        agenda.courts.forEach { court ->
                            val cell = court.cells[i]
                            AgendaCellBlock(
                                cell = cell,
                                courtName = court.name,
                                onClick = onCellClick,
                                selected = cell.slotId != null && cell.slotId == selectedSlotId,
                                showOrganizer = showOrganizer,
                                showSport = showSport,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                }
            }
            if (nowMinutes != null && agenda.hours.isNotEmpty()) {
                val offsetMin = nowMinutes - first * 60
                if (offsetMin in 0..(agenda.hours.size * 60)) {
                    val y = RowHeight * (offsetMin / 60f)
                    val nowCd = stringResource(Res.string.aadm_cd_now_line, ArenaFormat.hour(nowMinutes / 60, nowMinutes % 60))
                    val color = c.error
                    Box(
                        Modifier.offset(y = y - 4.dp).padding(start = HourColumn - 8.dp).fillMaxWidth().height(8.dp)
                            .semantics { contentDescription = nowCd }
                            .testTag("arena_now_line")
                            .drawBehind {
                                val cy = size.height / 2
                                drawCircle(color, radius = 4.dp.toPx(), center = Offset(4.dp.toPx(), cy))
                                drawLine(color, Offset(4.dp.toPx(), cy), Offset(size.width, cy), strokeWidth = 2.dp.toPx())
                            },
                    )
                }
            }
        }
    }
}

/** Painel lateral 320dp (`surfaceContainerLow`, borda esquerda 1dp `outlineVariant`, pad 24). */
@Composable
fun ArenaSidePanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = SportConnectTheme.colors
    Column(
        modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(c.surfaceContainerLow)
            .drawBehind { drawLine(c.outlineVariant, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx()) }
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        content = content,
    )
}

/** Cartao "Proximo repasse" (`payout_next_*`): valor em numericPrice + data. */
@Composable
fun NextPayoutCard(
    amount: Money?,
    dateLabel: String?,
    projected: Boolean,
    modifier: Modifier = Modifier,
    emptyLabel: String? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val cd = if (amount != null && dateLabel != null) stringResource(Res.string.payout_next_amount, spokenBrl(amount.cents), dateLabel) else emptyLabel
    Column(
        modifier
            .fillMaxWidth()
            .clip(SCShape.small)
            .background(c.surfaceContainerHigh)
            .padding(16.dp)
            .testTag("arena_dash_payout")
            .semantics(mergeDescendants = true) { cd?.let { contentDescription = it } },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(Res.string.payout_next_title), style = t.titleSmall, color = c.onSurface)
        if (amount != null && dateLabel != null) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(amount.formatBrl(), style = SportConnectTheme.typographyExt.numericPrice, color = c.onSurface, modifier = Modifier.alignByBaseline())
                Text(stringResource(Res.string.aadm_payout_on_date, dateLabel), style = t.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.alignByBaseline())
            }
            if (projected) Text(stringResource(Res.string.aadm_payout_projected), style = t.bodySmall, color = c.onSurfaceVariant)
        } else if (emptyLabel != null) {
            Text(emptyLabel, style = t.bodyMedium, color = c.onSurfaceVariant)
        }
    }
}

/** Cartao generico do painel lateral (detalhe da reserva). */
@Composable
fun ArenaPanelCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = SportConnectTheme.colors
    Column(
        modifier.fillMaxWidth().clip(SCShape.small).background(c.surfaceContainerLowest)
            .border(1.dp, c.outlineVariant, SCShape.small).padding(16.dp),
        content = content,
    )
}
