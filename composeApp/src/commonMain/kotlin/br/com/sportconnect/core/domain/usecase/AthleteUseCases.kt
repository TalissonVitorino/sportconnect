package br.com.sportconnect.core.domain.usecase

import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

// ------------------------------------------------------------------ pagamentos do atleta

/** Pagamento feito pelo usuario (derivado das cotas pagas/reembolsadas). */
data class MeuPagamento(
    val pagamentoId: String,
    val jogo: Jogo,
    val valorCota: Money,
    val taxa: Money,
    val metodo: MetodoPagamento,
    val pagoEm: Instant,
    val reembolsado: Boolean,
    val complemento: Boolean,
) {
    val total: Money get() = valorCota + taxa
}

/** Historico de pagamentos e reembolsos do usuario atual (`profile/payments`, `receipt/{id}`). */
@OptIn(ExperimentalCoroutinesApi::class)
class GetMyPaymentsUseCase(
    private val games: GameRepository,
    private val auth: AuthRepository,
) {
    operator fun invoke(): Flow<List<MeuPagamento>> = auth.currentUser.flatMapLatest { u ->
        if (u == null) flowOf(emptyList())
        else games.observeGames().map { jogos ->
            jogos.flatMap { j ->
                j.reserva.cotas.filter {
                    it.atletaId == u.id && it.pagaEm != null &&
                        (it.status == StatusCota.PAGA || it.status == StatusCota.REEMBOLSADA)
                }.map { c ->
                    MeuPagamento(
                        pagamentoId = c.pagamentoId ?: "pg-${c.id}",
                        jogo = j,
                        valorCota = c.valor,
                        taxa = c.taxaConveniencia,
                        metodo = c.metodo ?: MetodoPagamento.PIX,
                        pagoEm = c.pagaEm!!,
                        reembolsado = c.status == StatusCota.REEMBOLSADA,
                        complemento = c.tipo == TipoCota.COMPLEMENTO,
                    )
                }
            }.sortedByDescending { it.pagoEm }
        }
    }
}

// ------------------------------------------------------------------ avisos

enum class TipoAviso { PAGAMENTO_RECEBIDO, PROGRESSO, CONFIRMADO, PRAZO, CANCELADO }

/** Aviso in-app derivado do estado dos jogos (sem backend de notificacoes no MVP fake). */
data class Aviso(
    val id: String,
    val tipo: TipoAviso,
    val jogo: Jogo,
    val em: Instant,
    val lido: Boolean,
    /** Numeros usados pelo texto (pagas, total, faltam...). */
    val pagas: Int = 0,
    val total: Int = 0,
    val valor: Money = Money.ZERO,
)

/** Estado "lido" dos avisos (memoria do processo). */
class AvisosLidosStore {
    val lidos = MutableStateFlow<Set<String>>(emptySet())
    fun marcar(ids: Collection<String>) = lidos.update { it + ids }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetNotificationsUseCase(
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val store: AvisosLidosStore,
) {
    operator fun invoke(): Flow<List<Aviso>> = auth.currentUser.flatMapLatest { u ->
        if (u == null) flowOf(emptyList())
        else combine(games.observeGames(), store.lidos) { jogos, lidos ->
            jogos.filter { it.participa(u.id) }.flatMap { j -> avisosDo(j, u.id) }
                .map { it.copy(lido = it.id in lidos) }
                .sortedByDescending { it.em }
        }
    }

    fun marcarTodos(ids: Collection<String>) = store.marcar(ids)

    private fun avisosDo(j: Jogo, userId: String): List<Aviso> = buildList {
        val r = j.reserva
        when (j.status) {
            StatusReserva.CONFIRMADO, StatusReserva.EM_ANDAMENTO ->
                add(Aviso("conf-${j.id}", TipoAviso.CONFIRMADO, j, r.confirmadaEm ?: j.criadoEm, false))
            StatusReserva.CANCELADO, StatusReserva.EXPIRADO -> {
                val em = r.historico.lastOrNull()?.em ?: j.criadoEm
                val minha = r.cotas.firstOrNull { it.atletaId == userId && it.status == StatusCota.REEMBOLSADA }
                add(Aviso("canc-${j.id}", TipoAviso.CANCELADO, j, em, true, valor = minha?.totalPago ?: Money.ZERO))
            }
            StatusReserva.AGUARDANDO_PAGAMENTOS -> {
                if (j.organizadorId == userId) {
                    val ultimo = r.cotas.filter { it.status == StatusCota.PAGA && !it.organizador }.maxByOrNull { it.pagaEm ?: Instant.DISTANT_PAST }
                    if (ultimo?.pagaEm != null) {
                        add(Aviso("prog-${j.id}-${j.cotasPagas}", TipoAviso.PROGRESSO, j, ultimo.pagaEm, false, j.cotasPagas, j.numeroVagas))
                    }
                    r.prazoCotas?.let { p ->
                        add(Aviso("prazo-${j.id}", TipoAviso.PRAZO, j, r.ativadaEm ?: j.criadoEm, false, j.cotasPagas, j.numeroVagas, j.valorPendente))
                    }
                }
                r.cotas.firstOrNull { it.atletaId == userId && it.status == StatusCota.PAGA && it.pagaEm != null }?.let { c ->
                    add(Aviso("pago-${j.id}", TipoAviso.PAGAMENTO_RECEBIDO, j, c.pagaEm!!, true, valor = c.totalPago))
                }
            }
            else -> Unit
        }
    }
}
