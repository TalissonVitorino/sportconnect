@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package br.com.sportconnect.feature.onboarding

import br.com.sportconnect.resources.error_location_denied_title
import br.com.sportconnect.resources.onboarding_location_city_placeholder
import br.com.sportconnect.resources.onboarding_location_city_label
import br.com.sportconnect.resources.onboarding_location_city_unknown
import br.com.sportconnect.resources.onboarding_location_city_set
import br.com.sportconnect.resources.onboarding_location_unavailable
import br.com.sportconnect.resources.error_location_denied_body
import br.com.sportconnect.resources.onboarding_location_found
import br.com.sportconnect.resources.onboarding_location_locating
import br.com.sportconnect.resources.onboarding_location_manual
import br.com.sportconnect.resources.onboarding_location_allow
import br.com.sportconnect.resources.onboarding_location_body
import br.com.sportconnect.resources.onboarding_location_title
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCTextField
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.LocationOn
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.platform.rememberLocationRequest
import br.com.sportconnect.core.platform.deviceLocationSupported
import br.com.sportconnect.core.platform.LocationResult
import br.com.sportconnect.core.platform.KnownCities
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
/** Estado da etapa de localizacao (B10). */
sealed interface LocationUi {
    data object Idle : LocationUi
    data object Locating : LocationUi
    data object Found : LocationUi
    data object Denied : LocationUi
    data object Unavailable : LocationUi
    data class CitySet(val city: String) : LocationUi
    data class CityUnknown(val city: String) : LocationUi
}

class OnboardingViewModel(
    private val session: UiSessionStore,
    private val auth: AuthRepository,
    private val prefs: AppPreferences,
    private val analytics: AnalyticsTracker,
) : ViewModel() {
    private val _location = MutableStateFlow<LocationUi>(if (prefs.searchOrigin != null) LocationUi.Found else LocationUi.Idle)
    val location: StateFlow<LocationUi> = _location.asStateFlow()
    private val _city = MutableStateFlow(prefs.searchOrigin?.city.orEmpty())
    val city: StateFlow<String> = _city.asStateFlow()

    fun startLocating() { _location.value = LocationUi.Locating }

    fun onLocationResult(result: LocationResult) {
        _location.value = when (result) {
            is LocationResult.Found -> {
                prefs.searchOrigin = AppPreferences.SavedOrigin(result.point.latitude, result.point.longitude, null)
                analytics.track(AnalyticsEvents.CONSENTIMENTO_ATUALIZADO, mapOf("tipo_consentimento" to "localizacao", "valor" to true))
                LocationUi.Found
            }
            LocationResult.Denied -> {
                analytics.track(AnalyticsEvents.CONSENTIMENTO_ATUALIZADO, mapOf("tipo_consentimento" to "localizacao", "valor" to false))
                LocationUi.Denied
            }
            LocationResult.Unavailable -> LocationUi.Unavailable
        }
    }

    fun setCity(text: String) { _city.value = text.take(40) }

    /** Usa a cidade digitada: centro conhecido; se nao atendemos ainda, guarda o nome e busca em SP. */
    fun applyCity(choice: String = _city.value) {
        val typed = choice.trim()
        if (typed.isEmpty()) return
        val known = KnownCities.match(typed).firstOrNull()
        val point = known?.let { KnownCities.all.getValue(it) } ?: KnownCities.all.getValue("São Paulo")
        prefs.searchOrigin = AppPreferences.SavedOrigin(point.latitude, point.longitude, known ?: typed)
        _city.value = known ?: typed
        _location.value = if (known != null) LocationUi.CitySet(known) else LocationUi.CityUnknown(typed)
    }
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

    companion object { const val PAGES = 5 }
}

data class OnboardingActions(
    val finished: (loggedIn: Boolean) -> Unit = {},
    val arena: () -> Unit = {},
)

@Composable
fun OnboardingRoute(actions: OnboardingActions, vm: OnboardingViewModel = koinViewModel()) {
    val sports by vm.sports.collectAsStateWithLifecycle()
    val location by vm.location.collectAsStateWithLifecycle()
    val city by vm.city.collectAsStateWithLifecycle()
    val requestLocation = rememberLocationRequest(vm::onLocationResult)
    OnboardingScreen(
        selectedSports = sports,
        onToggleSport = vm::toggle,
        location = location,
        city = city,
        onUseLocation = { vm.startLocating(); requestLocation() },
        onCity = vm::setCity,
        onApplyCity = vm::applyCity,
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
    location: LocationUi = LocationUi.Idle,
    city: String = "",
    onUseLocation: () -> Unit = {},
    onCity: (String) -> Unit = {},
    onApplyCity: (String) -> Unit = {},
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
                    when {
                        p < pages.size -> MessagePage(p, card)
                        p == pages.size -> SportsPage(selectedSports, onToggleSport, card)
                        else -> LocationPage(location, city, onUseLocation, onCity, onApplyCity, card)
                    }
                }
                Spacer(Modifier.height(16.dp))
                PageDots(pager.currentPage)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // "Pular" so aparece nas mensagens e leva a 1a etapa de configuracao (esportes).
                    if (pager.currentPage < pages.size) {
                        SCButton(stringResource(Res.string.onboarding_skip), {
                            scope.launch { pager.animateScrollToPage(pages.size) }
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

/** B10: "Encontre quadras perto de voce" (09 ficha 1), com finalidade explicita (LGPD) e cidade digitada. */
@Composable
private fun LocationPage(
    location: LocationUi,
    city: String,
    onUseLocation: () -> Unit,
    onCity: (String) -> Unit,
    onApplyCity: (String) -> Unit,
    card: Boolean,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    var manual by rememberSaveable { mutableStateOf(!deviceLocationSupported) }
    val showManual = manual || location == LocationUi.Denied || location == LocationUi.Unavailable
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag("onboarding_location")) {
        Spacer(Modifier.height(if (card) 0.dp else 32.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(96.dp).background(c.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.LocationOn, null, tint = c.onPrimaryContainer, modifier = Modifier.size(48.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(Res.string.onboarding_location_title), style = t.headlineLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.onboarding_location_body), style = t.bodyLarge, color = c.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        if (deviceLocationSupported) {
            SCButton(
                stringResource(Res.string.onboarding_location_allow),
                onUseLocation,
                variant = SCButtonVariant.Tonal,
                leadingIcon = Icons.Rounded.MyLocation,
                loading = location == LocationUi.Locating,
                fullWidth = true,
                modifier = Modifier.testTag("onboarding_location_allow"),
            )
            if (!showManual) {
                SCButton(
                    stringResource(Res.string.onboarding_location_manual), { manual = true },
                    variant = SCButtonVariant.Text, fullWidth = true, modifier = Modifier.testTag("onboarding_location_manual"),
                )
            }
        }
        val status = when (location) {
            LocationUi.Locating -> stringResource(Res.string.onboarding_location_locating)
            LocationUi.Found -> stringResource(Res.string.onboarding_location_found)
            LocationUi.Denied -> stringResource(Res.string.error_location_denied_title) + ". " + stringResource(Res.string.error_location_denied_body)
            LocationUi.Unavailable -> if (deviceLocationSupported) stringResource(Res.string.onboarding_location_unavailable) else null
            is LocationUi.CitySet -> stringResource(Res.string.onboarding_location_city_set, location.city)
            is LocationUi.CityUnknown -> stringResource(Res.string.onboarding_location_city_unknown, location.city)
            LocationUi.Idle -> null
        }
        if (status != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                status,
                style = t.bodyMedium,
                color = if (location == LocationUi.Denied) c.error else c.onSurfaceVariant,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("onboarding_location_status"),
            )
        }
        AnimatedVisibility(showManual) {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                SCTextField(
                    value = city,
                    onValueChange = onCity,
                    label = stringResource(Res.string.onboarding_location_city_label),
                    placeholder = stringResource(Res.string.onboarding_location_city_placeholder),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onApplyCity(city) }),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_city_field"),
                )
                val suggestions = KnownCities.match(city).filter { it != city }
                if (suggestions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.take(4).forEach { s ->
                            SCButton(s, { onApplyCity(s) }, variant = SCButtonVariant.Secondary, size = SCButtonSize.Small)
                        }
                    }
                }
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
