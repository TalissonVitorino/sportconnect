package br.com.sportconnect.feature

import br.com.sportconnect.core.TestClock
import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.fake.FakeArenaRepository
import br.com.sportconnect.core.data.fake.InMemoryAnalyticsTracker
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.domain.usecase.GetArenaDetailUseCase
import br.com.sportconnect.core.domain.usecase.GetArenasNearbyUseCase
import br.com.sportconnect.core.domain.usecase.GetAvailableSlotsUseCase
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.platform.KnownCities
import br.com.sportconnect.feature.explore.ExploreViewModel
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/** B10/C7: a busca usa a origem salva no onboarding e mostra os esportes favoritos primeiro. */
@OptIn(ExperimentalCoroutinesApi::class)
class ExploreOriginTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = TestClock(Instant.parse("2026-11-17T12:00:00Z"))
    private val seed = DemoData(clock)
    private val arenas = FakeArenaRepository(seed)
    private val prefs = AppPreferences(MapSettings())

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun vm() = ExploreViewModel(
        GetArenasNearbyUseCase(arenas, InMemoryAnalyticsTracker()),
        GetArenaDetailUseCase(arenas),
        GetAvailableSlotsUseCase(arenas, clock),
        clock,
        prefs,
    )

    @Test
    fun com_a_posicao_na_vila_madalena_a_arena_de_la_vem_primeiro_e_a_0_km() = runTest(dispatcher) {
        val vila = seed.arenas.first { it.id == DemoData.ARENA_VILA_MADALENA }.localizacao
        prefs.searchOrigin = AppPreferences.SavedOrigin(vila.latitude, vila.longitude, null)
        val vm = vm()
        advanceUntilIdle()
        val first = vm.uiState.value.results.first()
        assertEquals(DemoData.ARENA_VILA_MADALENA, first.arena.arena.id)
        assertTrue(first.arena.distanciaKm < 0.1)
    }

    @Test
    fun cidade_digitada_conhecida_vira_origem_e_desconhecida_cai_em_sao_paulo() {
        assertEquals(listOf("São Paulo"), KnownCities.match("sao paulo"))
        assertEquals(listOf("Belo Horizonte"), KnownCities.match("belo"))
        assertTrue(KnownCities.match("Xique-Xique").isEmpty())
    }

    @Test
    fun esportes_favoritos_aparecem_primeiro_sem_esconder_os_outros() = runTest(dispatcher) {
        prefs.favoriteSports = setOf(Sport.PADEL.name)
        val vm = vm()
        advanceUntilIdle()
        val results = vm.uiState.value.results
        assertTrue(results.first().quadra.suporta(Sport.PADEL), "padel primeiro")
        assertTrue(results.any { !it.quadra.suporta(Sport.PADEL) }, "os outros esportes continuam na lista")
        val firstOther = results.indexOfFirst { !it.quadra.suporta(Sport.PADEL) }
        assertTrue(results.drop(firstOther).none { it.quadra.suporta(Sport.PADEL) }, "nenhum padel depois dos outros")
    }
}
