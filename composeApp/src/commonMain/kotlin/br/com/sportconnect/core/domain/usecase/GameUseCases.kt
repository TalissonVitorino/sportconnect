package br.com.sportconnect.core.domain.usecase

import br.com.sportconnect.core.domain.EventoReserva
import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.domain.ListaEsperaException
import br.com.sportconnect.core.domain.ReservationService
import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.Cobranca
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.domain.repository.PaymentGateway
import br.com.sportconnect.core.domain.repository.SolicitacaoCobranca
import br.com.sportconnect.core.model.Ator
import br.com.sportconnect.core.model.Convite
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.ListaEspera
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PapelUsuario
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.Reserva
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.core.model.TipoCota
import br.com.sportconnect.core.model.Usuario
import br.com.sportconnect.core.model.Visibilidade
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

private fun AuthRepository.exigirUsuario(): Usuario =
    currentUser.value ?: regra("NAO_AUTENTICADO", "Entre com seu celular para continuar.")

// ------------------------------------------------------------------ criacao

data class NovoJogo(
    val slotId: String,
    val esporte: Sport,
    val numeroVagas: Int = esporte.vagasPadrao,
    val politica: PoliticaNaoPagamento = PoliticaNaoPagamento.ORGANIZADOR_COBRE,
    val garantia: GarantiaCobertura = GarantiaCobertura.PIX,
    val tetoComplementoPct: Int = 50,
    val visibilidade: Visibilidade = Visibilidade.PRIVADO,
    val titulo: String? = null,
    val observacoes: String? = null,
)

/**
 * Cria o jogo em RASCUNHO (US-021/022): congela V (RN-02), valida N (RN-03/05), cota minima (RN-06) e
 * viabilidade da politica (RN-12); trava o slot em EM_CHECKOUT por 10 min (RN-08).
 * Em seguida a UI chama [PayShareUseCase] para a cota do organizador; a confirmacao do pagamento ativa a
 * pre-reserva (RN-09) automaticamente.
 */
class CreateGameUseCase(
    private val games: GameRepository,
    private val arenas: ArenaRepository,
    private val auth: AuthRepository,
    private val split: SplitCostUseCase,
    private val holdCalculator: HoldDeadlineCalculator,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) {
    suspend operator fun invoke(params: NovoJogo): Result<Jogo> = runCatching {
        val usuario = auth.exigirUsuario()
        val agora = clock.now()
        val slot = arenas.slot(params.slotId) ?: regra("SLOT_INEXISTENTE", "Horário não encontrado.")
        if (!slot.disponivelEm(agora)) regra("SLOT_INDISPONIVEL", "Esse horário acabou de ser reservado. Escolha outro.")
        val quadra = arenas.quadra(slot.quadraId) ?: regra("QUADRA_INEXISTENTE", "Quadra não encontrada.")
        val arena = arenas.arena(slot.arenaId) ?: regra("ARENA_INEXISTENTE", "Arena não encontrada.")
        val capacidade = quadra.capacidadePara(params.esporte)
            ?: regra("ESPORTE_NAO_SUPORTADO", "${quadra.nome} não recebe ${params.esporte.displayName}.")
        if (!params.esporte.aceitaVagas(params.numeroVagas, capacidade)) {
            regra(
                "VAGAS_INVALIDAS",
                "${params.esporte.displayName}: escolha entre ${params.esporte.minVagas} e ${minOf(params.esporte.maxVagas, capacidade)} vagas.",
            )
        }
        val rateio = split.calcular(slot.preco, params.numeroVagas)
        if (!rateio.respeitaCotaMinima) {
            regra("COTA_MINIMA", "A cota ficaria abaixo de R$ 5,00. Reduza para até ${split.maxVagasParaCotaMinima(slot.preco)} vagas.")
        }
        val garantia = if (params.politica == PoliticaNaoPagamento.ORGANIZADOR_COBRE) {
            if (params.garantia == GarantiaCobertura.NAO_SE_APLICA) GarantiaCobertura.PIX else params.garantia
        } else GarantiaCobertura.NAO_SE_APLICA
        if (params.numeroVagas > 1 &&
            !holdCalculator.politicaViavel(agora, slot.inicio, params.politica, garantia, arena.duracaoHoldHoras)
        ) {
            regra("POLITICA_INVIAVEL", "Falta pouco para o jogo: essa regra não cabe no prazo. Escolha outra ou pague o valor integral.")
        }

        val jogoId = games.novoId("jogo")
        val reservaId = "res-$jogoId"
        val cotas = rateio.cotas.mapIndexed { i, valor ->
            if (i == 0) Cota(id = "$reservaId-c0", valor = valor, organizador = true, atletaId = usuario.id, atletaNome = usuario.nome)
            else Cota(id = "$reservaId-c$i", valor = valor)
        }
        val codigo = gerarCodigo(jogoId)
        val jogo = Jogo(
            id = jogoId,
            titulo = params.titulo?.takeIf { it.isNotBlank() } ?: "${params.esporte.displayName} · ${arena.nome}",
            organizadorId = usuario.id,
            organizadorNome = usuario.nome,
            esporte = params.esporte,
            arenaId = arena.id,
            arenaNome = arena.nome,
            quadraId = quadra.id,
            quadraNome = quadra.nome,
            slotId = slot.id,
            inicio = slot.inicio,
            fim = slot.fim,
            numeroVagas = params.numeroVagas,
            politica = params.politica,
            garantia = garantia,
            tetoComplementoPct = params.tetoComplementoPct.coerceIn(0, 100),
            visibilidade = params.visibilidade,
            observacoes = params.observacoes,
            codigoConvite = codigo,
            reserva = Reserva(id = reservaId, valorTotal = slot.preco, cotas = cotas),
            criadoEm = agora,
        )
        games.save(jogo)
        games.saveConvite(Convite(id = "conv-$jogoId", jogoId = jogoId, codigoCurto = codigo, url = "https://sportconnect.app/j/$codigo"))
        arenas.atualizarSlot(slot.id) {
            it.copy(status = SlotStatus.EM_CHECKOUT, checkoutAte = agora + HoldDeadlineCalculator.TRAVA_CHECKOUT, jogoId = jogoId)
        }
        analytics.track(
            AnalyticsEvents.JOGO_CONFIGURADO,
            mapOf(
                "jogo_id" to jogoId, "esporte" to params.esporte.name, "numero_vagas" to params.numeroVagas,
                "cota_base_centavos" to rateio.cotaBase.cents, "sobra_centavos" to rateio.sobra.cents,
                "politica" to params.politica.name, "garantia" to garantia.name.lowercase(),
                "teto_complemento_pct" to params.tetoComplementoPct,
            ),
        )
        jogo
    }

    private fun gerarCodigo(jogoId: String): String {
        val alfabeto = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        var h = jogoId.hashCode().toLong() and 0xffffffffL
        return buildString { repeat(6) { append(alfabeto[(h % alfabeto.length).toInt()]); h /= alfabeto.length; h += 7 } }
    }
}

// ------------------------------------------------------------------ leitura

/** Jogo em tempo real (progresso das cotas atualiza a cada webhook). */
class GetGameUseCase(private val games: GameRepository) {
    operator fun invoke(jogoId: String): Flow<Jogo?> = games.observeGame(jogoId)
}

data class MeusJogos(val proximos: List<Jogo>, val historico: List<Jogo>)

/** Jogos em que o usuario atual organiza ou tem cota paga. */
@OptIn(ExperimentalCoroutinesApi::class)
class GetMyGamesUseCase(
    private val games: GameRepository,
    private val auth: AuthRepository,
) {
    operator fun invoke(): Flow<MeusJogos> = auth.currentUser.flatMapLatest { usuario ->
        if (usuario == null) flowOf(MeusJogos(emptyList(), emptyList()))
        else games.observeGames().map { todos ->
            val meus = todos.filter { it.participa(usuario.id) || it.reserva.cotas.any { c -> c.atletaId == usuario.id && c.status != StatusCota.PENDENTE } }
            val (ativos, passados) = meus.partition { !it.status.terminal }
            MeusJogos(
                proximos = ativos.sortedBy { it.inicio },
                historico = passados.sortedByDescending { it.inicio },
            )
        }
    }
}

/** Jogos avulsos com vagas abertas (visibilidade VAGAS_ABERTAS, aguardando pagamentos, no futuro). */
class GetOpenGamesUseCase(
    private val games: GameRepository,
    private val clock: Clock,
) {
    operator fun invoke(esporte: Sport? = null): Flow<List<Jogo>> = games.observeGames().map { todos ->
        val agora = clock.now()
        todos.filter {
            it.visibilidade == Visibilidade.VAGAS_ABERTAS &&
                it.status == StatusReserva.AGUARDANDO_PAGAMENTOS &&
                it.etapaPrazo == EtapaPrazo.COLETANDO_COTAS &&
                it.vagasRestantes > 0 &&
                it.inicio > agora &&
                (esporte == null || it.esporte == esporte)
        }.sortedBy { it.inicio }
    }
}

// ------------------------------------------------------------------ convite

/** Abre o jogo pelo codigo do convite (US-030). Nao cobra nada; o pagamento e feito em [PayShareUseCase]. */
class JoinGameByInviteUseCase(
    private val games: GameRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) {
    suspend operator fun invoke(codigo: String): Result<Jogo> = runCatching {
        val normalizado = codigo.trim().substringAfterLast('/').uppercase()
        val convite = games.conviteByCodigo(normalizado) ?: regra("CONVITE_INVALIDO", "Convite não encontrado. Confira o link com o organizador.")
        val jogo = games.game(convite.jogoId) ?: regra("JOGO_INEXISTENTE", "Esse jogo não existe mais.")
        games.saveConvite(convite.copy(aberturas = convite.aberturas + 1))
        analytics.track(
            AnalyticsEvents.CONVITE_LINK_ABERTO,
            mapOf(
                "jogo_id" to jogo.id, "convite_id" to convite.id, "canal" to convite.canal.name.lowercase(),
                "status_reserva" to jogo.status.name, "vagas_restantes" to jogo.vagasRestantes,
                "minutos_ate_prazo" to jogo.reserva.prazoCotas?.let { (it - clock.now()).inWholeMinutes },
            ),
        )
        jogo
    }
}

// ------------------------------------------------------------------ pagamento

/**
 * Paga a cota do usuario atual (US-031). Escolhe a cota certa conforme o estado:
 * - RASCUNHO: cota do organizador (ativa a pre-reserva quando confirmada);
 * - COLETANDO_COTAS: primeira cota PENDENTE livre (trava de 10 min, RN-19);
 * - JANELA_RESOLUCAO (politica C): o complemento do usuario, sem taxa (RN-07).
 * A confirmacao chega via webhook do gateway (~3 s no fake) e atualiza o [GetGameUseCase].
 */
class PayShareUseCase(
    private val games: GameRepository,
    private val gateway: PaymentGateway,
    private val auth: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        jogoId: String,
        metodo: MetodoPagamento = MetodoPagamento.PIX,
        cotaId: String? = null,
    ): Result<Cobranca> = runCatching {
        val usuario = auth.exigirUsuario()
        val agora = clock.now()
        val jogo = games.game(jogoId) ?: regra("JOGO_INEXISTENTE", "Jogo não encontrado.")
        val cota: Cota = when {
            jogo.status == StatusReserva.RASCUNHO -> {
                if (jogo.organizadorId != usuario.id) regra("JOGO_EM_RASCUNHO", "O organizador ainda não ativou este jogo.")
                jogo.cotaOrganizador?.takeIf { it.status == StatusCota.PENDENTE } ?: regra("JA_PAGOU", "Sua cota já está paga.")
            }
            jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO ->
                jogo.reserva.cotas.firstOrNull { it.tipo == TipoCota.COMPLEMENTO && it.atletaId == usuario.id && it.status == StatusCota.PENDENTE }
                    ?: regra("PRAZO_ENCERRADO", "O prazo para pagar as cotas terminou.")
            jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS -> {
                if (agora > jogo.reserva.prazoCotas!!) regra("PRAZO_ENCERRADO", "O prazo para pagar as cotas terminou.")
                if (jogo.reserva.cotas.any { it.atletaId == usuario.id && it.status == StatusCota.PAGA && it.tipo == TipoCota.PADRAO }) {
                    regra("JA_PAGOU", "Você já pagou sua cota neste jogo.")
                }
                val escolhida = if (cotaId != null) jogo.reserva.cotas.firstOrNull { it.id == cotaId }
                else jogo.reserva.cotas.firstOrNull { it.tipo == TipoCota.PADRAO && it.status == StatusCota.PENDENTE && it.atletaId == usuario.id && it.livreParaCheckout(agora, usuario.id) }
                    ?: jogo.reserva.cotas.firstOrNull { it.tipo == TipoCota.PADRAO && it.livreParaCheckout(agora, usuario.id) }
                if (escolhida == null || !escolhida.livreParaCheckout(agora, usuario.id)) {
                    analytics.track(AnalyticsEvents.CHECKOUT_CONFLITO, mapOf("jogo_id" to jogo.id, "tipo" to "cota"))
                    regra("SEM_VAGAS", "As vagas acabaram. Entre na lista de espera.")
                }
                escolhida
            }
            else -> regra("JOGO_FECHADO", "Este jogo não aceita mais pagamentos (${jogo.status.rotulo}).")
        }

        val expira = minOf(agora + 10.minutes, jogo.reserva.prazoHold ?: (agora + 10.minutes))
        // Trava a cota (RN-19) e associa ao usuario.
        games.update(jogo.id) { j ->
            j.copy(reserva = j.reserva.copy(cotas = j.reserva.cotas.map {
                if (it.id == cota.id) it.copy(checkoutAte = agora + HoldDeadlineCalculator.TRAVA_CHECKOUT, atletaId = usuario.id, atletaNome = usuario.nome) else it
            }))
        }
        val taxa = if (cota.tipo == TipoCota.COMPLEMENTO) Money.ZERO else TaxaConveniencia.para(metodo)
        analytics.track(
            AnalyticsEvents.CHECKOUT_INICIADO,
            mapOf(
                "jogo_id" to jogo.id, "cota_id" to cota.id,
                "papel" to if (cota.organizador) "organizador" else "atleta", "tipo_cota" to cota.tipo.name.lowercase(),
            ),
        )
        val solicitacao = SolicitacaoCobranca(
            jogoId = jogo.id, cotaId = cota.id, pagadorId = usuario.id, pagadorNome = usuario.nome,
            valorCota = cota.valor, taxaConveniencia = taxa, expiraEm = expira,
        )
        val cobranca = when (metodo) {
            MetodoPagamento.PIX -> gateway.criarCobrancaPix(solicitacao)
            MetodoPagamento.CARTAO -> gateway.criarCobrancaCartao(solicitacao)
        }
        analytics.track(
            AnalyticsEvents.COBRANCA_GERADA,
            mapOf("pagamento_id" to cobranca.pagamentoId, "metodo" to metodo.name.lowercase(), "valor_centavos" to cobranca.total.cents, "taxa_centavos" to taxa.cents),
        )
        cobranca
    }
}

/** Politica A via Pix na janela de resolucao: organizador paga o faltante, sem taxa (RN-14). */
class CoverDifferenceUseCase(
    private val games: GameRepository,
    private val gateway: PaymentGateway,
    private val auth: AuthRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(jogoId: String): Result<Cobranca> = runCatching {
        val usuario = auth.exigirUsuario()
        val jogo = games.game(jogoId) ?: regra("JOGO_INEXISTENTE", "Jogo não encontrado.")
        if (jogo.organizadorId != usuario.id) regra("SOMENTE_ORGANIZADOR", "Só o organizador pode cobrir a diferença.")
        if (jogo.etapaPrazo != EtapaPrazo.JANELA_RESOLUCAO) regra("FORA_DA_JANELA", "Não há diferença a cobrir agora.")
        gateway.criarCobrancaPix(
            SolicitacaoCobranca(
                jogoId = jogo.id, cotaId = "cobertura-${jogo.id}", pagadorId = usuario.id, pagadorNome = usuario.nome,
                valorCota = jogo.valorPendente, taxaConveniencia = Money.ZERO,
                expiraEm = jogo.reserva.prazoHold ?: (clock.now() + 30.minutes), cobertura = true,
            ),
        )
    }
}

// ------------------------------------------------------------------ cancelamento / saida / prazos

/** Cancela o jogo como organizador (RN-23) ou como arena (RN-24, motivo obrigatorio). */
class CancelGameUseCase(
    private val service: ReservationService,
    private val games: GameRepository,
    private val arenas: ArenaRepository,
    private val auth: AuthRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(jogoId: String, motivo: String? = null): Result<Jogo> {
        val usuario = runCatching { auth.exigirUsuario() }.getOrElse { return Result.failure(it) }
        val jogo = games.game(jogoId) ?: return Result.failure(NoSuchElementException("Jogo não encontrado."))
        val ator = when {
            jogo.organizadorId == usuario.id -> Ator.ORGANIZADOR
            usuario.arenaId == jogo.arenaId && (PapelUsuario.GESTOR_ARENA in usuario.papeis || PapelUsuario.OPERADOR_ARENA in usuario.papeis) -> Ator.ARENA
            else -> return Result.failure(RegraNegocioException("SEM_PERMISSAO", "Você não pode cancelar este jogo."))
        }
        val janela = arenas.arena(jogo.arenaId)?.janelaCancelamentoHoras ?: 24
        return service.aplicar(jogoId, EventoReserva.CancelarPor(ator, clock.now(), motivo, janela))
    }
}

/** Saida voluntaria antes da confirmacao (RN-21): reembolso da cota sem a taxa. */
class LeaveGameUseCase(
    private val service: ReservationService,
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(jogoId: String): Result<Jogo> {
        val usuario = runCatching { auth.exigirUsuario() }.getOrElse { return Result.failure(it) }
        val jogo = games.game(jogoId) ?: return Result.failure(NoSuchElementException("Jogo não encontrado."))
        val cota = jogo.reserva.cotas.firstOrNull { it.atletaId == usuario.id && it.status == StatusCota.PAGA && it.tipo == TipoCota.PADRAO }
            ?: return Result.failure(RegraNegocioException("SEM_COTA", "Você não tem cota paga neste jogo."))
        return service.aplicar(jogoId, EventoReserva.AtletaSaiu(cota.id, clock.now()))
    }
}

// ------------------------------------------------------------------ passar a vaga (T8, US-111, RN-26)

/** Atleta pago oferece/retira a propria vaga para a lista de espera (RN-26). */
class ToggleSpotOfferUseCase(
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val waitlist: WaitlistManager,
    private val clock: Clock,
) {
    suspend operator fun invoke(jogoId: String, cotaId: String, oferecer: Boolean): Result<Jogo> = runCatching {
        val usuario = auth.exigirUsuario()
        var jogo = games.game(jogoId) ?: regra("JOGO_INEXISTENTE", "Jogo não encontrado.")
        jogo = try {
            if (oferecer) waitlist.oferecer(jogo, cotaId, usuario.id, clock.now()) else waitlist.cancelarOferta(jogo, cotaId, usuario.id)
        } catch (e: ListaEsperaException) {
            regra("SUBSTITUICAO", e.message ?: "Não foi possível oferecer a vaga.")
        }
        games.save(jogo)
        jogo
    }
}

/** Quem esta na lista de espera paga a vaga oferecida; a confirmacao do gateway chama [WaitlistManager.substituir]. */
class ClaimOfferedSpotUseCase(
    private val games: GameRepository,
    private val gateway: PaymentGateway,
    private val auth: AuthRepository,
    private val waitlist: WaitlistManager,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) {
    suspend operator fun invoke(jogoId: String, cotaId: String, metodo: MetodoPagamento = MetodoPagamento.PIX): Result<Cobranca> = runCatching {
        val usuario = auth.exigirUsuario()
        val agora = clock.now()
        val jogo = games.game(jogoId) ?: regra("JOGO_INEXISTENTE", "Jogo não encontrado.")
        if (!waitlist.podeSubstituir(jogo, agora)) regra("SUBSTITUICAO_INDISPONIVEL", "Essa substituição não está mais disponível.")
        val cota = jogo.reserva.cotas.firstOrNull { it.id == cotaId }
            ?: regra("SUBSTITUICAO_INDISPONIVEL", "Essa vaga não está mais disponível.")
        if (!cota.offeredForSubstitution || cota.status != StatusCota.PAGA) regra("SUBSTITUICAO_INDISPONIVEL", "Essa vaga não está mais disponível.")
        if (jogo.participa(usuario.id)) regra("JA_PAGOU", "Você já está neste jogo.")
        val taxa = TaxaConveniencia.para(metodo)
        val solicitacao = SolicitacaoCobranca(
            jogoId = jogo.id, cotaId = "sub-${cota.id}", pagadorId = usuario.id, pagadorNome = usuario.nome,
            valorCota = cota.valor, taxaConveniencia = taxa, expiraEm = agora + 10.minutes, substituicaoCotaId = cota.id,
        )
        val cobranca = when (metodo) {
            MetodoPagamento.PIX -> gateway.criarCobrancaPix(solicitacao)
            MetodoPagamento.CARTAO -> gateway.criarCobrancaCartao(solicitacao)
        }
        analytics.track(
            AnalyticsEvents.COBRANCA_GERADA,
            mapOf("pagamento_id" to cobranca.pagamentoId, "metodo" to metodo.name.lowercase(), "valor_centavos" to cobranca.total.cents, "taxa_centavos" to taxa.cents),
        )
        cobranca
    }
}

/** Entra na lista de espera (RN-27). */
class JoinWaitlistUseCase(
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val waitlist: WaitlistManager,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) {
    suspend operator fun invoke(jogoId: String): Result<List<ListaEspera>> = runCatching {
        val usuario = auth.exigirUsuario()
        val jogo = games.game(jogoId) ?: regra("JOGO_INEXISTENTE", "Jogo não encontrado.")
        val fila = games.observeWaitlist(jogoId).firstOrNull() ?: emptyList()
        val nova = try {
            waitlist.entrar(jogo, fila, usuario.id, usuario.nome, clock.now())
        } catch (e: ListaEsperaException) {
            regra("LISTA_ESPERA", e.message ?: "Não foi possível entrar na lista.")
        }
        games.saveWaitlist(jogoId, nova)
        analytics.track(AnalyticsEvents.LISTA_ESPERA_ENTROU, mapOf("jogo_id" to jogoId, "posicao" to waitlist.ativos(nova).size))
        nova
    }
}

/** Executa os jobs de prazo agora (util para a demo e para testes). */
class ProcessDeadlinesUseCase(private val service: ReservationService, private val clock: Clock) {
    suspend operator fun invoke(): Int = service.processarDeadlines(clock.now())
}

/** Estado combinado jogo + lista de espera (tela de detalhe). */
class ObserveGameWithWaitlistUseCase(private val games: GameRepository) {
    operator fun invoke(jogoId: String): Flow<Pair<Jogo?, List<ListaEspera>>> =
        combine(games.observeGame(jogoId), games.observeWaitlist(jogoId)) { j, f -> j to f }
}
