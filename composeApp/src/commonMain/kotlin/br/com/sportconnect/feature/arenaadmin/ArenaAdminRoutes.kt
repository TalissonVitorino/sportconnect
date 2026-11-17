package br.com.sportconnect.feature.arenaadmin

import kotlinx.serialization.Serializable

/**
 * Rotas type-safe do modo "Minha Arena" (09 §2 / 10 §2.16–2.21).
 * Mapeamento com o inventario: `arena-admin/dashboard`, `.../schedule`, `.../booking/{id}`,
 * `.../courts`, `.../courts/{courtId}`, `.../payouts`, `.../onboarding`.
 */
sealed interface ArenaAdminRoute {
    /** Painel (inicio do modo arena). */
    @Serializable
    data object Dashboard : ArenaAdminRoute

    /** Agenda dia/semana. */
    @Serializable
    data object Schedule : ArenaAdminRoute

    /** Detalhe da reserva. [id] = id do slot (ou id do jogo — ambos sao aceitos). */
    @Serializable
    data class Booking(val id: String) : ArenaAdminRoute

    /** Lista de quadras. */
    @Serializable
    data object Courts : ArenaAdminRoute

    /** Edicao de quadra; [courtId] null = nova quadra. */
    @Serializable
    data class CourtEdit(val courtId: String?) : ArenaAdminRoute

    /** Repasses. */
    @Serializable
    data object Payouts : ArenaAdminRoute

    /** Relatorios (09 ficha 24, US-063). */
    @Serializable
    data object Reports : ArenaAdminRoute

    /** Regras da arena (09 ficha 25, US-015). */
    @Serializable
    data object Settings : ArenaAdminRoute

    /** Cadastro da arena (fluxo transacional, sem navegacao global). */
    @Serializable
    data object Onboarding : ArenaAdminRoute
}
