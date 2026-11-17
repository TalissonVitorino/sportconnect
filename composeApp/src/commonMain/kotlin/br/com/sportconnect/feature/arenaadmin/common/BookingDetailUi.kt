package br.com.sportconnect.feature.arenaadmin.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.PriceLine
import br.com.sportconnect.core.designsystem.component.SCBadgeSize
import br.com.sportconnect.core.designsystem.component.SCParticipantRow
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdown
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdownVariant
import br.com.sportconnect.core.designsystem.component.SCPriceEmphasis
import br.com.sportconnect.core.designsystem.component.SCSplitProgress
import br.com.sportconnect.core.designsystem.component.SCSplitProgressVariant
import br.com.sportconnect.core.designsystem.component.SCStatusBadge
import br.com.sportconnect.core.designsystem.component.formatBrl
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SCReservationTone
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SCShareState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_booking_contact_cd
import br.com.sportconnect.resources.aadm_booking_external_body
import br.com.sportconnect.resources.aadm_booking_participants
import br.com.sportconnect.resources.aadm_booking_values
import br.com.sportconnect.resources.aadm_unblock_cta
import br.com.sportconnect.resources.arena_booking_cancel_cta
import br.com.sportconnect.resources.arena_booking_net_preview
import br.com.sportconnect.resources.arena_booking_title
import br.com.sportconnect.resources.arena_game_detail_organizer
import br.com.sportconnect.resources.arena_game_detail_paid
import br.com.sportconnect.resources.arena_schedule_status_hold
import br.com.sportconnect.resources.common_date_time
import br.com.sportconnect.resources.aadm_booking_open_spots
import br.com.sportconnect.resources.aadm_role_organizer
import br.com.sportconnect.resources.game_status_awaiting
import br.com.sportconnect.resources.game_status_cancelled
import br.com.sportconnect.resources.game_status_confirmed
import br.com.sportconnect.resources.game_status_done
import br.com.sportconnect.resources.game_status_expired
import br.com.sportconnect.resources.game_status_in_progress
import br.com.sportconnect.resources.payout_gross
import br.com.sportconnect.resources.payout_net
import br.com.sportconnect.resources.payout_platform_fee
import org.jetbrains.compose.resources.stringResource

private fun StatusReserva.tone(): SCReservationTone = when (this) {
    StatusReserva.RASCUNHO, StatusReserva.AGUARDANDO_PAGAMENTOS -> SCReservationTone.AwaitingPayment
    StatusReserva.CONFIRMADO, StatusReserva.EM_ANDAMENTO -> SCReservationTone.Confirmed
    StatusReserva.CONCLUIDO -> SCReservationTone.Completed
    StatusReserva.CANCELADO -> SCReservationTone.Cancelled
    StatusReserva.EXPIRADO -> SCReservationTone.Expired
}

@Composable
private fun StatusReserva.label(): String = when (this) {
    StatusReserva.RASCUNHO, StatusReserva.AGUARDANDO_PAGAMENTOS -> stringResource(Res.string.game_status_awaiting)
    StatusReserva.CONFIRMADO -> stringResource(Res.string.game_status_confirmed)
    StatusReserva.EM_ANDAMENTO -> stringResource(Res.string.game_status_in_progress)
    StatusReserva.CONCLUIDO -> stringResource(Res.string.game_status_done)
    StatusReserva.CANCELADO -> stringResource(Res.string.game_status_cancelled)
    StatusReserva.EXPIRADO -> stringResource(Res.string.game_status_expired)
}

/** Status do detalhe: badge do jogo (`game_status_*`) ou, sem jogo, o selo da agenda. */
@Composable
fun BookingStatus(ui: BookingDetailUi, large: Boolean) {
    val gs = ui.gameStatus
    when {
        gs != null && ui.cellStatus == AgendaCellStatus.InProgress && !gs.terminal ->
            SCStatusBadge(SCReservationTone.Confirmed, size = if (large) SCBadgeSize.Large else SCBadgeSize.Medium, label = stringResource(Res.string.game_status_in_progress))
        gs == StatusReserva.AGUARDANDO_PAGAMENTOS && ui.holdUntil != null ->
            SCStatusBadge(SCReservationTone.AwaitingPayment, size = if (large) SCBadgeSize.Large else SCBadgeSize.Medium, label = stringResource(Res.string.arena_schedule_status_hold, ui.holdUntil))
        gs != null -> SCStatusBadge(gs.tone(), size = if (large) SCBadgeSize.Large else SCBadgeSize.Medium, label = gs.label())
        else -> AgendaStatusChip(AgendaCellUi(courtId = "", hour = 0, status = ui.cellStatus, holdUntil = ui.holdUntil))
    }
}

@Composable
private fun CancelButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = SCShape.control,
        border = BorderStroke(1.dp, c.outline),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, color = c.error) }
}

/** Titulo "Society · Quadra 2". */
@Composable
fun BookingDetailUi.title(): String = listOfNotNull(sport?.label(), courtName).joinToString(" · ")

/**
 * Corpo do detalhe da reserva. [panel] = versao do painel lateral (expanded): mais enxuta, sem
 * lista de participantes. Valores financeiros so aparecem com [showFinancials] (gestor).
 */
@Composable
fun BookingDetailContent(
    ui: BookingDetailUi,
    modifier: Modifier = Modifier,
    panel: Boolean = false,
    showFinancials: Boolean = true,
    onCancel: (() -> Unit)? = null,
    onRelease: (() -> Unit)? = null,
    onContact: (() -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (panel) 8.dp else 12.dp)) {
        if (panel) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.arena_booking_title), style = t.titleSmall, color = c.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
                BookingStatus(ui, large = false)
            }
        } else {
            BookingStatus(ui, large = true)
        }
        Text(ui.title(), style = if (panel) t.titleMedium else t.titleLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Text(stringResource(Res.string.common_date_time, ui.dateLabel, ui.rangeLabel), style = t.bodyMedium, color = c.onSurfaceVariant)

        if (ui.organizer != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.arena_game_detail_organizer, ui.organizer), style = t.bodyMedium, color = c.onSurface, modifier = Modifier.weight(1f))
                if (onContact != null && ui.organizerPhone != null) {
                    IconButton(onClick = onContact, modifier = Modifier.testTag("booking_contact")) {
                        Icon(Icons.Rounded.Call, stringResource(Res.string.aadm_booking_contact_cd, ui.organizer), tint = c.primary)
                    }
                }
            }
        }
        if (ui.jogoId != null && ui.total > 0) {
            SCSplitProgress(
                totalShares = ui.total,
                paid = ui.paid,
                variant = SCSplitProgressVariant.Compact,
                collected = ui.collected.takeIf { showFinancials && !panel },
                total = ui.gross.takeIf { showFinancials && !panel },
            )
        }
        if (ui.externalLabel != null) {
            Text(stringResource(Res.string.aadm_booking_external_body, ui.externalLabel), style = t.bodyMedium, color = c.onSurfaceVariant)
        }

        if (!panel && ui.participants.isNotEmpty()) {
            HorizontalDivider(color = c.outlineVariant)
            ArenaSectionTitle(stringResource(Res.string.aadm_booking_participants, ui.paid, ui.total), Modifier.padding(top = 4.dp))
            Column {
                ui.participants.forEach { p ->
                    SCParticipantRow(
                        name = p.name,
                        shareState = if (p.paid) SCShareState.Paid else SCShareState.Pending,
                        role = if (p.organizer) stringResource(Res.string.aadm_role_organizer) else null,
                    )
                }
                val open = ui.total - ui.participants.size
                if (open > 0) {
                    Text(stringResource(Res.string.aadm_booking_open_spots, open), style = t.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        if (showFinancials && (ui.jogoId != null)) {
            HorizontalDivider(color = c.outlineVariant)
            if (!panel) ArenaSectionTitle(stringResource(Res.string.aadm_booking_values))
            SCPriceBreakdown(
                lines = listOf(
                    PriceLine(stringResource(Res.string.payout_gross), ui.gross),
                    PriceLine(stringResource(Res.string.payout_platform_fee), valueText = "− ${ui.fee.formatBrl()}", emphasis = SCPriceEmphasis.Muted),
                ),
                total = PriceLine(stringResource(Res.string.payout_net), ui.net, emphasis = SCPriceEmphasis.Strong),
                variant = SCPriceBreakdownVariant.Arena,
                modifier = Modifier.testTag("booking_values"),
            )
            if (!panel && ui.gameStatus != StatusReserva.CONCLUIDO) {
                Text(stringResource(Res.string.arena_booking_net_preview, ui.net.formatBrl()), style = t.bodySmall, color = c.onSurfaceVariant)
            }
        }

        if (onCancel != null && ui.cancellable) {
            CancelButton(stringResource(Res.string.arena_booking_cancel_cta), onCancel, Modifier.padding(top = 8.dp).testTag("booking_cancel"))
        } else if (onRelease != null && ui.jogoId == null && ui.externalLabel != null) {
            CancelButton(stringResource(Res.string.aadm_unblock_cta), onRelease, Modifier.padding(top = 8.dp).testTag("booking_release"))
        }
    }
}
