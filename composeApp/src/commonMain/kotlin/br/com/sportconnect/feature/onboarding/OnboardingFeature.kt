@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCIllustrationImage
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.feature.common.UiSessionStore
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.onboarding_1_body
import br.com.sportconnect.resources.onboarding_1_title
import br.com.sportconnect.resources.onboarding_2_body
import br.com.sportconnect.resources.onboarding_2_title
import br.com.sportconnect.resources.onboarding_3_body
import br.com.sportconnect.resources.onboarding_3_title
import br.com.sportconnect.resources.onboarding_next
import br.com.sportconnect.resources.onboarding_role_arena
import br.com.sportconnect.resources.onboarding_skip
import br.com.sportconnect.resources.onboarding_start
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource

class OnboardingViewModel(private val session: UiSessionStore, private val auth: AuthRepository) : ViewModel() {
    private val _page = MutableStateFlow(0)
    val page: StateFlow<Int> = _page.asStateFlow()

    fun next() { if (_page.value < PAGES - 1) _page.value += 1 }
    fun back() { if (_page.value > 0) _page.value -= 1 }

    /** Conclui; retorna true se ja ha sessao (vai para a Home) ou false (vai para `auth`). */
    fun finish(): Boolean {
        session.finishOnboarding()
        return auth.currentUser.value != null
    }

    companion object { const val PAGES = 3 }
}

data class OnboardingActions(
    val finished: (loggedIn: Boolean) -> Unit = {},
    val arena: () -> Unit = {},
)

@Composable
fun OnboardingRoute(actions: OnboardingActions, vm: OnboardingViewModel = koinViewModel()) {
    val page by vm.page.collectAsStateWithLifecycle()
    OnboardingScreen(
        page = page,
        onNext = { if (page == OnboardingViewModel.PAGES - 1) actions.finished(vm.finish()) else vm.next() },
        onSkip = { actions.finished(vm.finish()) },
        onArena = { vm.finish(); actions.arena() },
    )
}

private val pages = listOf(
    Res.string.onboarding_1_title to Res.string.onboarding_1_body,
    Res.string.onboarding_2_title to Res.string.onboarding_2_body,
    Res.string.onboarding_3_title to Res.string.onboarding_3_body,
)

/** Boas-vindas (10 §2.1): 3 telas curtas, sem avanco automatico. Medium/expanded: cartao central de 560dp. */
@Composable
fun OnboardingScreen(page: Int, onNext: () -> Unit = {}, onSkip: () -> Unit = {}, onArena: () -> Unit = {}) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    BoxWithConstraints(Modifier.fillMaxSize().background(c.surface).windowInsetsPadding(WindowInsets.safeDrawing)) {
        val wc = widthClassOf(maxWidth)
        val card = wc != WidthClass.Compact
        Box(Modifier.fillMaxSize(), contentAlignment = if (card) Alignment.Center else Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 560.dp).fillMaxWidth()
                    .then(if (card) Modifier.background(c.surfaceContainerLowest, MaterialTheme.shapes.extraLarge).padding(32.dp) else Modifier.fillMaxSize().padding(16.dp)),
            ) {
                AnimatedContent(
                    page,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        slideInHorizontally { it * dir / 4 } togetherWith slideOutHorizontally { -it * dir / 4 }
                    },
                    label = "onboarding",
                    modifier = if (card) Modifier else Modifier.weight(1f),
                ) { p ->
                    Column(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(if (card) 0.dp else 40.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SCIllustrationImage(SCIllustration.Court, Modifier.size(if (card) 180.dp else 240.dp))
                        }
                        Spacer(Modifier.height(32.dp))
                        Text(stringResource(pages[p].first), style = t.headlineLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(pages[p].second), style = t.bodyLarge, color = c.onSurfaceVariant, maxLines = 3)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth().semantics { contentDescription = "Página ${page + 1} de 3" },
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    repeat(3) { i ->
                        Box(
                            Modifier.height(8.dp).width(if (i == page) 24.dp else 8.dp)
                                .background(if (i == page) c.primary else c.outlineVariant, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SCButton(stringResource(Res.string.onboarding_skip), onSkip, variant = SCButtonVariant.Text, modifier = Modifier.testTag("onboarding_skip"))
                    Spacer(Modifier.weight(1f))
                    SCButton(
                        stringResource(if (page == 2) Res.string.onboarding_start else Res.string.onboarding_next),
                        onNext,
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
