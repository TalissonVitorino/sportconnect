package br.com.sportconnect.core.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.module.Module
import org.koin.dsl.module

/** Conta como online so a rede padrao com internet VALIDADA (Wi-Fi sem internet nao conta). */
private class AndroidConnectivityMonitor(context: Context) : ConnectivityMonitor {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _online = MutableStateFlow(isValidated(cm.getNetworkCapabilities(cm.activeNetwork)))
    override val online: StateFlow<Boolean> = _online.asStateFlow()

    init {
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { _online.value = isValidated(caps) }
            override fun onLost(network: Network) { _online.value = false }
            override fun onUnavailable() { _online.value = false }
        })
    }

    private fun isValidated(caps: NetworkCapabilities?) =
        caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

actual val platformModule: Module = module {
    single<ReminderScheduler> { AndroidReminderScheduler(get<Context>()) }
    // Context vem do androidContext() registrado em SportConnectApplication.
    single<ConnectivityMonitor> { AndroidConnectivityMonitor(get<Context>()) }
}
