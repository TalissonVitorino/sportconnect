@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
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
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.auth_body
import br.com.sportconnect.resources.auth_code_body
import br.com.sportconnect.resources.auth_code_resend
import br.com.sportconnect.resources.auth_code_resend_in
import br.com.sportconnect.resources.auth_code_title
import br.com.sportconnect.resources.auth_code_wrong_number
import br.com.sportconnect.resources.auth_name_label
import br.com.sportconnect.resources.auth_name_title
import br.com.sportconnect.resources.auth_phone_label
import br.com.sportconnect.resources.auth_phone_placeholder
import br.com.sportconnect.resources.auth_send_code
import br.com.sportconnect.resources.auth_send_code_whatsapp
import br.com.sportconnect.resources.auth_terms
import br.com.sportconnect.resources.auth_title
import br.com.sportconnect.resources.common_continue
import br.com.sportconnect.resources.error_code_invalid_body
import br.com.sportconnect.resources.error_phone_invalid_body
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

enum class AuthStep { Phone, Code, Name }

enum class AuthError { PhoneInvalid, CodeInvalid }

data class AuthUiState(
    val step: AuthStep = AuthStep.Phone,
    val phone: String = "",
    val code: String = "",
    val name: String = "",
    val loading: Boolean = false,
    val error: AuthError? = null,
    val resendIn: Int = 0,
    val done: Boolean = false,
)

/** Login por celular + OTP (10 §2.2). OTP da demo: 123456. */
class AuthViewModel(private val auth: AuthRepository, private val analytics: AnalyticsTracker) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        analytics.track(AnalyticsEvents.CADASTRO_INICIADO, mapOf("contexto" to "app"))
    }

    fun setPhone(v: String) = _uiState.update { it.copy(phone = v.filter(Char::isDigit).take(11), error = null) }
    fun setName(v: String) = _uiState.update { it.copy(name = v.take(40)) }

    fun setCode(v: String) {
        val digits = v.filter(Char::isDigit).take(6)
        _uiState.update { it.copy(code = digits, error = null) }
        if (digits.length == 6) verify()
    }

    fun sendCode() {
        val s = _uiState.value
        if (s.loading) return
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            auth.solicitarOtp(s.phone).fold(
                onSuccess = {
                    _uiState.update { it.copy(loading = false, step = AuthStep.Code, code = "", resendIn = 30) }
                    while (_uiState.value.resendIn > 0) {
                        delay(1_000)
                        _uiState.update { it.copy(resendIn = it.resendIn - 1) }
                    }
                },
                onFailure = { _uiState.update { it.copy(loading = false, error = AuthError.PhoneInvalid) } },
            )
        }
    }

    fun changeNumber() = _uiState.update { it.copy(step = AuthStep.Phone, code = "", error = null) }

    private fun verify() {
        val s = _uiState.value
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            auth.verificarOtp(s.phone, s.code).fold(
                onSuccess = { u ->
                    val needsName = u.nome == "Convidado"
                    _uiState.update { it.copy(loading = false, step = if (needsName) AuthStep.Name else it.step, done = !needsName) }
                },
                onFailure = { _uiState.update { it.copy(loading = false, error = AuthError.CodeInvalid, code = "") } },
            )
        }
    }

    fun finishName() {
        analytics.track(AnalyticsEvents.CADASTRO_CONCLUIDO, mapOf("contexto" to "app"))
        _uiState.update { it.copy(done = true) }
    }
}

@Composable
fun AuthRoute(onClose: () -> Unit, onDone: () -> Unit, vm: AuthViewModel = koinViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onDone() }
    AuthScreen(state, onClose, vm::setPhone, vm::sendCode, vm::setCode, vm::changeNumber, vm::setName, vm::finishName)
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onClose: () -> Unit = {},
    onPhone: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onCode: (String) -> Unit = {},
    onChangeNumber: () -> Unit = {},
    onName: (String) -> Unit = {},
    onFinishName: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = c.surface,
            topBar = { SCTopBar("", navigation = SCTopBarNavigation.Close, onNavigationClick = onClose) },
            bottomBar = {
                BottomActionBar {
                    when (state.step) {
                        AuthStep.Phone -> {
                            SCButton(
                                stringResource(Res.string.auth_send_code), onSend,
                                size = SCButtonSize.Large, fullWidth = true, loading = state.loading,
                                enabled = state.phone.length >= 10, modifier = Modifier.testTag("auth_send_code"),
                            )
                            SCButton(
                                stringResource(Res.string.auth_send_code_whatsapp), onSend,
                                variant = SCButtonVariant.Text, fullWidth = true, enabled = state.phone.length >= 10,
                                modifier = Modifier.testTag("auth_send_code_whatsapp"),
                            )
                        }
                        AuthStep.Code -> SCButton(
                            if (state.resendIn > 0) stringResource(Res.string.auth_code_resend_in, state.resendIn) else stringResource(Res.string.auth_code_resend),
                            onSend,
                            variant = SCButtonVariant.Secondary, fullWidth = true, enabled = state.resendIn == 0,
                            modifier = Modifier.testTag("auth_resend"),
                        )
                        AuthStep.Name -> SCButton(
                            stringResource(Res.string.common_continue), onFinishName,
                            size = SCButtonSize.Large, fullWidth = true, enabled = state.name.isNotBlank(),
                            modifier = Modifier.testTag("auth_continue"),
                        )
                    }
                    Text(stringResource(Res.string.auth_terms), style = t.bodySmall, color = c.onSurfaceVariant)
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    when (state.step) {
                        AuthStep.Phone -> {
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
                                errorText = if (state.error == AuthError.PhoneInvalid) stringResource(Res.string.error_phone_invalid_body) else null,
                                modifier = Modifier.fillMaxWidth().testTag("auth_phone_field"),
                            )
                        }
                        AuthStep.Code -> {
                            Text(stringResource(Res.string.auth_code_title), style = t.headlineMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(Res.string.auth_code_body, PtBrFormat.maskedPhone(state.phone)), style = t.bodyLarge, color = c.onSurfaceVariant)
                            SCButton(stringResource(Res.string.auth_code_wrong_number), onChangeNumber, variant = SCButtonVariant.Text)
                            Spacer(Modifier.height(16.dp))
                            OtpField(state.code, onCode, isError = state.error == AuthError.CodeInvalid)
                            if (state.error == AuthError.CodeInvalid) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(Res.string.error_code_invalid_body),
                                    style = t.bodySmall,
                                    color = c.error,
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                )
                            }
                        }
                        AuthStep.Name -> {
                            Text(stringResource(Res.string.auth_name_title), style = t.headlineMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
                            Spacer(Modifier.height(24.dp))
                            SCTextField(
                                value = state.name,
                                onValueChange = onName,
                                label = stringResource(Res.string.auth_name_label),
                                modifier = Modifier.fillMaxWidth().testTag("auth_name_field"),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Campo OTP: uma semantica, 6 caixas visuais 48x56 (10 §2.2). */
@Composable
private fun OtpField(value: String, onChange: (String) -> Unit, isError: Boolean) {
    val c = SportConnectTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.testTag("auth_otp_field").semantics { contentDescription = "Código de 6 números" },
        decorationBox = { _ ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(6) { i ->
                    val ch = value.getOrNull(i)?.toString() ?: ""
                    val focused = i == value.length
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
