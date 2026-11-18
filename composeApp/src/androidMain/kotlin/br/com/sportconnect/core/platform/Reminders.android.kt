package br.com.sportconnect.core.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

actual val localRemindersSupported: Boolean = true

@Composable
actual fun rememberNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { callback.value(it) }
    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            callback.value(NotificationManagerCompat.from(context).areNotificationsEnabled())
        }
    }
}

/**
 * AlarmManager inexato (setAndAllowWhileIdle): nao precisa da permissao de alarme exato e um lembrete
 * alguns minutos depois e aceitavel. Alarmes somem no reboot; o app replaneja ao abrir.
 */
internal class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {
    private val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val store = context.getSharedPreferences("sportconnect_reminders", Context.MODE_PRIVATE)

    override fun replaceAll(reminders: List<LocalReminder>) {
        store.getStringSet(KEY_IDS, emptySet()).orEmpty().forEach { id -> alarms.cancel(pending(id, null)) }
        reminders.forEach { r ->
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.at.toEpochMilliseconds(), pending(r.id, r))
        }
        store.edit().putStringSet(KEY_IDS, reminders.map { it.id }.toSet()).apply()
    }

    private fun pending(id: String, r: LocalReminder?): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ACTION).setData(Uri.parse("sportconnect-reminder://$id"))
        if (r != null) intent.putExtra(EXTRA_TITLE, r.title).putExtra(EXTRA_BODY, r.body).putExtra(EXTRA_LINK, r.link)
        return PendingIntent.getBroadcast(context, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private const val KEY_IDS = "ids"
        const val ACTION = "br.com.sportconnect.REMINDER"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_LINK = "link"
    }
}

/** Recebe o alarme e posta a notificacao; o toque abre o link (deep link do jogo). Declarado no manifest do app. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(CHANNEL, "Lembretes de jogo e pagamento", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val link = intent.getStringExtra(AndroidReminderScheduler.EXTRA_LINK)
        val open = Intent(Intent.ACTION_VIEW, Uri.parse(link)).setPackage(context.packageName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val content = PendingIntent.getActivity(context, link.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val icon = context.resources.getIdentifier("ic_launcher_monochrome", "drawable", context.packageName).takeIf { it != 0 }
            ?: context.applicationInfo.icon
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(icon)
            .setContentTitle(intent.getStringExtra(AndroidReminderScheduler.EXTRA_TITLE))
            .setContentText(intent.getStringExtra(AndroidReminderScheduler.EXTRA_BODY))
            .setStyle(NotificationCompat.BigTextStyle().bigText(intent.getStringExtra(AndroidReminderScheduler.EXTRA_BODY)))
            .setContentIntent(content)
            .setAutoCancel(true)
            .build()
        runCatching { nm.notify(intent.data.hashCode(), notification) }
    }

    private companion object { const val CHANNEL = "lembretes" }
}
