package br.com.sportconnect.core.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module

/** Desktop: tratado como sempre online (a falha de rede aparece no erro de cada chamada). */
private class AlwaysOnline : ConnectivityMonitor {
    override val online: StateFlow<Boolean> = MutableStateFlow(true)
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { NoReminders() }
    single<ConnectivityMonitor> { AlwaysOnline() }
}
