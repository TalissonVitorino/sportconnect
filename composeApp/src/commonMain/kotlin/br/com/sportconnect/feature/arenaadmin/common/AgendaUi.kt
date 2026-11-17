package br.com.sportconnect.feature.arenaadmin.common

import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.scHatch
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SCReservationTone
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_cd_cell
import br.com.sportconnect.resources.aadm_status_checkout
import br.com.sportconnect.resources.arena_game_detail_paid
import br.com.sportconnect.resources.arena_schedule_status_blocked
import br.com.sportconnect.resources.arena_schedule_status_closed
import br.com.sportconnect.resources.arena_schedule_status_confirmed
import br.com.sportconnect.resources.arena_schedule_status_free
import br.com.sportconnect.resources.arena_schedule_status_hold
import br.com.sportconnect.resources.arena_schedule_status_in_progress
import org.jetbrains.compose.resources.stringResource

/** Cores + icone de um estado da agenda (sempre icone + texto; cor nunca e o unico sinal). */
@Immutable
data class AgendaVisual(
    val container: Color,
    val content: Color,
    val icon: ImageVector,
    val dashedBorder: Color? = null,
    val solidBorder: Color? = null,
    val hatch: Color? = null,
)

@Composable
fun AgendaCellStatus.visual(): AgendaVisual {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    return when (this) {
        AgendaCellStatus.Free -> AgendaVisual(c.surfaceContainerLowest, c.onSurfaceVariant, Icons.Rounded.CheckBoxOutlineBlank, dashedBorder = c.outline)
        AgendaCellStatus.Confirmed, AgendaCellStatus.External -> ext.reservation(SCReservationTone.Confirmed).let {
            AgendaVisual(it.container, it.content, Icons.Rounded.CheckCircle)
        }
        AgendaCellStatus.InProgress -> AgendaVisual(c.tertiaryContainer, c.onTertiaryContainer, Icons.Rounded.PlayCircle)
        AgendaCellStatus.Hold -> AgendaVisual(ext.warningContainer, ext.onWarningContainer, Icons.Rounded.HourglassTop)
        AgendaCellStatus.Checkout -> AgendaVisual(ext.warningContainer, ext.onWarningContainer, Icons.Rounded.Timer)
        AgendaCellStatus.Blocked -> AgendaVisual(c.surfaceContainerHighest, c.onSurfaceVariant, Icons.Rounded.Block)
        AgendaCellStatus.Closed -> AgendaVisual(c.surfaceContainer, c.onSurfaceVariant, Icons.Rounded.RemoveCircleOutline, hatch = c.outlineVariant)
    }
}

/** Rotulo de status (`arena_schedule_status_*`). */
@Composable
fun AgendaCellUi.statusLabel(): String = when (status) {
    AgendaCellStatus.Free -> stringResource(Res.string.arena_schedule_status_free)
    AgendaCellStatus.Confirmed, AgendaCellStatus.External -> stringResource(Res.string.arena_schedule_status_confirmed)
    AgendaCellStatus.InProgress -> stringResource(Res.string.arena_schedule_status_in_progress)
    AgendaCellStatus.Hold -> stringResource(Res.string.arena_schedule_status_hold, holdUntil ?: startLabel)
    AgendaCellStatus.Checkout -> stringResource(Res.string.aadm_status_checkout)
    AgendaCellStatus.Blocked -> stringResource(Res.string.arena_schedule_status_blocked)
    AgendaCellStatus.Closed -> stringResource(Res.string.arena_schedule_status_closed)
}

/** Linha 2 da celula: "Futsal · Bruno L. · 12/12", motivo do bloqueio ou "Mensalista". */
@Composable
fun AgendaCellUi.detailLine(withOrganizer: Boolean = true, withSport: Boolean = true): String? {
    val parts = buildList {
        if (withSport && sport != null && (jogoId != null || status == AgendaCellStatus.External || status == AgendaCellStatus.InProgress)) add(sport.label())
        if (jogoId != null) {
            if (withOrganizer && detail != null) add(detail)
            if (total > 0) add("$paid/$total")
        } else if (detail != null) add(detail)
    }
    return parts.joinToString(" · ").ifEmpty { null }
}

/** Leitura acessivel: "Quadra Futsal, 19h–20h, Confirmado · Futsal · 12 de 12 cotas pagas". */
@Composable
fun AgendaCellUi.accessibleLabel(courtName: String): String {
    val paidText = if (jogoId != null && total > 0) stringResource(Res.string.arena_game_detail_paid, paid, total) else null
    val extra = listOfNotNull(statusLabel(), sport?.takeIf { jogoId != null || status == AgendaCellStatus.External }?.label(), detail, paidText).joinToString(" · ")
    return stringResource(Res.string.aadm_cd_cell, courtName, rangeLabel, extra)
}

private fun Modifier.dashedBorder(color: Color, radius: Float) = drawBehind {
    val stroke = 1.dp.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(12.dp.toPx() + radius * 0f),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
    )
}

/**
 * Bloco da agenda (grade do expanded e semana): 64dp, inset 4, radius.md, pad 8, borda esquerda 4dp
 * `sport.accent` em reservas, linha 1 icone 16 + labelMedium (status), linha 2 bodySmall.
 */
@Composable
fun AgendaCellBlock(
    cell: AgendaCellUi,
    courtName: String,
    onClick: (AgendaCellUi) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showOrganizer: Boolean = true,
    showSport: Boolean = true,
    testTagPrefix: String = "arena_grid_cell",
) {
    val v = cell.status.visual()
    val c = SportConnectTheme.colors
    val accent = cell.sport?.let { SportConnectTheme.extendedColors.sport(it).accent }
    val radiusPx = 12f
    val clickable = cell.status != AgendaCellStatus.Closed
    val cd = cell.accessibleLabel(courtName)
    Box(
        modifier
            .padding(4.dp)
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .testTag("${testTagPrefix}_${cell.courtId}_${cell.hour.toString().padStart(2, '0')}00")
            .clip(SCShape.control)
            .background(v.container)
            .then(if (v.hatch != null) Modifier.scHatch(v.hatch) else Modifier)
            .then(if (v.dashedBorder != null) Modifier.dashedBorder(v.dashedBorder, radiusPx * 2) else Modifier)
            .then(if (selected) Modifier.border(2.dp, c.primary, SCShape.control) else Modifier)
            .then(if (clickable) Modifier.clickable(role = Role.Button) { onClick(cell) } else Modifier)
            .clearAndSetSemantics {
                contentDescription = cd
                if (clickable) role = Role.Button
            },
    ) {
        Row(Modifier.fillMaxWidth()) {
            if (accent != null && cell.isBooking) {
                Box(Modifier.width(4.dp).heightIn(min = 56.dp).fillMaxHeight().background(accent))
            }
            Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(v.icon, null, tint = v.content, modifier = Modifier.size(16.dp))
                    Text(cell.statusLabel(), style = MaterialTheme.typography.labelMedium, color = v.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                cell.detailLine(showOrganizer, showSport)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = v.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** Selo compacto (lista "Agora", agenda compact): fundo do estado + icone + texto. */
@Composable
fun AgendaStatusChip(cell: AgendaCellUi, modifier: Modifier = Modifier) {
    val v = cell.status.visual()
    Row(
        modifier
            .heightIn(min = 24.dp)
            .clip(SCShape.badge)
            .background(v.container)
            .then(if (v.hatch != null) Modifier.scHatch(v.hatch) else Modifier)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(v.icon, null, tint = v.content, modifier = Modifier.size(16.dp))
        Text(cell.statusLabel(), style = MaterialTheme.typography.labelMedium, color = v.content)
    }
}

/**
 * Linha de horario da agenda compact (64dp): hora `titleSmall` na coluna de 56dp, selo de status e
 * linha 2 com esporte · organizador · cotas pagas.
 */
@Composable
fun AgendaListRow(
    cell: AgendaCellUi,
    courtName: String,
    onClick: (AgendaCellUi) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "schedule_cell_${cell.courtId}_${cell.hour.toString().padStart(2, '0')}00",
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val cd = cell.accessibleLabel(courtName)
    val clickable = cell.status != AgendaCellStatus.Closed
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .testTag(testTag)
            .then(if (clickable) Modifier.clickable(role = Role.Button) { onClick(cell) } else Modifier)
            .semantics(mergeDescendants = true) { contentDescription = cd }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(cell.startLabel, style = t.titleSmall, color = c.onSurface, modifier = Modifier.width(44.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AgendaStatusChip(cell)
            val line2 = buildList {
                cell.detailLine(withOrganizer = true)?.let { add(it) }
            }.joinToString()
            if (line2.isNotEmpty()) Text(line2, style = t.bodyMedium, color = c.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        // C17: horario livre mostra que da para agir (bloquear/reservar); antes parecia so informativo.
        if (cell.status == AgendaCellStatus.Free) {
            Icon(Icons.Rounded.AddCircleOutline, null, tint = c.primary, modifier = Modifier.size(24.dp))
        } else if (clickable) {
            Icon(Icons.Rounded.ChevronRight, null, tint = c.onSurfaceVariant, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(0.dp))
    }
}
