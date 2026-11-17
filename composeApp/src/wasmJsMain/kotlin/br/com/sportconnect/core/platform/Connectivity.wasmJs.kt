package br.com.sportconnect.core.platform

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.module.Module
import org.koin.dsl.module

/** Web: navigator.onLine + eventos online/offline do navegador. */
private class BrowserConnectivityMonitor : ConnectivityMonitor {
    private val _online = MutableStateFlow(window.navigator.onLine)
    override val online: StateFlow<Boolean> = _online.asStateFlow()

    init {
        window.addEventListener("online") { _online.value = true }
        window.addEventListener("offline") { _online.value = false }
    }
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { NoReminders() }
    single<ConnectivityMonitor> { BrowserConnectivityMonitor() }
}
