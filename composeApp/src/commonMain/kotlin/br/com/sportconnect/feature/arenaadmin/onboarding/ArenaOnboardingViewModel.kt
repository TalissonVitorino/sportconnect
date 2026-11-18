package br.com.sportconnect.feature.arenaadmin.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.isValidPixKey
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.model.Endereco
import br.com.sportconnect.core.model.StatusArena
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.common.observeArenaId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Etapas do cadastro da arena (10 §2.16). */
enum class OnboardingStep { Info, Courts, Schedule, Bank }

data class ArenaOnboardingUiState(
    val load: LoadState = LoadState.Loading,
    val arenaId: String? = null,
    val current: OnboardingStep? = OnboardingStep.Info,
    val done: Set<OnboardingStep> = emptySet(),
    val name: String = "",
    val document: String = "",
    val logradouro: String = "",
    val numero: String = "",
    val bairro: String = "",
    val cep: String = "",
    val telefone: String = "",
    /** T9 (13/US-012): comodidades conhecidas (`amenityOf` no detalhe da arena). */
    val comodidades: Set<String> = emptySet(),
    val horario: String = "",
    val pixKey: String = "",
    val showErrors: Boolean = false,
    val published: Boolean = false,
) {
    val nameError get() = name.isBlank()
    val documentError get() = document.length != 11
    val addressError get() = logradouro.isBlank() || numero.isBlank() || bairro.isBlank() || cepError
    val cepError get() = cep.filter(Char::isDigit).length != 8
    val telefoneError get() = telefone.filter(Char::isDigit).length !in 10..11
    val pixError: Boolean get() = !isValidPixKey(pixKey)
    val infoError: Boolean get() = nameError || documentError || addressError || telefoneError
    val progress: Int get() = done.size
    val canPublish: Boolean get() = done.size == OnboardingStep.entries.size && !published
}

/**
 * Cadastro da arena em stepper (checklist de 4 etapas + publicar). A arena ja existe (o papel de
 * gestor so e concedido a quem ja tem uma arena vinculada, `observeArenaId`); o onboarding completa
 * o cadastro dela e, ao publicar, ela passa a aparecer na busca (T9, 13/US-012).
 */
class ArenaOnboardingViewModel(
    private val auth: AuthRepository,
    private val arenas: ArenaRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ArenaOnboardingUiState())
    val state: StateFlow<ArenaOnboardingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val arenaId = auth.observeArenaId().firstOrNull()
            val arena = arenaId?.let { arenas.arena(it) }
            if (arenaId == null || arena == null) {
                _state.update { it.copy(load = if (arenaId == null) LoadState.NoArena else LoadState.Error) }
                return@launch
            }
            val temQuadras = arenas.quadras(arenaId).isNotEmpty()
            val preenchido = ArenaOnboardingUiState(
                // CPF do responsavel: a Arena so guarda o CNPJ (documento) dela, um dado diferente
                // que ja vem do comercial; esse campo nao tem onde persistir, sempre comeca vazio.
                load = LoadState.Ready, arenaId = arenaId,
                name = arena.nome, document = "",
                logradouro = arena.endereco.logradouro, numero = arena.endereco.numero, bairro = arena.endereco.bairro, cep = arena.endereco.cep,
                telefone = arena.telefone.orEmpty(), comodidades = arena.comodidades.toSet(), horario = arena.horarioFuncionamento,
                pixKey = arena.chavePix.orEmpty(), published = arena.status == StatusArena.ATIVA,
            )
            val done = buildSet {
                if (!preenchido.infoError) add(OnboardingStep.Info)
                if (temQuadras) add(OnboardingStep.Courts)
                if (arena.status == StatusArena.ATIVA) add(OnboardingStep.Schedule)
                if (!preenchido.pixError) add(OnboardingStep.Bank)
            }
            _state.value = preenchido.copy(done = done, current = OnboardingStep.entries.firstOrNull { it !in done })
        }
    }

    fun open(step: OnboardingStep) = _state.update { it.copy(current = step, showErrors = false) }
    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setDocument(v: String) = _state.update { it.copy(document = v) }
    fun setLogradouro(v: String) = _state.update { it.copy(logradouro = v) }
    fun setNumero(v: String) = _state.update { it.copy(numero = v) }
    fun setBairro(v: String) = _state.update { it.copy(bairro = v) }
    fun setCep(v: String) = _state.update { it.copy(cep = v.filter(Char::isDigit).take(8)) }
    fun setTelefone(v: String) = _state.update { it.copy(telefone = v) }
    fun toggleComodidade(nome: String) = _state.update {
        it.copy(comodidades = if (nome in it.comodidades) it.comodidades - nome else it.comodidades + nome)
    }
    fun setHorario(v: String) = _state.update { it.copy(horario = v) }
    fun setPixKey(v: String) = _state.update { it.copy(pixKey = v) }

    /** Valida, persiste na arena e conclui a etapa atual; abre a proxima pendente. */
    fun saveStep() {
        val s = _state.value
        val step = s.current ?: return
        val arenaId = s.arenaId ?: return
        val valid = when (step) {
            OnboardingStep.Info -> !s.infoError
            OnboardingStep.Bank -> !s.pixError
            OnboardingStep.Courts, OnboardingStep.Schedule -> true
        }
        if (!valid) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        viewModelScope.launch {
            when (step) {
                // documento (CPF do responsavel) nao e persistido: o modelo so guarda o CNPJ da arena (documento),
                // um dado diferente que ja vem preenchido pelo comercial; aqui so validamos o CPF digitado.
                OnboardingStep.Info -> arenas.atualizarArena(arenaId) {
                    it.copy(
                        nome = s.name, telefone = s.telefone,
                        endereco = Endereco(logradouro = s.logradouro, numero = s.numero, bairro = s.bairro, cep = s.cep),
                        comodidades = s.comodidades.toList(), horarioFuncionamento = s.horario,
                    )
                }
                OnboardingStep.Bank -> arenas.atualizarArena(arenaId) { it.copy(chavePix = s.pixKey) }
                OnboardingStep.Courts, OnboardingStep.Schedule -> Unit
            }
            val done = s.done + step
            _state.update { it.copy(done = done, showErrors = false, current = OnboardingStep.entries.firstOrNull { e -> e !in done }) }
        }
    }

    fun publish() {
        val s = _state.value
        val arenaId = s.arenaId ?: return
        if (!s.canPublish) return
        viewModelScope.launch {
            arenas.atualizarArena(arenaId) { it.copy(status = StatusArena.ATIVA) }
            _state.update { it.copy(published = true, current = null) }
        }
    }
}
