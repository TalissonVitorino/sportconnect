package br.com.sportconnect.server.di

import br.com.sportconnect.server.config.AppConfig
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo Koin raiz do servidor. Expõe o [AppConfig] já resolvido para que outros módulos
 * (persistência, segurança, etc.) possam injetá-lo em vez de ler o ambiente diretamente.
 */
fun serverModule(config: AppConfig): Module = module {
    single { config }
}
