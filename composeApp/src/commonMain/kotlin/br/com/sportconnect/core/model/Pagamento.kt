package br.com.sportconnect.core.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class StatusPagamento {
    CRIADO, AGUARDANDO_CONFIRMACAO, CONFIRMADO, FALHOU, EXPIRADO, ESTORNO_EM_PROCESSAMENTO, ESTORNADO, ESTORNO_FALHOU
}

@Serializable
data class Pagamento(
    val id: String,
    val jogoId: String,
    val cotaId: String,
    val pagadorId: String,
    val metodo: MetodoPagamento,
    /** Valor total cobrado (cota + taxa). */
    val valor: Money,
    val taxaConveniencia: Money = Money.ZERO,
    val status: StatusPagamento = StatusPagamento.CRIADO,
    val gatewayChargeId: String,
    val idempotencyKey: String,
    val criadoEm: Instant,
    val expiraEm: Instant? = null,
    val pixCopiaECola: String? = null,
    val confirmadoEm: Instant? = null,
    val estornadoEm: Instant? = null,
)

@Serializable
enum class StatusRepasse { AGENDADO, LIQUIDADO, RETIDO }

/** Repasse a arena (RN-30): liquido = V − comissao (RN-07a). */
@Serializable
data class Repasse(
    val id: String,
    val arenaId: String,
    val jogoId: String,
    val valorBruto: Money,
    val comissao: Money,
    val valorLiquido: Money,
    val dataPrevista: LocalDate,
    val status: StatusRepasse = StatusRepasse.AGENDADO,
    val liquidadoEm: Instant? = null,
    val descricao: String = "",
)
