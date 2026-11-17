@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package br.com.sportconnect.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCIllustrationImage
import br.com.sportconnect.core.designsystem.component.SCSplitProgress
import br.com.sportconnect.core.designsystem.component.SCSplitProgressVariant
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.feature.common.UiSessionStore
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.common_currency_per_person
import br.com.sportconnect.resources.onboarding_1_body
import br.com.sportconnect.resources.onboarding_1_title
import br.com.sportconnect.resources.onboarding_2_body
import br.com.sportconnect.resources.onboarding_2_title
import br.com.sportconnect.resources.onboarding_3_body
import br.com.sportconnect.resources.onboarding_3_title
import br.com.sportconnect.resources.onboarding_next
import br.com.sportconnect.resources.onboarding_role_arena
import br.com.sportconnect.resources.onboarding_skip
import br.com.sportconnect.resources.onboarding_sports_body
import br.com.sportconnect.resources.onboarding_sports_title
import br.com.sportconnect.resources.onboarding_start
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Boas-vindas (09 ficha 1): 3 paginas de proposta + "Quais esportes voce joga?". Os esportes escolhidos
 * ficam em [AppPreferences.favoriteSports] (mesma escolha de Perfil › Meus esportes) e pre-filtram a busca.
 * A permissao de localizacao (`onboarding_location_*`) fica para quando a busca usar distancia real.
 */
class OnboardingViewModel(
    private val session: UiSessionStore,
    private val auth: AuthRepository,
    private val prefs: AppPreferences,
) : ViewModel() {
    private val _sports = MutableStateFlow(prefs.favoriteSports.mapNotNull { n -> Sport.entries.firstOrNull { it.name == n } }.toSet())
    val sports: StateFlow<Set<Sport>> = _sports.asStateFlow()

    fun toggle(sport: Sport) {
        _sports.update { if (sport in it) it - sport else it + sport }
        prefs.favoriteSports = _sports.value.map { it.name }.toSet()
    }

    /** Conclui; retorna true se ja ha sessao (vai para a Home) ou false (vai para `auth`). */
    fun finish(): Boolean {
        session.finishOnboarding()
        return auth.currentUser.value != null
    }

    companion object { const val PAGES = 4 }
}

data class OnboardingActions(
    val finished: (loggedIn: Boolean) -> Unit = {},
    val arena: () -> Unit = {},
)

@Composable
fun OnboardingRoute(actions: OnboardingActions, vm: OnboardingViewModel = koinViewModel()) {
    val sports by vm.sports.collectAsStateWithLifecycle()
    OnboardingScreen(
        selectedSports = sports,
        onToggleSport = vm::toggle,
        onFinish = { actions.finished(vm.finish()) },
        onArena = { vm.finish(); actions.arena() },
    )
}

private val pages = listOf(
    Res.string.onboarding_1_title to Res.string.onboarding_1_body,
    Res.string.onboarding_2_title to Res.string.onboarding_2_body,
    Res.string.onboarding_3_title to Res.string.onboarding_3_body,
)

/**
 * B10: paginas com arrastar (HorizontalPager), uma ilustracao por mensagem e o passo de esportes.
 * Medium/expanded: cartao central de 560dp.
 */
@Composable
fun OnboardingScreen(
    selectedSports: Set<Sport> = emptySet(),
    onToggleSport: (Sport) -> Unit = {},
    onFinish: () -> Unit = {},
    onArena: () -> Unit = {},
    initialPage: Int = 0,
) {
    val c = SportConnectTheme.colors
    val pager = rememberPagerState(initialPage = initialPage) { OnboardingViewModel.PAGES }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val last = pager.currentPage == OnboardingViewModel.PAGES - 1
    BoxWithConstraints(Modifier.fillMaxSize().background(c.surface).windowInsetsPadding(WindowInsets.safeDrawing)) {
        val card = widthClassOf(maxWidth) != WidthClass.Compact
        Box(Modifier.fillMaxSize(), contentAlignment = if (card) Alignment.Center else Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 560.dp).fillMaxWidth()
                    .then(if (card) Modifier.background(c.surfaceContainerLowest, MaterialTheme.shapes.extraLarge).padding(32.dp) else Modifier.fillMaxSize().padding(16.dp)),
            ) {
                HorizontalPager(
                    state = pager,
                    modifier = (if (card) Modifier.height(460.dp) else Modifier.weight(1f)).testTag("onboarding_pager"),
                    verticalAlignment = Alignment.Top,
                ) { p ->
                    if (p < pages.size) {
                        MessagePage(p, card)
                    } else {
                        SportsPage(selectedSports, onToggleSport, card)
                    }
                }
                Spacer(Modifier.height(16.dp))
                PageDots(pager.currentPage)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (!last) {
                        SCButton(stringResource(Res.string.onboarding_skip), {
                            scope.launch { pager.animateScrollToPage(OnboardingViewModel.PAGES - 1) }
                        }, variant = SCButtonVariant.Text, modifier = Modifier.testTag("onboarding_skip"))
                    }
                    Spacer(Modifier.weight(1f))
                    SCButton(
                        stringResource(if (last) Res.string.onboarding_start else Res.string.onboarding_next),
                        {
                            if (last) {
                                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                onFinish()
                            } else {
                                scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                            }
                        },
                        modifier = Modifier.testTag("onboarding_next"),
                    )
                }
                Spacer(Modifier.height(8.dp))
                SCButton(
                    stringResource(Res.string.onboarding_role_arena),
                    onArena,
                    variant = SCButtonVariant.Text,
                    fullWidth = true,
                    modifier = Modifier.testTag("onboarding_role_arena"),
                )
            }
        }
    }
}

@Composable
private fun MessagePage(p: Int, card: Boolean) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(if (card) 0.dp else 32.dp))
        Box(Modifier.fillMaxWidth().height(if (card) 180.dp else 240.dp), contentAlignment = Alignment.Center) {
            when (p) {
                // Uma ilustracao por mensagem (antes as 3 usavam a mesma quadra).
                0 -> SCIllustrationImage(SCIllustration.Court, Modifier.size(if (card) 180.dp else 240.dp))
                1 -> IllustrationSplit(paid = 8, total = 12)
                else -> IllustrationConfirmed()
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(stringResource(pages[p].first), style = t.headlineLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(12.dp))
        Text(stringResource(pages[p].second), style = t.bodyLarge, color = c.onSurfaceVariant)
    }
}

/** "Cada um paga a sua parte": a propria barra de rateio do app, como ilustracao. */
@Composable
private fun IllustrationSplit(paid: Int, total: Int) {
    val c = SportConnectTheme.colors
    Column(
        Modifier.widthIn(max = 300.dp).fillMaxWidth().background(c.surfaceContainerLowest, MaterialTheme.shapes.extraLarge).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.common_currency_per_person, Money(3_000).format()), style = SportConnectTheme.typographyExt.numericPrice, color = c.onSurface)
        SCSplitProgress(total, paid, variant = SCSplitProgressVariant.Compact)
    }
}

/** "Quadra garantida quando todo mundo paga": barra cheia + confirmado. */
@Composable
private fun IllustrationConfirmed() {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    Column(
        Modifier.widthIn(max = 300.dp).fillMaxWidth().background(c.surfaceContainerLowest, MaterialTheme.shapes.extraLarge).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(Icons.Rounded.CheckCircle, null, tint = ext.success, modifier = Modifier.size(56.dp))
        SCSplitProgress(10, 10, variant = SCSplitProgressVariant.Compact)
    }
}

@Composable
private fun SportsPage(selected: Set<Sport>, onToggle: (Sport) -> Unit, card: Boolean) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val haptics = LocalHapticFeedback.current
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag("onboarding_sports")) {
        Spacer(Modifier.height(if (card) 0.dp else 32.dp))
        Text(stringResource(Res.string.onboarding_sports_title), style = t.headlineLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboarding_sports_body), style = t.bodyLarge, color = c.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Sport.entries.forEach { s ->
                SCSportChip(
                    s, variant = SCSportChipVariant.Filter, selected = s in selected,
                    onClick = { haptics.performHapticFeedback(HapticFeedbackType.SegmentTick); onToggle(s) },
                    modifier = Modifier.testTag("onboarding_sport_${s.name}"),
                )
            }
        }
    }
}

@Composable
private fun PageDots(current: Int) {
    val c = SportConnectTheme.colors
    val total = OnboardingViewModel.PAGES
    Row(
        Modifier.fillMaxWidth().semantics { contentDescription = "Página ${current + 1} de $total" },
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(total) { i ->
            val active = i == current
            val w by animateDpAsState(if (active) 24.dp else 8.dp, tween(SCMotion.Medium1, easing = SCMotion.Standard), label = "dot")
            val color by animateColorAsState(if (active) c.primary else c.outlineVariant, tween(SCMotion.Medium1), label = "dotColor")
            Box(Modifier.height(8.dp).width(w).background(color, CircleShape))
        }
    }
}
