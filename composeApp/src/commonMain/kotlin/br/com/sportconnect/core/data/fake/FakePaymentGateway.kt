package br.com.sportconnect.core.data.fake

import br.com.sportconnect.core.domain.repository.Cobranca
import br.com.sportconnect.core.domain.repository.PaymentGateway
import br.com.sportconnect.core.domain.repository.SolicitacaoCobranca
import br.com.sportconnect.core.domain.repository.WebhookPagamento
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Pagamento
import br.com.sportconnect.core.model.StatusPagamento
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Gateway fake: gera um Pix copia-e-cola ficticio (formato EMV/BR Code com CRC16 valido) e, apos
 * [atrasoConfirmacao] (~1 s), confirma o pagamento e emite o webhook — como o PSP real faria.
 * Estornos passam por ESTORNO_EM_PROCESSAMENTO e concluem apos o mesmo atraso.
 */
class FakePaymentGateway(
    private val clock: Clock,
    private val scope: CoroutineScope,
    private val atrasoConfirmacao: Duration = 1.seconds,
    private val confirmarAutomaticamente: Boolean = true,
) : PaymentGateway {

    private val pagamentos = MutableStateFlow<Map<String, Pagamento>>(emptyMap())
    private val solicitacoes = mutableMapOf<String, SolicitacaoCobranca>()
    private val _webhooks = MutableSharedFlow<WebhookPagamento>(extraBufferCapacity = 64)
    override val webhooks: SharedFlow<WebhookPagamento> = _webhooks.asSharedFlow()
    private var seq = 0

    override suspend fun criarCobrancaPix(solicitacao: SolicitacaoCobranca): Cobranca = criar(solicitacao, MetodoPagamento.PIX)
    override suspend fun criarCobrancaCartao(solicitacao: SolicitacaoCobranca): Cobranca = criar(solicitacao, MetodoPagamento.CARTAO)

    private fun criar(s: SolicitacaoCobranca, metodo: MetodoPagamento): Cobranca {
        val n = ++seq
        val id = "pg-${n.toString().padStart(5, '0')}"
        val txid = "SC${s.jogoId.filter { it.isLetterOrDigit() }.takeLast(10).uppercase()}${n.toString().padStart(5, '0')}"
        val total = s.valorCota + s.taxaConveniencia
        val pix = if (metodo == MetodoPagamento.PIX) PixBrCode.gerar(total, txid) else null
        val pagamento = Pagamento(
            id = id, jogoId = s.jogoId, cotaId = s.cotaId, pagadorId = s.pagadorId, metodo = metodo,
            valor = total, taxaConveniencia = s.taxaConveniencia, status = StatusPagamento.AGUARDANDO_CONFIRMACAO,
            gatewayChargeId = "ch_$txid", idempotencyKey = "${s.cotaId}:${s.pagadorId}:$n",
            criadoEm = clock.now(), expiraEm = s.expiraEm, pixCopiaECola = pix,
        )
        pagamentos.update { it + (id to pagamento) }
        solicitacoes[id] = s
        if (confirmarAutomaticamente) {
            scope.launch {
                delay(atrasoConfirmacao)
                simularConfirmacao(id)
            }
        }
        return Cobranca(id, s.jogoId, s.cotaId, metodo, s.valorCota, s.taxaConveniencia, pix, s.expiraEm)
    }

    override fun observePagamento(id: String): Flow<Pagamento?> = pagamentos.map { it[id] }.distinctUntilChanged()
    override suspend fun pagamento(id: String): Pagamento? = pagamentos.value[id]

    override suspend fun simularConfirmacao(pagamentoId: String) {
        val atual = pagamentos.value[pagamentoId] ?: return
        if (atual.status != StatusPagamento.AGUARDANDO_CONFIRMACAO) return
        val agora = clock.now()
        if (atual.expiraEm != null && agora > atual.expiraEm) {
            atualizar(pagamentoId) { it.copy(status = StatusPagamento.EXPIRADO) }
            emitir(pagamentoId, StatusPagamento.EXPIRADO)
            return
        }
        atualizar(pagamentoId) { it.copy(status = StatusPagamento.CONFIRMADO, confirmadoEm = agora) }
        emitir(pagamentoId, StatusPagamento.CONFIRMADO)
    }

    /** Demo/teste: simula recusa do pagamento. */
    suspend fun simularFalha(pagamentoId: String) {
        atualizar(pagamentoId) { it.copy(status = StatusPagamento.FALHOU) }
        emitir(pagamentoId, StatusPagamento.FALHOU)
    }

    override suspend fun estornar(pagamentoId: String, valor: Money) {
        val atual = pagamentos.value[pagamentoId] ?: return // pagamentos da semente nao existem no gateway fake
        if (atual.status != StatusPagamento.CONFIRMADO) return
        atualizar(pagamentoId) { it.copy(status = StatusPagamento.ESTORNO_EM_PROCESSAMENTO) }
        scope.launch {
            delay(atrasoConfirmacao)
            if (atual.metodo == MetodoPagamento.CARTAO) {
                // T6 (13): demo da conta de origem recusando a devolucao no cartao.
                atualizar(pagamentoId) { it.copy(status = StatusPagamento.ESTORNO_FALHOU) }
                emitir(pagamentoId, StatusPagamento.ESTORNO_FALHOU)
            } else {
                atualizar(pagamentoId) { it.copy(status = StatusPagamento.ESTORNADO, estornadoEm = clock.now()) }
                emitir(pagamentoId, StatusPagamento.ESTORNADO)
            }
        }
    }

    override suspend fun informarChavePixReembolso(pagamentoId: String, chave: String) {
        val atual = pagamentos.value[pagamentoId] ?: return
        if (atual.status != StatusPagamento.ESTORNO_FALHOU) return
        atualizar(pagamentoId) { it.copy(status = StatusPagamento.ESTORNO_EM_PROCESSAMENTO) }
        scope.launch {
            delay(atrasoConfirmacao)
            atualizar(pagamentoId) { it.copy(status = StatusPagamento.ESTORNADO, estornadoEm = clock.now()) }
            emitir(pagamentoId, StatusPagamento.ESTORNADO)
        }
    }

    private fun atualizar(id: String, f: (Pagamento) -> Pagamento) {
        pagamentos.update { mapa -> mapa[id]?.let { mapa + (id to f(it)) } ?: mapa }
    }

    private suspend fun emitir(id: String, status: StatusPagamento) {
        val p = pagamentos.value[id] ?: return
        val s = solicitacoes[id] ?: return
        _webhooks.emit(
            WebhookPagamento(
                pagamentoId = id, jogoId = p.jogoId, cotaId = p.cotaId, pagadorId = p.pagadorId, pagadorNome = s.pagadorNome,
                metodo = p.metodo, status = status, em = clock.now(), cobertura = s.cobertura, substituicaoCotaId = s.substituicaoCotaId,
            ),
        )
    }
}

/** Gera um BR Code (Pix copia-e-cola) ficticio porem bem formado (TLV + CRC16-CCITT). */
object PixBrCode {
    private const val CHAVE = "pagamentos@sportconnect.app"
    private const val NOME = "SPORTCONNECT"
    private const val CIDADE = "SAO PAULO"

    private fun tlv(id: String, valor: String) = id + valor.length.toString().padStart(2, '0') + valor

    fun gerar(valor: Money, txid: String): String {
        val reais = "${valor.cents / 100}.${(valor.cents % 100).toString().padStart(2, '0')}"
        val conta = tlv("00", "br.gov.bcb.pix") + tlv("01", CHAVE)
        val semCrc = tlv("00", "01") + tlv("26", conta) + tlv("52", "0000") + tlv("53", "986") + tlv("54", reais) +
            tlv("58", "BR") + tlv("59", NOME) + tlv("60", CIDADE) + tlv("62", tlv("05", txid.take(25))) + "6304"
        return semCrc + crc16(semCrc)
    }

    fun crc16(dados: String): String {
        var crc = 0xFFFF
        for (c in dados.encodeToByteArray()) {
            crc = crc xor ((c.toInt() and 0xFF) shl 8)
            repeat(8) { crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1 }
            crc = crc and 0xFFFF
        }
        return crc.toString(16).uppercase().padStart(4, '0')
    }
}
