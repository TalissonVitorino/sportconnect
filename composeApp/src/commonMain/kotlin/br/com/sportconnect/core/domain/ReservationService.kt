package br.com.sportconnect.core.domain

import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.domain.repository.PaymentGateway
import br.com.sportconnect.core.domain.repository.WebhookPagamento
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.Plano
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusPagamento
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.EtapaPrazo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Orquestra a maquina de estados com a persistencia: carrega o jogo, aplica o evento, salva,
 * executa efeitos (slot, estornos, repasse, analytics). Serializa as transicoes com um [Mutex]
 * (no backend isso sera controle otimista via `Reserva.version`).
 */
class ReservationService(
    private val games: GameRepository,
    private val arenas: ArenaRepository,
    private val gateway: PaymentGateway,
    private val analytics: AnalyticsTracker,
    private val stateMachine: ReservationStateMachine,
    private val payoutCalculator: PayoutCalculator,
    private val waitlistManager: WaitlistManager,
    private val clock: Clock,
) {
    private val mutex = Mutex()

    suspend fun aplicar(jogoId: String, vararg eventos: EventoReserva): Result<Jogo> = mutex.withLock {
        aplicarSemLock(jogoId, eventos.toList())
    }

    private suspend fun aplicarSemLock(jogoId: String, eventos: List<EventoReserva>): Result<Jogo> {
        val inicial = games.game(jogoId) ?: return Result.failure(NoSuchElementException("Jogo $jogoId não encontrado."))
        var atual = inicial
        val resultados = mutableListOf<Pair<EventoReserva, ResultadoTransicao>>()
        for (evento in eventos) {
            val r = stateMachine.tentarAplicar(atual, evento).getOrElse { return Result.failure(it) }
            resultados += evento to r
            atual = r.jogo
        }
        games.save(atual)
        resultados.forEach { (evento, r) -> executarEfeitos(inicial, r, evento) }
        return Result.success(atual)
    }

    private suspend fun executarEfeitos(antes: Jogo, r: ResultadoTransicao, evento: EventoReserva) {
        val jogo = r.jogo
        if (evento is EventoReserva.CotaPaga) {
            val cota = jogo.reserva.cotas.first { it.id == evento.cotaId }
            analytics.track(
                AnalyticsEvents.COTA_PAGA,
                mapOf(
                    "jogo_id" to jogo.id, "cota_id" to cota.id, "origem" to "atleta", "tipo_cota" to cota.tipo.name.lowercase(),
                    "posicao" to "${jogo.cotasPagas} de ${jogo.numeroVagas}",
                    "pct_pago_apos" to (jogo.progresso * 100).toInt(),
                ),
            )
        }
        if (evento is EventoReserva.IniciarJogo) analytics.track(AnalyticsEvents.JOGO_INICIADO, mapOf("jogo_id" to jogo.id))
        if (evento is EventoReserva.AtletaSaiu) {
            analytics.track(AnalyticsEvents.ATLETA_SAIU_DO_JOGO, mapOf("jogo_id" to jogo.id, "cota_id" to evento.cotaId, "estado_reserva" to jogo.status.name))
        }
        if (evento is EventoReserva.Concluir) {
            analytics.track(AnalyticsEvents.JOGO_CONCLUIDO, mapOf("jogo_id" to jogo.id, "atletas_pagantes" to jogo.cotasPagas))
            gerarRepasse(jogo, evento.agora)
        }
        for (efeito in r.efeitos) {
            when (efeito) {
                is EfeitoReserva.SlotAlterado -> arenas.atualizarSlot(jogo.slotId) {
                    it.copy(
                        status = efeito.status,
                        jogoId = if (efeito.status == SlotStatus.LIVRE) null else jogo.id,
                        checkoutAte = null,
                    )
                }
                is EfeitoReserva.Reembolsar -> {
                    efeito.pagamentoId?.let { gateway.estornar(it, efeito.reembolso.total) }
                    analytics.track(
                        AnalyticsEvents.REEMBOLSO_SOLICITADO,
                        mapOf(
                            "pagamento_id" to efeito.pagamentoId, "motivo" to efeito.reembolso.situacao.analytics,
                            "valor_centavos" to efeito.reembolso.total.cents, "taxa_devolvida" to efeito.reembolso.devolveTaxa,
                        ),
                    )
                }
                is EfeitoReserva.Ativada -> analytics.track(
                    AnalyticsEvents.PRE_RESERVA_ATIVADA,
                    mapOf(
                        "jogo_id" to jogo.id, "arena_id" to jogo.arenaId, "esporte" to jogo.esporte.name,
                        "numero_vagas" to jogo.numeroVagas, "valor_total_centavos" to jogo.valorTotal.cents,
                        "politica" to jogo.politica.name,
                        "minutos_ate_prazo_cotas" to (efeito.prazos.cotas - evento.agora).inWholeMinutes,
                        "horas_ate_jogo" to (jogo.inicio - evento.agora).inWholeHours,
                    ),
                )
                is EfeitoReserva.Confirmada -> analytics.track(
                    AnalyticsEvents.JOGO_CONFIRMADO,
                    mapOf(
                        "jogo_id" to jogo.id, "arena_id" to jogo.arenaId, "esporte" to jogo.esporte.name,
                        "motivo" to efeito.motivo.analytics,
                        "minutos_ate_confirmar" to jogo.reserva.ativadaEm?.let { (evento.agora - it).inWholeMinutes },
                    ),
                )
                is EfeitoReserva.Expirada -> analytics.track(
                    AnalyticsEvents.RESERVA_EXPIRADA,
                    mapOf(
                        "jogo_id" to jogo.id, "politica" to jogo.politica.name,
                        "pct_pago" to (antes.progresso * 100).toInt(), "motivo" to efeito.motivo.analytics,
                    ),
                )
                is EfeitoReserva.Cancelada -> analytics.track(
                    AnalyticsEvents.JOGO_CANCELADO,
                    mapOf(
                        "jogo_id" to jogo.id, "por" to efeito.ator.name.lowercase(), "motivo" to jogo.reserva.motivoCancelamento,
                        "estado_anterior" to efeito.estadoAnterior.name, "horas_ate_jogo" to (jogo.inicio - evento.agora).inWholeHours,
                    ),
                )
                is EfeitoReserva.PoliticaAplicada -> analytics.track(
                    AnalyticsEvents.POLITICA_APLICADA,
                    mapOf("jogo_id" to jogo.id, "politica" to efeito.politica.name, "resultado" to efeito.resultado, "valor_coberto_centavos" to efeito.valorCoberto.cents),
                )
                is EfeitoReserva.CobrarCobertura -> analytics.track(
                    AnalyticsEvents.PRAZO_COTAS_ATINGIDO,
                    mapOf("jogo_id" to jogo.id, "politica" to jogo.politica.name, "pendentes" to antes.cotasPendentes, "valor_pendente_centavos" to efeito.valor.cents),
                )
                is EfeitoReserva.PropostaRerrateio -> Unit // notificacao push (fora do escopo do app)
                is EfeitoReserva.VagaLiberada -> {
                    val fila = games.observeWaitlistSnapshot(jogo.id)
                    games.saveWaitlist(jogo.id, waitlistManager.notificarProximo(jogo, fila, evento.agora))
                }
            }
        }
    }

    private suspend fun GameRepository.observeWaitlistSnapshot(jogoId: String) =
        observeWaitlist(jogoId).firstOrNull() ?: emptyList()

    private suspend fun gerarRepasse(jogo: Jogo, agora: Instant) {
        val arena = arenas.arena(jogo.arenaId)
        val repasse = payoutCalculator.gerarRepasse(
            id = "rep-${jogo.id}",
            arenaId = jogo.arenaId,
            jogoId = jogo.id,
            valorTotal = jogo.valorTotal,
            plano = arena?.plano ?: Plano.BASICO,
            dataConclusao = agora.toLocalDateTime(FusoSaoPaulo).date,
            descricao = "${jogo.esporte.displayName} · ${jogo.quadraNome}",
        )
        arenas.salvarRepasse(repasse)
    }

    /** Processa um webhook do gateway (confirmacao de cota, cobertura, falha). */
    suspend fun onWebhook(w: WebhookPagamento): Result<Jogo> = mutex.withLock {
        val jogo = games.game(w.jogoId) ?: return@withLock Result.failure(NoSuchElementException("Jogo ${w.jogoId}"))
        when (w.status) {
            StatusPagamento.CONFIRMADO -> {
                analytics.track(
                    AnalyticsEvents.PAGAMENTO_CONFIRMADO,
                    mapOf("pagamento_id" to w.pagamentoId, "metodo" to w.metodo.name.lowercase()),
                )
                if (w.substituicaoCotaId != null) {
                    // T8 (13/US-111): o pagamento do substituto fecha a troca (RN-26) e devolve o dinheiro de quem saiu.
                    return@withLock runCatching {
                        val original = jogo.reserva.cotas.firstOrNull { it.id == w.substituicaoCotaId }
                        val resultado = waitlistManager.substituir(jogo, w.substituicaoCotaId, w.pagadorId, w.pagadorNome, w.em, w.metodo, w.pagamentoId)
                        val salvo = games.update(jogo.id) { resultado.jogo } ?: resultado.jogo
                        original?.pagamentoId?.let { gateway.estornar(it, resultado.reembolso.total) }
                        analytics.track(
                            AnalyticsEvents.REEMBOLSO_SOLICITADO,
                            mapOf("pagamento_id" to (original?.pagamentoId ?: ""), "motivo" to SituacaoReembolso.SUBSTITUICAO.analytics, "taxa_devolvida" to false),
                        )
                        salvo
                    }
                }
                val eventos = when {
                    w.cobertura -> listOf(EventoReserva.CoberturaPaga(w.em, w.pagamentoId))
                    jogo.status == StatusReserva.RASCUNHO -> {
                        val hold = arenas.arena(jogo.arenaId)?.duracaoHoldHoras ?: HoldDeadlineCalculator.HOLD_PADRAO_HORAS
                        listOf(
                            EventoReserva.CotaPaga(w.cotaId, w.em, w.pagadorId, w.pagadorNome, w.metodo, w.pagamentoId),
                            EventoReserva.Ativar(w.em, hold),
                        )
                    }
                    else -> listOf(EventoReserva.CotaPaga(w.cotaId, w.em, w.pagadorId, w.pagadorNome, w.metodo, w.pagamentoId))
                }
                val resultado = aplicarSemLock(jogo.id, eventos)
                if (resultado.isFailure) {
                    // RN-18: pagamento tardio/indevido e estornado integralmente.
                    val pg = gateway.pagamento(w.pagamentoId)
                    if (pg != null) gateway.estornar(pg.id, pg.valor)
                    analytics.track(
                        AnalyticsEvents.REEMBOLSO_SOLICITADO,
                        mapOf("pagamento_id" to w.pagamentoId, "motivo" to SituacaoReembolso.PAGAMENTO_TARDIO.analytics, "taxa_devolvida" to true),
                    )
                }
                resultado
            }
            StatusPagamento.FALHOU, StatusPagamento.EXPIRADO -> {
                analytics.track(AnalyticsEvents.PAGAMENTO_FALHOU, mapOf("pagamento_id" to w.pagamentoId, "metodo" to w.metodo.name.lowercase()))
                val atualizado = games.update(jogo.id) { j ->
                    j.copy(reserva = j.reserva.copy(cotas = j.reserva.cotas.map {
                        if (it.id == w.cotaId && it.status == StatusCota.PENDENTE) it.copy(checkoutAte = null) else it
                    }))
                }
                Result.success(atualizado ?: jogo)
            }
            else -> Result.success(jogo)
        }
    }

    /**
     * Jobs temporais (RN-08, RN-11, RN-14..16, RN-29): aplica prazos vencidos em todos os jogos.
     * Retorna quantas transicoes foram aplicadas.
     */
    suspend fun processarDeadlines(agora: Instant = clock.now()): Int = mutex.withLock {
        var total = 0
        val ativos = games.observeGames().first().filter { !it.status.terminal }
        for (inicial in ativos) {
            var jogo = inicial
            while (true) {
                val evento = proximoEventoTemporal(jogo, agora) ?: break
                jogo = aplicarSemLock(jogo.id, listOf(evento)).getOrNull() ?: break
                total++
            }
            if (jogo.status == StatusReserva.RASCUNHO) liberarCheckoutExpirado(jogo, agora)
        }
        total
    }

    private fun proximoEventoTemporal(jogo: Jogo, agora: Instant): EventoReserva? = when (jogo.status) {
        StatusReserva.AGUARDANDO_PAGAMENTOS -> when (jogo.etapaPrazo) {
            EtapaPrazo.COLETANDO_COTAS -> if (agora >= jogo.reserva.prazoCotas!!) EventoReserva.PrazoCotasExpirou(agora) else null
            EtapaPrazo.JANELA_RESOLUCAO -> if (agora >= jogo.reserva.prazoHold!!) EventoReserva.PrazoResolucaoExpirou(agora) else null
            null -> null
        }
        StatusReserva.CONFIRMADO -> if (agora >= jogo.inicio) EventoReserva.IniciarJogo(agora) else null
        StatusReserva.EM_ANDAMENTO -> if (agora >= jogo.fim) EventoReserva.Concluir(agora) else null
        else -> null
    }

    private suspend fun liberarCheckoutExpirado(jogo: Jogo, agora: Instant) {
        val slot = arenas.slot(jogo.slotId) ?: return
        if (slot.status == SlotStatus.EM_CHECKOUT && slot.jogoId == jogo.id && slot.checkoutAte != null && slot.checkoutAte <= agora) {
            arenas.atualizarSlot(slot.id) { it.copy(status = SlotStatus.LIVRE, checkoutAte = null, jogoId = null) }
        }
    }

    /** Inicia os jobs de fundo: escuta webhooks e verifica prazos periodicamente. */
    fun start(scope: CoroutineScope, intervalo: Duration = 30.seconds): Job = scope.launch {
        launch { gateway.webhooks.collect { onWebhook(it) } }
        launch {
            while (isActive) {
                runCatching { processarDeadlines() }
                delay(intervalo)
            }
        }
    }
}
