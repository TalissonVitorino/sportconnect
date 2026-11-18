package br.com.sportconnect.core.designsystem.component

import br.com.sportconnect.core.designsystem.a11y.webCheckedLabel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.icon.icon
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_sport_icon
import org.jetbrains.compose.resources.stringResource

enum class SCSportChipVariant { Tag, Filter, Compact }

/**
 * Chip de esporte (06 §5.3). O nome do esporte e sempre texto (ou contentDescription no Compact);
 * cor e icone sao reforco. Cores de esporte so identificam esporte — nunca status.
 * `Filter` e um checkbox acessivel (Role.Checkbox) com area de toque de 48dp.
 */
@Composable
fun SCSportChip(
    sport: Sport,
    modifier: Modifier = Modifier,
    variant: SCSportChipVariant = SCSportChipVariant.Tag,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    label: String = sport.label(),
    enabled: Boolean = true,
) {
    val sc = SportConnectTheme.extendedColors.sport(sport)
    val c = SportConnectTheme.colors
    val interaction = remember { MutableInteractionSource() }

    if (variant == SCSportChipVariant.Compact) {
        val cd = stringResource(Res.string.cd_sport_icon, label)
        Box(
            modifier = modifier
                .size(32.dp)
                .clip(SCShape.full)
                .background(sc.container)
                .semantics { contentDescription = cd },
            contentAlignment = Alignment.Center,
        ) {
            Icon(sport.icon, contentDescription = null, tint = sc.onContainer, modifier = Modifier.size(20.dp))
        }
        return
    }

    val isFilter = variant == SCSportChipVariant.Filter
    val container = when {
        !isFilter -> sc.container
        selected -> sc.container
        else -> c.surfaceContainerLowest
    }
    val contentColor = when {
        !isFilter -> sc.onContainer
        selected -> sc.onContainer
        else -> c.onSurfaceVariant
    }
    val iconTint = if (isFilter && !selected) sc.accent else sc.onContainer
    val borderMod = when {
        isFilter && selected -> Modifier.border(2.dp, sc.accent, SCShape.chip)
        isFilter -> Modifier.border(1.dp, c.outline, SCShape.chip)
        else -> Modifier
    }
    val interactive = isFilter && onClick != null

    Box(
        modifier = modifier
            .then(if (interactive) Modifier.minimumInteractiveComponentSize() else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .scFocusRing(interaction, SCShape.chip)
                .clip(SCShape.chip)
                .background(container)
                .then(borderMod)
                .then(if (interactive) Modifier.webCheckedLabel(label, selected) else Modifier)
                .then(
                    if (interactive) {
                        Modifier.toggleable(
                            value = selected,
                            enabled = enabled,
                            role = Role.Checkbox,
                            interactionSource = interaction,
                            indication = ripple(),
                            onValueChange = { onClick() },
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(start = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isFilter && selected) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = sc.onContainer, modifier = Modifier.size(18.dp))
            }
            Icon(sport.icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor, maxLines = 1)
        }
    }
}
