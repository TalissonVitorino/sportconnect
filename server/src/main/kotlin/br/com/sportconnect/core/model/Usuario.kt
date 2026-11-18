package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class PapelUsuario { ATLETA, ORGANIZADOR, GESTOR_ARENA, OPERADOR_ARENA }

/** Quem executa uma acao (auditoria RN-34, cancelamento RN-23/RN-24). */
@Serializable
enum class Ator { ORGANIZADOR, ATLETA, ARENA, SUPORTE, SISTEMA }

@Serializable
data class Consentimento(val tipo: String, val concedido: Boolean, val em: Instant)

@Serializable
data class Usuario(
    val id: String,
    val nome: String,
    val celular: String,
    val email: String? = null,
    val cpf: String? = null,
    val celularVerificado: Boolean = true,
    val papeis: Set<PapelUsuario> = setOf(PapelUsuario.ATLETA),
    val consentimentos: List<Consentimento> = emptyList(),
    val criadoEm: Instant,
    /** Arena gerida (apenas GESTOR_ARENA/OPERADOR_ARENA). */
    val arenaId: String? = null,
    val avatarUrl: String? = null,
) {
    val primeiroNome: String get() = nome.substringBefore(' ')
    val iniciais: String
        get() = nome.split(' ').filter { it.isNotBlank() }.let { partes ->
            (partes.firstOrNull()?.take(1).orEmpty() + if (partes.size > 1) partes.last().take(1) else "").uppercase()
        }
}
