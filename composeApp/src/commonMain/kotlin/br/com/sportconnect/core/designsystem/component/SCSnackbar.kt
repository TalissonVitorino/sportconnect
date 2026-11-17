package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCElevation
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_close
import org.jetbrains.compose.resources.stringResource

enum class SCSnackbarKind { Info, Success, Error, Undo }

/** Visuals com o tipo do DS. */
data class SCSnackbarVisuals(
    override val message: String,
    val kind: SCSnackbarKind = SCSnackbarKind.Info,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
) : SnackbarVisuals

/**
 * Exibe um snackbar do DS. Duracao: curta (4s), longa com acao (10s), indefinida para erros
 * que exigem acao (passe `SnackbarDuration.Indefinite`).
 */
suspend fun SnackbarHostState.showSC(
    message: String,
    kind: SCSnackbarKind = SCSnackbarKind.Info,
    actionLabel: String? = null,
    duration: SnackbarDuration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
): SnackbarResult = showSnackbar(
    SCSnackbarVisuals(message, kind, actionLabel, withDismissAction = duration == SnackbarDuration.Indefinite, duration = duration),
)

/** Host que renderiza [SCSnackbar] para cada mensagem (liveRegion Polite). */
@Composable
fun SCSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data: SnackbarData ->
        val v = data.visuals
        SCSnackbar(
            message = v.message,
            kind = (v as? SCSnackbarVisuals)?.kind ?: SCSnackbarKind.Info,
            actionLabel = v.actionLabel,
            onAction = { data.performAction() },
            onDismiss = if (v.withDismissAction) ({ data.dismiss() }) else null,
        )
    }
}

private val SCSnackbarKind.icon: ImageVector
    get() = when (this) {
        SCSnackbarKind.Info -> Icons.Rounded.Info
        SCSnackbarKind.Success -> Icons.Rounded.CheckCircle
        SCSnackbarKind.Error -> Icons.Rounded.ErrorOutline
        SCSnackbarKind.Undo -> Icons.AutoMirrored.Rounded.Undo
    }

/**
 * Snackbar (06 §5.17): inverseSurface, raio 12, elevacao 3, icone em tom inverso,
 * mensagem bodyMedium (ate 2 linhas), acao inversePrimary.
 */
@Composable
fun SCSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    kind: SCSnackbarKind = SCSnackbarKind.Info,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    // tom inverso: sobre inverseSurface usa o tom do tema oposto
    val iconTint = when (kind) {
        SCSnackbarKind.Success -> ext.inverseSuccess
        SCSnackbarKind.Error -> ext.inverseError
        else -> c.inverseOnSurface
    }
    Surface(
        modifier = modifier.padding(12.dp).semantics { liveRegion = LiveRegionMode.Polite },
        shape = SCShape.control,
        color = c.inverseSurface,
        contentColor = c.inverseOnSurface,
        shadowElevation = SCElevation.level2,
    ) {
        Row(
            Modifier.heightIn(min = 48.dp).padding(start = 16.dp, end = if (actionLabel != null || onDismiss != null) 4.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(kind.icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, maxLines = 2, modifier = Modifier.weight(1f).padding(vertical = 14.dp))
            if (actionLabel != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel, color = c.inversePrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, stringResource(Res.string.cd_close), tint = c.inverseOnSurface)
                }
            }
        }
    }
}
