package br.com.sportconnect.feature.arenaadmin.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Etapas do cadastro da arena (10 §2.16). */
enum class OnboardingStep { Info, Courts, Schedule, Bank }

data class ArenaOnboardingUiState(
    val current: OnboardingStep? = OnboardingStep.Info,
    val done: Set<OnboardingStep> = emptySet(),
    val name: String = "",
    val document: String = "",
    val cep: String = "",
    val pixKey: String = "",
    val showErrors: Boolean = false,
    val published: Boolean = false,
) {
    val nameError get() = name.isBlank()
    val documentError get() = document.length != 11
    val cepError get() = cep.filter(Char::isDigit).length != 8
    /** Chave Pix: CPF/CNPJ (11/14 digitos), e-mail, celular (+55...) ou aleatoria (32+ chars). */
    val pixError: Boolean
        get() {
            val k = pixKey.trim()
            val digits = k.filter(Char::isDigit)
            return !(
                (k.all { it.isDigit() || it in ".-/" } && (digits.length == 11 || digits.length == 14)) ||
                    Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(k) ||
                    (k.startsWith("+") && digits.length in 12..13) ||
                    (k.length >= 32 && k.count { it == '-' } >= 4)
                )
        }
    val progress: Int get() = done.size
    val canPublish: Boolean get() = done.size == OnboardingStep.entries.size && !published
}

/**
 * Cadastro da arena em stepper (checklist de 4 etapas + publicar). No MVP fake os dados ficam locais;
 * "Quadras" e "Horarios" levam as telas de quadras (salvar marca a etapa como concluida).
 */
class ArenaOnboardingViewModel : ViewModel() {
    private val _state = MutableStateFlow(ArenaOnboardingUiState())
    val state: StateFlow<ArenaOnboardingUiState> = _state.asStateFlow()

    fun open(step: OnboardingStep) = _state.update { it.copy(current = step, showErrors = false) }
    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setDocument(v: String) = _state.update { it.copy(document = v) }
    fun setCep(v: String) = _state.update { it.copy(cep = v.filter(Char::isDigit).take(8)) }
    fun setPixKey(v: String) = _state.update { it.copy(pixKey = v) }

    /** Valida e conclui a etapa atual; abre a proxima pendente. */
    fun saveStep() {
        val s = _state.value
        val step = s.current ?: return
        val valid = when (step) {
            OnboardingStep.Info -> !s.nameError && !s.documentError && !s.cepError
            OnboardingStep.Bank -> !s.pixError
            OnboardingStep.Courts, OnboardingStep.Schedule -> true
        }
        if (!valid) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        val done = s.done + step
        _state.update {
            it.copy(done = done, showErrors = false, current = OnboardingStep.entries.firstOrNull { e -> e !in done })
        }
    }

    fun publish() {
        if (!_state.value.canPublish) return
        _state.update { it.copy(published = true, current = null) }
    }
}
