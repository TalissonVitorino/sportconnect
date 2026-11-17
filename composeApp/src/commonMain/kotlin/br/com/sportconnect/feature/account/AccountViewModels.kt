package br.com.sportconnect.feature.account

import br.com.sportconnect.core.data.local.ProfilePhotoStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthFailure
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.AuthRules
import br.com.sportconnect.core.domain.usecase.DeleteAccountUseCase
import br.com.sportconnect.core.domain.usecase.ExportMyDataUseCase
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

/** Erros de codigo compartilhados pela exclusao (reautenticacao) e pela troca de celular. */
enum class CodeError { Invalid, Expired, Blocked, PhoneInvalid }

internal fun Throwable.toCodeError(): CodeError = when (this) {
    is AuthFailure.CodeExpired -> CodeError.Expired
    is AuthFailure.TooManyAttempts -> CodeError.Blocked
    is AuthFailure.PhoneInvalid -> CodeError.PhoneInvalid
    else -> CodeError.Invalid
}

// =================================================================== privacidade e dados (09 ficha 16)

data class PrivacyUiState(
    val user: Usuario? = null,
    val marketing: Boolean = false,
    val exporting: Boolean = false,
    val exported: String? = null,
)

class PrivacyViewModel(
    private val auth: AuthRepository,
    private val exportMyData: ExportMyDataUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            auth.currentUser.collect { u ->
                val marketing = u?.consentimentos?.lastOrNull { it.tipo == "marketing" }?.concedido ?: false
                _uiState.update { it.copy(user = u, marketing = marketing) }
            }
        }
    }

    /** Revogar/conceder marketing (US-003): efeito imediato no app; o servidor para os envios em ate 24h. */
    fun setMarketing(value: Boolean) {
        _uiState.update { it.copy(marketing = value) }
        viewModelScope.launch { auth.registrarConsentimentos(AuthRules.VERSAO_TERMOS, value) }
    }

    fun export() {
        if (_uiState.value.exporting) return
        _uiState.update { it.copy(exporting = true) }
        viewModelScope.launch {
            val json = exportMyData().getOrNull()
            _uiState.update { it.copy(exporting = false, exported = json) }
        }
    }

    fun dismissExport() = _uiState.update { it.copy(exported = null) }
}

// =================================================================== excluir conta (B11, US-003)

enum class DeleteStep { Info, Code, Done }

data class DeleteAccountUiState(
    val step: DeleteStep = DeleteStep.Info,
    val phone: String = "",
    val pendingGames: List<Jogo> = emptyList(),
    val code: String = "",
    val loading: Boolean = false,
    val error: CodeError? = null,
    val resendIn: Int = 0,
    val scheduledAfter: Instant? = null,
)

class DeleteAccountViewModel(
    private val auth: AuthRepository,
    private val deleteAccount: DeleteAccountUseCase,
    private val photos: ProfilePhotoStore,
) : ViewModel() {
    private val _local = MutableStateFlow(DeleteAccountUiState())
    val uiState: StateFlow<DeleteAccountUiState> = combine(_local, deleteAccount.jogosQueSeguram(), auth.currentUser) { s, jogos, u ->
        // Depois de excluir a sessao some: mantem o telefone e os jogos da tela final.
        if (s.step == DeleteStep.Done) s else s.copy(pendingGames = jogos, phone = u?.celular?.filter(Char::isDigit)?.takeLast(11).orEmpty())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DeleteAccountUiState())
    private var countdown: Job? = null

    /** Depois do dialogo de confirmacao: manda o codigo para o celular da conta (reautenticacao). */
    fun requestCode() {
        val phone = uiState.value.phone
        if (_local.value.loading || phone.isEmpty()) return
        _local.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            auth.solicitarOtp(phone).fold(
                onSuccess = {
                    _local.update { it.copy(loading = false, step = DeleteStep.Code, code = "", resendIn = 30, phone = phone) }
                    countdown?.cancel()
                    countdown = launch {
                        while (_local.value.resendIn > 0) {
                            delay(1_000)
                            _local.update { it.copy(resendIn = (it.resendIn - 1).coerceAtLeast(0)) }
                        }
                    }
                },
                onFailure = { e -> _local.update { it.copy(loading = false, error = e.toCodeError()) } },
            )
        }
    }

    fun setCode(v: String) {
        val s = _local.value
        if (s.loading || s.error == CodeError.Blocked) return
        val digits = v.filter(Char::isDigit).take(6)
        _local.update { it.copy(code = digits, error = null) }
        if (digits.length == 6) confirm()
    }

    private fun confirm() {
        val s = uiState.value
        _local.update { it.copy(loading = true) }
        viewModelScope.launch {
            val verified = auth.verificarOtp(s.phone, s.code)
            verified.exceptionOrNull()?.let { e ->
                _local.update { it.copy(loading = false, code = "", error = e.toCodeError()) }
                return@launch
            }
            val pending = s.pendingGames
            val uid = auth.currentUser.value?.id
            deleteAccount().fold(
                onSuccess = { r ->
                    // A foto sai junto com os dados pessoais.
                    uid?.let { photos.remove(it) }
                    _local.update { it.copy(loading = false, step = DeleteStep.Done, scheduledAfter = r.agendadaApos, pendingGames = pending) }
                },
                onFailure = { _local.update { it.copy(loading = false, error = CodeError.Invalid) } },
            )
        }
    }
}

// =================================================================== editar perfil (B13)

enum class ProfileField { Name, Email }

data class EditProfileUiState(
    val user: Usuario? = null,
    val name: String = "",
    val email: String = "",
    val saving: Boolean = false,
    val invalid: ProfileField? = null,
    val saved: Boolean = false,
    // Troca de celular (inline): numero novo -> codigo.
    val changingPhone: Boolean = false,
    val newPhone: String = "",
    val codeSent: Boolean = false,
    val code: String = "",
    val phoneLoading: Boolean = false,
    val phoneError: CodeError? = null,
    val phoneChanged: Boolean = false,
) {
    val dirty: Boolean get() = user != null && (name.trim() != user.nome || email.trim() != user.email.orEmpty())
}

class EditProfileViewModel(
    private val auth: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val photos: ProfilePhotoStore,
) : ViewModel() {
    fun removePhoto() {
        val uid = auth.currentUser.value?.id ?: return
        viewModelScope.launch { photos.remove(uid) }
    }

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        val u = auth.currentUser.value
        _uiState.value = EditProfileUiState(user = u, name = u?.nome.orEmpty(), email = u?.email.orEmpty())
        viewModelScope.launch { auth.currentUser.collect { u2 -> _uiState.update { it.copy(user = u2) } } }
    }

    fun setName(v: String) = _uiState.update { it.copy(name = v.take(40), invalid = null, saved = false) }
    fun setEmail(v: String) = _uiState.update { it.copy(email = v.take(80).trim(), invalid = null, saved = false) }

    fun save() {
        val s = _uiState.value
        if (s.saving || !s.dirty) return
        if (s.name.trim().length < 2) return _uiState.update { it.copy(invalid = ProfileField.Name) }
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            auth.atualizarPerfil(s.name, s.email).fold(
                onSuccess = {
                    val campos = buildList {
                        if (s.name.trim() != s.user?.nome) add("nome")
                        if (s.email.trim() != s.user?.email.orEmpty()) add("email")
                    }
                    analytics.track(AnalyticsEvents.PERFIL_ATUALIZADO, mapOf("campos" to campos.joinToString(",")))
                    _uiState.update { it.copy(saving = false, saved = true) }
                },
                onFailure = { _uiState.update { it.copy(saving = false, invalid = ProfileField.Email) } },
            )
        }
    }

    fun consumeSaved() = _uiState.update { it.copy(saved = false, phoneChanged = false) }

    fun startPhoneChange() = _uiState.update { it.copy(changingPhone = true, newPhone = "", codeSent = false, code = "", phoneError = null) }
    fun cancelPhoneChange() = _uiState.update { it.copy(changingPhone = false, codeSent = false, code = "", phoneError = null) }
    fun setNewPhone(v: String) = _uiState.update { it.copy(newPhone = v.filter(Char::isDigit).take(11), phoneError = null) }

    fun sendPhoneCode() {
        val s = _uiState.value
        if (s.phoneLoading || s.newPhone.length < 10) return
        _uiState.update { it.copy(phoneLoading = true, phoneError = null) }
        viewModelScope.launch {
            auth.solicitarOtp(s.newPhone).fold(
                onSuccess = { _uiState.update { it.copy(phoneLoading = false, codeSent = true, code = "") } },
                onFailure = { e -> _uiState.update { it.copy(phoneLoading = false, phoneError = e.toCodeError()) } },
            )
        }
    }

    fun setPhoneCode(v: String) {
        val s = _uiState.value
        if (s.phoneLoading || s.phoneError == CodeError.Blocked) return
        val digits = v.filter(Char::isDigit).take(6)
        _uiState.update { it.copy(code = digits, phoneError = null) }
        if (digits.length == 6) {
            _uiState.update { it.copy(phoneLoading = true) }
            viewModelScope.launch {
                auth.trocarCelular(s.newPhone, digits).fold(
                    onSuccess = {
                        analytics.track(AnalyticsEvents.PERFIL_ATUALIZADO, mapOf("campos" to "celular"))
                        _uiState.update { it.copy(phoneLoading = false, changingPhone = false, codeSent = false, code = "", phoneChanged = true) }
                    },
                    onFailure = { e -> _uiState.update { it.copy(phoneLoading = false, code = "", phoneError = e.toCodeError()) } },
                )
            }
        }
    }
}

// =================================================================== meus esportes (B15)

class SportsViewModel(private val prefs: AppPreferences) : ViewModel() {
    private val _selected = MutableStateFlow(prefs.favoriteSports.mapNotNull { n -> Sport.entries.firstOrNull { it.name == n } }.toSet())
    val selected: StateFlow<Set<Sport>> = _selected.asStateFlow()

    fun toggle(sport: Sport) {
        _selected.update { if (sport in it) it - sport else it + sport }
        prefs.favoriteSports = _selected.value.map { it.name }.toSet()
    }
}
