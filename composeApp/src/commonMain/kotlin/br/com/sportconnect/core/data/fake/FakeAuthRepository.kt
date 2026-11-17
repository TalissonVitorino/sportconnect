package br.com.sportconnect.core.data.fake

import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthFailure
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.AuthRules
import br.com.sportconnect.core.domain.repository.CanalOtp
import br.com.sportconnect.core.model.Consentimento
import br.com.sportconnect.core.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Autenticacao fake. OTP valido: [CODIGO_OTP_DEMO].
 *
 * - Sem [prefs] (testes): inicia logado como o organizador da demo, como antes.
 * - Com [prefs] (app): restaura a sessao salva; numa instalacao nova comeca deslogado, entao o fluxo
 *   real e Onboarding -> Entrar com celular -> Home. Toda troca de usuario e salva.
 * - Regras do US-002: codigo vale 10 min; 5 erros seguidos bloqueiam por 15 min.
 */
class FakeAuthRepository(
    seed: DemoData,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
    private val prefs: AppPreferences? = null,
) : AuthRepository {
    private val usuarios = seed.usuarios.associateBy { it.id }.toMutableMap()
    private val _current = MutableStateFlow(initialUser(seed))
    override val currentUser: StateFlow<Usuario?> = _current.asStateFlow()

    private var otpEnviadoEm: Instant? = null
    private var otpCelular: String? = null
    private var tentativas = 0
    private var bloqueadoAte: Instant? = null
    private var envios = 0

    private fun initialUser(seed: DemoData): Usuario? {
        if (prefs == null) return seed.organizador
        val saved = prefs.session ?: return null
        return usuarios[saved.id]?.let { it.copy(nome = saved.nome.ifBlank { it.nome }, celular = saved.celular.ifBlank { it.celular }, email = saved.email ?: it.email) }
            ?: Usuario(id = saved.id, nome = saved.nome, celular = saved.celular, email = saved.email, criadoEm = clock.now())
            .also { usuarios[it.id] = it }
    }

    private fun setCurrent(u: Usuario?) {
        _current.value = u
        if (u != null) usuarios[u.id] = u
        prefs?.session = u?.let { AppPreferences.SavedSession(it.id, it.nome, it.celular, it.email) }
    }

    override suspend fun solicitarOtp(celular: String, canal: CanalOtp): Result<Unit> {
        val digitos = celular.filter { it.isDigit() }
        if (digitos.length !in 10..13) return Result.failure(AuthFailure.PhoneInvalid())
        bloqueio()?.let { return Result.failure(it) }
        if (otpCelular != digitos) tentativas = 0
        otpCelular = digitos
        otpEnviadoEm = clock.now()
        envios++
        analytics.track(AnalyticsEvents.OTP_SOLICITADO, mapOf("canal_otp" to canal.analytics, "tentativa_n" to envios))
        return Result.success(Unit)
    }

    override suspend fun verificarOtp(celular: String, codigo: String): Result<Usuario> {
        bloqueio()?.let { return Result.failure(it) }
        val enviado = otpEnviadoEm
        if (enviado != null && clock.now() - enviado > AuthRules.OTP_VALIDADE_MIN.minutes) {
            analytics.track(AnalyticsEvents.OTP_FALHOU, mapOf("motivo" to "expirado"))
            return Result.failure(AuthFailure.CodeExpired())
        }
        if (codigo != CODIGO_OTP_DEMO) {
            tentativas++
            if (tentativas >= AuthRules.MAX_TENTATIVAS) {
                val ate = clock.now() + AuthRules.BLOQUEIO_MIN.minutes
                bloqueadoAte = ate
                analytics.track(AnalyticsEvents.OTP_FALHOU, mapOf("motivo" to "bloqueado"))
                return Result.failure(AuthFailure.TooManyAttempts(ate))
            }
            analytics.track(AnalyticsEvents.OTP_FALHOU, mapOf("motivo" to "invalido"))
            return Result.failure(AuthFailure.CodeInvalid())
        }
        val digitos = celular.filter { it.isDigit() }
        val usuario = usuarios.values.firstOrNull { it.celular.filter(Char::isDigit).endsWith(digitos.takeLast(9)) }
            ?: Usuario(id = "u-${digitos.takeLast(8)}", nome = NOME_PROVISORIO, celular = "+55$digitos", criadoEm = clock.now())
        setCurrent(usuario)
        analytics.track(AnalyticsEvents.OTP_VERIFICADO, mapOf("tentativas" to tentativas + 1))
        tentativas = 0
        otpEnviadoEm = null
        return Result.success(usuario)
    }

    private fun bloqueio(): AuthFailure.TooManyAttempts? {
        val ate = bloqueadoAte ?: return null
        if (clock.now() >= ate) {
            bloqueadoAte = null
            tentativas = 0
            return null
        }
        return AuthFailure.TooManyAttempts(ate)
    }

    override suspend fun atualizarPerfil(nome: String, email: String?): Result<Usuario> {
        val atual = _current.value ?: return Result.failure(IllegalStateException("Sem sessão"))
        val limpo = nome.trim().replace(Regex("\\s+"), " ")
        if (limpo.length < 2) return Result.failure(IllegalArgumentException("Nome muito curto"))
        val emailLimpo = email?.trim()?.takeIf { it.isNotEmpty() }
        if (emailLimpo != null && !EMAIL.matches(emailLimpo)) return Result.failure(IllegalArgumentException("E-mail inválido"))
        val novo = atual.copy(nome = limpo, email = emailLimpo ?: atual.email.takeIf { email == null })
        setCurrent(novo)
        return Result.success(novo)
    }

    override suspend fun trocarCelular(novoCelular: String, codigo: String): Result<Usuario> {
        val atual = _current.value ?: return Result.failure(IllegalStateException("Sem sessão"))
        bloqueio()?.let { return Result.failure(it) }
        val digitos = novoCelular.filter { it.isDigit() }
        if (digitos.length !in 10..13) return Result.failure(AuthFailure.PhoneInvalid())
        if (otpCelular != digitos) return Result.failure(AuthFailure.CodeExpired())
        val enviado = otpEnviadoEm
        if (enviado != null && clock.now() - enviado > AuthRules.OTP_VALIDADE_MIN.minutes) return Result.failure(AuthFailure.CodeExpired())
        if (codigo != CODIGO_OTP_DEMO) {
            tentativas++
            if (tentativas >= AuthRules.MAX_TENTATIVAS) {
                val ate = clock.now() + AuthRules.BLOQUEIO_MIN.minutes
                bloqueadoAte = ate
                return Result.failure(AuthFailure.TooManyAttempts(ate))
            }
            return Result.failure(AuthFailure.CodeInvalid())
        }
        tentativas = 0
        otpEnviadoEm = null
        val novo = atual.copy(celular = "+55$digitos", celularVerificado = true)
        setCurrent(novo)
        return Result.success(novo)
    }

    override suspend fun excluirConta(aposJogosAte: Instant?): Result<Unit> {
        val atual = _current.value ?: return Result.failure(IllegalStateException("Sem sessão"))
        // Demo: os dados pessoais saem na hora deste aparelho; o servidor real anonimiza em ate 15 dias
        // (ou depois de [aposJogosAte]) e guarda so o que a lei exige dos pagamentos.
        usuarios.remove(atual.id)
        prefs?.acceptedTermsVersion = null
        prefs?.marketingOptIn = false
        setCurrent(null)
        return Result.success(Unit)
    }

    override suspend fun registrarConsentimentos(versaoTermos: String, marketing: Boolean): Result<Usuario> {
        val atual = _current.value ?: return Result.failure(IllegalStateException("Sem sessão"))
        val agora = clock.now()
        val novos = atual.consentimentos.filterNot { it.tipo == TIPO_TERMOS || it.tipo == TIPO_MARKETING } +
            Consentimento("$TIPO_TERMOS:$versaoTermos", true, agora) +
            Consentimento(TIPO_MARKETING, marketing, agora)
        prefs?.acceptedTermsVersion = versaoTermos
        prefs?.marketingOptIn = marketing
        analytics.track(AnalyticsEvents.CONSENTIMENTO_ATUALIZADO, mapOf("tipo_consentimento" to "marketing", "valor" to marketing))
        val novo = atual.copy(consentimentos = novos)
        setCurrent(novo)
        return Result.success(novo)
    }

    override suspend fun logout() {
        setCurrent(null)
    }

    override suspend fun entrarComo(usuarioId: String): Result<Usuario> {
        val u = usuarios[usuarioId] ?: return Result.failure(NoSuchElementException("Usuário $usuarioId"))
        setCurrent(u)
        return Result.success(u)
    }

    override suspend fun usuario(id: String): Usuario? = usuarios[id]

    companion object {
        const val CODIGO_OTP_DEMO = "123456"
        /** Nome de quem acabou de criar conta e ainda nao passou pelo passo "Como a galera te chama?". */
        const val NOME_PROVISORIO = "Convidado"
        private const val TIPO_TERMOS = "termos"
        private const val TIPO_MARKETING = "marketing"
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
