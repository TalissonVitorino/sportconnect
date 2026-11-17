@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package br.com.sportconnect.feature.creategame

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.CourtCardUi
import br.com.sportconnect.core.designsystem.component.PriceLine
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCourtCard
import br.com.sportconnect.core.designsystem.component.SCCourtCardVariant
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdown
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdownVariant
import br.com.sportconnect.core.designsystem.component.SCPriceEmphasis
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.icon.label
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.feature.common.NoticeCard
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.navigation.Route
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.arena_detail_cancellation_policy
import br.com.sportconnect.resources.auth_terms
import br.com.sportconnect.resources.cd_decrease_spots
import br.com.sportconnect.resources.cd_increase_spots
import br.com.sportconnect.resources.cd_info_rule
import br.com.sportconnect.resources.common_close
import br.com.sportconnect.resources.common_continue
import br.com.sportconnect.resources.common_convenience_fee
import br.com.sportconnect.resources.common_edit
import br.com.sportconnect.resources.error_deadline_too_short_body
import br.com.sportconnect.resources.error_deadline_too_short_title
import br.com.sportconnect.resources.error_generic_title
import br.com.sportconnect.resources.error_slot_taken_action
import br.com.sportconnect.resources.error_slot_taken_body
import br.com.sportconnect.resources.error_slot_taken_title
import br.com.sportconnect.resources.game_create_calculating
import br.com.sportconnect.resources.game_create_deadline_helper
import br.com.sportconnect.resources.game_create_deadline_label
import br.com.sportconnect.resources.game_create_deadline_value
import br.com.sportconnect.resources.game_create_empty_slot_cta
import br.com.sportconnect.resources.game_create_exit_body
import br.com.sportconnect.resources.game_create_exit_confirm
import br.com.sportconnect.resources.game_create_exit_keep
import br.com.sportconnect.resources.game_create_exit_title
import br.com.sportconnect.resources.game_create_fee_notice
import br.com.sportconnect.resources.game_create_guarantee_card
import br.com.sportconnect.resources.game_create_guarantee_card_body
import br.com.sportconnect.resources.game_create_guarantee_pix
import br.com.sportconnect.resources.game_create_guarantee_pix_body
import br.com.sportconnect.resources.game_create_guarantee_title
import br.com.sportconnect.resources.game_create_name_label
import br.com.sportconnect.resources.game_create_name_placeholder
import br.com.sportconnect.resources.game_create_pay_and_hold_cta
import br.com.sportconnect.resources.game_create_pay_and_hold_note
import br.com.sportconnect.resources.game_create_resplit_cap_helper
import br.com.sportconnect.resources.game_create_resplit_cap_label
import br.com.sportconnect.resources.game_create_review_court_line
import br.com.sportconnect.resources.game_create_review_deadline
import br.com.sportconnect.resources.game_create_review_each_share
import br.com.sportconnect.resources.game_create_review_pay_now
import br.com.sportconnect.resources.game_create_review_rule
import br.com.sportconnect.resources.game_create_review_spots_line
import br.com.sportconnect.resources.game_create_review_title
import br.com.sportconnect.resources.game_create_review_your_share
import br.com.sportconnect.resources.game_create_rounding_note
import br.com.sportconnect.resources.game_create_rule_body
import br.com.sportconnect.resources.game_create_rule_cancel_body
import br.com.sportconnect.resources.game_create_rule_cancel_example
import br.com.sportconnect.resources.game_create_rule_cancel_title
import br.com.sportconnect.resources.game_create_rule_covers_body
import br.com.sportconnect.resources.game_create_rule_covers_example
import br.com.sportconnect.resources.game_create_rule_covers_title
import br.com.sportconnect.resources.game_create_rule_recommended
import br.com.sportconnect.resources.game_create_rule_resplit_body
import br.com.sportconnect.resources.game_create_rule_resplit_example
import br.com.sportconnect.resources.game_create_rule_resplit_title
import br.com.sportconnect.resources.game_create_rule_title
import br.com.sportconnect.resources.game_create_rule_unavailable
import br.com.sportconnect.resources.game_create_share_min_error
import br.com.sportconnect.resources.game_create_spots_helper
import br.com.sportconnect.resources.game_create_spots_label
import br.com.sportconnect.resources.game_create_spots_range_error
import br.com.sportconnect.resources.game_create_split_preview
import br.com.sportconnect.resources.game_create_sport_label
import br.com.sportconnect.resources.game_create_step_counter
import br.com.sportconnect.resources.game_create_step_court
import br.com.sportconnect.resources.game_create_step_rule
import br.com.sportconnect.resources.game_create_step_split
import br.com.sportconnect.resources.game_create_title
import br.com.sportconnect.resources.split_organizer_exposure
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

data class CreateGameActions(
    val close: () -> Unit = {},
    val pickSlot: () -> Unit = {},
    val goToPay: (String) -> Unit = {},
)

@Composable
fun CreateGameRoute(route: Route.GameCreate, actions: CreateGameActions) {
    val vm: CreateGameViewModel = koinViewModel(key = "create-${route.slotId}") { parametersOf(route) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(vm) {
        vm.events.collect { ev -> if (ev is CreateGameEvent.GoToPay) actions.goToPay(ev.gameId) }
    }
    CreateGameScreen(
        state = state,
        actions = actions,
        onSport = vm::selectSport,
        onName = vm::setName,
        onInc = vm::incrementSpots,
        onDec = vm::decrementSpots,
        onRule = vm::selectRule,
        onGuarantee = vm::selectGuarantee,
        onCap = vm::setCapPct,
        onNext = vm::next,
        onBack = vm::back,
        onGoTo = vm::goTo,
        onDismissError = vm::dismissError,
    )
}

/** Criar jogo: stepper de 3 passos + revisao (10 §2.7, mockup "Criar jogo"). */
@Composable
fun CreateGameScreen(
    state: CreateGameUiState,
    actions: CreateGameActions = CreateGameActions(),
    onSport: (br.com.sportconnect.core.model.Sport) -> Unit = {},
    onName: (String) -> Unit = {},
    onInc: () -> Unit = {},
    onDec: () -> Unit = {},
    onRule: (RuleOption) -> Unit = {},
    onGuarantee: (GarantiaCobertura) -> Unit = {},
    onCap: (Int) -> Unit = {},
    onNext: () -> Unit = {},
    onBack: () -> Boolean = { false },
    onGoTo: (CreateStep) -> Unit = {},
    onDismissError: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    var confirmExit by remember { mutableStateOf(false) }
    val navAction: () -> Unit = {
        if (!onBack()) {
            if (state.hasData) confirmExit = true else actions.close()
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        Scaffold(
            containerColor = c.surface,
            topBar = {
                SCTopBar(
                    stringResource(Res.string.game_create_title),
                    navigation = if (state.step == CreateStep.Court) SCTopBarNavigation.Close else SCTopBarNavigation.Back,
                    onNavigationClick = navAction,
                )
            },
            bottomBar = {
                BottomActionBar {
                    if (state.step == CreateStep.Review) {
                        state.hold?.let {
                            Text(
                                stringResource(Res.string.game_create_pay_and_hold_note, PtBrFormat.relative(it, state.now)),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.onSurfaceVariant,
                            )
                        }
                        SCButton(
                            stringResource(Res.string.game_create_pay_and_hold_cta),
                            onNext,
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            loading = state.submitting,
                            enabled = state.canContinue,
                            modifier = Modifier.testTag("create_pay_and_hold"),
                        )
                        Text(stringResource(Res.string.auth_terms), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                    } else {
                        SCButton(
                            stringResource(Res.string.common_continue),
                            onNext,
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            enabled = state.canContinue,
                            modifier = Modifier.testTag("create_continue"),
                        )
                    }
                }
            },
        ) { padding ->
            val maxW = if (wc == WidthClass.Expanded) 1040.dp else 640.dp
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                Row(Modifier.widthIn(max = maxW).fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(
                        Modifier.weight(7f).fillMaxSize().verticalScroll(rememberScrollState())
                            .padding(horizontal = if (wc == WidthClass.Compact) 16.dp else 24.dp),
                    ) {
                        Stepper(state.step, onGoTo, wide = wc != WidthClass.Compact)
                        Spacer(Modifier.height(16.dp))
                        AnimatedContent(state.step, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "step") { step ->
                            Column {
                                when (step) {
                                    CreateStep.Court -> CourtStep(state, actions, onSport, onName)
                                    CreateStep.Split -> SplitStep(state, onInc, onDec)
                                    CreateStep.Rule -> RuleStep(state, onRule, onGuarantee, onCap)
                                    CreateStep.Review -> ReviewStep(state, onGoTo)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                    if (wc == WidthClass.Expanded && state.rateio != null && state.step != CreateStep.Review) {
                        // Resumo "ao vivo" fixo (10 §2.7 expanded).
                        Column(Modifier.weight(5f).padding(top = 16.dp)) {
                            Column(
                                Modifier.fillMaxWidth().background(c.surfaceContainerLow, MaterialTheme.shapes.large).padding(20.dp),
                            ) {
                                Text(stringResource(Res.string.game_create_review_title), style = MaterialTheme.typography.titleMedium, color = c.onSurface)
                                Spacer(Modifier.height(12.dp))
                                ReviewBreakdown(state)
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmExit) {
        SCDialog(
            title = stringResource(Res.string.game_create_exit_title),
            body = stringResource(Res.string.game_create_exit_body),
            onDismissRequest = { confirmExit = false },
            confirm = SCAction(stringResource(Res.string.game_create_exit_keep), { confirmExit = false }),
            dismiss = SCAction(stringResource(Res.string.game_create_exit_confirm), { confirmExit = false; actions.close() }),
        )
    }
    when (state.error) {
        CreateError.SlotTaken -> SCDialog(
            title = stringResource(Res.string.error_slot_taken_title),
            body = stringResource(Res.string.error_slot_taken_body),
            onDismissRequest = onDismissError,
            confirm = SCAction(stringResource(Res.string.error_slot_taken_action), { onDismissError(); actions.pickSlot() }),
            dismiss = SCAction(stringResource(Res.string.common_close), onDismissError),
        )
        CreateError.DeadlineTooShort -> SCDialog(
            title = stringResource(Res.string.error_deadline_too_short_title),
            body = stringResource(Res.string.error_deadline_too_short_body),
            onDismissRequest = onDismissError,
            confirm = SCAction(stringResource(Res.string.error_slot_taken_action), { onDismissError(); actions.pickSlot() }),
            dismiss = SCAction(stringResource(Res.string.common_close), onDismissError),
        )
        CreateError.Generic, CreateError.CheckoutExpired -> SCDialog(
            title = stringResource(Res.string.error_generic_title),
            body = state.errorMessage,
            variant = SCDialogVariant.Info,
            onDismissRequest = onDismissError,
            confirm = SCAction(stringResource(Res.string.common_close), onDismissError),
        )
        null -> Unit
    }
}

private val stepLabels = listOf(
    Res.string.game_create_step_court,
    Res.string.game_create_step_split,
    Res.string.game_create_step_rule,
    Res.string.game_create_review_title,
)

/** Stepper horizontal: circulos 24dp, conectores 2dp, rotulos labelMedium (10 §2.7). */
@Composable
private fun Stepper(step: CreateStep, onGoTo: (CreateStep) -> Unit, wide: Boolean) {
    val c = SportConnectTheme.colors
    val counter = stringResource(Res.string.game_create_step_counter, step.ordinal + 1, 4, stringResource(stepLabels[step.ordinal]))
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp).testTag("create_step_indicator").semantics(mergeDescendants = true) {
            contentDescription = counter
        },
        verticalAlignment = Alignment.Top,
    ) {
        CreateStep.entries.forEachIndexed { i, s ->
            val done = s.ordinal < step.ordinal
            val current = s == step
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(2.dp).background(if (i == 0) Color.Transparent else if (s.ordinal <= step.ordinal) c.primary else c.outlineVariant))
                    Box(
                        Modifier.size(24.dp)
                            .then(
                                when {
                                    done || current -> Modifier.background(c.primary, CircleShape)
                                    else -> Modifier.border(2.dp, c.outline, CircleShape)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) {
                            Icon(Icons.Rounded.Check, null, tint = c.onPrimary, modifier = Modifier.size(16.dp))
                        } else {
                            Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = if (current) c.onPrimary else c.onSurfaceVariant)
                        }
                    }
                    Box(Modifier.weight(1f).height(2.dp).background(if (i == 3) Color.Transparent else if (s.ordinal < step.ordinal) c.primary else c.outlineVariant))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(stepLabels[i]),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (current) c.onSurface else c.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = if (wide) 1 else 3,
                )
            }
        }
    }
}

@Composable
private fun StepTitle(res: StringResource) {
    Text(stringResource(res), style = MaterialTheme.typography.headlineSmall, color = SportConnectTheme.colors.onSurface, modifier = Modifier.semantics { heading() })
}

@Composable
private fun CourtStep(
    state: CreateGameUiState,
    actions: CreateGameActions,
    onSport: (br.com.sportconnect.core.model.Sport) -> Unit,
    onName: (String) -> Unit,
) {
    val c = SportConnectTheme.colors
    StepTitle(Res.string.game_create_step_court)
    Spacer(Modifier.height(16.dp))
    val slot = state.slot
    val q = state.quadra
    val a = state.arena
    if (state.loading) {
        Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (slot == null || q == null || a == null) {
        SCEmptyState(
            title = stringResource(Res.string.game_create_empty_slot_cta),
            description = stringResource(Res.string.game_create_spots_helper),
            primaryAction = SCAction(stringResource(Res.string.game_create_empty_slot_cta), actions.pickSlot, icon = Icons.Rounded.Search),
        )
        return
    }
    SCCourtCard(
        court = CourtCardUi(
            arenaName = a.nome,
            courtName = q.nome,
            detailsLabel = "${PtBrFormat.dateTimeRange(slot.inicio, slot.fim)} · ${slot.preco.format()}",
            sports = q.esportes.map { it.esporte },
            priceFrom = slot.preco,
            priceFromPrefix = "",
            priceSuffix = "",
        ),
        onClick = actions.pickSlot,
        variant = SCCourtCardVariant.Horizontal,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    Text(stringResource(Res.string.game_create_sport_label), style = MaterialTheme.typography.titleSmall, color = c.onSurface)
    Spacer(Modifier.height(8.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.sports.forEach { sp ->
            SCSportChip(
                sp,
                variant = SCSportChipVariant.Filter,
                selected = state.sport == sp,
                onClick = { onSport(sp) },
                modifier = Modifier.testTag("create_sport_chip_${sp.name.lowercase()}"),
            )
        }
    }
    Spacer(Modifier.height(24.dp))
    SCTextField(
        value = state.name,
        onValueChange = onName,
        label = stringResource(Res.string.game_create_name_label),
        placeholder = stringResource(Res.string.game_create_name_placeholder),
        maxLength = 40,
        modifier = Modifier.fillMaxWidth().testTag("create_name_field"),
    )
}

@Composable
private fun SplitStep(state: CreateGameUiState, onInc: () -> Unit, onDec: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val r = state.rateio ?: return
    Text(stringResource(Res.string.game_create_spots_label), style = t.titleMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        FilledTonalIconButton(
            onClick = onDec,
            enabled = state.spots > state.minSpots,
            modifier = Modifier.size(48.dp).testTag("create_spots_decrease"),
        ) { Icon(Icons.Rounded.Remove, stringResource(Res.string.cd_decrease_spots)) }
        Text(
            state.spots.toString(),
            style = SportConnectTheme.typographyExt.numericScore,
            color = c.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(96.dp).testTag("create_spots_value").semantics { liveRegion = LiveRegionMode.Polite },
        )
        FilledTonalIconButton(
            onClick = onInc,
            enabled = state.spots < state.maxSpots,
            modifier = Modifier.size(48.dp).testTag("create_spots_increase"),
        ) { Icon(Icons.Rounded.Add, stringResource(Res.string.cd_increase_spots)) }
    }
    Spacer(Modifier.height(8.dp))
    Text(stringResource(Res.string.game_create_spots_helper), style = t.bodySmall, color = c.onSurfaceVariant)
    state.sport?.let { sp ->
        Text("${sp.label()}: ${state.minSpots}–${state.maxSpots} vagas", style = t.bodySmall, color = c.onSurfaceVariant)
    }
    when (state.spotsError) {
        SpotsError.Range -> state.sport?.let {
            Text(stringResource(Res.string.game_create_spots_range_error, it.label(), state.minSpots, state.maxSpots), style = t.bodySmall, color = c.error)
        }
        SpotsError.MinShare -> Text(stringResource(Res.string.game_create_share_min_error, Money(500).format()), style = t.bodySmall, color = c.error)
        null -> Unit
    }
    Spacer(Modifier.height(16.dp))
    Column(
        Modifier.fillMaxWidth().background(c.surfaceContainerLow, MaterialTheme.shapes.large).padding(16.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        val preview = stringResource(Res.string.game_create_split_preview, r.valorTotal.format(), r.numeroVagas, r.cotaConvidado.format())
        Text(boldValue(preview, r.cotaConvidado.format()), style = t.titleMedium, color = c.onSurface)
        if (!r.sobra.isZero) {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(Res.string.game_create_rounding_note, r.cotaOrganizador.format(), r.sobra.format()), style = t.bodySmall, color = c.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.game_create_fee_notice, r.taxaConveniencia.format()), style = t.bodySmall, color = c.onSurfaceVariant)
    }
    Spacer(Modifier.height(24.dp))
    Text(stringResource(Res.string.game_create_deadline_label), style = t.titleSmall, color = c.onSurface)
    Spacer(Modifier.height(4.dp))
    val dl = state.deadline
    if (dl == null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.game_create_calculating), style = t.bodyMedium)
        }
    } else {
        Text(stringResource(Res.string.game_create_deadline_value, PtBrFormat.relative(dl, state.now)), style = t.titleMedium, color = c.onSurface)
    }
    Spacer(Modifier.height(4.dp))
    Text(stringResource(Res.string.game_create_deadline_helper), style = t.bodySmall, color = c.onSurfaceVariant)
}

@Composable
private fun boldValue(full: String, value: String) = buildAnnotatedString {
    val i = full.lastIndexOf(value)
    if (i < 0) append(full) else {
        append(full.substring(0, i))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value) }
        append(full.substring(i + value.length))
    }
}

@Composable
private fun RuleStep(
    state: CreateGameUiState,
    onRule: (RuleOption) -> Unit,
    onGuarantee: (GarantiaCobertura) -> Unit,
    onCap: (Int) -> Unit,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    var info by remember { mutableStateOf<RuleOption?>(null) }
    Text(stringResource(Res.string.game_create_rule_title), style = t.headlineSmall, color = c.onSurface, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(4.dp))
    Text(stringResource(Res.string.game_create_rule_body), style = t.bodyMedium, color = c.onSurfaceVariant)
    Spacer(Modifier.height(16.dp))
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RuleCard(
            option = RuleOption.Covers,
            state = state,
            title = stringResource(Res.string.game_create_rule_covers_title),
            recommended = true,
            body = stringResource(Res.string.game_create_rule_covers_body),
            example = stringResource(Res.string.game_create_rule_covers_example, state.twoMissing.format()),
            exampleValue = state.twoMissing.format(),
            onSelect = onRule,
            onInfo = { info = RuleOption.Covers },
            tag = "create_rule_covers",
        ) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = c.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(Res.string.game_create_guarantee_title), style = t.titleSmall, color = c.onSurface)
            Spacer(Modifier.height(4.dp))
            Column(Modifier.selectableGroup()) {
                GuaranteeRow(
                    selected = state.guarantee == GarantiaCobertura.CARTAO,
                    enabled = state.guaranteeViable[GarantiaCobertura.CARTAO] != false,
                    title = stringResource(Res.string.game_create_guarantee_card),
                    recommended = true,
                    body = stringResource(Res.string.game_create_guarantee_card_body, state.exposure.format()),
                    tag = "create_guarantee_card",
                ) { onGuarantee(GarantiaCobertura.CARTAO) }
                GuaranteeRow(
                    selected = state.guarantee == GarantiaCobertura.PIX,
                    enabled = state.guaranteeViable[GarantiaCobertura.PIX] != false,
                    title = stringResource(Res.string.game_create_guarantee_pix),
                    recommended = false,
                    body = if (state.guarantee == GarantiaCobertura.PIX) {
                        stringResource(Res.string.game_create_guarantee_pix_body, state.hold?.let { PtBrFormat.relative(it, state.now) } ?: "")
                    } else null,
                    tag = "create_guarantee_pix",
                ) { onGuarantee(GarantiaCobertura.PIX) }
            }
        }
        RuleCard(
            option = RuleOption.Cancel,
            state = state,
            title = stringResource(Res.string.game_create_rule_cancel_title),
            body = stringResource(Res.string.game_create_rule_cancel_body),
            example = stringResource(Res.string.game_create_rule_cancel_example),
            onSelect = onRule,
            onInfo = { info = RuleOption.Cancel },
            tag = "create_rule_cancel",
        )
        RuleCard(
            option = RuleOption.Resplit,
            state = state,
            title = stringResource(Res.string.game_create_rule_resplit_title),
            body = stringResource(Res.string.game_create_rule_resplit_body),
            example = stringResource(Res.string.game_create_rule_resplit_example, state.resplitPayers, state.resplitExtra.format()),
            exampleValue = state.resplitExtra.format(),
            onSelect = onRule,
            onInfo = { info = RuleOption.Resplit },
            tag = "create_rule_resplit",
        ) {
            Spacer(Modifier.height(12.dp))
            SCTextField(
                value = state.capValue.cents.toString(),
                onValueChange = { v ->
                    val cents = v.toLongOrNull() ?: 0
                    val base = state.rateio?.cotaConvidado?.cents ?: 1
                    onCap(((cents * 100) / base.coerceAtLeast(1)).toInt())
                },
                label = stringResource(Res.string.game_create_resplit_cap_label),
                type = SCTextFieldType.Money,
                supportingText = stringResource(Res.string.game_create_resplit_cap_helper, state.capValue.format()),
                modifier = Modifier.fillMaxWidth().testTag("create_resplit_cap_field"),
            )
        }
    }
    info?.let { opt ->
        val (title, body) = when (opt) {
            RuleOption.Covers -> Res.string.game_create_rule_covers_title to Res.string.game_create_rule_covers_body
            RuleOption.Cancel -> Res.string.game_create_rule_cancel_title to Res.string.game_create_rule_cancel_body
            RuleOption.Resplit -> Res.string.game_create_rule_resplit_title to Res.string.game_create_rule_resplit_body
        }
        SCDialog(
            title = stringResource(title),
            body = stringResource(body),
            icon = Icons.Rounded.Info,
            variant = SCDialogVariant.Info,
            onDismissRequest = { info = null },
            confirm = SCAction(stringResource(Res.string.common_close), { info = null }),
        )
    }
}

@Composable
private fun RecommendedTag() {
    val c = SportConnectTheme.colors
    Text(
        stringResource(Res.string.game_create_rule_recommended),
        style = MaterialTheme.typography.labelMedium,
        color = c.onSecondaryContainer,
        modifier = Modifier.background(c.secondaryContainer, SCShape.badge).padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** Cartao de regra (radio): selecionado = borda 2dp primary + primaryContainer @ 12%. Inviavel = 38% + motivo a 100%. */
@Composable
private fun RuleCard(
    option: RuleOption,
    state: CreateGameUiState,
    title: String,
    body: String,
    example: String,
    onSelect: (RuleOption) -> Unit,
    onInfo: () -> Unit,
    tag: String,
    recommended: Boolean = false,
    exampleValue: String? = null,
    expanded: (@Composable () -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val selected = state.rule == option
    val viable = state.viable[option] != false
    val shape = MaterialTheme.shapes.large
    val container = if (selected) c.primaryContainer.copy(alpha = 0.12f).compositeOverSurface(c.surfaceContainerLowest) else c.surfaceContainerLowest
    Column(
        Modifier.fillMaxWidth()
            .background(container, shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.outlineVariant, shape)
            .selectable(selected = selected, enabled = viable, role = Role.RadioButton) { onSelect(option) }
            .padding(16.dp)
            .testTag(tag),
    ) {
        Column(Modifier.alpha(if (viable) 1f else 0.38f)) {
            Row(verticalAlignment = Alignment.Top) {
                RadioButton(selected = selected, onClick = null, enabled = viable, modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = t.titleMedium, color = c.onSurface)
                    if (recommended) {
                        Spacer(Modifier.height(8.dp))
                        RecommendedTag()
                    }
                }
                IconButton(onClick = onInfo, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Info, stringResource(Res.string.cd_info_rule), tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.padding(start = 36.dp)) {
                Spacer(Modifier.height(8.dp))
                Text(body, style = t.bodyMedium, color = c.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (exampleValue != null) boldValue(example, exampleValue) else buildAnnotatedString { append(example) },
                    style = t.bodyMedium,
                    color = c.onSurface,
                )
            }
        }
        if (!viable) {
            Row(Modifier.padding(start = 36.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, tint = c.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(Res.string.game_create_rule_unavailable, PtBrFormat.shortHours(CreateGameViewModel.minLead(option, state.guarantee))),
                    style = t.bodySmall,
                    color = c.onSurfaceVariant,
                )
            }
        }
        if (selected && viable && expanded != null) {
            AnimatedVisibility(true) { Column(Modifier.padding(start = 36.dp)) { expanded() } }
        }
    }
}

private fun Color.compositeOverSurface(bg: Color): Color {
    val a = alpha
    return Color(
        red = red * a + bg.red * (1 - a),
        green = green * a + bg.green * (1 - a),
        blue = blue * a + bg.blue * (1 - a),
        alpha = 1f,
    )
}

@Composable
private fun GuaranteeRow(
    selected: Boolean,
    enabled: Boolean,
    title: String,
    recommended: Boolean,
    body: String?,
    tag: String,
    onClick: () -> Unit,
) {
    val c = SportConnectTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 6.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .testTag(tag),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f).padding(top = 2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = c.onSurface)
                if (recommended) RecommendedTag()
            }
            if (body != null && selected) {
                Spacer(Modifier.height(2.dp))
                Text(body, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ruleTitle(rule: RuleOption): String = stringResource(
    when (rule) {
        RuleOption.Covers -> Res.string.game_create_rule_covers_title
        RuleOption.Cancel -> Res.string.game_create_rule_cancel_title
        RuleOption.Resplit -> Res.string.game_create_rule_resplit_title
    },
)

@Composable
private fun ReviewBreakdown(state: CreateGameUiState) {
    val r = state.rateio ?: return
    val slot = state.slot ?: return
    val duration = PtBrFormat.shortHours(slot.fim - slot.inicio)
    SCPriceBreakdown(
        lines = listOf(
            PriceLine(stringResource(Res.string.game_create_review_court_line, duration), r.valorTotal),
            PriceLine(stringResource(Res.string.game_create_review_spots_line), valueText = r.numeroVagas.toString()),
            PriceLine(stringResource(Res.string.game_create_review_each_share), r.cotaConvidado, emphasis = SCPriceEmphasis.Muted),
            PriceLine(stringResource(Res.string.game_create_review_your_share), r.cotaOrganizador),
            PriceLine(stringResource(Res.string.common_convenience_fee), r.taxaConveniencia, emphasis = SCPriceEmphasis.Muted),
        ),
        total = PriceLine(stringResource(Res.string.game_create_review_pay_now), state.payNow),
        variant = SCPriceBreakdownVariant.Organizer,
    )
}

@Composable
private fun ReviewStep(state: CreateGameUiState, onGoTo: (CreateStep) -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val slot = state.slot ?: return
    val sport = state.sport ?: return
    Text(stringResource(Res.string.game_create_review_title), style = t.headlineSmall, color = c.onSurface, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(16.dp))
    SCSportChip(sport)
    Spacer(Modifier.height(8.dp))
    Text(state.name.ifBlank { "${sport.label()} · ${state.arena?.nome.orEmpty()}" }, style = t.headlineSmall, color = c.onSurface)
    Text("${state.arena?.nome.orEmpty()} · ${state.quadra?.nome.orEmpty()}", style = t.bodyMedium, color = c.onSurfaceVariant)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(PtBrFormat.dateTimeRange(slot.inicio, slot.fim), style = t.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.weight(1f))
        SCButton(stringResource(Res.string.common_edit), { onGoTo(CreateStep.Court) }, variant = SCButtonVariant.Text, size = SCButtonSize.Small, leadingIcon = Icons.Rounded.Edit)
    }
    Spacer(Modifier.height(16.dp))
    Column(
        Modifier.fillMaxWidth().background(c.surfaceContainerLowest, MaterialTheme.shapes.large)
            .border(1.dp, c.outlineVariant, MaterialTheme.shapes.large).padding(16.dp),
    ) { ReviewBreakdown(state) }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(Res.string.game_create_review_rule, ruleTitle(state.rule)), style = t.titleSmall, color = c.onSurface, modifier = Modifier.weight(1f))
        SCButton(
            stringResource(Res.string.common_edit),
            { onGoTo(CreateStep.Rule) },
            variant = SCButtonVariant.Text,
            size = SCButtonSize.Small,
            modifier = Modifier.testTag("create_review_edit_rule"),
        )
    }
    if (state.rule == RuleOption.Covers) {
        Spacer(Modifier.height(8.dp))
        NoticeCard(stringResource(Res.string.split_organizer_exposure, state.exposure.format()))
    }
    state.deadline?.let {
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.game_create_review_deadline, PtBrFormat.relative(it, state.now)), style = t.bodyMedium, color = c.onSurface)
    }
    Spacer(Modifier.height(16.dp))
    Text(stringResource(Res.string.arena_detail_cancellation_policy), style = t.titleSmall, color = c.onSurface)
    Spacer(Modifier.height(4.dp))
    Text(
        "Cancelamento grátis até ${state.arena?.janelaCancelamentoHoras ?: 24}h antes do jogo. Depois disso, só a arena pode cancelar.",
        style = t.bodyMedium,
        color = c.onSurfaceVariant,
    )
}
