package br.com.sportconnect.core

import br.com.sportconnect.core.domain.ReservationService
import br.com.sportconnect.core.domain.SportRepository
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
import br.com.sportconnect.core.domain.usecase.PayShareUseCase
import br.com.sportconnect.core.domain.usecase.ProcessDeadlinesUseCase
import br.com.sportconnect.di.dataModule
import br.com.sportconnect.di.domainModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.Test
import kotlin.test.assertNotNull

class DiModulesTest {
    @Test
    fun grafo_de_dados_e_dominio_resolve_todos_os_casos_de_uso() {
        val koin = startKoin { modules(dataModule, domainModule) }.koin
        try {
            listOf(
                SportRepository::class, PaymentGateway::class, ReservationService::class,
                GetArenasNearbyUseCase::class, GetArenaDetailUseCase::class, GetAvailableSlotsUseCase::class,
                CreateGameUseCase::class, GetGameUseCase::class, PayShareUseCase::class, CoverDifferenceUseCase::class,
                JoinGameByInviteUseCase::class, JoinWaitlistUseCase::class, GetMyGamesUseCase::class,
                GetOpenGamesUseCase::class, ArenaDashboardUseCase::class, CancelGameUseCase::class,
                LeaveGameUseCase::class, ProcessDeadlinesUseCase::class,
            ).forEach { assertNotNull(koin.get<Any>(it), it.simpleName) }
        } finally {
            stopKoin()
        }
    }
}
