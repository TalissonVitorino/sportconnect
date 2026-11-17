package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.time.Clock

actual val localRemindersSupported: Boolean = true

@Composable
actual fun rememberNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val callback = rememberUpdatedState(onResult)
    return {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { granted, _ -> dispatch_async(dispatch_get_main_queue()) { callback.value(granted) } }
    }
}

/** UNUserNotificationCenter; o toque e tratado no Swift (iOSApp.swift) e vira deep link. */
internal class IosReminderScheduler : ReminderScheduler {
    override fun replaceAll(reminders: List<LocalReminder>) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removeAllPendingNotificationRequests()
        val now = Clock.System.now()
        reminders.forEach { r ->
            val seconds = (r.at - now).inWholeSeconds.toDouble()
            if (seconds <= 0) return@forEach
            val content = UNMutableNotificationContent().apply {
                setTitle(r.title)
                setBody(r.body)
                setSound(UNNotificationSound.defaultSound)
                setUserInfo(mapOf("link" to r.link))
            }
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, repeats = false)
            center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(r.id, content, trigger), withCompletionHandler = null)
        }
    }
}
