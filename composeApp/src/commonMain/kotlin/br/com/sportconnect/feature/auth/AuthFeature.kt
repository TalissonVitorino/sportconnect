@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.theme.LocalSCReducedMotion
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthFailure
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.AuthRules
import br.com.sportconnect.core.domain.repository.CanalOtp
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.Usuario
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.auth_body
import br.com.sportconnect.resources.auth_code_body
import br.com.sportconnect.resources.auth_code_body_whatsapp
import br.com.sportconnect.resources.auth_code_resend
import br.com.sportconnect.resources.auth_code_resend_in
import br.com.sportconnect.resources.auth_code_title
import br.com.sportconnect.resources.auth_code_wrong_number
import br.com.sportconnect.resources.auth_marketing_optin
import br.com.sportconnect.resources.auth_name_label
import br.com.sportconnect.resources.auth_name_title
import br.com.sportconnect.resources.auth_name_too_short
import br.com.sportconnect.resources.auth_phone_label
import br.com.sportconnect.resources.auth_phone_placeholder
import br.com.sportconnect.resources.auth_send_code
import br.com.sportconnect.resources.auth_send_code_whatsapp
import br.com.sportconnect.resources.auth_sending
import br.com.sportconnect.resources.auth_terms
import br.com.sportconnect.resources.auth_terms_link_privacy
import br.com.sportconnect.resources.auth_terms_link_terms
import br.com.sportconnect.resources.auth_title
import br.com.sportconnect.resources.cd_otp_field
import br.com.sportconnect.resources.common_continue
import br.com.sportconnect.resources.error_code_expired_body
import br.com.sportconnect.resources.error_code_expired_title
import br.com.sportconnect.resources.error_code_invalid_body
import br.com.sportconnect.resources.error_no_connection_body
import br.com.sportconnect.resources.error_no_connection_title
import br.com.sportconnect.resources.error_phone_invalid_body
import br.com.sportconnect.resources.error_too_many_attempts_body
import br.com.sportconnect.resources.error_too_many_attempts_title
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant

enum class AuthStep { Phone, Code, Name }

/** Erros tratados na tela (09 ficha 2 · 07 §4). */
sealed interface AuthError {
    data object PhoneInvalid : AuthError
    data object CodeInvalid : AuthError
    data object CodeExpired : AuthError
    data class TooManyAttempts(val retryAt: Instant) : AuthError
    data object NoConnection : AuthError
    data object NameTooShort : AuthError
}

data class AuthUiState(
    val step: AuthStep = AuthStep.Phone,
    val channel: CanalOtp = CanalOtp.SMS,
    val phone: String = "",
    val code: String = "",
    val name: String = "",
    val marketingOptIn: Boolean = false,
    val loading: Boolean = false,
    val error: AuthError? = null,
    val resendIn: Int = 0,
    val done: Boolean = false,
) {
    val phoneValid: Boolean get() = phone.length >= 10
}

/**
 * Entrar/criar conta por celular + codigo (09 ficha 2, US-002). OTP da demo: 123456.
 * Passo 3 (nome) so aparece na primeira vez; ali tambem fica o opt-in de marketing (LGPD, CT-05).
 */
class AuthViewModel(
    private val auth: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var countdown: Job? = null

    init {
        analytics.track(AnalyticsEvents.CADASTRO_INICIADO, mapOf("contexto" to "app"))
    }

    fun setPhone(v: String) = _uiState.update { it.copy(phone = v.filter(Char::isDigit).take(11), error = null) }
    fun setName(v: String) = _uiState.update { it.copy(name = v.take(40), error = null) }
    fun setMarketing(v: Boolean) = _uiState.update { it.copy(marketingOptIn = v) }

    fun setCode(v: String) {
        val s = _uiState.value
        if (s.loading || s.error is AuthError.TooManyAttempts) return
        val digits = v.filter(Char::isDigit).take(6)
        _uiState.update { it.copy(code = digits, error = null) }
        if (digits.length == 6) verify()
    }

    fun sendCode(channel: CanalOtp = _uiState.value.channel) {
        val s = _uiState.value
        if (s.loading || !s.phoneValid) return
        _uiState.update { it.copy(loading = true, error = null, channel = channel) }
        viewModelScope.launch {
            auth.solicitarOtp(s.phone, channel).fold(
                onSuccess = {
                    _uiState.update { it.copy(loading = false, step = AuthStep.Code, code = "", resendIn = RESEND_SECONDS) }
                    startCountdown()
                },
                onFailure = { e -> _uiState.update { it.copy(loading = false, error = e.toAuthError()) } },
            )
        }
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            while (_uiState.value.resendIn > 0) {
                delay(1_000)
                _uiState.update { it.copy(resendIn = (it.resendIn - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun changeNumber() {
        countdown?.cancel()
        _uiState.update { it.copy(step = AuthStep.Phone, code = "", error = null, resendIn = 0) }
    }

    private fun verify() {
        val s = _uiState.value
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            auth.verificarOtp(s.phone, s.code).fold(
                onSuccess = { u ->
                    val needsName = u.nome == NOME_PROVISORIO
                    if (!needsName && !u.acceptedCurrentTerms()) {
                        // Quem ja tinha conta aceita a versao nova dos termos ao entrar (aviso no rodape do passo 1).
                        auth.registrarConsentimentos(AuthRules.VERSAO_TERMOS, u.marketingConsent())
                    }
                    _uiState.update { it.copy(loading = false, step = if (needsName) AuthStep.Name else it.step, done = !needsName) }
                },
                onFailure = { e -> _uiState.update { it.copy(loading = false, error = e.toAuthError(), code = "") } },
            )
        }
    }

    fun finishName() {
        val s = _uiState.value
        if (s.loading) return
        if (s.name.trim().length < 2) {
            _uiState.update { it.copy(error = AuthError.NameTooShort) }
            return
        }
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            auth.atualizarPerfil(s.name).onFailure {
                _uiState.update { it.copy(loading = false, error = AuthError.NameTooShort) }
                return@launch
            }
            auth.registrarConsentimentos(AuthRules.VERSAO_TERMOS, s.marketingOptIn)
            analytics.track(AnalyticsEvents.CADASTRO_CONCLUIDO, mapOf("contexto" to "app", "tipo" to "conta_completa"))
            _uiState.update { it.copy(loading = false, done = true) }
        }
    }

    /** Minutos que faltam para liberar o bloqueio (texto `error_too_many_attempts_body`). */
    fun minutesUntil(retryAt: Instant): Int = ((retryAt - clock.now()).inWholeSeconds.coerceAtLeast(0) + 59).toInt() / 60

    private fun Throwable.toAuthError(): AuthError = when (this) {
        is AuthFailure.PhoneInvalid -> AuthError.PhoneInvalid
        is AuthFailure.CodeExpired -> AuthError.CodeExpired
        is AuthFailure.TooManyAttempts -> AuthError.TooManyAttempts(retryAt)
        is AuthFailure.NoConnection -> AuthError.NoConnection
        is AuthFailure.CodeInvalid -> AuthError.CodeInvalid
        else -> if (_uiState.value.step == AuthStep.Phone) AuthError.PhoneInvalid else AuthError.CodeInvalid
    }

    private fun Usuario.acceptedCurrentTerms() = consentimentos.any { it.tipo == "termos:${AuthRules.VERSAO_TERMOS}" && it.concedido }
    private fun Usuario.marketingConsent() = consentimentos.lastOrNull { it.tipo == "marketing" }?.concedido ?: false

    companion object {
        const val RESEND_SECONDS = 30
        private const val NOME_PROVISORIO = "Convidado"
    }
}

/** Documentos legais abertos pelos links do rodape (CT-05). */
enum class LegalDoc { Terms, Privacy }

@Composable
fun AuthRoute(
    onClose: () -> Unit,
    onDone: () -> Unit,
    onOpenLegal: (LegalDoc) -> Unit = {},
    vm: AuthViewModel = koinViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onDone() }
    AuthScreen(
        state = state,
        onClose = onClose,
        onPhone = vm::setPhone,
        onSend = { vm.sendCode(CanalOtp.SMS) },
        onSendWhatsapp = { vm.sendCode(CanalOtp.WHATSAPP) },
        onResend = { vm.sendCode() },
        onCode = vm::setCode,
        onChangeNumber = vm::changeNumber,
        onName = vm::setName,
        onMarketing = vm::setMarketing,
        onFinishName = vm::finishName,
        onOpenLegal = onOpenLegal,
        minutesUntil = vm::minutesUntil,
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onClose: () -> Unit = {},
    onPhone: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSendWhatsapp: () -> Unit = {},
    onResend: () -> Unit = {},
    onCode: (String) -> Unit = {},
    onChangeNumber: () -> Unit = {},
    onName: (String) -> Unit = {},
    onMarketing: (Boolean) -> Unit = {},
    onFinishName: () -> Unit = {},
    onOpenLegal: (LegalDoc) -> Unit = {},
    minutesUntil: (Instant) -> Int = { AuthRules.BLOQUEIO_MIN },
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val reduced = LocalSCReducedMotion.current
    Scaffold(
        containerColor = c.surface,
        topBar = {
            // No passo do nome a conta ja existe: nao da pra "fechar" e ficar como "Convidado".
            SCTopBar(
                "",
                navigation = if (state.step == AuthStep.Name) null else SCTopBarNavigation.Close,
                onNavigationClick = onClose,
            )
        },
        bottomBar = {
            BottomActionBar(Modifier.imePadding()) {
                when (state.step) {
                    AuthStep.Phone -> {
                        SCButton(
                            if (state.loading && state.channel == CanalOtp.SMS) stringResource(Res.string.auth_sending) else stringResource(Res.string.auth_send_code),
                            onSend,
                            size = SCButtonSize.Large, fullWidth = true, loading = state.loading && state.channel == CanalOtp.SMS,
                            enabled = state.phoneValid && !state.loading, modifier = Modifier.testTag("auth_send_code"),
                        )
                        SCButton(
                            stringResource(Res.string.auth_send_code_whatsapp), onSendWhatsapp,
                            variant = SCButtonVariant.Text, fullWidth = true,
                            loading = state.loading && state.channel == CanalOtp.WHATSAPP,
                            enabled = state.phoneValid && !state.loading,
                            modifier = Modifier.testTag("auth_send_code_whatsapp"),
                        )
                        TermsText(onOpenLegal)
                    }
                    AuthStep.Code -> SCButton(
                        if (state.resendIn > 0) stringResource(Res.string.auth_code_resend_in, state.resendIn) else stringResource(Res.string.auth_code_resend),
                        onResend,
                        variant = SCButtonVariant.Secondary, fullWidth = true,
                        enabled = state.resendIn == 0 && !state.loading && state.error !is AuthError.TooManyAttempts,
                        modifier = Modifier.testTag("auth_resend"),
                    )
                    AuthStep.Name -> SCButton(
                        stringResource(Res.string.common_continue), onFinishName,
                        size = SCButtonSize.Large, fullWidth = true, loading = state.loading, enabled = state.name.isNotBlank(),
                        modifier = Modifier.testTag("auth_continue"),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    if (reduced) {
                        fadeIn(tween(SCMotion.Short3)) togetherWith fadeOut(tween(SCMotion.Short2))
                    } else {
                        val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(tween(SCMotion.Medium2, easing = SCMotion.EmphasizedDecelerate)) { it * dir / 8 } + fadeIn(tween(SCMotion.Medium1))) togetherWith
                            (slideOutHorizontally(tween(SCMotion.Medium1, easing = SCMotion.EmphasizedAccelerate)) { -it * dir / 8 } + fadeOut(tween(SCMotion.Short3)))
                    }
                },
                label = "auth-step",
            ) { step ->
                Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    when (step) {
                        AuthStep.Phone -> PhoneStep(state, onPhone, onSend)
                        AuthStep.Code -> CodeStep(state, onCode, onChangeNumber, minutesUntil)
                        AuthStep.Name -> NameStep(state, onName, onMarketing, onFinishName)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneStep(state: AuthUiState, onPhone: (String) -> Unit, onSend: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    Text(stringResource(Res.string.auth_title), style = t.headlineMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(8.dp))
    Text(stringResource(Res.string.auth_body), style = t.bodyLarge, color = c.onSurfaceVariant)
    Spacer(Modifier.height(24.dp))
    SCTextField(
        value = state.phone,
        onValueChange = onPhone,
        label = stringResource(Res.string.auth_phone_label),
        placeholder = stringResource(Res.string.auth_phone_placeholder),
        type = SCTextFieldType.Phone,
        enabled = !state.loading,
        errorText = when (state.error) {
            AuthError.PhoneInvalid -> stringResource(Res.string.error_phone_invalid_body)
            AuthError.NoConnection -> stringResource(Res.string.error_no_connection_body)
            else -> null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { if (state.phoneValid) onSend() }),
        modifier = Modifier.fillMaxWidth().focusRequester(focus)
            .semantics { contentType = ContentType.PhoneNumber }
            .testTag("auth_phone_field"),
    )
    if (state.error is AuthError.TooManyAttempts) {
        Spacer(Modifier.height(12.dp))
        ErrorBox(stringResource(Res.string.error_too_many_attempts_title), stringResource(Res.string.error_too_many_attempts_body, "${AuthRules.BLOQUEIO_MIN}min"))
    }
}

@Composable
private fun CodeStep(state: AuthUiState, onCode: (String) -> Unit, onChangeNumber: () -> Unit, minutesUntil: (Instant) -> Int) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(state.error) {
        if (state.error == AuthError.CodeInvalid) haptics.performHapticFeedback(HapticFeedbackType.Reject)
    }
    Text(stringResource(Res.string.auth_code_title), style = t.headlineMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(8.dp))
    val masked = PtBrFormat.maskedPhone(state.phone)
    Text(
        if (state.channel == CanalOtp.WHATSAPP) stringResource(Res.string.auth_code_body_whatsapp, masked) else stringResource(Res.string.auth_code_body, masked),
        style = t.bodyLarge,
        color = c.onSurfaceVariant,
    )
    SCButton(stringResource(Res.string.auth_code_wrong_number), onChangeNumber, variant = SCButtonVariant.Text, modifier = Modifier.testTag("auth_change_number"))
    Spacer(Modifier.height(16.dp))
    val blocked = state.error is AuthError.TooManyAttempts
    OtpField(state.code, onCode, isError = state.error == AuthError.CodeInvalid || state.error == AuthError.CodeExpired, enabled = !state.loading && !blocked)
    Spacer(Modifier.height(12.dp))
    when (val e = state.error) {
        AuthError.CodeInvalid -> ErrorLine(stringResource(Res.string.error_code_invalid_body))
        AuthError.CodeExpired -> ErrorBox(stringResource(Res.string.error_code_expired_title), stringResource(Res.string.error_code_expired_body))
        is AuthError.TooManyAttempts -> ErrorBox(
            stringResource(Res.string.error_too_many_attempts_title),
            stringResource(Res.string.error_too_many_attempts_body, "${minutesUntil(e.retryAt)}min"),
        )
        AuthError.NoConnection -> ErrorBox(stringResource(Res.string.error_no_connection_title), stringResource(Res.string.error_no_connection_body))
        else -> Unit
    }
}

@Composable
private fun NameStep(state: AuthUiState, onName: (String) -> Unit, onMarketing: (Boolean) -> Unit, onFinish: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    Text(stringResource(Res.string.auth_name_title), style = t.headlineMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(24.dp))
    SCTextField(
        value = state.name,
        onValueChange = onName,
        label = stringResource(Res.string.auth_name_label),
        enabled = !state.loading,
        errorText = if (state.error == AuthError.NameTooShort) stringResource(Res.string.auth_name_too_short) else null,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onFinish() }),
        modifier = Modifier.fillMaxWidth().focusRequester(focus)
            .semantics { contentType = ContentType.PersonFullName }
            .testTag("auth_name_field"),
    )
    Spacer(Modifier.height(16.dp))
    // Opt-in de marketing separado e desmarcado por padrao (LGPD).
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .clickable(role = Role.Checkbox) { onMarketing(!state.marketingOptIn) }
            .testTag("auth_marketing_optin"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = state.marketingOptIn, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(Res.string.auth_marketing_optin), style = t.bodyMedium, color = c.onSurface)
    }
}

/** "Ao continuar, voce concorda com os Termos de Uso e a Politica de Privacidade." com os dois links. */
@Composable
private fun TermsText(onOpenLegal: (LegalDoc) -> Unit) {
    val c = SportConnectTheme.colors
    val full = stringResource(Res.string.auth_terms)
    val termsLabel = stringResource(Res.string.auth_terms_link_terms)
    val privacyLabel = stringResource(Res.string.auth_terms_link_privacy)
    val linkStyle = TextLinkStyles(SpanStyle(color = c.primary, textDecoration = TextDecoration.Underline))
    val text = remember(full, termsLabel, privacyLabel, linkStyle) {
        buildAnnotatedString {
            append(full)
            listOf(termsLabel to LegalDoc.Terms, privacyLabel to LegalDoc.Privacy).forEach { (label, doc) ->
                val start = full.indexOf(label)
                if (start >= 0) {
                    addLink(LinkAnnotation.Clickable(doc.name, linkStyle) { onOpenLegal(doc) }, start, start + label.length)
                }
            }
        }
    }
    Text(text, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.testTag("auth_terms"))
}

@Composable
private fun ErrorLine(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = SportConnectTheme.colors.error,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("auth_error"),
    )
}

@Composable
private fun ErrorBox(title: String, body: String) {
    val c = SportConnectTheme.colors
    Column(
        Modifier.fillMaxWidth().background(c.errorContainer, MaterialTheme.shapes.medium).padding(12.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }.testTag("auth_error"),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = c.onErrorContainer)
        Spacer(Modifier.height(2.dp))
        Text(body, style = MaterialTheme.typography.bodySmall, color = c.onErrorContainer)
    }
}

/** Campo OTP: uma semantica, 6 caixas visuais 48x56 (10 §2.2), com autofill do codigo do SMS. */
@Composable
private fun OtpField(value: String, onChange: (String) -> Unit, isError: Boolean, enabled: Boolean) {
    val c = SportConnectTheme.colors
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    val description = stringResource(Res.string.cd_otp_field)
    BasicTextField(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        modifier = Modifier.focusRequester(focus).testTag("auth_otp_field").semantics {
            contentDescription = description
            contentType = ContentType.SmsOtpCode
        },
        decorationBox = { _ ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { i ->
                    val ch = value.getOrNull(i)?.toString() ?: ""
                    val focused = enabled && i == value.length
                    Box(
                        Modifier.size(48.dp, 56.dp)
                            .background(c.surfaceContainerLowest, SCShape.control)
                            .border(if (focused || isError) 2.dp else 1.dp, if (isError) c.error else if (focused) c.primary else c.outline, SCShape.control),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(ch, style = SportConnectTheme.typographyExt.numericPrice, color = c.onSurface)
                    }
                }
            }
        },
    )
}
