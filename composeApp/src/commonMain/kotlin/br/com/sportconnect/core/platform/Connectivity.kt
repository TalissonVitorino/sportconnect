package br.com.sportconnect.core.platform

import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module

/** Estado da conexao do aparelho (E7, RNF-22). true = ha internet utilizavel. */
interface ConnectivityMonitor {
    val online: StateFlow<Boolean>
}

/** Dependencias que so existem em cada plataforma (conectividade). Entra em appModules. */
expect val platformModule: Module
