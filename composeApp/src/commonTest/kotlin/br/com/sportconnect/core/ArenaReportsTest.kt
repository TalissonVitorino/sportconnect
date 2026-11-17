package br.com.sportconnect.core

import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.fake.FakeArenaRepository
import br.com.sportconnect.core.data.fake.FakeGameRepository
import br.com.sportconnect.core.domain.usecase.ArenaReportsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/** E11: relatorio da arena e regras editaveis. */
class ArenaReportsTest {
    private val clock = TestClock(Instant.parse("2026-11-17T12:00:00Z"))
    private val seed = DemoData(clock)
    private val arenas = FakeArenaRepository(seed)
    private val reports = ArenaReportsUseCase(arenas, FakeGameRepository(seed), clock)

    @Test
    fun relatorio_tem_todas_as_quadras_e_ocupacao_entre_0_e_100() = runTest {
        val r = reports(DemoData.ARENA_VILA_MADALENA, 30)
        assertEquals(arenas.quadras(DemoData.ARENA_VILA_MADALENA).size, r.courts.size)
        assertTrue(r.courts.all { it.booked <= it.offered })
        assertTrue(r.occupancyPct in 0..100)
        assertTrue(r.courts.sumOf { it.offered } > 0)
    }

    @Test
    fun atualizar_arena_muda_as_regras() = runTest {
        arenas.atualizarArena(DemoData.ARENA_VILA_MADALENA) { it.copy(duracaoHoldHoras = 12, janelaCancelamentoHoras = 6) }
        val a = arenas.arena(DemoData.ARENA_VILA_MADALENA)!!
        assertEquals(12, a.duracaoHoldHoras)
        assertEquals(6, a.janelaCancelamentoHoras)
    }
}
