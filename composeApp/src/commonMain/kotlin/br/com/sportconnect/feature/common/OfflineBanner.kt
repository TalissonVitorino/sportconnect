package br.com.sportconnect.feature.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.offline_banner
import br.com.sportconnect.resources.online_banner
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * true = ha conexao. Telas com dinheiro (pagar, cobrir diferenca) desligam a acao quando false
 * (09 §6.6: "offline mostra cache com banner e acoes de dinheiro desabilitadas").
 */
val LocalOnline = staticCompositionLocalOf { true }

/**
 * Banner de conexao (E7): "Sem conexao — exibindo dados de 14h32" enquanto offline; ao voltar,
 * "Conectado" por 2 s. Fica no topo, abaixo da status bar, e empurra o conteudo (nao cobre).
 */
@Composable
fun OfflineBanner(online: Boolean, modifier: Modifier = Modifier, onVisibleChange: (Boolean) -> Unit = {}) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val clock = LocalSCClock.current
    var offlineSince by remember { mutableStateOf<Instant?>(null) }
    var showBackOnline by remember { mutableStateOf(false) }
    LaunchedEffect(online) {
        if (!online) {
            if (offlineSince == null) offlineSince = clock.now()
            showBackOnline = false
        } else if (offlineSince != null) {
            offlineSince = null
            showBackOnline = true
            delay(2_000)
            showBackOnline = false
        }
    }
    val visible = !online || showBackOnline
    LaunchedEffect(visible) { onVisibleChange(visible) }
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(tween(SCMotion.Medium1, easing = SCMotion.EmphasizedDecelerate)) + fadeIn(),
        exit = shrinkVertically(tween(SCMotion.Medium1, easing = SCMotion.EmphasizedAccelerate)) + fadeOut(),
        modifier = modifier,
    ) {
        val offline = !online
        val bg = if (offline) c.inverseSurface else ext.successContainer
        val fg = if (offline) c.inverseOnSurface else ext.onSuccessContainer
        Row(
            Modifier.fillMaxWidth().background(bg).windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .testTag(if (offline) "offline_banner" else "online_banner"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(if (offline) Icons.Rounded.CloudOff else Icons.Rounded.Wifi, null, tint = fg, modifier = Modifier.size(18.dp))
            Text(
                if (offline) stringResource(Res.string.offline_banner, PtBrFormat.clock(offlineSince ?: clock.now()))
                else stringResource(Res.string.online_banner),
                style = MaterialTheme.typography.labelLarge,
                color = fg,
            )
        }
    }
}
