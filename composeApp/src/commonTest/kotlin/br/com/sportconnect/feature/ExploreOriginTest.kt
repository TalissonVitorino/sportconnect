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
import kotlinx.datetime.toLocalDateTime

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

    @Test
    fun filtro_noite_so_mostra_horarios_depois_das_18h() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        vm.setPeriod(br.com.sportconnect.feature.explore.DayPeriod.NIGHT)
        advanceUntilIdle()
        val slots = vm.uiState.value.results.flatMap { it.freeSlots }
        assertTrue(slots.isNotEmpty())
        assertTrue(slots.all { it.inicio.toLocalDateTime(br.com.sportconnect.core.model.FusoSaoPaulo).hour >= 18 })
    }

    @Test
    fun preco_maximo_e_ordenacao_por_preco() = runTest(dispatcher) {
        val vm = vm()
        advanceUntilIdle()
        vm.setSort(br.com.sportconnect.feature.explore.ExploreSort.PRICE)
        advanceUntilIdle()
        val precos = vm.uiState.value.results.map { it.quadra.precoHora.cents }
        assertEquals(precos.sorted(), precos)
        val teto = br.com.sportconnect.core.model.Money(20_000)
        vm.setMaxPrice(teto)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.results.all { it.quadra.precoHora <= teto })
        vm.clearFilters()
        advanceUntilIdle()
        assertEquals(null, vm.uiState.value.maxPrice)
    }
}
