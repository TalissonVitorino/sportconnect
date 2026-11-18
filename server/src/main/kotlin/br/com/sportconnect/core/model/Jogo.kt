package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Jogo (1:1 com [Reserva]). Campos de arena/quadra sao desnormalizados para exibicao.
 * O estado de negocio e o da reserva ([status]).
 */
@Serializable
data class Jogo(
    val id: String,
    val titulo: String,
    val organizadorId: String,
    val organizadorNome: String,
    val esporte: Sport,
    val arenaId: String,
    val arenaNome: String,
    val quadraId: String,
    val quadraNome: String,
    val slotId: String,
    val inicio: Instant,
    val fim: Instant,
    val numeroVagas: Int,
    val politica: PoliticaNaoPagamento = PoliticaNaoPagamento.ORGANIZADOR_COBRE,
    val garantia: GarantiaCobertura = GarantiaCobertura.PIX,
    /** RN-16: teto do complemento em % da cota base (0..100, padrao 50). */
    val tetoComplementoPct: Int = 50,
    val visibilidade: Visibilidade = Visibilidade.PRIVADO,
    val observacoes: String? = null,
    val codigoConvite: String,
    val reserva: Reserva,
    val criadoEm: Instant,
) {
    val status: StatusReserva get() = reserva.status
    val etapaPrazo: EtapaPrazo? get() = reserva.etapaPrazo
    val valorTotal: Money get() = reserva.valorTotal

    /** Cotas PADRAO que contam para o progresso (exclui reembolsadas/complementos). */
    val cotasPadrao: List<Cota> get() = reserva.cotas.filter { it.tipo == TipoCota.PADRAO && it.status != StatusCota.REEMBOLSADA }
    val cotasPagas: Int get() = cotasPadrao.count { it.status == StatusCota.PAGA }
    val cotasPendentes: Int get() = cotasPadrao.count { it.status == StatusCota.PENDENTE }
    val vagasRestantes: Int get() = cotasPendentes
    val progresso: Float get() = if (numeroVagas == 0) 0f else cotasPagas.toFloat() / numeroVagas
    val valorArrecadado: Money get() = Money(reserva.cotas.filter { it.status == StatusCota.PAGA }.sumOf { it.valor.cents })
    val valorPendente: Money get() = Money(reserva.cotas.filter { it.status == StatusCota.PENDENTE }.sumOf { it.valor.cents })
    val cotaOrganizador: Cota? get() = reserva.cotas.firstOrNull { it.organizador && it.tipo == TipoCota.PADRAO }

    /** Valor da cota dos convidados (cota base, RN-04). */
    val valorCotaConvidado: Money get() = Money(valorTotal.cents / numeroVagas)

    fun cotaDe(usuarioId: String): Cota? =
        reserva.cotas.firstOrNull { it.atletaId == usuarioId && it.tipo == TipoCota.PADRAO && it.status == StatusCota.PAGA }
            ?: reserva.cotas.firstOrNull { it.atletaId == usuarioId && it.status == StatusCota.PENDENTE }

    fun participa(usuarioId: String): Boolean =
        organizadorId == usuarioId || reserva.cotas.any { it.atletaId == usuarioId && it.status == StatusCota.PAGA }
}
