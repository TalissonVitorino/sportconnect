package br.com.sportconnect.core.domain.repository

import br.com.sportconnect.core.model.Usuario
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Instant

/** Canal de envio do codigo de verificacao (09 ficha 2). */
enum class CanalOtp(val analytics: String) { SMS("sms"), WHATSAPP("whatsapp") }

/** Falhas de autenticacao que a tela trata com texto proprio (07 §4, US-002). */
sealed class AuthFailure(message: String) : Exception(message) {
    class PhoneInvalid : AuthFailure("Celular inválido. Use DDD + número.")
    class CodeInvalid : AuthFailure("Código inválido.")
    class CodeExpired : AuthFailure("O código expirou.")
    /** Bloqueio apos [AuthRules.MAX_TENTATIVAS] erros; libera em [retryAt]. */
    class TooManyAttempts(val retryAt: Instant) : AuthFailure("Muitas tentativas seguidas.")
    class NoConnection : AuthFailure("Sem conexão.")
}

object AuthRules {
    /** Codigo vale por 10 min (`error_code_expired_body`). */
    const val OTP_VALIDADE_MIN = 10
    /** 5 erros seguidos bloqueiam por 15 min (US-002). */
    const val MAX_TENTATIVAS = 5
    const val BLOQUEIO_MIN = 15
    /** Versao vigente dos Termos de Uso + Politica de Privacidade (CT-05). */
    const val VERSAO_TERMOS = "2026-11"
}

interface AuthRepository {
    val currentUser: StateFlow<Usuario?>
    suspend fun solicitarOtp(celular: String, canal: CanalOtp = CanalOtp.SMS): Result<Unit>
    suspend fun verificarOtp(celular: String, codigo: String): Result<Usuario>
    /**
     * Login social (B8): o back-end valida o [idToken] (Google/Apple) e devolve o usuario. Sem nome do
     * provedor, o usuario volta como "Convidado" e passa pelo passo "Como a galera te chama?".
     */
    suspend fun entrarComProvedor(provedor: String, idToken: String, email: String?, nome: String?): Result<Usuario>
    /** Nome/apelido exibido na lista do jogo (passo 3 do cadastro e edicao de perfil) e e-mail opcional. */
    suspend fun atualizarPerfil(nome: String, email: String? = null): Result<Usuario>
    /** Troca o celular da conta: exige [solicitarOtp] para o numero novo e o codigo recebido nele. */
    suspend fun trocarCelular(novoCelular: String, codigo: String): Result<Usuario>
    /**
     * Exclusao da conta (US-003, LGPD art. 18). [aposJogosAte] != null quando ha jogo ativo pago: a exclusao
     * fica agendada para depois dele. Em ambos os casos a sessao e encerrada neste aparelho.
     */
    suspend fun excluirConta(aposJogosAte: Instant?): Result<Unit>
    /** Registra o aceite dos termos e o opt-in de marketing (LGPD, CT-05, US-003). */
    suspend fun registrarConsentimentos(versaoTermos: String, marketing: Boolean): Result<Usuario>
    suspend fun logout()
    /** Demo: alterna entre perfis (organizador, atleta, gestor de arena). */
    suspend fun entrarComo(usuarioId: String): Result<Usuario>
    suspend fun usuario(id: String): Usuario?
}
