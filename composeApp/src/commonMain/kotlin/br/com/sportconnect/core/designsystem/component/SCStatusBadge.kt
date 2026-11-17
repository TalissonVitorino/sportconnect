package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.SportsScore
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCReservationTone
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.ds_booking_awaiting_payment
import br.com.sportconnect.resources.ds_booking_cancelled
import br.com.sportconnect.resources.ds_booking_completed
import br.com.sportconnect.resources.ds_booking_confirmed
import br.com.sportconnect.resources.ds_booking_expired
import br.com.sportconnect.resources.ds_status_prefix
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class SCBadgeSize { Medium, Large }

val SCReservationTone.icon: ImageVector
    get() = when (this) {
        SCReservationTone.AwaitingPayment -> Icons.Rounded.HourglassTop
        SCReservationTone.Confirmed -> Icons.Rounded.CheckCircle
        SCReservationTone.Cancelled -> Icons.Rounded.Cancel
        SCReservationTone.Expired -> Icons.Rounded.TimerOff
        SCReservationTone.Completed -> Icons.Rounded.SportsScore
    }

val SCReservationTone.labelRes: StringResource
    get() = when (this) {
        SCReservationTone.AwaitingPayment -> Res.string.ds_booking_awaiting_payment
        SCReservationTone.Confirmed -> Res.string.ds_booking_confirmed
        SCReservationTone.Cancelled -> Res.string.ds_booking_cancelled
        SCReservationTone.Expired -> Res.string.ds_booking_expired
        SCReservationTone.Completed -> Res.string.ds_booking_completed
    }

/**
 * Selo de estado da reserva (06 §5.4): SEMPRE icone + texto (cor nunca e o unico sinal).
 * [label] permite a copy contextual do 07 (ex.: `home_card_status_pending` "Pre-reserva · 8 de 12 pagas").
 */
@Composable
fun SCStatusBadge(
    tone: SCReservationTone,
    modifier: Modifier = Modifier,
    size: SCBadgeSize = SCBadgeSize.Medium,
    label: String = stringResource(tone.labelRes),
    detail: String? = null,
) {
    val colors = SportConnectTheme.extendedColors.reservation(tone)
    val text = if (detail != null) "$label · $detail" else label
    val cd = stringResource(Res.string.ds_status_prefix, if (detail != null) "$label, $detail" else label)
    val large = size == SCBadgeSize.Large
    Row(
        modifier = modifier
            .heightIn(min = if (large) 32.dp else 24.dp)
            .background(colors.container, SCShape.badge)
            .padding(horizontal = if (large) 10.dp else 8.dp, vertical = 2.dp)
            .clearAndSetSemantics {
                contentDescription = cd
                liveRegion = LiveRegionMode.Polite
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SportConnectTheme.spacing.xxs),
    ) {
        Icon(tone.icon, contentDescription = null, tint = colors.content, modifier = Modifier.size(if (large) 20.dp else 16.dp))
        Text(
            text = text,
            color = colors.content,
            style = if (large) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
        )
    }
}
