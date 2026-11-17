package br.com.sportconnect.feature.arenaadmin.courts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.component.formatBrl
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.TipoQuadra
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat
import br.com.sportconnect.feature.arenaadmin.common.ArenaNoArenaState
import br.com.sportconnect.feature.arenaadmin.common.ArenaSectionTitle
import br.com.sportconnect.feature.arenaadmin.common.ArenaWidth
import br.com.sportconnect.feature.arenaadmin.common.ChoiceChips
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_cd_decrease
import br.com.sportconnect.resources.aadm_cd_increase
import br.com.sportconnect.resources.aadm_court_close_label
import br.com.sportconnect.resources.aadm_court_covered
import br.com.sportconnect.resources.aadm_court_days_label
import br.com.sportconnect.resources.aadm_court_duration_label
import br.com.sportconnect.resources.aadm_court_duration_value
import br.com.sportconnect.resources.aadm_court_error_hours
import br.com.sportconnect.resources.aadm_court_error_name
import br.com.sportconnect.resources.aadm_court_error_price
import br.com.sportconnect.resources.aadm_court_heatmap_title
import br.com.sportconnect.resources.aadm_court_new_title
import br.com.sportconnect.resources.aadm_court_open_label
import br.com.sportconnect.resources.aadm_court_peak_end_label
import br.com.sportconnect.resources.aadm_court_peak_start_label
import br.com.sportconnect.resources.aadm_court_price_normal
import br.com.sportconnect.resources.aadm_court_price_peak
import br.com.sportconnect.resources.aadm_court_published
import br.com.sportconnect.resources.aadm_court_surface_label
import br.com.sportconnect.resources.aadm_court_type_label
import br.com.sportconnect.resources.aadm_court_view_grid
import br.com.sportconnect.resources.arena_court_error_overlap
import br.com.sportconnect.resources.arena_court_error_sports
import br.com.sportconnect.resources.arena_court_generated_preview
import br.com.sportconnect.resources.arena_court_name_label
import br.com.sportconnect.resources.arena_court_price_change_note
import br.com.sportconnect.resources.arena_court_price_label
import br.com.sportconnect.resources.arena_court_price_peak_label
import br.com.sportconnect.resources.arena_court_publish_schedule
import br.com.sportconnect.resources.arena_court_saved
import br.com.sportconnect.resources.arena_court_section_court
import br.com.sportconnect.resources.arena_court_section_hours
import br.com.sportconnect.resources.arena_court_section_prices
import br.com.sportconnect.resources.arena_court_sports_label
import br.com.sportconnect.resources.common_save
import br.com.sportconnect.resources.search_list_view
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** `arena-admin/courts/{courtId}` — quadra, funcionamento, precos e publicacao (10 §2.20). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaCourtEditScreen(
    courtId: String?,
    nav: ArenaAdminNavigator,
    viewModel: ArenaCourtEditViewModel = koinViewModel(key = "court-${courtId ?: "new"}") { parametersOf(courtId.orEmpty()) },
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.event) {
        when (state.event) {
            CourtEditEvent.Saved -> { viewModel.consumeEvent(); snackbar.showSC(getString(Res.string.arena_court_saved), SCSnackbarKind.Success) }
            is CourtEditEvent.Published -> { viewModel.consumeEvent(); snackbar.showSC(getString(Res.string.aadm_court_published), SCSnackbarKind.Success) }
            null -> Unit
        }
    }
    Scaffold(
        topBar = {
            SCTopBar(
                title = if (state.isNew) stringResource(Res.string.aadm_court_new_title) else state.name.ifBlank { stringResource(Res.string.aadm_court_new_title) },
                navigation = SCTopBarNavigation.Back,
                onNavigationClick = nav::back,
            )
        },
        bottomBar = {
            if (state.load == LoadState.Ready) {
                Column {
                    HorizontalDivider(color = SportConnectTheme.colors.outlineVariant)
                    Row(Modifier.fillMaxWidth().background(SportConnectTheme.colors.surface).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                        SCButton(stringResource(Res.string.common_save), { viewModel.save() }, variant = SCButtonVariant.Secondary, size = SCButtonSize.Large, modifier = Modifier.testTag("court_save"))
                        SCButton(stringResource(Res.string.arena_court_publish_schedule), viewModel::publish, size = SCButtonSize.Large, loading = state.saving, modifier = Modifier.testTag("court_publish"))
                    }
                }
            }
        },
        snackbarHost = { SCSnackbarHost(snackbar) },
        containerColor = SportConnectTheme.colors.surface,
    ) { padding ->
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val width = ArenaWidth.of(maxWidth)
            when (state.load) {
                LoadState.Loading -> Unit
                LoadState.NoArena, LoadState.Error -> ArenaNoArenaState(nav::openOnboarding)
                LoadState.Ready -> if (width == ArenaWidth.Expanded) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(7f).verticalScroll(rememberScrollState()).padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            CourtSection(state, viewModel)
                            HoursSection(state, viewModel)
                            PriceFields(state, viewModel)
                        }
                        Column(Modifier.weight(5f).verticalScroll(rememberScrollState()).padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            PreviewBlock(state, viewModel)
                        }
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = if (width == ArenaWidth.Compact) 16.dp else 24.dp, vertical = 16.dp)
                            .widthIn(max = 640.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        CourtSection(state, viewModel)
                        HoursSection(state, viewModel)
                        PriceFields(state, viewModel)
                        PreviewBlock(state, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = SportConnectTheme.colors.onSurface, modifier = Modifier.semantics { heading() })
}

/** Erro com icone + texto (nao so vermelho). */
@Composable
private fun FieldError(text: String, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = c.error, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = c.error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourtSection(state: CourtEditUiState, vm: ArenaCourtEditViewModel) {
    val c = SportConnectTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(stringResource(Res.string.arena_court_section_court))
        SCTextField(
            value = state.name, onValueChange = vm::setName, label = stringResource(Res.string.arena_court_name_label),
            errorText = if (state.showErrors && state.nameError) stringResource(Res.string.aadm_court_error_name) else null,
            modifier = Modifier.fillMaxWidth().testTag("court_name_field"),
        )
        ArenaSectionTitle(stringResource(Res.string.aadm_court_type_label))
        ChoiceChips(TipoQuadra.entries, state.tipo, { stringResource(it.labelRes) }, vm::setTipo, tag = { "court_type_${it.name}" })
        SCTextField(value = state.surface, onValueChange = vm::setSurface, label = stringResource(Res.string.aadm_court_surface_label), modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.aadm_court_covered), style = MaterialTheme.typography.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
            Switch(checked = state.covered, onCheckedChange = vm::setCovered, modifier = Modifier.testTag("court_covered"))
        }
        ArenaSectionTitle(stringResource(Res.string.arena_court_sports_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Sport.entries.forEach { s ->
                SCSportChip(s, variant = SCSportChipVariant.Filter, selected = s in state.sports, onClick = { vm.toggleSport(s) }, modifier = Modifier.testTag("court_sport_${s.name}"))
            }
        }
        if (state.showErrors && state.sportsError) FieldError(stringResource(Res.string.arena_court_error_sports))
    }
}

@Composable
private fun HourStepper(label: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier, testTag: String? = null) {
    val c = SportConnectTheme.colors
    val shown = ArenaFormat.hour(value % 24)
    Row(
        modifier.fillMaxWidth().heightIn(min = 56.dp).then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange(value - 1) }) { Icon(Icons.Rounded.Remove, stringResource(Res.string.aadm_cd_decrease, label)) }
        Text(
            shown, style = MaterialTheme.typography.titleMedium, color = c.onSurface, textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp).semantics { contentDescription = "$label $shown" },
        )
        IconButton(onClick = { onChange(value + 1) }) { Icon(Icons.Rounded.Add, stringResource(Res.string.aadm_cd_increase, label)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HoursSection(state: CourtEditUiState, vm: ArenaCourtEditViewModel) {
    val c = SportConnectTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(stringResource(Res.string.arena_court_section_hours))
        ArenaSectionTitle(stringResource(Res.string.aadm_court_days_label))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DayOfWeek.entries.forEach { d ->
                val sel = d in state.schedule.days
                FilterChip(
                    selected = sel,
                    onClick = { vm.toggleDay(d) },
                    label = { Text(ArenaFormat.weekdayShort(d)) },
                    leadingIcon = if (sel) ({ Icon(Icons.Rounded.Check, null, Modifier.size(18.dp)) }) else null,
                    shape = SCShape.chip,
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = c.secondaryContainer, selectedLabelColor = c.onSecondaryContainer),
                    modifier = Modifier.testTag("court_day_${d.name}"),
                )
            }
        }
        HourStepper(stringResource(Res.string.aadm_court_open_label), state.schedule.openHour, vm::setOpen, testTag = "court_open")
        HourStepper(stringResource(Res.string.aadm_court_close_label), state.schedule.closeHour, vm::setClose, testTag = "court_close")
        if (state.hoursError) FieldError(stringResource(Res.string.aadm_court_error_hours))
        ArenaSectionTitle(stringResource(Res.string.aadm_court_duration_label))
        ChoiceChips(listOf(60, 90), state.schedule.durationMinutes, { stringResource(Res.string.aadm_court_duration_value, it) }, vm::setDuration, tag = { "court_duration_$it" })
    }
}

@Composable
private fun PriceFields(state: CourtEditUiState, vm: ArenaCourtEditViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(stringResource(Res.string.arena_court_section_prices))
        val priceErr = if (state.showErrors && state.priceError) stringResource(Res.string.aadm_court_error_price) else null
        SCTextField(state.priceDigits, vm::setPrice, stringResource(Res.string.arena_court_price_label), Modifier.fillMaxWidth().testTag("court_price_field"), type = SCTextFieldType.Money, errorText = priceErr.takeIf { state.price.cents <= 0 })
        SCTextField(state.peakPriceDigits, vm::setPeakPrice, stringResource(Res.string.arena_court_price_peak_label), Modifier.fillMaxWidth().testTag("court_peak_price_field"), type = SCTextFieldType.Money, errorText = priceErr.takeIf { state.peakPrice.cents <= 0 })
        HourStepper(stringResource(Res.string.aadm_court_peak_start_label), state.schedule.peakStartHour, vm::setPeakStart)
        HourStepper(stringResource(Res.string.aadm_court_peak_end_label), state.schedule.peakEndHour, vm::setPeakEnd)
        if (state.overlapError) FieldError(stringResource(Res.string.arena_court_error_overlap))
        Text(stringResource(Res.string.arena_court_price_change_note), style = MaterialTheme.typography.bodySmall, color = SportConnectTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun PreviewBlock(state: CourtEditUiState, vm: ArenaCourtEditViewModel) {
    val c = SportConnectTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader(stringResource(Res.string.aadm_court_heatmap_title))
            Box(Modifier.weight(1f))
            TextButton(onClick = vm::toggleHeatmapView) {
                Text(stringResource(if (state.heatmapAsList) Res.string.aadm_court_view_grid else Res.string.search_list_view))
            }
        }
        if (state.heatmapAsList) PriceTable(state) else Heatmap(state)
        Text(
            stringResource(Res.string.arena_court_generated_preview, state.previewCount(vm.today()), CourtEditUiState.PREVIEW_DAYS),
            style = MaterialTheme.typography.bodyMedium,
            color = c.onTertiaryContainer,
            modifier = Modifier.fillMaxWidth().clip(SCShape.control).background(c.tertiaryContainer).padding(16.dp).testTag("court_preview"),
        )
    }
}

/** Grade de calor dia x hora: 4 tons de primaryContainer (fechado, normal, pico) + legenda textual. */
@Composable
private fun Heatmap(state: CourtEditUiState) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val cells = state.heatmap()
    val maxPrice = cells.filter { it.open }.maxOfOrNull { it.price.cents }?.coerceAtLeast(1) ?: 1
    val desc = "${stringResource(Res.string.aadm_court_price_normal)} ${state.price.formatBrl()}, ${stringResource(Res.string.aadm_court_price_peak)} ${state.peakPrice.formatBrl()}"
    Column(Modifier.fillMaxWidth().testTag("court_heatmap").clearAndSetSemantics { contentDescription = desc }, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row {
            Box(Modifier.width(36.dp))
            (CourtEditUiState.HEAT_FIRST..CourtEditUiState.HEAT_LAST).forEach { h ->
                Text(if (h % 3 == 0) "$h" else "", style = t.labelSmall, color = c.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        DayOfWeek.entries.forEach { d ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ArenaFormat.weekdayShort(d), style = t.labelMedium, color = c.onSurfaceVariant, modifier = Modifier.width(36.dp))
                cells.filter { it.day == d }.forEach { cell ->
                    // 4 tons de primaryContainer (24/48/72/100%): normal = 48%, pico = 100%.
                    val alpha = when {
                        !cell.open -> 0f
                        cell.peak -> 1f
                        cell.price.cents * 2 > maxPrice -> 0.48f
                        else -> 0.24f
                    }
                    Box(
                        Modifier.weight(1f).height(20.dp).padding(1.dp).clip(SCShape.badge)
                            .background(if (cell.open) c.primaryContainer.copy(alpha = alpha) else c.surfaceContainerHigh),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
            Legend(c.primaryContainer.copy(alpha = 0.48f), "${stringResource(Res.string.aadm_court_price_normal)} · ${state.price.formatBrl()}")
            Legend(c.primaryContainer, "${stringResource(Res.string.aadm_court_price_peak)} · ${state.peakPrice.formatBrl()}")
        }
    }
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(14.dp).clip(SCShape.badge).background(color))
        Text(text, style = MaterialTheme.typography.labelMedium, color = SportConnectTheme.colors.onSurfaceVariant)
    }
}

/** Alternativa em tabela da grade de calor (acessivel). */
@Composable
private fun PriceTable(state: CourtEditUiState) {
    val t = MaterialTheme.typography
    val c = SportConnectTheme.colors
    val s = state.schedule
    Column(Modifier.testTag("court_price_table"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DayOfWeek.entries.forEach { d ->
            val text = if (d !in s.days) "—" else buildString {
                append("${ArenaFormat.hour(s.openHour)}–${ArenaFormat.hour(s.peakStartHour)}: ${state.price.formatBrl()}")
                append(" · ${ArenaFormat.hour(s.peakStartHour)}–${ArenaFormat.hour(s.peakEndHour % 24)}: ${state.peakPrice.formatBrl()}")
            }
            Row {
                Text(ArenaFormat.weekdayShort(d), style = t.labelLarge, color = c.onSurface, modifier = Modifier.width(48.dp))
                Text(text, style = t.bodyMedium, color = c.onSurfaceVariant)
            }
        }
    }
}
