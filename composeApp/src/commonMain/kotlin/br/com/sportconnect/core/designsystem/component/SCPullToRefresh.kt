@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import kotlinx.coroutines.launch

/**
 * Puxar para atualizar (A8 do backlog 11). O indicador fica visivel enquanto [onRefresh] roda e da um
 * toque leve (haptic) quando a pessoa solta. O conteudo deve ser rolavel (LazyColumn/Grid ou verticalScroll).
 */
@Composable
fun SCPullToRefresh(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val action by rememberUpdatedState(onRefresh)
    val state = rememberPullToRefreshState()
    val c = SportConnectTheme.colors
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            if (refreshing) return@PullToRefreshBox
            haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            scope.launch {
                refreshing = true
                try { action() } finally { refreshing = false }
            }
        },
        state = state,
        modifier = modifier.testTag("pull_to_refresh"),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = refreshing,
                containerColor = c.surfaceContainerHigh,
                color = c.primary,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
        content = content,
    )
}
