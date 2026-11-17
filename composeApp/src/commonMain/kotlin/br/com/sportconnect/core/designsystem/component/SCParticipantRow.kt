package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SCShareState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.ds_open_spot
import br.com.sportconnect.resources.ds_share_covered
import br.com.sportconnect.resources.ds_share_paid
import br.com.sportconnect.resources.ds_share_pending
import br.com.sportconnect.resources.ds_share_processing
import br.com.sportconnect.resources.ds_share_refunded
import br.com.sportconnect.resources.ds_you
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val SCShareState.icon: ImageVector
    get() = when (this) {
        SCShareState.Paid -> Icons.Rounded.CheckCircle
        SCShareState.Pending -> Icons.Rounded.Schedule
        SCShareState.Processing -> Icons.Rounded.HourglassTop
        SCShareState.Covered -> Icons.Rounded.Shield
        SCShareState.Refunded -> Icons.Rounded.Replay
    }

val SCShareState.labelRes: StringResource
    get() = when (this) {
        SCShareState.Paid -> Res.string.ds_share_paid
        SCShareState.Pending -> Res.string.ds_share_pending
        SCShareState.Processing -> Res.string.ds_share_processing
        SCShareState.Covered -> Res.string.ds_share_covered
        SCShareState.Refunded -> Res.string.ds_share_refunded
    }

@Composable
internal fun SCShareState.color(): Color {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    return when (this) {
        SCShareState.Paid -> ext.success
        SCShareState.Pending -> ext.warning
        SCShareState.Processing -> c.onSurfaceVariant
        SCShareState.Covered -> c.tertiary
        SCShareState.Refunded -> c.onSurfaceVariant
    }
}

/**
 * Linha de participante do rateio (06 §5.9). Altura minima 64dp. Status da cota = icone + texto.
 * A linha e lida como um no ("Ana Souza, Pagou as 14:32"); a [action] e um alvo separado de
 * 48dp com rotulo explicito (ex.: "Lembrar Ana de pagar").
 *
 * [paidAtLabel] vem pre-formatado pela feature (o DS nao formata datas/fusos).
 */
@Composable
fun SCParticipantRow(
    name: String,
    shareState: SCShareState,
    modifier: Modifier = Modifier,
    avatar: AvatarUi = AvatarUi(name),
    role: String? = null,
    paidAtLabel: String? = null,
    isSelf: Boolean = false,
    statusLabel: String = stringResource(shareState.labelRes),
    action: SCAction? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val displayName = if (isSelf) stringResource(Res.string.ds_you) else name
    val status = if (paidAtLabel != null) "$statusLabel · $paidAtLabel" else statusLabel
    val rowCd = listOfNotNull(displayName, role, status).joinToString(", ")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(if (isSelf) Modifier.background(c.surfaceContainerLow, SCShape.control) else Modifier)
            .padding(horizontal = if (isSelf) 12.dp else 0.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.weight(1f).clearAndSetSemantics { contentDescription = rowCd },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SCAvatar(avatar, size = 40.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(displayName, style = t.bodyLarge, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (role != null) {
                        Text(role, style = t.labelSmall, color = c.onSurfaceVariant, maxLines = 1)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(shareState.icon, null, tint = shareState.color(), modifier = Modifier.size(16.dp))
                    Text(status, style = t.bodySmall, color = c.onSurfaceVariant)
                }
            }
        }
        if (action != null) {
            SCButton(
                text = action.label,
                onClick = action.onClick,
                variant = if (isSelf) SCButtonVariant.Primary else SCButtonVariant.Text,
                size = SCButtonSize.Small,
                leadingIcon = action.icon,
                enabled = action.enabled,
                loading = action.loading,
                accessibilityLabel = action.accessibilityLabel,
            )
        }
    }
}

/** Variante `Invite` (06 §5.9): vaga vazia com avatar tracejado + "Vaga aberta" + acao "Convidar". */
@Composable
fun SCOpenSpotRow(
    modifier: Modifier = Modifier,
    label: String = stringResource(Res.string.ds_open_spot),
    action: SCAction? = null,
) {
    val c = SportConnectTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val outline = c.outline
        Box(
            Modifier.size(40.dp).drawBehind {
                drawCircle(
                    outline,
                    radius = size.minDimension / 2 - 1.dp.toPx(),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                )
            },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.PersonAdd, null, tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant, modifier = Modifier.weight(1f))
        if (action != null) {
            SCButton(action.label, action.onClick, variant = SCButtonVariant.Tonal, size = SCButtonSize.Small, leadingIcon = action.icon)
        }
    }
}
