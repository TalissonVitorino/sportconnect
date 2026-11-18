package br.com.sportconnect.core.domain.repository

import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Pagamento
import br.com.sportconnect.core.model.StatusPagamento
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/** Cobranca gerada no gateway (Pix: copia-e-cola + expiracao). */
data class Cobranca(
    val pagamentoId: String,
    val jogoId: String,
    val cotaId: String,
    val metodo: MetodoPagamento,
    val valorCota: Money,
    val taxaConveniencia: Money,
    val pixCopiaECola: String?,
    val expiraEm: Instant,
) {
    val total: Money get() = valorCota + taxaConveniencia
}

data class SolicitacaoCobranca(
    val jogoId: String,
    val cotaId: String,
    val pagadorId: String,
    val pagadorNome: String,
    val valorCota: Money,
    val taxaConveniencia: Money,
    val expiraEm: Instant,
    /** Cobertura do organizador (RN-14) — confirma toda a janela, nao uma cota. */
    val cobertura: Boolean = false,
)

/** Notificacao assincrona do gateway (webhook). */
data class WebhookPagamento(
    val pagamentoId: String,
    val jogoId: String,
    val cotaId: String,
    val pagadorId: String,
    val pagadorNome: String,
    val metodo: MetodoPagamento,
    val status: StatusPagamento,
    val em: Instant,
    val cobertura: Boolean = false,
)

interface PaymentGateway {
    suspend fun criarCobrancaPix(solicitacao: SolicitacaoCobranca): Cobranca
    suspend fun criarCobrancaCartao(solicitacao: SolicitacaoCobranca): Cobranca

    /** Stream de webhooks (confirmacao, falha, estorno). */
    val webhooks: Flow<WebhookPagamento>

    fun observePagamento(id: String): Flow<Pagamento?>
    suspend fun pagamento(id: String): Pagamento?

    /**
     * Solicita estorno (RN-25); o pagamento passa por ESTORNO_EM_PROCESSAMENTO -> ESTORNADO.
     * T6 (13): no cartao, a conta de origem pode recusar a devolucao -> ESTORNO_FALHOU.
     */
    suspend fun estornar(pagamentoId: String, valor: Money)

    /** T6 (13): depois de um ESTORNO_FALHOU, tenta de novo enviando o reembolso para uma chave Pix. */
    suspend fun informarChavePixReembolso(pagamentoId: String, chave: String)

    /** Apenas para demo/teste: forca a confirmacao imediata (simula o webhook). */
    suspend fun simularConfirmacao(pagamentoId: String)
}
