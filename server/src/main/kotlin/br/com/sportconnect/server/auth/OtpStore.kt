package br.com.sportconnect.server.auth

import kotlinx.datetime.Instant

/** Resultado de uma tentativa de validação de OTP (docs/15-contrato-api.md §1). */
sealed interface OtpResultado {
    data object Ok : OtpResultado
    data object Invalido : OtpResultado
    data object Expirado : OtpResultado
    /** Bloqueio após [br.com.sportconnect.core.domain.repository.AuthRules.MAX_TENTATIVAS] erros seguidos; libera em [ate]. */
    data class Bloqueado(val ate: Instant) : OtpResultado
}

/**
 * Geração e validação de códigos de uso único (OTP).
 *
 * Interface isolada para que a implementação em memória usada por este esqueleto
 * ([InMemoryOtpStore]) possa ser trocada por uma distribuída (ex.: Redis) sem alterar quem a
 * consome (`authRoutes`).
 */
interface OtpStore {
    fun gerar(celular: String): String
    fun validar(celular: String, codigo: String): OtpResultado
}
