package br.com.sportconnect.feature.arenaadmin.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCBottomNav
import br.com.sportconnect.core.designsystem.component.SCNavItem
import br.com.sportconnect.core.designsystem.component.SCNavigationRail
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.icon.SCLogo
import br.com.sportconnect.core.designsystem.theme.SCBreakpoints
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.ArenaTab
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.arena_dashboard_title
import br.com.sportconnect.resources.arena_tab_courts
import br.com.sportconnect.resources.arena_tab_payouts
import br.com.sportconnect.resources.arena_tab_schedule
import br.com.sportconnect.resources.nav_profile
import org.jetbrains.compose.resources.stringResource

/** Classe de largura (10 §1.1). */
enum class ArenaWidth { Compact, Medium, Expanded;

    companion object {
        fun of(width: Dp): ArenaWidth = when {
            width < SCBreakpoints.medium -> Compact
            width < SCBreakpoints.expanded -> Medium
            else -> Expanded
        }
    }
}

/** Largura da area de conteudo (sem a navegacao), disponivel para as telas. */
val LocalArenaWidth = compositionLocalOf { ArenaWidth.Compact }

/**
 * Casca do modo "Minha Arena" (10 §1.2): bottom nav no compact (Agenda · Quadras · Repasses · Perfil,
 * com o Painel como inicio da aba Agenda); rail a partir de 600dp com "Painel" como destino proprio.
 * "Perfil" sai do modo arena ([ArenaAdminNavigator.openTab] com [ArenaTab.Profile]).
 */
@Composable
fun ArenaAdminScaffold(
    selected: ArenaTab,
    nav: ArenaAdminNavigator,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    topBar: @Composable (ArenaWidth) -> Unit = {},
    bottomBar: @Composable (ArenaWidth) -> Unit = {},
    content: @Composable (PaddingValues, ArenaWidth) -> Unit,
) {
    val labelDashboard = stringResource(Res.string.arena_dashboard_title)
    val labelSchedule = stringResource(Res.string.arena_tab_schedule)
    val labelCourts = stringResource(Res.string.arena_tab_courts)
    val labelPayouts = stringResource(Res.string.arena_tab_payouts)
    val labelProfile = stringResource(Res.string.nav_profile)
    val onSelect: (String) -> Unit = { route -> nav.openTab(ArenaTab.valueOf(route)) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val width = ArenaWidth.of(maxWidth)
        if (width == ArenaWidth.Compact) {
            // Compact: a aba "Agenda" abre o Painel (resumo do dia + agenda).
            val items = listOf(
                SCNavItem(ArenaTab.Dashboard.name, labelSchedule, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
                SCNavItem(ArenaTab.Courts.name, labelCourts, Icons.Outlined.Stadium, Icons.Rounded.Stadium),
                SCNavItem(ArenaTab.Payouts.name, labelPayouts, Icons.Outlined.Payments, Icons.Rounded.Payments),
                SCNavItem(ArenaTab.Profile.name, labelProfile, Icons.Outlined.Person, Icons.Rounded.Person),
            )
            val sel = if (selected == ArenaTab.Schedule) ArenaTab.Dashboard else selected
            Scaffold(
                topBar = { topBar(width) },
                bottomBar = {
                    Column {
                        bottomBar(width)
                        SCBottomNav(items, sel.name, onSelect, Modifier.testTag("arena_bottom_nav"))
                    }
                },
                snackbarHost = { snackbarHostState?.let { SCSnackbarHost(it) } },
                containerColor = SportConnectTheme.colors.surface,
            ) { padding ->
                CompositionLocalProvider(LocalArenaWidth provides width) { content(padding, width) }
            }
        } else {
            val items = listOf(
                SCNavItem(ArenaTab.Dashboard.name, labelDashboard, Icons.Outlined.SpaceDashboard, Icons.Rounded.SpaceDashboard),
                SCNavItem(ArenaTab.Schedule.name, labelSchedule, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
                SCNavItem(ArenaTab.Courts.name, labelCourts, Icons.Outlined.Stadium, Icons.Rounded.Stadium),
                SCNavItem(ArenaTab.Payouts.name, labelPayouts, Icons.Outlined.Payments, Icons.Rounded.Payments),
                SCNavItem(ArenaTab.Profile.name, labelProfile, Icons.Outlined.Person, Icons.Rounded.Person),
            )
            Row(Modifier.fillMaxSize()) {
                SCNavigationRail(
                    items, selected.name, onSelect,
                    Modifier.fillMaxHeight().testTag("arena_rail"),
                    header = { Image(SCLogo.Symbol, contentDescription = "SportConnect", modifier = Modifier.padding(top = 16.dp, bottom = 12.dp).size(40.dp)) },
                )
                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = { topBar(width) },
                    bottomBar = { bottomBar(width) },
                    snackbarHost = { snackbarHostState?.let { SCSnackbarHost(it) } },
                    containerColor = SportConnectTheme.colors.surface,
                ) { padding ->
                    CompositionLocalProvider(LocalArenaWidth provides width) { content(padding, width) }
                }
            }
        }
    }
}
