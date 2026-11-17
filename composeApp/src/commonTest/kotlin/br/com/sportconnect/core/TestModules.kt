package br.com.sportconnect.core

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

/** Substitui o armazenamento da plataforma por um mapa em memoria (sem Context do Android nos testes). */
val testSettingsModule = module {
    single<Settings> { MapSettings() }
}
