package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

actual val localRemindersSupported: Boolean = false

@Composable
actual fun rememberNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit = { onResult(false) }

/** Sem notificacoes locais nesta plataforma. */
internal class NoReminders : ReminderScheduler {
    override fun replaceAll(reminders: List<LocalReminder>) = Unit
}
