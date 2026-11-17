@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.arenaadmin.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.SCSelection
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.ArenaReport
import br.com.sportconnect.core.domain.usecase.ArenaReportsUseCase
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.common.observeArenaId
import br.com.sportconnect.feature.common.CenteredContent
import br.com.sportconnect.feature.common.SectionHeader
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.arena_reports_by_court
import br.com.sportconnect.resources.arena_reports_cancelled
import br.com.sportconnect.resources.arena_reports_court_line
import br.com.sportconnect.resources.arena_reports_expired
import br.com.sportconnect.resources.arena_reports_last_days
import br.com.sportconnect.resources.arena_reports_occupancy
import br.com.sportconnect.resources.arena_reports_revenue
import br.com.sportconnect.resources.arena_settings_cancel_help
import br.com.sportconnect.resources.arena_settings_cancel_label
import br.com.sportconnect.resources.arena_settings_hold_help
import br.com.sportconnect.resources.arena_settings_hold_label
import br.com.sportconnect.resources.arena_settings_hours
import br.com.sportconnect.resources.arena_settings_saved
import br.com.sportconnect.resources.arena_settings_team_note
import br.com.sportconnect.resources.arena_settings_title
import br.com.sportconnect.resources.arena_tab_reports
import br.com.sportconnect.resources.cd_decrease
import br.com.sportconnect.resources.cd_increase
import br.com.sportconnect.resources.common_save
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// =================================================================== relatorios (US-063)

data class ArenaReportsUiState(val days: Int = 30, val report: ArenaReport? = null)

class ArenaReportsViewModel(private val reports: ArenaReportsUseCase, private val auth: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ArenaReportsUiState())
    val uiState: StateFlow<ArenaReportsUiState> = _uiState.asStateFlow()

    init { load(30) }

    fun load(days: Int) {
        _uiState.update { it.copy(days = days) }
        viewModelScope.launch {
            val arenaId = auth.observeArenaId().first() ?: return@launch
            _uiState.update { it.copy(report = reports(arenaId, days)) }
        }
    }
}

@Composable
fun ArenaReportsScreen(nav: ArenaAdminNavigator, vm: ArenaReportsViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    ArenaReportsContent(state, nav::back, vm::load)
}

@Composable
fun ArenaReportsContent(state: ArenaReportsUiState, onBack: () -> Unit = {}, onDays: (Int) -> Unit = {}) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Scaffold(containerColor = c.surface, topBar = { SCTopBar(stringResource(Res.string.arena_tab_reports), onNavigationClick = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).testTag("arena_reports")) {
            CenteredContent(Modifier.padding(16.dp)) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf(7, 30).forEachIndexed { i, d ->
                        SegmentedButton(
                            selected = state.days == d, onClick = { onDays(d) },
                            shape = SegmentedButtonDefaults.itemShape(i, 2), colors = SCSelection.segmentedColors(),
                        ) { Text(stringResource(Res.string.arena_reports_last_days, d)) }
                    }
                }
                val r = state.report ?: return@CenteredContent
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Metric(stringResource(Res.string.arena_reports_occupancy), "${r.occupancyPct}%", Modifier.weight(1f))
                    Metric(stringResource(Res.string.arena_reports_revenue), r.netRevenue.format(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Metric(stringResource(Res.string.arena_reports_cancelled), "${r.cancelled}", Modifier.weight(1f))
                    Metric(stringResource(Res.string.arena_reports_expired), "${r.expired}", Modifier.weight(1f))
                }
                SectionHeader(stringResource(Res.string.arena_reports_by_court), Modifier.padding(top = 24.dp, bottom = 8.dp))
                r.courts.forEach { q ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("report_court_${q.courtId}")) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(q.courtName, style = t.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
                            Text("${q.occupancyPct}%", style = SportConnectTheme.typographyExt.numericPriceSmall, color = c.onSurface)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { q.occupancyPct / 100f }, modifier = Modifier.fillMaxWidth(), color = c.primary, trackColor = c.surfaceContainerHighest)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(Res.string.arena_reports_court_line, q.booked, q.offered), style = t.bodySmall, color = c.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    Column(modifier.background(c.surfaceContainerLow, MaterialTheme.shapes.large).padding(16.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = SportConnectTheme.typographyExt.numericPrice, color = c.onSurface)
    }
}

// =================================================================== regras da arena (US-015)

data class ArenaSettingsUiState(val loaded: Boolean = false, val holdHours: Int = 4, val cancelHours: Int = 24, val saved: Boolean = false)

class ArenaSettingsViewModel(private val arenas: ArenaRepository, private val auth: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ArenaSettingsUiState())
    val uiState: StateFlow<ArenaSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val a = auth.observeArenaId().first()?.let { arenas.arena(it) } ?: return@launch
            _uiState.value = ArenaSettingsUiState(true, a.duracaoHoldHoras, a.janelaCancelamentoHoras)
        }
    }

    /** Limites do 09 (arena-admin/settings): pre-reserva 1–48h; cancelamento gratis 0–72h. */
    fun setHold(h: Int) = _uiState.update { it.copy(holdHours = h.coerceIn(1, 48), saved = false) }
    fun setCancel(h: Int) = _uiState.update { it.copy(cancelHours = h.coerceIn(0, 72), saved = false) }

    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            val id = auth.observeArenaId().first() ?: return@launch
            arenas.atualizarArena(id) { it.copy(duracaoHoldHoras = s.holdHours, janelaCancelamentoHoras = s.cancelHours) }
            _uiState.update { it.copy(saved = true) }
        }
    }
}

@Composable
fun ArenaSettingsScreen(nav: ArenaAdminNavigator, vm: ArenaSettingsViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.saved) { if (state.saved) snackbar.showSC(getString(Res.string.arena_settings_saved), SCSnackbarKind.Success) }
    ArenaSettingsContent(state, snackbar, nav::back, vm::setHold, vm::setCancel, vm::save)
}

@Composable
fun ArenaSettingsContent(
    state: ArenaSettingsUiState,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {},
    onHold: (Int) -> Unit = {},
    onCancel: (Int) -> Unit = {},
    onSave: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    Scaffold(
        containerColor = c.surface,
        topBar = { SCTopBar(stringResource(Res.string.arena_settings_title), onNavigationClick = onBack) },
        snackbarHost = { SCSnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).testTag("arena_settings")) {
            CenteredContent(Modifier.padding(16.dp)) {
                HoursStepper(stringResource(Res.string.arena_settings_hold_label), stringResource(Res.string.arena_settings_hold_help), state.holdHours, 1, 48, onHold, "settings_hold")
                Spacer(Modifier.height(24.dp))
                HoursStepper(stringResource(Res.string.arena_settings_cancel_label), stringResource(Res.string.arena_settings_cancel_help), state.cancelHours, 0, 72, onCancel, "settings_cancel")
                Spacer(Modifier.height(24.dp))
                SCButton(stringResource(Res.string.common_save), onSave, size = SCButtonSize.Large, fullWidth = true, enabled = state.loaded, modifier = Modifier.testTag("settings_save"))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.arena_settings_team_note), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HoursStepper(label: String, help: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit, tag: String) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Text(label, style = t.titleMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
    Text(help, style = t.bodyMedium, color = c.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    Row(Modifier.heightIn(min = 56.dp).testTag(tag), verticalAlignment = Alignment.CenterVertically) {
        val dec = stringResource(Res.string.cd_decrease, label)
        val inc = stringResource(Res.string.cd_increase, label)
        IconButton(onClick = { onChange(value - 1) }, enabled = value > min, modifier = Modifier.semantics { contentDescription = dec }) { Icon(Icons.Rounded.Remove, null) }
        Text(stringResource(Res.string.arena_settings_hours, value), style = SportConnectTheme.typographyExt.numericPrice, color = c.onSurface, modifier = Modifier.width(88.dp).testTag("${tag}_value"))
        IconButton(onClick = { onChange(value + 1) }, enabled = value < max, modifier = Modifier.semantics { contentDescription = inc }) { Icon(Icons.Rounded.Add, null) }
    }
}
