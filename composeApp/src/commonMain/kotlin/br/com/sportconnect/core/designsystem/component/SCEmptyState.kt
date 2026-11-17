package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme

/**
 * Estado vazio (06 §5.13): ilustracao "linha de cal" 160dp (decorativa), titulo headlineSmall
 * com heading(), descricao ate 2 linhas, acao Primary e acao Text opcional. Centralizado.
 */
@Composable
fun SCEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    illustration: SCIllustration = SCIllustration.Court,
    primaryAction: SCAction? = null,
    secondaryAction: SCAction? = null,
) {
    val c = SportConnectTheme.colors
    val sp = SportConnectTheme.spacing
    Column(
        modifier.fillMaxWidth().padding(top = sp.xxl, start = sp.lg, end = sp.lg, bottom = sp.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(sp.xs),
    ) {
        SCIllustrationImage(illustration, Modifier.size(160.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = c.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = sp.md).semantics { heading() },
        )
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp),
        )
        if (primaryAction != null) {
            SCButton(
                primaryAction.label,
                primaryAction.onClick,
                leadingIcon = primaryAction.icon,
                loading = primaryAction.loading,
                modifier = Modifier.padding(top = sp.md),
            )
        }
        if (secondaryAction != null) {
            SCButton(secondaryAction.label, secondaryAction.onClick, variant = SCButtonVariant.Text)
        }
    }
}
