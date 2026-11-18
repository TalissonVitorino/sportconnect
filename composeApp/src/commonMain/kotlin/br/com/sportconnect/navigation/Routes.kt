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
 * | legal/{doc}             | [Route.Legal]              | nao        |
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
 * | profile/privacy         | [Route.ProfilePrivacy]     | sim        |
 * | profile/delete          | [Route.DeleteAccount]      | nao        |
 * | profile/edit            | [Route.ProfileEdit]        | nao        |
 * | profile/sports          | [Route.ProfileSports]      | sim        |
 * | profile/cards           | [Route.SavedCards]         | sim        |
 * | support                 | [Route.Support]            | sim        |
 * | receipt/{paymentId}     | [Route.Receipt]            | sim        |
 * | notifications           | [Route.Notifications]      | sim        |
 */
sealed interface Route {
    @Serializable
    data object Onboarding : Route

    /**
     * Regra de retorno (09 §2.5): depois do OTP segue para onde a pessoa ia.
     * [payGameId] -> `game/{id}/pay`; [createGame] -> `game/create` (com [slotId], se ja escolheu horario).
     */
    @Serializable
    data class Auth(val payGameId: String? = null, val createGame: Boolean = false, val slotId: String? = null) : Route

    /** Termos de Uso ou Politica de Privacidade (CT-05). [doc] = nome de [br.com.sportconnect.feature.auth.LegalDoc]. */
    @Serializable
    data class Legal(val doc: String) : Route

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
    data class GamePay(val id: String, val afterCreate: Boolean = false, val substituicaoCotaId: String? = null) : Route

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

    /** Privacidade e dados (09 ficha 16): consentimentos, baixar dados, excluir conta. */
    @Serializable
    data object ProfilePrivacy : Route

    /** Excluir conta (US-003): fluxo transacional, sem navegacao global. */
    @Serializable
    data object DeleteAccount : Route

    @Serializable
    data object ProfileEdit : Route

    @Serializable
    data object ProfileSports : Route

    @Serializable
    data object SavedCards : Route

    @Serializable
    data object Support : Route

    @Serializable
    data class Receipt(val paymentId: String) : Route

    @Serializable
    data object Notifications : Route
}
