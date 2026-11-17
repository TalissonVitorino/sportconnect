package br.com.sportconnect.navigation

import kotlinx.serialization.Serializable

/**
 * Rotas type-safe (kotlinx.serialization) do modo Atleta/Organizador — 09 §11 (MVP).
 * O modo "Minha Arena" registra as proprias rotas em `feature/arenaadmin` (ArenaAdminRoute).
 *
 * | Rota (09)               | Classe                     | Nav global |
 * |-------------------------|----------------------------|------------|
 * | onboarding              | [Route.Onboarding]         | nao        |
 * | auth                    | [Route.Auth]               | nao        |
 * | home                    | [Route.Home]               | sim        |
 * | explore                 | [Route.Explore]            | sim        |
 * | arena/{id}              | [Route.ArenaDetail]        | sim        |
 * | slot-picker/{courtId}   | [Route.SlotPicker]         | sim        |
 * | game/create             | [Route.GameCreate]         | nao        |
 * | game/{id}               | [Route.GameDetail]         | sim        |
 * | game/{id}/invite        | [Route.GameInvite]         | nao        |
 * | game/{id}/pay           | [Route.GamePay]            | nao        |
 * | game/{id}/resolve       | [Route.GameResolve]        | nao        |
 * | invite/{code}           | [Route.Invite]             | nao        |
 * | my-games                | [Route.MyGames]            | sim        |
 * | profile                 | [Route.Profile]            | sim        |
 * | profile/payments        | [Route.ProfilePayments]    | sim        |
 * | receipt/{paymentId}     | [Route.Receipt]            | sim        |
 * | notifications           | [Route.Notifications]      | sim        |
 */
sealed interface Route {
    @Serializable
    data object Onboarding : Route

    /** [payGameId]: depois do OTP, segue direto para `game/{id}/pay` (regra de retorno, 09 §2.5). */
    @Serializable
    data class Auth(val payGameId: String? = null) : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Explore : Route

    @Serializable
    data class ArenaDetail(val id: String) : Route

    /** [date] ISO (yyyy-mm-dd) opcional para abrir num dia especifico. */
    @Serializable
    data class SlotPicker(val courtId: String, val date: String? = null) : Route

    /** [slotId] null = passo 1 vazio ("Escolher quadra e horário"). */
    @Serializable
    data class GameCreate(val slotId: String? = null) : Route

    @Serializable
    data class GameDetail(val id: String) : Route

    @Serializable
    data class GameInvite(val id: String) : Route

    /** [afterCreate]: pagamento da cota do organizador logo apos criar (sucesso leva ao convite). */
    @Serializable
    data class GamePay(val id: String, val afterCreate: Boolean = false) : Route

    @Serializable
    data class GameResolve(val id: String) : Route

    @Serializable
    data class Invite(val code: String) : Route

    @Serializable
    data object MyGames : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data object ProfilePayments : Route

    @Serializable
    data class Receipt(val paymentId: String) : Route

    @Serializable
    data object Notifications : Route
}
