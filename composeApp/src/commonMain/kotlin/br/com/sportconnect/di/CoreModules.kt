package br.com.sportconnect.di

import br.com.sportconnect.core.data.InMemorySportRepository
import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.local.AppPreferences
import com.russhwolf.settings.Settings
import br.com.sportconnect.core.data.fake.FakeArenaRepository
import br.com.sportconnect.core.data.fake.FakeAuthRepository
import br.com.sportconnect.core.data.fake.FakeGameRepository
import br.com.sportconnect.core.data.fake.FakePaymentGateway
import br.com.sportconnect.core.data.fake.InMemoryAnalyticsTracker
import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.RefundPolicy
import br.com.sportconnect.core.domain.ReservationService
import br.com.sportconnect.core.domain.ReservationStateMachine
import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.domain.SportRepository
import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.domain.repository.PaymentGateway
import br.com.sportconnect.core.domain.usecase.ArenaDashboardUseCase
import br.com.sportconnect.core.domain.usecase.CancelGameUseCase
import br.com.sportconnect.core.domain.usecase.CoverDifferenceUseCase
import br.com.sportconnect.core.domain.usecase.CreateGameUseCase
import br.com.sportconnect.core.domain.usecase.GetArenaDetailUseCase
import br.com.sportconnect.core.domain.usecase.GetArenasNearbyUseCase
import br.com.sportconnect.core.domain.usecase.GetAvailableSlotsUseCase
import br.com.sportconnect.core.domain.usecase.GetGameUseCase
import br.com.sportconnect.core.domain.usecase.GetMyGamesUseCase
import br.com.sportconnect.core.domain.usecase.GetOpenGamesUseCase
import br.com.sportconnect.core.domain.usecase.JoinGameByInviteUseCase
import br.com.sportconnect.core.domain.usecase.JoinWaitlistUseCase
import br.com.sportconnect.core.domain.usecase.LeaveGameUseCase
import br.com.sportconnect.core.domain.usecase.ObserveGameWithWaitlistUseCase
import br.com.sportconnect.core.domain.usecase.PayShareUseCase
import br.com.sportconnect.core.domain.usecase.ProcessDeadlinesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

/** Escopo de aplicacao para jobs de fundo (webhooks, prazos). */
val APP_SCOPE = named("appScope")

/**
 * Armazenamento local da plataforma (SharedPreferences/NSUserDefaults/java.util.prefs/localStorage).
 * Separado do [dataModule] para os testes trocarem por um MapSettings em memoria.
 */
val settingsModule = module {
    single<Settings> { Settings() }
}

/** Implementacoes de dados (fakes em memoria para a demo; trocar por Ktor depois). */
val dataModule = module {
    single<Clock> { Clock.System }
    single<CoroutineScope>(APP_SCOPE) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { DemoData(get()) }
    single { AppPreferences(get()) }

    singleOf(::InMemorySportRepository) bind SportRepository::class
    single { InMemoryAnalyticsTracker() } bind AnalyticsTracker::class
    single { FakeArenaRepository(get()) } bind ArenaRepository::class
    single { FakeGameRepository(get()) } bind GameRepository::class
    single { FakeAuthRepository(get(), get(), get(), get()) } bind AuthRepository::class
    single { FakePaymentGateway(get(), get(APP_SCOPE)) } bind PaymentGateway::class
}

/** Regras de negocio puras, servico de reservas e casos de uso das telas. */
val domainModule = module {
    factoryOf(::SplitCostUseCase)
    singleOf(::HoldDeadlineCalculator)
    singleOf(::RefundPolicy)
    singleOf(::PayoutCalculator)
    singleOf(::WaitlistManager)
    singleOf(::ReservationStateMachine)
    // Jobs de fundo (webhooks do gateway + prazos) iniciam junto com o Koin.
    single(createdAtStart = true) {
        ReservationService(get(), get(), get(), get(), get(), get(), get(), get()).also { it.start(get(APP_SCOPE)) }
    }

    factoryOf(::GetArenasNearbyUseCase)
    factoryOf(::GetArenaDetailUseCase)
    factoryOf(::GetAvailableSlotsUseCase)
    factoryOf(::CreateGameUseCase)
    factoryOf(::GetGameUseCase)
    factoryOf(::ObserveGameWithWaitlistUseCase)
    factoryOf(::PayShareUseCase)
    factoryOf(::CoverDifferenceUseCase)
    factoryOf(::JoinGameByInviteUseCase)
    factoryOf(::JoinWaitlistUseCase)
    factoryOf(::GetMyGamesUseCase)
    factoryOf(::GetOpenGamesUseCase)
    factoryOf(::ArenaDashboardUseCase)
    factoryOf(::CancelGameUseCase)
    factoryOf(::LeaveGameUseCase)
    factoryOf(::ProcessDeadlinesUseCase)
}
