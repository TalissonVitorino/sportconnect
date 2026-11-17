package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.ds_loading
import org.jetbrains.compose.resources.stringResource

enum class SCButtonVariant { Primary, Secondary, Tonal, Text, Destructive }
enum class SCButtonSize { Small, Medium, Large }

private data class SCButtonMetrics(val height: Dp, val paddingH: Dp, val icon: Dp)

private fun SCButtonSize.metrics() = when (this) {
    SCButtonSize.Small -> SCButtonMetrics(40.dp, 16.dp, 18.dp)
    SCButtonSize.Medium -> SCButtonMetrics(48.dp, 24.dp, 20.dp)
    SCButtonSize.Large -> SCButtonMetrics(56.dp, 24.dp, 24.dp)
}

/**
 * Botao do DS (06 §5.1). Raio 12, rotulo labelLarge em caixa de sentenca.
 * Small tem 40dp visuais e 48dp de area de toque (minimumInteractiveComponentSize do M3).
 * Em [loading], o spinner substitui o icone, o rotulo permanece, o clique e ignorado e
 * `stateDescription = "Carregando"` e anunciado — o botao mantem a largura e o foco.
 */
@Composable
fun SCButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SCButtonVariant = SCButtonVariant.Primary,
    size: SCButtonSize = SCButtonSize.Medium,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    /** Nome acessivel quando o rotulo visivel e curto demais (ex.: "Lembrar Ana de pagar"). */
    accessibilityLabel: String? = null,
) {
    val m = size.metrics()
    val c = SportConnectTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val (container, content) = when (variant) {
        SCButtonVariant.Primary -> c.primary to c.onPrimary
        SCButtonVariant.Secondary -> Color.Transparent to c.primary
        SCButtonVariant.Tonal -> c.primaryContainer to c.onPrimaryContainer
        SCButtonVariant.Text -> Color.Transparent to c.primary
        SCButtonVariant.Destructive -> c.error to c.onError
    }
    val disabledContainer = if (container == Color.Transparent) Color.Transparent else c.onSurface.copy(alpha = 0.12f)
    val colors = ButtonDefaults.buttonColors(
        containerColor = container,
        contentColor = content,
        disabledContainerColor = disabledContainer,
        disabledContentColor = c.onSurface.copy(alpha = 0.38f),
    )
    val border = if (variant == SCButtonVariant.Secondary) {
        BorderStroke(1.dp, if (enabled) c.outline else c.onSurface.copy(alpha = 0.12f))
    } else {
        null
    }
    val loadingLabel = stringResource(Res.string.ds_loading)
    val paddingH = if (variant == SCButtonVariant.Text) 12.dp else m.paddingH

    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = m.height)
            .scFocusRing(interaction, SCShape.control)
            .semantics {
                if (loading) stateDescription = loadingLabel
                if (accessibilityLabel != null) contentDescription = accessibilityLabel
            },
        enabled = enabled,
        shape = SCShape.control,
        colors = colors,
        border = border,
        elevation = null,
        contentPadding = PaddingValues(horizontal = paddingH, vertical = 0.dp),
        interactionSource = interaction,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SportConnectTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                loading -> Box(Modifier.size(m.icon), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(m.icon - 2.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp,
                    )
                }
                leadingIcon != null -> Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(m.icon))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
