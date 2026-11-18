package br.com.sportconnect.core.designsystem.component

import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_back
import br.com.sportconnect.resources.cd_close
import org.jetbrains.compose.resources.stringResource

enum class SCTopBarVariant { Small, CenterAligned, Large, Contextual }
enum class SCTopBarNavigation { Back, Close }

/**
 * Barra superior (06 §5.14). Titulo com heading(); icones com contentDescription.
 * No topo: surface sem elevacao; rolada: surfaceContainer (via scrollBehavior).
 * `Large` usa headlineMedium (Saira) e colapsa ao rolar; `Contextual` usa secondaryContainer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SCTopBar(
    title: String,
    modifier: Modifier = Modifier,
    variant: SCTopBarVariant = SCTopBarVariant.Small,
    navigation: SCTopBarNavigation? = SCTopBarNavigation.Back,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val c = SportConnectTheme.colors
    val contextual = variant == SCTopBarVariant.Contextual
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (contextual) c.secondaryContainer else c.surface,
        scrolledContainerColor = if (contextual) c.secondaryContainer else c.surfaceContainer,
        navigationIconContentColor = if (contextual) c.onSecondaryContainer else c.onSurface,
        titleContentColor = if (contextual) c.onSecondaryContainer else c.onSurface,
        actionIconContentColor = if (contextual) c.onSecondaryContainer else c.onSurfaceVariant,
    )
    val titleContent: @Composable () -> Unit = {
        Text(
            title,
            style = if (variant == SCTopBarVariant.Large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            maxLines = if (variant == SCTopBarVariant.Large) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
    }
    val navIcon: @Composable () -> Unit = {
        val nav = if (contextual) SCTopBarNavigation.Close else navigation
        if (nav != null) {
            IconButton(onClick = onNavigationClick) {
                when (nav) {
                    SCTopBarNavigation.Back -> Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.cd_back))
                    SCTopBarNavigation.Close -> Icon(Icons.Rounded.Close, stringResource(Res.string.cd_close))
                }
            }
        }
    }
    when (variant) {
        SCTopBarVariant.CenterAligned -> CenterAlignedTopAppBar(title = titleContent, modifier = modifier.then(TopBarFirst), navigationIcon = navIcon, actions = actions, colors = colors, scrollBehavior = scrollBehavior)
        SCTopBarVariant.Large -> LargeTopAppBar(title = titleContent, modifier = modifier.then(TopBarFirst), navigationIcon = navIcon, actions = actions, colors = colors, scrollBehavior = scrollBehavior)
        else -> TopAppBar(title = titleContent, modifier = modifier.then(TopBarFirst), navigationIcon = navIcon, actions = actions, colors = colors, scrollBehavior = scrollBehavior)
    }
}

/**
 * A1 (doc 13): o Scaffold do Material 3 poe o conteudo antes da barra de topo na arvore de acessibilidade;
 * o leitor de tela lia o titulo da tela por ultimo. Este grupo faz a barra ser lida primeiro.
 */
val TopBarFirst: Modifier = Modifier.semantics { isTraversalGroup = true; traversalIndex = -1f }
