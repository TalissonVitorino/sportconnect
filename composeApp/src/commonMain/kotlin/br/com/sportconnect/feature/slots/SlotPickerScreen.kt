@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.slots

import br.com.sportconnect.core.designsystem.a11y.webSelectedLabel
import br.com.sportconnect.core.designsystem.theme.SCSelection
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCTimeSlotPicker
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SlotUi
import br.com.sportconnect.core.designsystem.component.TimeSlotGridSkeleton
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SCSlotState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.feature.common.LoadErrorState
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.navigation.Route
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.common_per_person_estimate
import br.com.sportconnect.resources.slot_duration_label
import br.com.sportconnect.resources.slot_empty_day
import br.com.sportconnect.resources.slot_full_payment_tag
import br.com.sportconnect.resources.slot_hold_notice
import br.com.sportconnect.resources.slot_next_free_day
import br.com.sportconnect.resources.slot_select_cta
import br.com.sportconnect.resources.slot_summary
import br.com.sportconnect.resources.slot_taken_live
import br.com.sportconnect.resources.slot_title
import br.com.sportconnect.resources.slot_too_soon_hint
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.hours

/**
 * Escolher horario (10 §2.6, redline §4.4). [embedded] = dentro da coluna direita de `arena/{id}` em expanded
 * (sem top bar; rodape fixo so na coluna).
 */
@Composable
fun SlotPickerRoute(
    route: Route.SlotPicker,
    onBack: () -> Unit,
    onContinue: (slotId: String) -> Unit,
    embedded: Boolean = false,
) {
    val vm: SlotPickerViewModel = koinViewModel(key = "slots-${route.courtId}-${route.date}") { parametersOf(route) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm) {
        vm.events.collect { if (it == SlotPickerEvent.TakenLive) snackbar.showSC(getString(Res.string.slot_taken_live)) }
    }
    SlotPickerScreen(state, snackbar, onBack, onContinue, vm::selectDay, vm::toggle, embedded)
}

@Composable
fun SlotPickerScreen(
    state: SlotPickerUiState,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {},
    onContinue: (String) -> Unit = {},
    onDay: (kotlinx.datetime.LocalDate) -> Unit = {},
    onToggle: (String) -> Unit = {},
    embedded: Boolean = false,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        Scaffold(
            containerColor = c.surface,
            snackbarHost = { SCSnackbarHost(snackbar) },
            topBar = { if (!embedded) SCTopBar(stringResource(Res.string.slot_title), onNavigationClick = onBack) },
            bottomBar = { Footer(state, onContinue) },
        ) { padding ->
            if (state.error) {
                LoadErrorState(onBack, Modifier.padding(padding))
                return@Scaffold
            }
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                Column(
                    Modifier.widthIn(max = if (wc == WidthClass.Medium) 760.dp else 1040.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = if (wc == WidthClass.Compact) 16.dp else 24.dp, vertical = 8.dp),
                ) {
                    val q = state.quadra
                    val a = state.arena
                    if (q != null && a != null) {
                        Text("${a.nome} · ${q.nome}", style = t.titleSmall, color = c.onSurface)
                        Spacer(Modifier.height(2.dp))
                        val sportNames = q.esportes.map { it.esporte.label() }
                        Text(
                            listOfNotNull(q.piso, if (q.coberta) "Coberta" else null, sportNames.joinToString(" e ")).joinToString(" · "),
                            style = t.bodySmall,
                            color = c.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    // Duracao: a grade das arenas e de 1h (slots horarios); 1h30/2h ficam indisponiveis no MVP.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(Res.string.slot_duration_label), style = t.titleSmall, color = c.onSurface)
                        Spacer(Modifier.width(12.dp))
                        SingleChoiceSegmentedButtonRow {
                            listOf("1h" to true, "1h30" to false, "2h" to false).forEachIndexed { i, (label, enabled) ->
                                SegmentedButton(
                                    selected = i == 0,
                                    onClick = {},
                                    enabled = enabled,
                                    shape = SegmentedButtonDefaults.itemShape(i, 3),
                                    colors = SCSelection.segmentedColors(),
                                    modifier = Modifier.webSelectedLabel(label, i == 0).testTag("slot_duration_${if (i == 0) 60 else if (i == 1) 90 else 120}"),
                                ) { Text(label) }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    val day = state.day
                    if (day != null && state.days.isNotEmpty()) {
                        val cols = when (wc) {
                            WidthClass.Compact -> 2
                            WidthClass.Medium -> 4
                            WidthClass.Expanded -> if (embedded) 4 else 6
                        }
                        if (state.loading) {
                            SCTimeSlotPicker(
                                days = state.days, selectedDay = day, onDaySelected = onDay,
                                slots = emptyList(), selection = emptySet(), onSelectionChange = {}, columns = cols,
                            )
                            TimeSlotGridSkeleton(columns = cols, rows = 4)
                        } else if (state.cells.isEmpty() || state.cells.none { it.state == SlotCellState.Available }) {
                            SCTimeSlotPicker(
                                days = state.days, selectedDay = day, onDaySelected = onDay,
                                slots = emptyList(), selection = emptySet(), onSelectionChange = {}, columns = cols, showLegend = false,
                            )
                            val next = state.days.firstOrNull { it > day }
                            SCEmptyState(
                                title = stringResource(Res.string.slot_empty_day),
                                description = "",
                                primaryAction = next?.let { n -> SCAction(stringResource(Res.string.slot_next_free_day, PtBrFormat.date(n)), { onDay(n) }) },
                            )
                        } else {
                            val ui = state.cells.map { cell ->
                                SlotUi(
                                    id = cell.slot.id,
                                    startLabel = PtBrFormat.hour(cell.slot.inicio),
                                    endLabel = PtBrFormat.hour(cell.slot.fim),
                                    price = cell.slot.preco,
                                    state = when (cell.state) {
                                        SlotCellState.Available -> SCSlotState.Available
                                        SlotCellState.Busy -> SCSlotState.Busy
                                        SlotCellState.OnHold -> SCSlotState.OnHold
                                    },
                                    holdReleasesAtLabel = cell.releasesAt?.let { PtBrFormat.hour(it) },
                                )
                            }
                            SCTimeSlotPicker(
                                days = state.days,
                                selectedDay = day,
                                onDaySelected = onDay,
                                slots = ui,
                                selection = setOfNotNull(state.selection),
                                // Uma quadra = um slot de 1h por jogo no MVP: o toque mais recente vira a selecao.
                                onSelectionChange = { next ->
                                    val added = next - setOfNotNull(state.selection)
                                    when {
                                        added.isNotEmpty() -> onToggle(added.first())
                                        state.selection != null -> onToggle(state.selection)
                                    }
                                },
                                onSelectionRejected = {},
                                maxContiguous = 2,
                                columns = cols,
                                modifier = Modifier.testTag("slot_grid"),
                            )
                            val fullOnly = state.cells.filter { it.fullPaymentOnly }
                            if (fullOnly.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "${stringResource(Res.string.slot_full_payment_tag)}: " + fullOnly.joinToString(", ") { PtBrFormat.hour(it.slot.inicio) },
                                    style = t.labelMedium,
                                    color = c.onTertiaryContainer,
                                    modifier = Modifier.background(c.tertiaryContainer, MaterialTheme.shapes.extraSmall).padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun Footer(state: SlotPickerUiState, onContinue: (String) -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val sel = state.selected
    val q = state.quadra
    BottomActionBar {
        if (sel != null && q != null) {
            Column(Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }.testTag("slot_summary")) {
                Text(
                    stringResource(Res.string.slot_summary, q.nome, "${PtBrFormat.date(PtBrFormat.date(sel.slot.inicio))} · ${PtBrFormat.hour(sel.slot.inicio)}", sel.slot.preco.format()),
                    style = t.titleMedium,
                    color = c.onSurface,
                )
                val esporte = q.esportes.first()
                val spots = esporte.esporte.vagasPadrao.coerceAtMost(esporte.capacidadeMax)
                Text(
                    stringResource(Res.string.common_per_person_estimate, Money(sel.slot.preco.cents / spots).format(), spots),
                    style = t.bodyMedium,
                    color = c.onSurfaceVariant,
                )
            }
        }
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Info, null, tint = c.tertiary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(Res.string.slot_hold_notice, PtBrFormat.shortHours((state.arena?.duracaoHoldHoras ?: 4).hours)),
                style = t.bodySmall,
                color = c.onSurfaceVariant,
            )
        }
        if (sel?.fullPaymentOnly == true) {
            Text(
                stringResource(Res.string.slot_too_soon_hint),
                style = t.bodySmall,
                color = c.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().background(c.surfaceContainer, MaterialTheme.shapes.small).padding(8.dp),
            )
        }
        SCButton(
            stringResource(Res.string.slot_select_cta),
            { sel?.let { onContinue(it.slot.id) } },
            size = SCButtonSize.Large,
            fullWidth = true,
            enabled = sel != null && !sel.fullPaymentOnly,
            modifier = Modifier.testTag("slot_select_cta"),
        )
    }
}

