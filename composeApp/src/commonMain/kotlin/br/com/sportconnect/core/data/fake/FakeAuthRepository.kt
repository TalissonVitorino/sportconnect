package br.com.sportconnect.core.data.fake

import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

/**
 * Autenticacao fake: inicia logado como o organizador da demo. OTP valido: [CODIGO_OTP_DEMO].
 */
class FakeAuthRepository(
    seed: DemoData,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : AuthRepository {
    private val usuarios = seed.usuarios.associateBy { it.id }.toMutableMap()
    private val _current = MutableStateFlow<Usuario?>(seed.organizador)
    override val currentUser: StateFlow<Usuario?> = _current.asStateFlow()

    override suspend fun solicitarOtp(celular: String): Result<Unit> {
        val digitos = celular.filter { it.isDigit() }
        if (digitos.length !in 10..13) return Result.failure(IllegalArgumentException("Celular inválido. Use DDD + número."))
        analytics.track(AnalyticsEvents.OTP_SOLICITADO, mapOf("canal_otp" to "sms", "tentativa_n" to 1))
        return Result.success(Unit)
    }

    override suspend fun verificarOtp(celular: String, codigo: String): Result<Usuario> {
        if (codigo != CODIGO_OTP_DEMO) {
            analytics.track(AnalyticsEvents.OTP_FALHOU, mapOf("motivo" to "invalido"))
            return Result.failure(IllegalArgumentException("Código inválido. Confira o SMS e tente de novo."))
        }
        val digitos = celular.filter { it.isDigit() }
        val usuario = usuarios.values.firstOrNull { it.celular.filter(Char::isDigit).endsWith(digitos.takeLast(9)) }
            ?: Usuario(id = "u-${digitos.takeLast(8)}", nome = "Convidado", celular = "+55$digitos", criadoEm = clock.now())
                .also { usuarios[it.id] = it }
        _current.value = usuario
        analytics.track(AnalyticsEvents.OTP_VERIFICADO, mapOf("tentativas" to 1))
        return Result.success(usuario)
    }

    override suspend fun logout() {
        _current.value = null
    }

    override suspend fun entrarComo(usuarioId: String): Result<Usuario> {
        val u = usuarios[usuarioId] ?: return Result.failure(NoSuchElementException("Usuário $usuarioId"))
        _current.value = u
        return Result.success(u)
    }

    override suspend fun usuario(id: String): Usuario? = usuarios[id]

    companion object {
        const val CODIGO_OTP_DEMO = "123456"
    }
}
