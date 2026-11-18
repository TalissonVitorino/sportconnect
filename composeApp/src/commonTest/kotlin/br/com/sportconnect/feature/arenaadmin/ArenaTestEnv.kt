package br.com.sportconnect.feature.arenaadmin

import br.com.sportconnect.core.TestClock
import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.fake.FakeArenaRepository
import br.com.sportconnect.core.data.fake.FakeAuthRepository
import br.com.sportconnect.core.data.fake.FakeGameRepository
import br.com.sportconnect.core.data.fake.InMemoryAnalyticsTracker
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.usecase.ArenaDashboardUseCase
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtStore
import br.com.sportconnect.feature.arenaadmin.dashboard.ArenaDashboardViewModel
import br.com.sportconnect.feature.arenaadmin.onboarding.ArenaOnboardingViewModel
import br.com.sportconnect.feature.arenaadmin.schedule.ArenaScheduleViewModel
import kotlin.time.Instant

/** Ambiente fake do modo arena: quinta 19/11/2026 19:32 em Sao Paulo (como o mockup 10-desk). */
class ArenaTestEnv(now: Instant = QUINTA_19H32) {
    val clock = TestClock(now)
    val seed = DemoData(clock)
    val analytics = InMemoryAnalyticsTracker()
    val arenas = FakeArenaRepository(seed)
    val games = FakeGameRepository(seed)
    val auth = FakeAuthRepository(seed, analytics, clock)
    val payout = PayoutCalculator()
    val store = ArenaCourtStore()
    val dashboardUseCase = ArenaDashboardUseCase(arenas, games, analytics)

    fun dashboardVm() = ArenaDashboardViewModel(dashboardUseCase, arenas, games, auth, payout, store, analytics, clock)
    fun scheduleVm() = ArenaScheduleViewModel(dashboardUseCase, arenas, auth, payout, analytics, clock)
    fun onboardingVm() = ArenaOnboardingViewModel(auth, arenas)

    suspend fun loginGestor() {
        auth.entrarComo(DemoData.ID_GESTOR).getOrThrow()
    }

    companion object {
        val QUINTA_19H32: Instant = Instant.parse("2026-11-19T22:32:00Z")
        const val VM = DemoData.ARENA_VILA_MADALENA
    }
}
