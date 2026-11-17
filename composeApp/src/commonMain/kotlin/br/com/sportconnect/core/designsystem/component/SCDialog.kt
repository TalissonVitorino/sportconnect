package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.sportconnect.core.designsystem.theme.SCElevation
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme

enum class SCDialogVariant { Confirm, Destructive, Info }

/**
 * Dialogo (06 §5.19): container surfaceContainerHigh, raio 28, nivel 5, largura 280–560dp,
 * acoes a direita (Text dispensar + Primary/Destructive confirmar). Esc/voltar fecha;
 * em `Destructive` o foco inicial vai para a acao SEGURA (dispensar).
 * Variante FullScreen fica para a feature (Scaffold + SCTopBar Close) — ver desvios.
 */
@Composable
fun SCDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirm: SCAction,
    modifier: Modifier = Modifier,
    dismiss: SCAction? = null,
    icon: ImageVector? = null,
    variant: SCDialogVariant = SCDialogVariant.Confirm,
    body: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        SCDialogContent(title, confirm, modifier.padding(24.dp), dismiss, icon, variant, body, content = content)
    }
}

/** Superficie do dialogo sem a janela (reutilizavel no catalogo/testes). */
@Composable
fun SCDialogContent(
    title: String,
    confirm: SCAction,
    modifier: Modifier = Modifier,
    dismiss: SCAction? = null,
    icon: ImageVector? = null,
    variant: SCDialogVariant = SCDialogVariant.Confirm,
    body: String? = null,
    requestInitialFocus: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val safeFocus = remember { FocusRequester() }
    if (requestInitialFocus && variant == SCDialogVariant.Destructive && dismiss != null) {
        LaunchedEffect(Unit) { runCatching { safeFocus.requestFocus() } }
    }
    Surface(
        modifier = modifier.widthIn(min = 280.dp, max = 560.dp).semantics { paneTitle = title },
        shape = SCShape.dialog,
        color = c.surfaceContainerHigh,
        shadowElevation = SCElevation.level5,
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (variant == SCDialogVariant.Destructive) c.error else c.secondary,
                    modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                )
            }
            Text(
                title,
                style = t.titleLarge,
                color = c.onSurface,
                textAlign = if (icon != null) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
            )
            if (body != null) {
                Text(body, style = t.bodyMedium, color = c.onSurfaceVariant)
            }
            content?.invoke()
            FlowRow(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (dismiss != null) {
                    SCButton(
                        dismiss.label,
                        dismiss.onClick,
                        variant = SCButtonVariant.Text,
                        modifier = Modifier.focusRequester(safeFocus),
                    )
                }
                SCButton(
                    confirm.label,
                    confirm.onClick,
                    variant = if (variant == SCDialogVariant.Destructive) SCButtonVariant.Destructive else SCButtonVariant.Primary,
                    leadingIcon = confirm.icon,
                    loading = confirm.loading,
                    enabled = confirm.enabled,
                )
            }
        }
    }
}
