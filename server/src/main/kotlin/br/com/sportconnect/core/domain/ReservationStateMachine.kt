package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.Ator
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.MotivoConfirmacao
import br.com.sportconnect.core.model.MotivoExpiracao
import br.com.sportconnect.core.model.OrigemPagamento
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.RegistroTransicao
import br.com.sportconnect.core.model.Reserva
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.core.model.TipoCota
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** Eventos aceitos pela maquina de estados da Reserva (PRD §9.1). */
sealed interface EventoReserva {
    val agora: Instant

    /** RN-09: ativa a pre-reserva (exige cota do organizador PAGA e garantia aprovada). */
    data class Ativar(
        override val agora: Instant,
        val duracaoHoldHoras: Int = HoldDeadlineCalculator.HOLD_PADRAO_HORAS,
        val garantiaAprovada: Boolean = true,
    ) : EventoReserva

    /** Pagamento confirmado pelo gateway para uma cota (padrao ou complemento). */
    data class CotaPaga(
        val cotaId: String,
        override val agora: Instant,
        val atletaId: String? = null,
        val atletaNome: String? = null,
        val metodo: MetodoPagamento = MetodoPagamento.PIX,
        val pagamentoId: String? = null,
    ) : EventoReserva

    /** P atingido (RN-14..RN-16). */
    data class PrazoCotasExpirou(override val agora: Instant) : EventoReserva

    /** Organizador pagou a cobertura (A-Pix na janela, ou residual de C). */
    data class CoberturaPaga(override val agora: Instant, val pagamentoId: String? = null) : EventoReserva

    /** H atingido durante a janela de resolucao. */
    data class PrazoResolucaoExpirou(override val agora: Instant) : EventoReserva

    data class CancelarPor(
        val ator: Ator,
        override val agora: Instant,
        val motivo: String? = null,
        val janelaCancelamentoHoras: Int = 24,
    ) : EventoReserva

    /** RN-21: saida voluntaria antes da confirmacao. */
    data class AtletaSaiu(val cotaId: String, override val agora: Instant) : EventoReserva

    /** RN-29: T atingido. */
    data class IniciarJogo(override val agora: Instant) : EventoReserva

    /** RN-29: T + duracao atingido. */
    data class Concluir(override val agora: Instant) : EventoReserva
}

/** Efeitos colaterais que a camada de servico executa (gateway, slot, analytics, notificacoes). */
sealed interface EfeitoReserva {
    data class Ativada(val prazos: Prazos) : EfeitoReserva
    data class Confirmada(val motivo: MotivoConfirmacao) : EfeitoReserva
    data class Expirada(val motivo: MotivoExpiracao) : EfeitoReserva
    data class Cancelada(val ator: Ator, val estadoAnterior: StatusReserva) : EfeitoReserva
    data class Reembolsar(val reembolso: Reembolso, val pagamentoId: String?) : EfeitoReserva
    data class CobrarCobertura(val valor: Money, val ate: Instant) : EfeitoReserva
    data class PropostaRerrateio(val cotaId: String, val atletaId: String?, val valor: Money, val ate: Instant) : EfeitoReserva
    data class PoliticaAplicada(val politica: PoliticaNaoPagamento, val resultado: String, val valorCoberto: Money) : EfeitoReserva
    data class VagaLiberada(val novaCotaId: String) : EfeitoReserva
    data class SlotAlterado(val status: SlotStatus) : EfeitoReserva
}

data class ResultadoTransicao(val jogo: Jogo, val efeitos: List<EfeitoReserva>) {
    inline fun <reified T : EfeitoReserva> efeitos(): List<T> = efeitos.filterIsInstance<T>()
}

class TransicaoInvalidaException(
    val estado: StatusReserva,
    val evento: String,
    mensagem: String,
) : IllegalStateException("[$estado + $evento] $mensagem")

/**
 * Maquina de estados pura da Reserva/Jogo. Nao faz I/O: recebe o jogo e um evento e devolve o novo jogo
 * mais os efeitos a executar. Transicoes invalidas lancam [TransicaoInvalidaException]
 * (use [tentarAplicar] para obter um [Result]).
 */
class ReservationStateMachine(
    private val holdCalculator: HoldDeadlineCalculator = HoldDeadlineCalculator(),
    private val refundPolicy: RefundPolicy = RefundPolicy(),
) {

    fun tentarAplicar(jogo: Jogo, evento: EventoReserva): Result<ResultadoTransicao> =
        runCatching { aplicar(jogo, evento) }

    /** Conveniencia: dispara o prazo correto conforme a etapa (P ou H). */
    fun prazoExpirou(jogo: Jogo, agora: Instant): ResultadoTransicao = when (jogo.etapaPrazo) {
        EtapaPrazo.JANELA_RESOLUCAO -> aplicar(jogo, EventoReserva.PrazoResolucaoExpirou(agora))
        else -> aplicar(jogo, EventoReserva.PrazoCotasExpirou(agora))
    }

    fun aplicar(jogo: Jogo, evento: EventoReserva): ResultadoTransicao {
        if (jogo.status.terminal) falha(jogo, evento, "Reserva em estado terminal não aceita eventos.")
        return when (evento) {
            is EventoReserva.Ativar -> ativar(jogo, evento)
            is EventoReserva.CotaPaga -> cotaPaga(jogo, evento)
            is EventoReserva.PrazoCotasExpirou -> prazoCotas(jogo, evento)
            is EventoReserva.CoberturaPaga -> coberturaPaga(jogo, evento)
            is EventoReserva.PrazoResolucaoExpirou -> prazoResolucao(jogo, evento)
            is EventoReserva.CancelarPor -> cancelar(jogo, evento)
            is EventoReserva.AtletaSaiu -> atletaSaiu(jogo, evento)
            is EventoReserva.IniciarJogo -> iniciar(jogo, evento)
            is EventoReserva.Concluir -> concluir(jogo, evento)
        }
    }

    // ---------------------------------------------------------------- transicoes

    private fun ativar(jogo: Jogo, e: EventoReserva.Ativar): ResultadoTransicao {
        exigir(jogo, e, jogo.status == StatusReserva.RASCUNHO, "Só é possível ativar a partir de RASCUNHO.")
        val org = jogo.cotaOrganizador
        exigir(jogo, e, org?.status == StatusCota.PAGA, "A cota do organizador precisa estar paga (RN-09).")
        val exigeGarantia = jogo.politica == PoliticaNaoPagamento.ORGANIZADOR_COBRE && jogo.garantia == GarantiaCobertura.CARTAO
        exigir(jogo, e, !exigeGarantia || e.garantiaAprovada, "Pré-autorização do cartão não aprovada (RN-09b).")

        val todasPagas = jogo.reserva.cotas.none { it.status == StatusCota.PENDENTE }
        if (todasPagas) {
            // Pagamento integral (RN-12) ou N = 1: nasce confirmado.
            val r = jogo.reserva.copy(ativadaEm = e.agora)
            val novo = confirmar(jogo.copy(reserva = r), e.agora, MotivoConfirmacao.INTEGRAL, Ator.ORGANIZADOR)
            return ResultadoTransicao(novo, listOf(EfeitoReserva.Confirmada(MotivoConfirmacao.INTEGRAL), EfeitoReserva.SlotAlterado(SlotStatus.RESERVADO)))
        }

        val prazos = holdCalculator.calcular(e.agora, jogo.inicio, jogo.politica, jogo.garantia, e.duracaoHoldHoras)
        exigir(
            jogo, e, prazos.cotas - e.agora >= HoldDeadlineCalculator.JANELA_COLETA_MINIMA,
            "Janela de coleta menor que 30 min: política indisponível, só pagamento integral (RN-12).",
        )
        val reserva = jogo.reserva.copy(
            ativadaEm = e.agora,
            prazoHold = prazos.hold,
            prazoCotas = prazos.cotas,
            etapaPrazo = EtapaPrazo.COLETANDO_COTAS,
        )
        val novo = mudarStatus(jogo.copy(reserva = reserva), StatusReserva.AGUARDANDO_PAGAMENTOS, e.agora, Ator.ORGANIZADOR)
        return ResultadoTransicao(novo, listOf(EfeitoReserva.Ativada(prazos), EfeitoReserva.SlotAlterado(SlotStatus.PRE_RESERVADO)))
    }

    private fun cotaPaga(jogo: Jogo, e: EventoReserva.CotaPaga): ResultadoTransicao {
        val cota = jogo.reserva.cotas.firstOrNull { it.id == e.cotaId } ?: falha(jogo, e, "Cota ${e.cotaId} inexistente.")
        exigir(jogo, e, cota.status == StatusCota.PENDENTE, "Cota não está PENDENTE (está ${cota.status}).")

        when (jogo.status) {
            StatusReserva.RASCUNHO -> {
                exigir(jogo, e, cota.organizador && cota.tipo == TipoCota.PADRAO, "Em RASCUNHO só a cota do organizador pode ser paga (RN-09).")
                val paga = pagar(cota, e, jogo.organizadorId, jogo.organizadorNome)
                return ResultadoTransicao(substituirCota(jogo, paga), emptyList())
            }
            StatusReserva.AGUARDANDO_PAGAMENTOS -> Unit
            else -> falha(jogo, e, "Pagamento de cota não aceito em ${jogo.status} (RN-18: estornar).")
        }

        val emJanela = jogo.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO
        if (emJanela) {
            exigir(jogo, e, cota.tipo == TipoCota.COMPLEMENTO, "Na janela de resolução só complementos de re-rateio são pagos.")
            exigir(jogo, e, e.agora <= jogo.reserva.prazoHold!!, "Prazo H encerrado.")
        } else {
            exigir(jogo, e, cota.tipo == TipoCota.PADRAO, "Complemento só existe na janela de resolução.")
            exigir(jogo, e, e.agora <= jogo.reserva.prazoCotas!!, "Prazo das cotas (P) encerrado (RN-18).")
            if (e.atletaId != null) {
                val jaTem = jogo.reserva.cotas.any {
                    it.id != cota.id && it.tipo == TipoCota.PADRAO && it.status == StatusCota.PAGA && it.atletaId == e.atletaId
                }
                exigir(jogo, e, !jaTem, "Uma cota por pessoa (RN-20).")
            }
        }

        val paga = pagar(cota, e, e.atletaId ?: cota.atletaId, e.atletaNome ?: cota.atletaNome)
        val atualizado = substituirCota(jogo, paga)
        if (atualizado.reserva.cotas.none { it.status == StatusCota.PENDENTE }) {
            val motivo = if (emJanela) MotivoConfirmacao.RERRATEIO else MotivoConfirmacao.TODAS_PAGAS
            val novo = confirmar(atualizado, e.agora, motivo, Ator.SISTEMA)
            return ResultadoTransicao(novo, listOf(EfeitoReserva.Confirmada(motivo), EfeitoReserva.SlotAlterado(SlotStatus.RESERVADO)))
        }
        return ResultadoTransicao(atualizado, emptyList())
    }

    private fun prazoCotas(jogo: Jogo, e: EventoReserva.PrazoCotasExpirou): ResultadoTransicao {
        exigir(
            jogo, e,
            jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.etapaPrazo == EtapaPrazo.COLETANDO_COTAS,
            "Prazo das cotas só se aplica em AGUARDANDO_PAGAMENTOS/COLETANDO_COTAS.",
        )
        exigir(jogo, e, e.agora >= jogo.reserva.prazoCotas!!, "P ainda não foi atingido.")
        val pendentes = jogo.reserva.cotas.filter { it.status == StatusCota.PENDENTE }
        if (pendentes.isEmpty()) {
            val novo = confirmar(jogo, e.agora, MotivoConfirmacao.TODAS_PAGAS, Ator.SISTEMA)
            return ResultadoTransicao(novo, listOf(EfeitoReserva.Confirmada(MotivoConfirmacao.TODAS_PAGAS), EfeitoReserva.SlotAlterado(SlotStatus.RESERVADO)))
        }
        val faltante = Money(pendentes.sumOf { it.valor.cents })

        return when (jogo.politica) {
            PoliticaNaoPagamento.ORGANIZADOR_COBRE -> if (jogo.garantia == GarantiaCobertura.CARTAO) {
                // RN-14 cartao: captura imediata do faltante.
                val coberto = cobrirPendentes(jogo, e.agora, null)
                val novo = confirmar(coberto, e.agora, MotivoConfirmacao.COBERTURA, Ator.SISTEMA)
                ResultadoTransicao(
                    novo,
                    listOf(
                        EfeitoReserva.PoliticaAplicada(jogo.politica, "coberto", faltante),
                        EfeitoReserva.Confirmada(MotivoConfirmacao.COBERTURA),
                        EfeitoReserva.SlotAlterado(SlotStatus.RESERVADO),
                    ),
                )
            } else {
                // RN-14 Pix: janela de resolucao de 30 min com cobranca ao organizador ate H.
                val novo = jogo.copy(reserva = jogo.reserva.copy(etapaPrazo = EtapaPrazo.JANELA_RESOLUCAO, version = jogo.reserva.version + 1))
                ResultadoTransicao(novo, listOf(EfeitoReserva.CobrarCobertura(faltante, jogo.reserva.prazoHold!!)))
            }

            PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR -> expirar(jogo, e.agora, MotivoExpiracao.POLITICA_B, listOf(EfeitoReserva.PoliticaAplicada(jogo.politica, "expirado", Money.ZERO)))

            PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES -> rerratear(jogo, e, pendentes, faltante)
        }
    }

    private fun rerratear(jogo: Jogo, e: EventoReserva, pendentes: List<Cota>, faltante: Money): ResultadoTransicao {
        val pagantes = jogo.reserva.cotas.filter { it.status == StatusCota.PAGA && it.tipo == TipoCota.PADRAO }
        val k = pagantes.size
        val base = if (k == 0) Long.MAX_VALUE else faltante.cents / k
        val sobra = if (k == 0) 0 else faltante.cents - base * k
        val teto = jogo.valorCotaConvidado.cents * jogo.tetoComplementoPct / 100
        val viavel = k > 0 && base <= teto && k >= jogo.esporte.minVagas
        if (!viavel) {
            // Fallback para B (RN-16).
            return expirar(jogo, e.agora, MotivoExpiracao.POLITICA_B, listOf(EfeitoReserva.PoliticaAplicada(jogo.politica, "expirado", Money.ZERO)))
        }
        val hold = jogo.reserva.prazoHold!!
        val idsPendentes = pendentes.map { it.id }.toSet()
        val semPendentes = jogo.reserva.cotas.map { if (it.id in idsPendentes) it.copy(status = StatusCota.EXPIRADA, checkoutAte = null) else it }
        val complementos = pagantes.map { p ->
            Cota(
                id = "${p.id}-comp",
                valor = Money(base + if (p.organizador) sobra else 0),
                tipo = TipoCota.COMPLEMENTO,
                atletaId = p.atletaId,
                atletaNome = p.atletaNome,
                organizador = p.organizador,
                cotaOriginalId = p.id,
                preAceiteRerrateio = p.preAceiteRerrateio,
            )
        }
        // Caso o organizador nao tenha cota paga entre os pagantes (improvavel), a sobra vai para o 1o.
        val ajustados = if (complementos.none { it.organizador } && sobra > 0) {
            complementos.mapIndexed { i, c -> if (i == 0) c.copy(valor = c.valor + Money(sobra)) else c }
        } else complementos
        val reserva = jogo.reserva.copy(
            cotas = semPendentes + ajustados,
            etapaPrazo = EtapaPrazo.JANELA_RESOLUCAO,
            version = jogo.reserva.version + 1,
        )
        val efeitos = buildList<EfeitoReserva> {
            add(EfeitoReserva.PoliticaAplicada(jogo.politica, "rerrateado", Money.ZERO))
            ajustados.forEach { add(EfeitoReserva.PropostaRerrateio(it.id, it.atletaId, it.valor, hold)) }
        }
        return ResultadoTransicao(jogo.copy(reserva = reserva), efeitos)
    }

    private fun coberturaPaga(jogo: Jogo, e: EventoReserva.CoberturaPaga): ResultadoTransicao {
        exigir(
            jogo, e,
            jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO,
            "Cobertura só é possível na janela de resolução.",
        )
        exigir(jogo, e, e.agora <= jogo.reserva.prazoHold!!, "Prazo H encerrado.")
        val faltante = jogo.valorPendente
        val coberto = cobrirPendentes(jogo, e.agora, e.pagamentoId)
        val novo = confirmar(coberto, e.agora, MotivoConfirmacao.COBERTURA, Ator.ORGANIZADOR)
        return ResultadoTransicao(
            novo,
            listOf(
                EfeitoReserva.PoliticaAplicada(jogo.politica, "coberto", faltante),
                EfeitoReserva.Confirmada(MotivoConfirmacao.COBERTURA),
                EfeitoReserva.SlotAlterado(SlotStatus.RESERVADO),
            ),
        )
    }

    private fun prazoResolucao(jogo: Jogo, e: EventoReserva.PrazoResolucaoExpirou): ResultadoTransicao {
        exigir(
            jogo, e,
            jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO,
            "Prazo H de resolução só se aplica na JANELA_RESOLUCAO.",
        )
        exigir(jogo, e, e.agora >= jogo.reserva.prazoHold!!, "H ainda não foi atingido.")
        val motivo = if (jogo.politica == PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES) MotivoExpiracao.RERRATEIO_FALHOU else MotivoExpiracao.COBERTURA_NAO_PAGA
        return expirar(jogo, e.agora, motivo, emptyList())
    }

    private fun cancelar(jogo: Jogo, e: EventoReserva.CancelarPor): ResultadoTransicao {
        val situacao = when (e.ator) {
            Ator.ORGANIZADOR -> SituacaoReembolso.CANCELAMENTO_ORGANIZADOR
            Ator.ARENA, Ator.SUPORTE -> SituacaoReembolso.CANCELAMENTO_ARENA
            else -> falha(jogo, e, "${e.ator} não pode cancelar a reserva.")
        }
        when (jogo.status) {
            StatusReserva.AGUARDANDO_PAGAMENTOS -> Unit
            StatusReserva.CONFIRMADO -> if (e.ator == Ator.ORGANIZADOR) {
                exigir(
                    jogo, e, jogo.inicio - e.agora >= e.janelaCancelamentoHoras.hours,
                    "Fora da janela de cancelamento gratuito (${e.janelaCancelamentoHoras}h) — só a arena ou o suporte podem cancelar (RN-23).",
                )
            }
            else -> falha(jogo, e, "Cancelamento não permitido em ${jogo.status}.")
        }
        if (e.ator == Ator.ARENA) exigir(jogo, e, !e.motivo.isNullOrBlank(), "A arena deve informar o motivo (RN-24).")

        val anterior = jogo.status
        val (cotas, reembolsos) = encerrarCotas(jogo.reserva.cotas, situacao)
        val reserva = jogo.reserva.copy(
            cotas = cotas,
            etapaPrazo = null,
            motivoCancelamento = e.motivo,
            canceladoPor = e.ator,
        )
        val novo = mudarStatus(jogo.copy(reserva = reserva), StatusReserva.CANCELADO, e.agora, e.ator, e.motivo)
        return ResultadoTransicao(
            novo,
            listOf(EfeitoReserva.Cancelada(e.ator, anterior), EfeitoReserva.SlotAlterado(SlotStatus.LIVRE)) + reembolsos,
        )
    }

    private fun atletaSaiu(jogo: Jogo, e: EventoReserva.AtletaSaiu): ResultadoTransicao {
        exigir(
            jogo, e,
            jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.etapaPrazo == EtapaPrazo.COLETANDO_COTAS,
            "Saída com reembolso só antes da confirmação (RN-21/RN-22).",
        )
        exigir(jogo, e, e.agora <= jogo.reserva.prazoCotas!!, "Saída permitida apenas até P (RN-21).")
        val cota = jogo.reserva.cotas.firstOrNull { it.id == e.cotaId } ?: falha(jogo, e, "Cota inexistente.")
        exigir(jogo, e, !cota.organizador, "O organizador não pode sair do próprio jogo; ele deve cancelar.")
        exigir(jogo, e, cota.status == StatusCota.PAGA && cota.tipo == TipoCota.PADRAO, "Somente cota PAGA pode sair.")
        val reembolso = refundPolicy.calcular(cota, SituacaoReembolso.SAIDA_ATLETA)!!
        val nova = Cota(id = "${jogo.reserva.id}-c${jogo.reserva.cotas.size}", valor = cota.valor)
        val cotas = jogo.reserva.cotas.map { if (it.id == cota.id) it.copy(status = StatusCota.REEMBOLSADA) else it } + nova
        val novo = jogo.copy(reserva = jogo.reserva.copy(cotas = cotas, version = jogo.reserva.version + 1))
        return ResultadoTransicao(novo, listOf(EfeitoReserva.Reembolsar(reembolso, cota.pagamentoId), EfeitoReserva.VagaLiberada(nova.id)))
    }

    private fun iniciar(jogo: Jogo, e: EventoReserva.IniciarJogo): ResultadoTransicao {
        exigir(jogo, e, jogo.status == StatusReserva.CONFIRMADO, "Só jogos CONFIRMADO podem iniciar.")
        exigir(jogo, e, e.agora >= jogo.inicio, "Ainda não chegou o horário de início (T).")
        return ResultadoTransicao(mudarStatus(jogo, StatusReserva.EM_ANDAMENTO, e.agora, Ator.SISTEMA), emptyList())
    }

    private fun concluir(jogo: Jogo, e: EventoReserva.Concluir): ResultadoTransicao {
        exigir(jogo, e, jogo.status == StatusReserva.EM_ANDAMENTO, "Só jogos EM_ANDAMENTO podem ser concluídos.")
        exigir(jogo, e, e.agora >= jogo.fim, "O horário ainda não terminou.")
        return ResultadoTransicao(mudarStatus(jogo, StatusReserva.CONCLUIDO, e.agora, Ator.SISTEMA), emptyList())
    }

    // ---------------------------------------------------------------- helpers

    private fun expirar(jogo: Jogo, agora: Instant, motivo: MotivoExpiracao, extra: List<EfeitoReserva>): ResultadoTransicao {
        val (cotas, reembolsos) = encerrarCotas(jogo.reserva.cotas, SituacaoReembolso.EXPIRACAO)
        val reserva = jogo.reserva.copy(cotas = cotas, etapaPrazo = null, motivoExpiracao = motivo)
        val novo = mudarStatus(jogo.copy(reserva = reserva), StatusReserva.EXPIRADO, agora, Ator.SISTEMA, motivo.analytics)
        return ResultadoTransicao(novo, extra + listOf(EfeitoReserva.Expirada(motivo), EfeitoReserva.SlotAlterado(SlotStatus.LIVRE)) + reembolsos)
    }

    /** PAGA -> REEMBOLSADA (com efeito de reembolso); PENDENTE -> EXPIRADA. */
    private fun encerrarCotas(cotas: List<Cota>, situacao: SituacaoReembolso): Pair<List<Cota>, List<EfeitoReserva.Reembolsar>> {
        val reembolsos = mutableListOf<EfeitoReserva.Reembolsar>()
        val novas = cotas.map { c ->
            when (c.status) {
                StatusCota.PAGA -> {
                    refundPolicy.calcular(c, situacao)?.let { reembolsos += EfeitoReserva.Reembolsar(it, c.pagamentoId) }
                    c.copy(status = StatusCota.REEMBOLSADA)
                }
                StatusCota.PENDENTE -> c.copy(status = StatusCota.EXPIRADA, checkoutAte = null)
                else -> c
            }
        }
        return novas to reembolsos
    }

    private fun cobrirPendentes(jogo: Jogo, agora: Instant, pagamentoId: String?): Jogo {
        val cotas = jogo.reserva.cotas.map {
            if (it.status == StatusCota.PENDENTE) {
                it.copy(
                    status = StatusCota.PAGA,
                    origem = OrigemPagamento.COBERTURA,
                    atletaId = if (it.tipo == TipoCota.COMPLEMENTO) it.atletaId else jogo.organizadorId,
                    atletaNome = if (it.tipo == TipoCota.COMPLEMENTO) it.atletaNome else jogo.organizadorNome,
                    taxaConveniencia = Money.ZERO,
                    pagaEm = agora,
                    checkoutAte = null,
                    pagamentoId = pagamentoId,
                )
            } else it
        }
        return jogo.copy(reserva = jogo.reserva.copy(cotas = cotas))
    }

    private fun pagar(cota: Cota, e: EventoReserva.CotaPaga, atletaId: String?, atletaNome: String?): Cota = cota.copy(
        status = StatusCota.PAGA,
        origem = OrigemPagamento.ATLETA,
        atletaId = atletaId,
        atletaNome = atletaNome,
        metodo = e.metodo,
        // RN-07: complementos de re-rateio nao pagam taxa.
        taxaConveniencia = if (cota.tipo == TipoCota.COMPLEMENTO) Money.ZERO else TaxaConveniencia.para(e.metodo),
        pagaEm = e.agora,
        checkoutAte = null,
        pagamentoId = e.pagamentoId,
    )

    private fun substituirCota(jogo: Jogo, cota: Cota): Jogo = jogo.copy(
        reserva = jogo.reserva.copy(
            cotas = jogo.reserva.cotas.map { if (it.id == cota.id) cota else it },
            version = jogo.reserva.version + 1,
        ),
    )

    private fun confirmar(jogo: Jogo, agora: Instant, motivo: MotivoConfirmacao, ator: Ator): Jogo {
        val r = jogo.reserva.copy(etapaPrazo = null, motivoConfirmacao = motivo, confirmadaEm = agora)
        return mudarStatus(jogo.copy(reserva = r), StatusReserva.CONFIRMADO, agora, ator, motivo.analytics)
    }

    private fun mudarStatus(jogo: Jogo, para: StatusReserva, agora: Instant, ator: Ator, motivo: String? = null): Jogo {
        val r = jogo.reserva
        return jogo.copy(
            reserva = r.copy(
                status = para,
                historico = r.historico + RegistroTransicao(r.status, para, agora, ator, motivo),
                version = r.version + 1,
            ),
        )
    }

    private fun nomeEvento(e: EventoReserva): String = e::class.simpleName ?: "Evento"

    private fun falha(jogo: Jogo, e: EventoReserva, msg: String): Nothing =
        throw TransicaoInvalidaException(jogo.status, nomeEvento(e), msg)

    private fun exigir(jogo: Jogo, e: EventoReserva, condicao: Boolean, msg: String) {
        if (!condicao) falha(jogo, e, msg)
    }

    companion object {
        /** Status do slot associado a cada estado da reserva (PRD §9.1). */
        fun slotStatusPara(status: StatusReserva): SlotStatus = when (status) {
            StatusReserva.RASCUNHO -> SlotStatus.EM_CHECKOUT
            StatusReserva.AGUARDANDO_PAGAMENTOS -> SlotStatus.PRE_RESERVADO
            StatusReserva.CONFIRMADO, StatusReserva.EM_ANDAMENTO, StatusReserva.CONCLUIDO -> SlotStatus.RESERVADO
            StatusReserva.CANCELADO, StatusReserva.EXPIRADO -> SlotStatus.LIVRE
        }
    }
}
