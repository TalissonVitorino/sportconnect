package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.ListaEspera
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.OrigemPagamento
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusListaEspera
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.core.model.TipoCota
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ListaEsperaException(mensagem: String) : IllegalStateException(mensagem)

data class ResultadoSubstituicao(val jogo: Jogo, val reembolso: Reembolso)

/**
 * Lista de espera (RN-27) e substituicao (RN-26). Funcoes puras sobre a fila e o jogo.
 * - FIFO por horario de entrada, limite de 10, encerrada em T − 1h.
 * - So abre quando nao ha cota PENDENTE livre.
 * - Ao liberar vaga, o 1o AGUARDANDO recebe exclusividade de 15 min (ou ate P / T−1h, o que vier antes).
 * - Substituicao: em CONFIRMADO ate T − 1h; quando o substituto paga, a cota original e REEMBOLSADA sem taxa.
 */
class WaitlistManager(private val refundPolicy: RefundPolicy = RefundPolicy()) {

    fun fechamentoDaLista(jogo: Jogo): Instant = jogo.inicio - ANTECEDENCIA_FECHAMENTO

    fun ativos(fila: List<ListaEspera>): List<ListaEspera> =
        fila.filter { it.status == StatusListaEspera.AGUARDANDO || it.status == StatusListaEspera.NOTIFICADO }
            .sortedBy { it.entrouEm }

    fun podeEntrar(jogo: Jogo, fila: List<ListaEspera>, agora: Instant): Boolean =
        runCatching { validarEntrada(jogo, fila, "", agora) }.isSuccess

    fun entrar(jogo: Jogo, fila: List<ListaEspera>, usuarioId: String, usuarioNome: String, agora: Instant): List<ListaEspera> {
        validarEntrada(jogo, fila, usuarioId, agora)
        val posicao = ativos(fila).size + 1
        return fila + ListaEspera(
            id = "le-${jogo.id}-${fila.size + 1}",
            jogoId = jogo.id,
            usuarioId = usuarioId,
            usuarioNome = usuarioNome,
            posicao = posicao,
            entrouEm = agora,
        )
    }

    private fun validarEntrada(jogo: Jogo, fila: List<ListaEspera>, usuarioId: String, agora: Instant) {
        if (jogo.status != StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.status != StatusReserva.CONFIRMADO) {
            throw ListaEsperaException("Lista de espera indisponível para jogo ${jogo.status}.")
        }
        if (agora >= fechamentoDaLista(jogo)) throw ListaEsperaException("Lista de espera encerrada (T − 1h).")
        if (jogo.reserva.cotas.any { it.tipo == TipoCota.PADRAO && it.livreParaCheckout(agora) }) {
            throw ListaEsperaException("Ainda há vagas livres: pague a cota diretamente.")
        }
        val ativos = ativos(fila)
        if (ativos.size >= LIMITE) throw ListaEsperaException("Lista de espera cheia ($LIMITE pessoas).")
        if (usuarioId.isNotEmpty() && ativos.any { it.usuarioId == usuarioId }) {
            throw ListaEsperaException("Você já está na lista de espera.")
        }
        if (usuarioId.isNotEmpty() && jogo.participa(usuarioId)) throw ListaEsperaException("Você já está no jogo.")
    }

    /** Prazo-limite da exclusividade: P (se ainda coletando) ou T − 1h. */
    fun limiteExclusividade(jogo: Jogo): Instant {
        val t1h = fechamentoDaLista(jogo)
        val p = jogo.reserva.prazoCotas
        return if (jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && p != null) minOf(p, t1h) else t1h
    }

    /** Vaga liberada: notifica o proximo AGUARDANDO (se ninguem estiver NOTIFICADO). */
    fun notificarProximo(jogo: Jogo, fila: List<ListaEspera>, agora: Instant): List<ListaEspera> {
        val ativos = ativos(fila)
        if (ativos.any { it.status == StatusListaEspera.NOTIFICADO }) return fila
        val proximo = ativos.firstOrNull { it.status == StatusListaEspera.AGUARDANDO } ?: return fila
        val limite = limiteExclusividade(jogo)
        if (agora >= limite) return fila
        val ate = minOf(agora + EXCLUSIVIDADE, limite)
        return fila.map {
            if (it.id == proximo.id) it.copy(status = StatusListaEspera.NOTIFICADO, notificadoEm = agora, exclusividadeAte = ate) else it
        }
    }

    /** Exclusividades vencidas -> EXPIRADO; passa ao proximo. */
    fun expirarNotificacoes(jogo: Jogo, fila: List<ListaEspera>, agora: Instant): List<ListaEspera> {
        var houveExpiracao = false
        val atualizada = fila.map {
            if (it.status == StatusListaEspera.NOTIFICADO && it.exclusividadeAte != null && agora >= it.exclusividadeAte) {
                houveExpiracao = true
                it.copy(status = StatusListaEspera.EXPIRADO)
            } else it
        }
        return if (houveExpiracao) notificarProximo(jogo, atualizada, agora) else atualizada
    }

    fun converter(fila: List<ListaEspera>, usuarioId: String): List<ListaEspera> = reposicionar(
        fila.map { if (it.usuarioId == usuarioId && it.status == StatusListaEspera.NOTIFICADO) it.copy(status = StatusListaEspera.CONVERTIDO) else it },
    )

    fun sair(fila: List<ListaEspera>, usuarioId: String): List<ListaEspera> = reposicionar(
        fila.map {
            if (it.usuarioId == usuarioId && (it.status == StatusListaEspera.AGUARDANDO || it.status == StatusListaEspera.NOTIFICADO)) {
                it.copy(status = StatusListaEspera.SAIU)
            } else it
        },
    )

    /** Quem tem exclusividade sobre a vaga agora (se alguem). */
    fun exclusivo(fila: List<ListaEspera>, agora: Instant): ListaEspera? =
        fila.firstOrNull { it.status == StatusListaEspera.NOTIFICADO && (it.exclusividadeAte == null || agora < it.exclusividadeAte) }

    private fun reposicionar(fila: List<ListaEspera>): List<ListaEspera> {
        val ordem = ativos(fila).mapIndexed { i, e -> e.id to i + 1 }.toMap()
        return fila.map { e -> ordem[e.id]?.let { e.copy(posicao = it) } ?: e }
    }

    // ------------------------------------------------------------ substituicao (RN-26)

    fun podeSubstituir(jogo: Jogo, agora: Instant): Boolean =
        jogo.status == StatusReserva.CONFIRMADO && agora < fechamentoDaLista(jogo)

    /**
     * O substituto pagou uma nova cota de mesmo valor: a cota original vira REEMBOLSADA (taxa retida)
     * e uma nova cota PAGA e criada para o substituto. O valor do repasse nao muda.
     */
    fun substituir(
        jogo: Jogo,
        cotaOriginalId: String,
        substitutoId: String,
        substitutoNome: String,
        agora: Instant,
        metodo: MetodoPagamento = MetodoPagamento.PIX,
        pagamentoId: String? = null,
    ): ResultadoSubstituicao {
        if (!podeSubstituir(jogo, agora)) throw ListaEsperaException("Substituição só em jogo CONFIRMADO até T − 1h.")
        val original = jogo.reserva.cotas.firstOrNull { it.id == cotaOriginalId }
            ?: throw ListaEsperaException("Cota inexistente.")
        if (original.status != StatusCota.PAGA || original.tipo != TipoCota.PADRAO) throw ListaEsperaException("Somente cota PAGA pode ser substituída.")
        if (original.organizador) throw ListaEsperaException("O organizador não pode ser substituído.")
        if (jogo.participa(substitutoId)) throw ListaEsperaException("O substituto já está no jogo.")
        val reembolso = refundPolicy.calcular(original, SituacaoReembolso.SUBSTITUICAO)!!
        val nova = Cota(
            id = "${jogo.reserva.id}-c${jogo.reserva.cotas.size}",
            valor = original.valor,
            taxaConveniencia = TaxaConveniencia.para(metodo),
            origem = OrigemPagamento.ATLETA,
            status = StatusCota.PAGA,
            atletaId = substitutoId,
            atletaNome = substitutoNome,
            pagaEm = agora,
            metodo = metodo,
            pagamentoId = pagamentoId,
        )
        val cotas = jogo.reserva.cotas.map { if (it.id == original.id) it.copy(status = StatusCota.REEMBOLSADA) else it } + nova
        val novo = jogo.copy(reserva = jogo.reserva.copy(cotas = cotas, version = jogo.reserva.version + 1))
        return ResultadoSubstituicao(novo, reembolso)
    }

    companion object {
        const val LIMITE = 10
        val EXCLUSIVIDADE: Duration = 15.minutes
        val ANTECEDENCIA_FECHAMENTO: Duration = 1.hours
    }
}
