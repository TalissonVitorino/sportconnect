package br.com.sportconnect.feature.arenaadmin

import br.com.sportconnect.di.dataModule
import br.com.sportconnect.di.domainModule
import br.com.sportconnect.feature.arenaadmin.booking.ArenaBookingViewModel
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtEditViewModel
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtsViewModel
import br.com.sportconnect.feature.arenaadmin.dashboard.ArenaDashboardViewModel
import br.com.sportconnect.feature.arenaadmin.onboarding.ArenaOnboardingViewModel
import br.com.sportconnect.feature.arenaadmin.payouts.ArenaPayoutsViewModel
import br.com.sportconnect.feature.arenaadmin.schedule.ArenaScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ArenaAdminModuleTest {
    @Test
    fun modulo_da_arena_resolve_todos_os_view_models() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val koin = startKoin { modules(dataModule, domainModule, arenaAdminModule) }.koin
        try {
            assertNotNull(koin.get<ArenaDashboardViewModel>())
            assertNotNull(koin.get<ArenaScheduleViewModel>())
            assertNotNull(koin.get<ArenaBookingViewModel> { parametersOf("slot-x") })
            assertNotNull(koin.get<ArenaCourtsViewModel>())
            assertNotNull(koin.get<ArenaCourtEditViewModel> { parametersOf("") })
            assertNotNull(koin.get<ArenaCourtEditViewModel> { parametersOf("q-vm-society1") })
            assertNotNull(koin.get<ArenaPayoutsViewModel>())
            assertNotNull(koin.get<ArenaOnboardingViewModel>())
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }
}
