package br.com.sportconnect.feature.arenaadmin.onboarding

import br.com.sportconnect.resources.error_pix_key_invalid_body
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.common.ArenaWidth
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.common_back
import br.com.sportconnect.resources.error_server_body
import br.com.sportconnect.resources.error_server_title
import br.com.sportconnect.resources.aadm_onb_courts_body
import br.com.sportconnect.resources.aadm_onb_error_address
import br.com.sportconnect.resources.aadm_onb_error_cep
import br.com.sportconnect.resources.aadm_onb_error_doc
import br.com.sportconnect.resources.aadm_onb_error_name
import br.com.sportconnect.resources.aadm_onb_error_phone
import br.com.sportconnect.resources.aadm_onb_field_amenities
import br.com.sportconnect.resources.aadm_onb_field_cep
import br.com.sportconnect.resources.aadm_onb_field_doc
import br.com.sportconnect.resources.aadm_onb_field_hours
import br.com.sportconnect.resources.aadm_onb_field_name
import br.com.sportconnect.resources.aadm_onb_field_neighborhood
import br.com.sportconnect.resources.aadm_onb_field_number
import br.com.sportconnect.resources.aadm_onb_field_phone
import br.com.sportconnect.resources.aadm_onb_field_pix
import br.com.sportconnect.resources.aadm_onb_field_street
import br.com.sportconnect.resources.aadm_onb_go_dashboard
import br.com.sportconnect.resources.aadm_onb_hours_placeholder
import br.com.sportconnect.resources.aadm_onb_progress
import br.com.sportconnect.resources.aadm_onb_publish_hint
import br.com.sportconnect.resources.aadm_onb_schedule_body
import br.com.sportconnect.resources.aadm_onb_step_save
import br.com.sportconnect.resources.arena_court_add
import br.com.sportconnect.resources.arena_onb_help
import br.com.sportconnect.resources.arena_onb_publish
import br.com.sportconnect.resources.arena_onb_status_draft
import br.com.sportconnect.resources.arena_onb_status_live
import br.com.sportconnect.resources.arena_onb_step_courts
import br.com.sportconnect.resources.arena_onb_step_done
import br.com.sportconnect.resources.arena_onb_step_info
import br.com.sportconnect.resources.arena_onb_step_pending
import br.com.sportconnect.resources.arena_onb_step_schedule
import br.com.sportconnect.resources.arena_onb_success_body
import br.com.sportconnect.resources.arena_onb_success_title
import br.com.sportconnect.resources.arena_onb_title
import br.com.sportconnect.resources.arena_detail_amenity_bar
import br.com.sportconnect.resources.arena_detail_amenity_lighting
import br.com.sportconnect.resources.arena_detail_amenity_locker
import br.com.sportconnect.resources.arena_detail_amenity_parking
import br.com.sportconnect.resources.error_pix_key_invalid_title
import br.com.sportconnect.resources.payout_bank_account
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

val OnboardingStep.titleRes: StringResource
    get() = when (this) {
        OnboardingStep.Info -> Res.string.arena_onb_step_info
        OnboardingStep.Courts -> Res.string.arena_onb_step_courts
        OnboardingStep.Schedule -> Res.string.arena_onb_step_schedule
        OnboardingStep.Bank -> Res.string.payout_bank_account
    }

/** `arena-admin/onboarding` — Cadastro da arena (10 §2.16). Fluxo transacional: sem navegacao global. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaOnboardingScreen(
    nav: ArenaAdminNavigator,
    viewModel: ArenaOnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            SCTopBar(stringResource(Res.string.arena_onb_title), navigation = SCTopBarNavigation.Close, onNavigationClick = nav::back)
        },
        bottomBar = {
            if (!state.published) {
                Column(Modifier.fillMaxWidth().background(SportConnectTheme.colors.surface)) {
                    HorizontalDivider(color = SportConnectTheme.colors.outlineVariant)
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SCButton(
                            stringResource(Res.string.arena_onb_publish), viewModel::publish, size = SCButtonSize.Large,
                            enabled = state.canPublish, fullWidth = true, modifier = Modifier.testTag("arenaonb_publish"),
                        )
                        if (!state.canPublish) Text(stringResource(Res.string.aadm_onb_publish_hint), style = MaterialTheme.typography.bodySmall, color = SportConnectTheme.colors.onSurfaceVariant)
                    }
                }
            }
        },
        containerColor = SportConnectTheme.colors.surface,
    ) { padding ->
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val width = ArenaWidth.of(maxWidth)
            if (state.load == LoadState.Loading) {
                br.com.sportconnect.core.designsystem.component.SCSkeleton(
                    br.com.sportconnect.core.designsystem.component.SCSkeletonShape.Box(height = 280.dp),
                    Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(16.dp),
                )
            } else if (state.load == LoadState.NoArena || state.load == LoadState.Error) {
                SCEmptyState(
                    stringResource(Res.string.error_server_title),
                    stringResource(Res.string.error_server_body),
                    Modifier.widthIn(max = 640.dp),
                    illustration = SCIllustration.Error,
                    primaryAction = SCAction(stringResource(Res.string.common_back), nav::back),
                )
            } else if (state.published) {
                SCEmptyState(
                    stringResource(Res.string.arena_onb_success_title),
                    stringResource(Res.string.arena_onb_success_body),
                    Modifier.widthIn(max = 640.dp),
                    illustration = SCIllustration.Court,
                    primaryAction = SCAction(stringResource(Res.string.aadm_onb_go_dashboard), nav::finishOnboarding),
                )
            } else if (width == ArenaWidth.Expanded) {
                Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(4f).verticalScroll(rememberScrollState())) { Checklist(state, viewModel, nav, showForms = false) }
                    Column(Modifier.weight(8f).verticalScroll(rememberScrollState())) {
                        state.current?.let { StepForm(it, state, viewModel, nav) }
                    }
                }
            } else {
                Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    Checklist(state, viewModel, nav, showForms = true)
                }
            }
        }
    }
}

@Composable
private fun Checklist(state: ArenaOnboardingUiState, vm: ArenaOnboardingViewModel, nav: ArenaAdminNavigator, showForms: Boolean) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val t = MaterialTheme.typography
    val total = OnboardingStep.entries.size
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val live = state.published
            Text(
                stringResource(if (live) Res.string.arena_onb_status_live else Res.string.arena_onb_status_draft),
                style = t.labelLarge,
                color = if (live) ext.onSuccessContainer else ext.onWarningContainer,
                modifier = Modifier.clip(SCShape.chip).background(if (live) ext.successContainer else ext.warningContainer).padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Text(stringResource(Res.string.aadm_onb_progress, state.progress, total), style = t.bodyMedium, color = c.onSurfaceVariant)
        }
        LinearProgressIndicator(progress = { state.progress / total.toFloat() }, modifier = Modifier.fillMaxWidth(), color = c.primary, trackColor = c.surfaceContainerHighest, drawStopIndicator = {})
        OnboardingStep.entries.forEachIndexed { i, step ->
            val done = step in state.done
            val open = state.current == step
            val stepState = stringResource(if (done) Res.string.arena_onb_step_done else Res.string.arena_onb_step_pending)
            Column(
                Modifier.fillMaxWidth().clip(SCShape.small).background(c.surfaceContainerLowest)
                    .border(if (open) 2.dp else 1.dp, if (open) c.primary else c.outlineVariant, SCShape.small),
            ) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 72.dp).clickable(role = Role.Button) { vm.open(step) }
                        .semantics { stateDescription = stepState }
                        .testTag("arenaonb_step_${i + 1}").padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(32.dp).clip(SCShape.full).background(if (done) ext.successContainer else c.secondaryContainer), contentAlignment = Alignment.Center) {
                        Text("${i + 1}", style = t.titleSmall, color = if (done) ext.onSuccessContainer else c.onSecondaryContainer)
                    }
                    Text(stringResource(step.titleRes), style = t.titleMedium, color = c.onSurface, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, null, tint = if (done) ext.success else c.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Text(stepState, style = t.labelMedium, color = c.onSurfaceVariant)
                    }
                }
                if (showForms && open) {
                    Box(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) { StepForm(step, state, vm, nav) }
                }
            }
        }
        SCButton(stringResource(Res.string.arena_onb_help), {}, variant = SCButtonVariant.Text, modifier = Modifier.testTag("arenaonb_help"))
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun StepForm(step: OnboardingStep, state: ArenaOnboardingUiState, vm: ArenaOnboardingViewModel, nav: ArenaAdminNavigator) {
    val t = MaterialTheme.typography
    val c = SportConnectTheme.colors
    val err = state.showErrors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (step) {
            OnboardingStep.Info -> {
                SCTextField(state.name, vm::setName, stringResource(Res.string.aadm_onb_field_name), Modifier.fillMaxWidth(), errorText = if (err && state.nameError) stringResource(Res.string.aadm_onb_error_name) else null)
                SCTextField(state.document, vm::setDocument, stringResource(Res.string.aadm_onb_field_doc), Modifier.fillMaxWidth(), type = SCTextFieldType.Cpf, placeholder = "000.000.000-00", errorText = if (err && state.documentError) stringResource(Res.string.aadm_onb_error_doc) else null)
                SCTextField(state.telefone, vm::setTelefone, stringResource(Res.string.aadm_onb_field_phone), Modifier.fillMaxWidth(), type = SCTextFieldType.Phone, errorText = if (err && state.telefoneError) stringResource(Res.string.aadm_onb_error_phone) else null)
                SCTextField(state.logradouro, vm::setLogradouro, stringResource(Res.string.aadm_onb_field_street), Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SCTextField(state.numero, vm::setNumero, stringResource(Res.string.aadm_onb_field_number), Modifier.weight(1f))
                    SCTextField(state.bairro, vm::setBairro, stringResource(Res.string.aadm_onb_field_neighborhood), Modifier.weight(2f))
                }
                SCTextField(
                    state.cep, vm::setCep, stringResource(Res.string.aadm_onb_field_cep), Modifier.fillMaxWidth(), type = SCTextFieldType.Cep, placeholder = "00000-000",
                    errorText = if (err && state.addressError) stringResource(Res.string.aadm_onb_error_address) else null,
                )
                Text(stringResource(Res.string.aadm_onb_field_amenities), style = t.titleSmall, color = c.onSurface)
                // Mesmos rotulos que `amenityOf` reconhece com icone no detalhe da arena (E9).
                val amenities = listOf(
                    stringResource(Res.string.arena_detail_amenity_parking), stringResource(Res.string.arena_detail_amenity_locker),
                    stringResource(Res.string.arena_detail_amenity_bar), stringResource(Res.string.arena_detail_amenity_lighting),
                    "Churrasqueira", "Wi-Fi",
                )
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    amenities.forEach { nome ->
                        androidx.compose.material3.FilterChip(
                            selected = nome in state.comodidades,
                            onClick = { vm.toggleComodidade(nome) },
                            label = { Text(nome) },
                            modifier = Modifier.testTag("arenaonb_amenity_$nome"),
                        )
                    }
                }
                SCTextField(
                    state.horario, vm::setHorario, stringResource(Res.string.aadm_onb_field_hours), Modifier.fillMaxWidth(),
                    placeholder = stringResource(Res.string.aadm_onb_hours_placeholder),
                )
            }
            OnboardingStep.Courts -> {
                Text(stringResource(Res.string.aadm_onb_courts_body), style = t.bodyMedium, color = c.onSurfaceVariant)
                SCButton(stringResource(Res.string.arena_court_add), { nav.openCourt(null) }, variant = SCButtonVariant.Secondary)
            }
            OnboardingStep.Schedule -> Text(stringResource(Res.string.aadm_onb_schedule_body), style = t.bodyMedium, color = c.onSurfaceVariant)
            OnboardingStep.Bank -> SCTextField(
                state.pixKey, vm::setPixKey, stringResource(Res.string.aadm_onb_field_pix), Modifier.fillMaxWidth(),
                errorText = if (err && state.pixError) stringResource(Res.string.error_pix_key_invalid_body) else null,
            )
        }
        SCButton(stringResource(Res.string.aadm_onb_step_save), vm::saveStep, modifier = Modifier.testTag("arenaonb_save_${step.name}"))
    }
}
