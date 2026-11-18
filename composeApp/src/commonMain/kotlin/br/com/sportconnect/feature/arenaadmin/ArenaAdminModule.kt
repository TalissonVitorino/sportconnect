package br.com.sportconnect.feature.arenaadmin

import br.com.sportconnect.core.domain.usecase.ArenaReportsUseCase
import br.com.sportconnect.feature.arenaadmin.reports.ArenaSettingsViewModel
import br.com.sportconnect.feature.arenaadmin.reports.ArenaReportsViewModel
import br.com.sportconnect.feature.arenaadmin.booking.ArenaBookingViewModel
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtEditViewModel
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtStore
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtsViewModel
import br.com.sportconnect.feature.arenaadmin.dashboard.ArenaDashboardViewModel
import br.com.sportconnect.feature.arenaadmin.onboarding.ArenaOnboardingViewModel
import br.com.sportconnect.feature.arenaadmin.payouts.ArenaPayoutsViewModel
import br.com.sportconnect.feature.arenaadmin.schedule.ArenaScheduleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ViewModels do modo "Minha Arena". Depende de `dataModule` e `domainModule` (di/CoreModules.kt).
 * Booking e CourtEdit recebem o id via `parametersOf(...)` (CourtEdit: "" = nova quadra).
 */
val arenaAdminModule = module {
    single { ArenaCourtStore() }
    viewModel { ArenaDashboardViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ArenaScheduleViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { (bookingId: String) -> ArenaBookingViewModel(bookingId, get(), get(), get(), get(), get(), get()) }
    viewModel { ArenaCourtsViewModel(get(), get(), get(), get()) }
    viewModel { (courtId: String) -> ArenaCourtEditViewModel(courtId.ifEmpty { null }, get(), get(), get(), get(), get()) }
    viewModel { ArenaPayoutsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ArenaOnboardingViewModel(get(), get()) }
    viewModel { ArenaReportsViewModel(get(), get()) }
    viewModel { ArenaSettingsViewModel(get(), get()) }
    factory { ArenaReportsUseCase(get(), get(), get()) }
}
