package br.com.sportconnect.feature.common

import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.domain.ReminderKind
import br.com.sportconnect.core.domain.ReminderPlanner
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.GetMyGamesUseCase
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.platform.LocalReminder
import br.com.sportconnect.core.platform.ReminderScheduler
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.push_game_tomorrow_body
import br.com.sportconnect.resources.push_game_tomorrow_title
import br.com.sportconnect.resources.push_payment_reminder_2h_body
import br.com.sportconnect.resources.push_payment_reminder_2h_title
import br.com.sportconnect.resources.push_payment_reminder_30m_body
import br.com.sportconnect.resources.push_payment_reminder_30m_title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock

/**
 * Mantem os lembretes locais em dia (E8): sempre que meus jogos mudam (paguei, jogo confirmou, cancelou)
 * ou a pessoa liga/desliga os lembretes, replaneja e reagenda tudo. Deslogado = nenhum lembrete.
 */
class ReminderSync(
    private val getMyGames: GetMyGamesUseCase,
    private val auth: AuthRepository,
    private val scheduler: ReminderScheduler,
    private val prefs: AppPreferences,
    private val clock: Clock,
) {
    /** Muda quando a pessoa ativa os lembretes, para replanejar na hora. */
    val enabled = MutableStateFlow(prefs.remindersEnabled)

    fun setEnabled(value: Boolean) {
        prefs.remindersAsked = true
        prefs.remindersEnabled = value
        enabled.value = value
    }

    suspend fun run() {
        combine(getMyGames(), auth.currentUser, enabled) { meus, user, on -> Triple(meus, user, on) }
            .collectLatest { (meus, user, on) ->
                if (user == null || !on) {
                    scheduler.replaceAll(emptyList())
                    return@collectLatest
                }
                val byId = meus.proximos.associateBy { it.id }
                val reminders = ReminderPlanner.plan(meus.proximos, user.id, clock.now()).mapNotNull { r ->
                    val j = byId[r.gameId] ?: return@mapNotNull null
                    val link = "https://sportconnect.app/g/${j.id}"
                    val valor = j.valorCotaConvidado.format()
                    val prazo = j.reserva.prazoCotas?.let { PtBrFormat.clock(it) }.orEmpty()
                    when (r.kind) {
                        ReminderKind.PAY_2H -> LocalReminder(r.id, r.at, getString(Res.string.push_payment_reminder_2h_title),
                            getString(Res.string.push_payment_reminder_2h_body, valor, prazo, j.titulo), link)
                        ReminderKind.PAY_30M -> LocalReminder(r.id, r.at, getString(Res.string.push_payment_reminder_30m_title),
                            getString(Res.string.push_payment_reminder_30m_body, valor, prazo), link)
                        ReminderKind.GAME_TOMORROW -> LocalReminder(r.id, r.at, getString(Res.string.push_game_tomorrow_title),
                            getString(Res.string.push_game_tomorrow_body, j.titulo, j.arenaNome), link)
                    }
                }
                scheduler.replaceAll(reminders)
            }
    }
}
