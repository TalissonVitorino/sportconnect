@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package br.com.sportconnect.feature.account

import br.com.sportconnect.resources.card_secure_note
import br.com.sportconnect.resources.card_remove
import br.com.sportconnect.resources.card_saved_expiry
import br.com.sportconnect.resources.card_saved_item
import androidx.compose.material.icons.rounded.CreditCard
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.resources.profile_photo_remove
import br.com.sportconnect.resources.profile_photo_change
import br.com.sportconnect.resources.profile_photo_add
import androidx.compose.material.icons.rounded.AddAPhoto
import br.com.sportconnect.core.designsystem.component.AvatarUi
import br.com.sportconnect.core.designsystem.component.SCAvatar
import br.com.sportconnect.feature.common.LocalPickProfilePhoto
import org.koin.compose.koinInject
import br.com.sportconnect.core.data.local.ProfilePhotoStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.feature.auth.LegalDoc
import br.com.sportconnect.feature.auth.OtpField
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.feature.common.CenteredContent
import br.com.sportconnect.feature.common.SectionHeader
import br.com.sportconnect.feature.common.rememberShareActions
import br.com.sportconnect.feature.common.urlEncode
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.auth_code_body
import br.com.sportconnect.resources.auth_code_resend
import br.com.sportconnect.resources.auth_code_resend_in
import br.com.sportconnect.resources.auth_send_code
import br.com.sportconnect.resources.auth_terms_link_privacy
import br.com.sportconnect.resources.auth_terms_link_terms
import br.com.sportconnect.resources.cards_empty_body
import br.com.sportconnect.resources.cards_empty_title
import br.com.sportconnect.resources.common_cancel
import br.com.sportconnect.resources.common_copied
import br.com.sportconnect.resources.common_copy
import br.com.sportconnect.resources.common_save
import br.com.sportconnect.resources.delete_code_title
import br.com.sportconnect.resources.delete_done_body_now
import br.com.sportconnect.resources.delete_done_body_scheduled
import br.com.sportconnect.resources.delete_done_cta
import br.com.sportconnect.resources.delete_done_title
import br.com.sportconnect.resources.delete_pending_body
import br.com.sportconnect.resources.delete_pending_title
import br.com.sportconnect.resources.delete_point_data
import br.com.sportconnect.resources.delete_point_games
import br.com.sportconnect.resources.delete_point_payments
import br.com.sportconnect.resources.delete_what_happens
import br.com.sportconnect.resources.error_code_expired_body
import br.com.sportconnect.resources.error_code_invalid_body
import br.com.sportconnect.resources.error_phone_invalid_body
import br.com.sportconnect.resources.error_too_many_attempts_body
import br.com.sportconnect.resources.onboarding_sports_body
import br.com.sportconnect.resources.privacy_delete_body
import br.com.sportconnect.resources.privacy_download_body
import br.com.sportconnect.resources.privacy_download_hint
import br.com.sportconnect.resources.privacy_download_ready
import br.com.sportconnect.resources.privacy_marketing_body
import br.com.sportconnect.resources.privacy_marketing_title
import br.com.sportconnect.resources.privacy_section_consents
import br.com.sportconnect.resources.privacy_section_documents
import br.com.sportconnect.resources.privacy_section_your_data
import br.com.sportconnect.resources.auth_name_label
import br.com.sportconnect.resources.profile_delete_account
import br.com.sportconnect.resources.profile_delete_confirm
import br.com.sportconnect.resources.profile_delete_dialog_body
import br.com.sportconnect.resources.profile_delete_dialog_title
import br.com.sportconnect.resources.profile_download_data
import br.com.sportconnect.resources.profile_edit_title
import br.com.sportconnect.resources.profile_email_helper
import br.com.sportconnect.resources.profile_email_invalid
import br.com.sportconnect.resources.profile_email_label
import br.com.sportconnect.resources.profile_phone_change
import br.com.sportconnect.resources.profile_phone_changed
import br.com.sportconnect.resources.profile_phone_label
import br.com.sportconnect.resources.profile_phone_new_label
import br.com.sportconnect.resources.profile_privacy
import br.com.sportconnect.resources.profile_saved
import br.com.sportconnect.resources.profile_saved_cards
import br.com.sportconnect.resources.profile_sports
import br.com.sportconnect.resources.profile_support
import br.com.sportconnect.resources.auth_name_too_short
import br.com.sportconnect.resources.support_a1
import br.com.sportconnect.resources.support_a2
import br.com.sportconnect.resources.support_a3
import br.com.sportconnect.resources.support_a4
import br.com.sportconnect.resources.support_contact_title
import br.com.sportconnect.resources.support_email
import br.com.sportconnect.resources.support_faq_title
import br.com.sportconnect.resources.support_hours
import br.com.sportconnect.resources.support_q1
import br.com.sportconnect.resources.support_q2
import br.com.sportconnect.resources.support_q3
import br.com.sportconnect.resources.support_q4
import br.com.sportconnect.resources.support_whatsapp
import br.com.sportconnect.resources.support_whatsapp_message
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Canais de suporte. Trocar pelos oficiais antes do lancamento (numero do WhatsApp Business e e-mail). */
object SupportContacts {
    const val WHATSAPP_URL = "https://wa.me/5511000000000"
    const val EMAIL = "suporte@sportconnect.app"
}

// =================================================================== privacidade e dados

@Composable
fun PrivacyRoute(
    onBack: () -> Unit,
    onOpenLegal: (LegalDoc) -> Unit,
    onDeleteAccount: () -> Unit,
    vm: PrivacyViewModel = koinViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    PrivacyScreen(state, onBack, vm::setMarketing, onOpenLegal, vm::export, vm::dismissExport, onDeleteAccount)
}

/** Privacidade e dados (09 ficha 16, US-003, RNF-11). */
@Composable
fun PrivacyScreen(
    state: PrivacyUiState,
    onBack: () -> Unit = {},
    onMarketing: (Boolean) -> Unit = {},
    onOpenLegal: (LegalDoc) -> Unit = {},
    onExport: () -> Unit = {},
    onDismissExport: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val share = rememberShareActions()
    val snackbar = remember { SnackbarHostState() }
    val copiedMsg = stringResource(Res.string.common_copied)
    var copyRequest by remember { mutableStateOf(0) }
    LaunchedEffect(copyRequest) { if (copyRequest > 0) snackbar.showSC(copiedMsg, SCSnackbarKind.Success) }

    state.exported?.let { json ->
        SCDialog(
            title = stringResource(Res.string.privacy_download_ready),
            body = stringResource(Res.string.privacy_download_hint),
            onDismissRequest = onDismissExport,
            confirm = SCAction(stringResource(Res.string.common_copy), { share.copy(json); onDismissExport(); copyRequest++ }, icon = Icons.Rounded.ContentCopy),
            dismiss = SCAction(stringResource(Res.string.common_cancel), onDismissExport),
            modifier = Modifier.testTag("privacy_export_dialog"),
        )
    }

    Scaffold(
        containerColor = c.surface,
        topBar = { SCTopBar(stringResource(Res.string.profile_privacy), onNavigationClick = onBack) },
        snackbarHost = { SCSnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(stringResource(Res.string.privacy_section_consents), Modifier.padding(top = 8.dp, bottom = 4.dp))
                val marketingTitle = stringResource(Res.string.privacy_marketing_title)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 72.dp)
                        .clickable(role = Role.Switch) { onMarketing(!state.marketing) }
                        .testTag("privacy_marketing"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(marketingTitle, style = t.bodyLarge, color = c.onSurface)
                        Text(stringResource(Res.string.privacy_marketing_body), style = t.bodyMedium, color = c.onSurfaceVariant)
                    }
                    Switch(checked = state.marketing, onCheckedChange = null)
                }
                HorizontalDivider(color = c.outlineVariant)

                SectionHeader(stringResource(Res.string.privacy_section_documents), Modifier.padding(top = 20.dp, bottom = 4.dp))
                LinkRow(stringResource(Res.string.auth_terms_link_terms), "privacy_terms") { onOpenLegal(LegalDoc.Terms) }
                LinkRow(stringResource(Res.string.auth_terms_link_privacy), "privacy_policy") { onOpenLegal(LegalDoc.Privacy) }
                HorizontalDivider(color = c.outlineVariant)

                SectionHeader(stringResource(Res.string.privacy_section_your_data), Modifier.padding(top = 20.dp, bottom = 4.dp))
                ActionRow(
                    icon = Icons.Rounded.Download,
                    title = stringResource(Res.string.profile_download_data),
                    body = stringResource(Res.string.privacy_download_body),
                    tag = "privacy_download",
                    loading = state.exporting,
                    onClick = onExport,
                )
                ActionRow(
                    icon = Icons.Rounded.DeleteForever,
                    title = stringResource(Res.string.profile_delete_account),
                    body = stringResource(Res.string.privacy_delete_body),
                    tag = "privacy_delete",
                    destructive = true,
                    onClick = onDeleteAccount,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LinkRow(label: String, tag: String, onClick: () -> Unit) {
    val c = SportConnectTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button, onClick = onClick).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = c.onSurfaceVariant)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    body: String,
    tag: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
    loading: Boolean = false,
) {
    val c = SportConnectTheme.colors
    val color = if (destructive) c.error else c.onSurface
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp).clickable(enabled = !loading, role = Role.Button, onClick = onClick).testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (destructive) c.error else c.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = color)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = c.onSurfaceVariant)
    }
}

// =================================================================== excluir conta

@Composable
fun DeleteAccountRoute(onBack: () -> Unit, onFinished: () -> Unit, vm: DeleteAccountViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    DeleteAccountScreen(state, onBack, vm::requestCode, vm::setCode, onFinished)
}

/** Exclusao de conta em 3 passos: consequencias + dialogo -> codigo no celular -> confirmacao (B11, US-003). */
@Composable
fun DeleteAccountScreen(
    state: DeleteAccountUiState,
    onBack: () -> Unit = {},
    onRequestCode: () -> Unit = {},
    onCode: (String) -> Unit = {},
    onFinished: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    var confirm by rememberSaveable { mutableStateOf(false) }
    if (confirm) {
        SCDialog(
            title = stringResource(Res.string.profile_delete_dialog_title),
            body = stringResource(Res.string.profile_delete_dialog_body),
            onDismissRequest = { confirm = false },
            confirm = SCAction(stringResource(Res.string.profile_delete_confirm), { confirm = false; onRequestCode() }),
            dismiss = SCAction(stringResource(Res.string.common_cancel), { confirm = false }),
            variant = SCDialogVariant.Destructive,
            modifier = Modifier.testTag("delete_dialog"),
        )
    }
    Scaffold(
        containerColor = c.surface,
        topBar = {
            SCTopBar(
                stringResource(Res.string.profile_delete_account),
                navigation = if (state.step == DeleteStep.Done) null else SCTopBarNavigation.Back,
                onNavigationClick = onBack,
            )
        },
        bottomBar = {
            BottomActionBar {
                when (state.step) {
                    DeleteStep.Info -> SCButton(
                        stringResource(Res.string.profile_delete_account), { confirm = true },
                        variant = SCButtonVariant.Destructive, size = SCButtonSize.Large, fullWidth = true,
                        loading = state.loading, modifier = Modifier.testTag("delete_start"),
                    )
                    DeleteStep.Code -> SCButton(
                        if (state.resendIn > 0) stringResource(Res.string.auth_code_resend_in, state.resendIn) else stringResource(Res.string.auth_code_resend),
                        onRequestCode, variant = SCButtonVariant.Secondary, fullWidth = true,
                        enabled = state.resendIn == 0 && !state.loading && state.error != CodeError.Blocked,
                    )
                    DeleteStep.Done -> SCButton(
                        stringResource(Res.string.delete_done_cta), onFinished,
                        size = SCButtonSize.Large, fullWidth = true, modifier = Modifier.testTag("delete_done"),
                    )
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(16.dp)) {
                when (state.step) {
                    DeleteStep.Info -> {
                        Text(stringResource(Res.string.delete_what_happens), style = t.headlineSmall, color = c.onSurface, modifier = Modifier.semantics { heading() })
                        Spacer(Modifier.height(16.dp))
                        Bullet(stringResource(Res.string.delete_point_data))
                        Bullet(stringResource(Res.string.delete_point_payments))
                        Bullet(stringResource(Res.string.delete_point_games))
                        if (state.pendingGames.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            val last = state.pendingGames.maxBy { it.fim }
                            Column(
                                Modifier.fillMaxWidth().background(c.tertiaryContainer, MaterialTheme.shapes.medium).padding(16.dp).testTag("delete_pending"),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Schedule, null, tint = c.onTertiaryContainer, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(Res.string.delete_pending_title), style = t.titleSmall, color = c.onTertiaryContainer)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(Res.string.delete_pending_body, PtBrFormat.dateTime(last.fim)), style = t.bodyMedium, color = c.onTertiaryContainer)
                                state.pendingGames.forEach { j ->
                                    Text("• ${j.titulo} · ${PtBrFormat.dateTime(j.inicio)}", style = t.bodySmall, color = c.onTertiaryContainer)
                                }
                            }
                        }
                    }
                    DeleteStep.Code -> {
                        Text(stringResource(Res.string.delete_code_title), style = t.headlineSmall, color = c.onSurface, modifier = Modifier.semantics { heading() })
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(Res.string.auth_code_body, PtBrFormat.maskedPhone(state.phone)), style = t.bodyLarge, color = c.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        OtpField(state.code, onCode, isError = state.error != null, enabled = !state.loading && state.error != CodeError.Blocked)
                        CodeErrorText(state.error)
                    }
                    DeleteStep.Done -> {
                        Spacer(Modifier.height(24.dp))
                        Icon(Icons.Rounded.CheckCircle, null, tint = c.primary, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(Res.string.delete_done_title), style = t.headlineSmall, color = c.onSurface, modifier = Modifier.semantics { heading() })
                        Spacer(Modifier.height(8.dp))
                        val after = state.scheduledAfter
                        Text(
                            if (after != null) stringResource(Res.string.delete_done_body_scheduled, PtBrFormat.dateTime(after)) else stringResource(Res.string.delete_done_body_now),
                            style = t.bodyLarge, color = c.onSurfaceVariant, modifier = Modifier.testTag("delete_done_body"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text("•", style = MaterialTheme.typography.bodyLarge, color = SportConnectTheme.colors.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = SportConnectTheme.colors.onSurface)
    }
}

@Composable
private fun CodeErrorText(error: CodeError?) {
    val res = when (error) {
        CodeError.Invalid -> stringResource(Res.string.error_code_invalid_body)
        CodeError.Expired -> stringResource(Res.string.error_code_expired_body)
        CodeError.Blocked -> stringResource(Res.string.error_too_many_attempts_body, "15min")
        CodeError.PhoneInvalid -> stringResource(Res.string.error_phone_invalid_body)
        null -> null
    } ?: return
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(error) { haptics.performHapticFeedback(HapticFeedbackType.Reject) }
    Spacer(Modifier.height(8.dp))
    Text(res, style = MaterialTheme.typography.bodySmall, color = SportConnectTheme.colors.error, modifier = Modifier.testTag("code_error"))
}

// =================================================================== editar perfil

@Composable
fun EditProfileRoute(onBack: () -> Unit, vm: EditProfileViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val savedMsg = stringResource(Res.string.profile_saved)
    val phoneMsg = stringResource(Res.string.profile_phone_changed)
    LaunchedEffect(state.saved, state.phoneChanged) {
        when {
            state.saved -> { snackbar.showSC(savedMsg, SCSnackbarKind.Success); vm.consumeSaved() }
            state.phoneChanged -> { snackbar.showSC(phoneMsg, SCSnackbarKind.Success); vm.consumeSaved() }
        }
    }
    val photo by koinInject<ProfilePhotoStore>().photo.collectAsStateWithLifecycle()
    EditProfileScreen(
        state, snackbar, onBack, vm::setName, vm::setEmail, vm::save,
        vm::startPhoneChange, vm::cancelPhoneChange, vm::setNewPhone, vm::sendPhoneCode, vm::setPhoneCode,
        photo = photo,
        onPickPhoto = LocalPickProfilePhoto.current,
        onRemovePhoto = vm::removePhoto,
    )
}

/** Editar perfil (B13): nome, e-mail opcional e troca de celular com codigo no numero novo. */
@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit = {},
    onName: (String) -> Unit = {},
    onEmail: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onStartPhone: () -> Unit = {},
    onCancelPhone: () -> Unit = {},
    onNewPhone: (String) -> Unit = {},
    onSendPhoneCode: () -> Unit = {},
    onPhoneCode: (String) -> Unit = {},
    photo: ImageBitmap? = null,
    onPickPhoto: () -> Unit = {},
    onRemovePhoto: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Scaffold(
        containerColor = c.surface,
        topBar = { SCTopBar(stringResource(Res.string.profile_edit_title), onNavigationClick = onBack) },
        snackbarHost = { SCSnackbarHost(snackbar) },
        bottomBar = {
            BottomActionBar {
                SCButton(
                    stringResource(Res.string.common_save), onSave,
                    size = SCButtonSize.Large, fullWidth = true, loading = state.saving, enabled = state.dirty,
                    modifier = Modifier.testTag("profile_edit_save"),
                )
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(16.dp)) {
                // B7: foto de perfil (trocar/remover).
                Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    SCAvatar(AvatarUi(state.name.ifBlank { "?" }, photo = photo), size = 80.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SCButton(
                            stringResource(if (photo == null) Res.string.profile_photo_add else Res.string.profile_photo_change),
                            onPickPhoto, variant = SCButtonVariant.Secondary, size = SCButtonSize.Small,
                            leadingIcon = Icons.Rounded.AddAPhoto, modifier = Modifier.testTag("profile_photo_change"),
                        )
                        if (photo != null) {
                            SCButton(
                                stringResource(Res.string.profile_photo_remove), onRemovePhoto,
                                variant = SCButtonVariant.Text, size = SCButtonSize.Small, modifier = Modifier.testTag("profile_photo_remove"),
                            )
                        }
                    }
                }
                SCTextField(
                    value = state.name, onValueChange = onName,
                    label = stringResource(Res.string.auth_name_label),
                    errorText = if (state.invalid == ProfileField.Name) stringResource(Res.string.auth_name_too_short) else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("profile_edit_name"),
                )
                Spacer(Modifier.height(16.dp))
                SCTextField(
                    value = state.email, onValueChange = onEmail,
                    label = stringResource(Res.string.profile_email_label),
                    supportingText = stringResource(Res.string.profile_email_helper),
                    errorText = if (state.invalid == ProfileField.Email) stringResource(Res.string.profile_email_invalid) else null,
                    leadingIcon = Icons.Rounded.Mail,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSave() }),
                    modifier = Modifier.fillMaxWidth().testTag("profile_edit_email"),
                )
                Spacer(Modifier.height(24.dp))
                Column(Modifier.fillMaxWidth().animateContentSize()) {
                    Text(stringResource(Res.string.profile_phone_label), style = t.titleSmall, color = c.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(PtBrFormat.maskedPhone(state.user?.celular.orEmpty()), style = t.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
                        if (!state.changingPhone) {
                            SCButton(stringResource(Res.string.profile_phone_change), onStartPhone, variant = SCButtonVariant.Text, modifier = Modifier.testTag("profile_phone_change"))
                        }
                    }
                    AnimatedVisibility(state.changingPhone) {
                        Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            if (!state.codeSent) {
                                SCTextField(
                                    value = state.newPhone, onValueChange = onNewPhone,
                                    label = stringResource(Res.string.profile_phone_new_label),
                                    type = SCTextFieldType.Phone,
                                    errorText = if (state.phoneError == CodeError.PhoneInvalid) stringResource(Res.string.error_phone_invalid_body) else null,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { onSendPhoneCode() }),
                                    modifier = Modifier.fillMaxWidth().testTag("profile_new_phone"),
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SCButton(
                                        stringResource(Res.string.auth_send_code), onSendPhoneCode,
                                        enabled = state.newPhone.length >= 10, loading = state.phoneLoading,
                                        modifier = Modifier.testTag("profile_new_phone_send"),
                                    )
                                    SCButton(stringResource(Res.string.common_cancel), onCancelPhone, variant = SCButtonVariant.Text)
                                }
                            } else {
                                Text(stringResource(Res.string.auth_code_body, PtBrFormat.maskedPhone(state.newPhone)), style = t.bodyMedium, color = c.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                OtpField(state.code, onPhoneCode, isError = state.phoneError != null, enabled = !state.phoneLoading && state.phoneError != CodeError.Blocked)
                                CodeErrorText(state.phoneError)
                                SCButton(stringResource(Res.string.common_cancel), onCancelPhone, variant = SCButtonVariant.Text)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =================================================================== meus esportes

@Composable
fun SportsRoute(onBack: () -> Unit, vm: SportsViewModel = koinViewModel()) {
    val selected by vm.selected.collectAsStateWithLifecycle()
    SportsScreen(selected, onBack, vm::toggle)
}

/** Meus esportes (B15): mesma escolha do onboarding (`onboarding_sports_*`), salva no aparelho. */
@Composable
fun SportsScreen(selected: Set<Sport>, onBack: () -> Unit = {}, onToggle: (Sport) -> Unit = {}) {
    val c = SportConnectTheme.colors
    val haptics = LocalHapticFeedback.current
    Scaffold(containerColor = c.surface, topBar = { SCTopBar(stringResource(Res.string.profile_sports), onNavigationClick = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(16.dp)) {
                Text(stringResource(Res.string.onboarding_sports_body), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("sports_chips")) {
                    Sport.entries.forEach { s ->
                        SCSportChip(
                            s, variant = SCSportChipVariant.Filter, selected = s in selected,
                            onClick = { haptics.performHapticFeedback(HapticFeedbackType.SegmentTick); onToggle(s) },
                            modifier = Modifier.testTag("sports_chip_${s.name}"),
                        )
                    }
                }
            }
        }
    }
}

// =================================================================== cartoes salvos

/** Cartoes salvos (B15/E5): so bandeira, final e validade; remover tira do aparelho. */
@Composable
fun SavedCardsRoute(onBack: () -> Unit) {
    val prefs: AppPreferences = koinInject()
    var cards by remember { mutableStateOf(prefs.savedCards) }
    SavedCardsScreen(cards, onBack, onRemove = { card -> prefs.savedCards = cards - card; cards = prefs.savedCards })
}

@Composable
fun SavedCardsScreen(cards: List<AppPreferences.SavedCard> = emptyList(), onBack: () -> Unit = {}, onRemove: (AppPreferences.SavedCard) -> Unit = {}) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Scaffold(containerColor = c.surface, topBar = { SCTopBar(stringResource(Res.string.profile_saved_cards), onNavigationClick = onBack) }) { padding ->
        if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding)) {
                SCEmptyState(
                    title = stringResource(Res.string.cards_empty_title),
                    description = stringResource(Res.string.cards_empty_body),
                    modifier = Modifier.testTag("cards_empty"),
                )
            }
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(horizontal = 16.dp)) {
                cards.forEach { card ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("saved_card_${card.last4}"), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CreditCard, null, tint = c.onSurfaceVariant, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(Res.string.card_saved_item, card.brand, card.last4), style = t.bodyLarge, color = c.onSurface)
                            Text(stringResource(Res.string.card_saved_expiry, card.expiry), style = t.bodyMedium, color = c.onSurfaceVariant)
                        }
                        SCButton(stringResource(Res.string.card_remove), { onRemove(card) }, variant = SCButtonVariant.Text, size = SCButtonSize.Small)
                    }
                    HorizontalDivider(color = c.outlineVariant)
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.card_secure_note), style = t.bodySmall, color = c.onSurfaceVariant)
            }
        }
    }
}

// =================================================================== suporte

/** Falar com o suporte (B15): perguntas frequentes + WhatsApp e e-mail. */
@Composable
fun SupportScreen(onBack: () -> Unit = {}) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val share = rememberShareActions()
    val waMessage = stringResource(Res.string.support_whatsapp_message)
    Scaffold(containerColor = c.surface, topBar = { SCTopBar(stringResource(Res.string.profile_support), onNavigationClick = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(stringResource(Res.string.support_faq_title), Modifier.padding(top = 8.dp, bottom = 4.dp))
                listOf(
                    Res.string.support_q1 to Res.string.support_a1,
                    Res.string.support_q2 to Res.string.support_a2,
                    Res.string.support_q3 to Res.string.support_a3,
                    Res.string.support_q4 to Res.string.support_a4,
                ).forEachIndexed { i, (q, a) -> FaqItem(q, a, "support_faq_$i") }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(Res.string.support_contact_title), style = t.titleMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
                Text(stringResource(Res.string.support_hours), style = t.bodyMedium, color = c.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                SCButton(
                    stringResource(Res.string.support_whatsapp),
                    { share.openUrl(SupportContacts.WHATSAPP_URL + "?text=" + urlEncode(waMessage)) },
                    fullWidth = true, leadingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                    modifier = Modifier.testTag("support_whatsapp"),
                )
                Spacer(Modifier.height(8.dp))
                SCButton(
                    stringResource(Res.string.support_email),
                    { share.openUrl("mailto:${SupportContacts.EMAIL}") },
                    variant = SCButtonVariant.Secondary, fullWidth = true, leadingIcon = Icons.Rounded.Mail,
                    modifier = Modifier.testTag("support_email"),
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FaqItem(q: StringResource, a: StringResource, tag: String) {
    val c = SportConnectTheme.colors
    var open by rememberSaveable { mutableStateOf(false) }
    val rotation = if (open) 180f else 0f
    Column(Modifier.fillMaxWidth().animateContentSize().testTag(tag)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .clickable(role = Role.Button) { open = !open }
                .semantics { stateDescription = if (open) "aberto" else "fechado" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(q), style = MaterialTheme.typography.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ExpandMore, null, tint = c.onSurfaceVariant, modifier = Modifier.rotate(rotation))
        }
        if (open) {
            Text(stringResource(a), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
        }
        HorizontalDivider(color = c.outlineVariant)
    }
}
