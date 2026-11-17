package br.com.sportconnect.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCBreakpoints
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme

/**
 * Destino de navegacao. [selectedIcon] = variante preenchida (FILL 1) usada quando ativo.
 * [badgeDescription] e lido junto do rotulo ("2 pendencias").
 */
@Immutable
data class SCNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val badgeCount: Int? = null,
    val badgeDescription: String? = null,
)

@Composable
private fun NavIcon(item: SCNavItem, selected: Boolean) {
    val icon = if (selected) item.selectedIcon else item.icon
    if (item.badgeCount != null && item.badgeCount > 0) {
        BadgedBox(badge = {
            Badge(containerColor = SportConnectTheme.colors.error, contentColor = SportConnectTheme.colors.onError) {
                Text(if (item.badgeCount > 99) "99+" else item.badgeCount.toString())
            }
        }) { Icon(icon, contentDescription = null) }
    } else {
        Icon(icon, contentDescription = null)
    }
}

private fun Modifier.badgeSemantics(item: SCNavItem): Modifier =
    if (item.badgeDescription != null) semantics { stateDescription = item.badgeDescription } else this

/**
 * Bottom navigation (06 §5.15): container surfaceContainer 80dp, indicador primaryContainer,
 * rotulos SEMPRE visiveis (labelSmall), Role.Tab com selected (nativo do NavigationBarItem).
 */
@Composable
fun SCBottomNav(
    items: List<SCNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SportConnectTheme.colors
    NavigationBar(modifier = modifier, containerColor = c.surfaceContainer, tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item.route) },
                icon = { NavIcon(item, selected) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true,
                modifier = Modifier.badgeSemantics(item).testTag("nav_${item.route}"),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = c.onPrimaryContainer,
                    selectedTextColor = c.onSurface,
                    indicatorColor = c.primaryContainer,
                    unselectedIconColor = c.onSurfaceVariant,
                    unselectedTextColor = c.onSurfaceVariant,
                ),
            )
        }
    }
}

/** Navigation rail (medium/expanded). [header] costuma ser o FAB "Criar jogo" ou o simbolo. */
@Composable
fun SCNavigationRail(
    items: List<SCNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    NavigationRail(
        modifier = modifier,
        containerColor = c.surfaceContainer,
        header = header?.let { h -> { Box(Modifier.padding(top = 12.dp, bottom = 8.dp)) { h() } } },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEach { item ->
                val selected = item.route == selectedRoute
                NavigationRailItem(
                    selected = selected,
                    onClick = { onItemSelected(item.route) },
                    icon = { NavIcon(item, selected) },
                    label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                    alwaysShowLabel = true,
                    modifier = Modifier.badgeSemantics(item).testTag("nav_${item.route}"),
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = c.onPrimaryContainer,
                        selectedTextColor = c.onSurface,
                        indicatorColor = c.primaryContainer,
                        unselectedIconColor = c.onSurfaceVariant,
                        unselectedTextColor = c.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

/** Drawer permanente (large >= 1200dp — painel da arena). */
@Composable
fun SCNavigationDrawerSheet(
    items: List<SCNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    PermanentDrawerSheet(modifier.width(280.dp), drawerContainerColor = c.surfaceContainerLow) {
        if (header != null) Box(Modifier.padding(horizontal = 28.dp, vertical = 20.dp)) { header() }
        items.forEach { item ->
            val selected = item.route == selectedRoute
            NavigationDrawerItem(
                label = { Text(item.label, style = MaterialTheme.typography.labelLarge) },
                selected = selected,
                onClick = { onItemSelected(item.route) },
                icon = { NavIcon(item, selected) },
                badge = item.badgeCount?.takeIf { it > 0 }?.let { n -> { Text(n.toString(), style = MaterialTheme.typography.labelLarge) } },
                modifier = Modifier.padding(horizontal = 12.dp).badgeSemantics(item),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = c.primaryContainer,
                    selectedIconColor = c.onPrimaryContainer,
                    selectedTextColor = c.onPrimaryContainer,
                    unselectedContainerColor = c.surfaceContainerLow,
                ),
            )
        }
    }
}

/**
 * Navegacao adaptativa (05 §9): bottom nav < 600dp, rail 600dp ate [drawerBreakpoint], drawer a partir dele.
 * Calculado pela largura disponivel (BoxWithConstraints), sem depender de window-size-class.
 *
 * - [showNavigation] = false esconde a barra/rail sem recriar o [content] (fluxos transacionais, 09 §2.2 regra 4).
 * - [drawerBreakpoint]: o modo Atleta usa rail tambem em large (10 §1.2/I5) passando [Dp.Infinity];
 *   o painel da arena mantem o padrao (drawer >= 1200dp).
 */
@Composable
fun SCAdaptiveNavigation(
    items: List<SCNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    showNavigation: Boolean = true,
    drawerBreakpoint: Dp = SCBreakpoints.large,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        // A barra/rail entra e sai animada: sem isso o conteudo "pula" ao abrir pagar/criar jogo (A4).
        when {
            maxWidth < SCBreakpoints.medium -> Scaffold(
                bottomBar = {
                    AnimatedVisibility(
                        visible = showNavigation,
                        enter = expandVertically(tween(SCMotion.Medium2, easing = SCMotion.EmphasizedDecelerate), expandFrom = Alignment.Top) +
                            fadeIn(tween(SCMotion.Medium1)),
                        exit = shrinkVertically(tween(SCMotion.Medium1, easing = SCMotion.EmphasizedAccelerate), shrinkTowards = Alignment.Top) +
                            fadeOut(tween(SCMotion.Short4)),
                    ) { SCBottomNav(items, selectedRoute, onItemSelected) }
                },
                containerColor = SportConnectTheme.colors.surface,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { padding -> content(Modifier.padding(padding).consumeWindowInsets(padding)) }
            maxWidth < drawerBreakpoint -> Row(Modifier.fillMaxSize()) {
                SideNavigation(showNavigation) { SCNavigationRail(items, selectedRoute, onItemSelected, Modifier.fillMaxHeight(), header) }
                content(Modifier.weight(1f).fillMaxHeight())
            }
            else -> Row(Modifier.fillMaxSize()) {
                SideNavigation(showNavigation) { SCNavigationDrawerSheet(items, selectedRoute, onItemSelected, Modifier.fillMaxHeight(), header) }
                content(Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun SideNavigation(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(tween(SCMotion.Medium2, easing = SCMotion.EmphasizedDecelerate), expandFrom = Alignment.Start) +
            fadeIn(tween(SCMotion.Medium1)),
        exit = shrinkHorizontally(tween(SCMotion.Medium1, easing = SCMotion.EmphasizedAccelerate), shrinkTowards = Alignment.Start) +
            fadeOut(tween(SCMotion.Short4)),
    ) { content() }
}
