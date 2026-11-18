package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import kotlin.time.Instant

/** Notificacao local pronta para agendar; [link] abre a tela certa (mesmo caminho dos deep links). */
data class LocalReminder(val id: String, val at: Instant, val title: String, val body: String, val link: String)

/** Agenda lembretes no aparelho (E8). [replaceAll] cancela os anteriores do app e agenda estes. */
interface ReminderScheduler {
    fun replaceAll(reminders: List<LocalReminder>)
}

/** true quando a plataforma consegue mostrar notificacoes locais (Android e iOS). */
expect val localRemindersSupported: Boolean

/** Pede a permissao de notificacoes (Android 13+ e iOS); entrega o resultado em [onResult]. */
@Composable
expect fun rememberNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit
