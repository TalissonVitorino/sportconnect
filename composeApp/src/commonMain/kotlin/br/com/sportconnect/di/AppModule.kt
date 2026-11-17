package br.com.sportconnect.di

import br.com.sportconnect.core.domain.usecase.AvisosLidosStore
import br.com.sportconnect.core.domain.usecase.GetMyPaymentsUseCase
import br.com.sportconnect.core.domain.usecase.GetNotificationsUseCase
import br.com.sportconnect.feature.common.UiSessionStore
import br.com.sportconnect.feature.game.GameDetailViewModel
import br.com.sportconnect.feature.arena.ArenaDetailViewModel
import br.com.sportconnect.feature.arenaadmin.arenaAdminModule
import br.com.sportconnect.feature.auth.AuthViewModel
import br.com.sportconnect.feature.onboarding.OnboardingViewModel
import br.com.sportconnect.navigation.ShellViewModel
import br.com.sportconnect.feature.creategame.CreateGameViewModel
import br.com.sportconnect.feature.explore.ExploreViewModel
import br.com.sportconnect.feature.slots.SlotPickerViewModel
import br.com.sportconnect.feature.home.HomeViewModel
import br.com.sportconnect.feature.invite.GuestInviteViewModel
import br.com.sportconnect.feature.invite.OrganizerInviteViewModel
import br.com.sportconnect.feature.mygames.MyGamesViewModel
import br.com.sportconnect.feature.profile.NotificationsViewModel
import br.com.sportconnect.feature.profile.PaymentsViewModel
import br.com.sportconnect.feature.profile.ProfileViewModel
import br.com.sportconnect.feature.profile.ReceiptViewModel
import br.com.sportconnect.feature.resolve.ResolveViewModel
import br.com.sportconnect.feature.payment.PaymentViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

// dataModule e domainModule ficam em CoreModules.kt.

/**
 * Telas do modo Atleta/Organizador. ViewModels com argumento de rota recebem a propria rota
 * (`koinViewModel { parametersOf(route) }`): cada rota e um tipo distinto, entao o Koin resolve sem ambiguidade.
 */
val featureModule = module {
    singleOf(::UiSessionStore)
    singleOf(::AvisosLidosStore)
    factoryOf(::GetNotificationsUseCase)
    factoryOf(::GetMyPaymentsUseCase)

    viewModelOf(::ShellViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::GameDetailViewModel)
    viewModelOf(::PaymentViewModel)
    viewModelOf(::CreateGameViewModel)
    viewModelOf(::ExploreViewModel)
    viewModelOf(::ArenaDetailViewModel)
    viewModelOf(::SlotPickerViewModel)
    viewModelOf(::OrganizerInviteViewModel)
    viewModelOf(::GuestInviteViewModel)
    viewModelOf(::ResolveViewModel)
    viewModelOf(::MyGamesViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::PaymentsViewModel)
    viewModelOf(::ReceiptViewModel)
    viewModelOf(::NotificationsViewModel)
}

/** Modo Atleta/Organizador ([featureModule]) + modo Minha Arena ([arenaAdminModule], feature/arenaadmin). */
val appModules = listOf(dataModule, domainModule, featureModule, arenaAdminModule)

/**
 * Inicia o Koin uma unica vez (idempotente). Chamado pelos entry points de cada plataforma.
 * [platformConfig] permite registrar extras especificos (ex.: androidContext no Android).
 */
fun initKoin(platformConfig: KoinApplication.() -> Unit = {}) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin {
        platformConfig()
        modules(appModules)
    }
}
