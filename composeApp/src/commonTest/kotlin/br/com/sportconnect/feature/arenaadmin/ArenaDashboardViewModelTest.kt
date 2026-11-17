package br.com.sportconnect.feature.arenaadmin

import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellStatus
import br.com.sportconnect.feature.arenaadmin.common.ArenaMessage
import br.com.sportconnect.feature.arenaadmin.common.BlockReason
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ArenaDashboardViewModelTest {

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun usuario_sem_papel_de_arena_ve_estado_sem_arena() = runTest {
        val env = ArenaTestEnv() // logado como organizador por padrao
        val vm = env.dashboardVm()
        assertEquals(LoadState.NoArena, vm.state.value.load)
    }

    @Test
    fun gestor_ve_painel_da_propria_arena_com_grade_por_quadra() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val s = env.dashboardVm().state.value
        assertEquals(LoadState.Ready, s.load)
        assertEquals("Arena Vila Madalena Sports", s.arenaName)
        assertEquals(LocalDate(2026, 11, 19), s.date)
        assertEquals("qui, 19 nov", s.dateLabel)
        assertEquals(19 * 60 + 32, s.nowMinutes)
        assertEquals(3, s.agenda.courts.size)
        // Grade 7h..23h; 23h nao tem slot -> "Fechado".
        assertEquals((7..23).toList(), s.agenda.hours)
        s.agenda.courts.forEach { c ->
            assertEquals(s.agenda.hours.size, c.cells.size)
            assertEquals(AgendaCellStatus.Closed, c.cells.last().status)
        }
        // Linha "Agora" por quadra.
        assertEquals(s.agenda.courts.map { it.courtId }, s.nowRows.map { it.courtId })
        assertTrue(s.showFinancials)
    }

    @Test
    fun resumo_confere_com_o_caso_de_uso() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.dashboardVm()
        val painel = assertNotNull(vm.painel())
        val s = vm.state.value.summary
        assertEquals(painel.confirmados, s.confirmed)
        assertEquals(painel.preReservas, s.holds)
        assertEquals(painel.bloqueados, s.blocked)
        assertEquals(painel.ocupacaoPct, s.occupancyPct)
    }

    @Test
    fun pre_reservas_futuras_e_proximo_repasse_projetado() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val s = env.dashboardVm().state.value
        // "Racha dos amigos" (Society 1, daqui a 2 dias) aguarda pagamentos.
        val hold = s.holds.single { it.jogoId == DemoData.JOGO_RACHA }
        assertEquals(12, hold.total)
        assertEquals(8, hold.paid)
        assertNotNull(hold.deadlineLabel)
        // Futsal confirmado amanha (sex 20/11, R$ 240, plano Pro 4%) -> repasse seg 23/11 de R$ 230,40.
        val next = assertNotNull(s.nextPayout)
        assertEquals("seg, 23 nov", next.dateLabel)
        assertEquals(23_040L, next.amount.cents)
        assertTrue(next.projected)
    }

    @Test
    fun alerta_de_quadras_sem_horarios_some_ao_publicar() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.dashboardVm()
        val alerts = vm.state.value.alerts
        assertEquals(3, alerts.size)
        assertTrue(alerts.all { it.fromDate == "26/11/2026" })
        env.store.markPublished("q-vm-futsal", LocalDate(2026, 12, 31))
        assertEquals(2, vm.state.value.alerts.size)
        assertTrue(vm.state.value.alerts.none { it.courtId == "q-vm-futsal" })
    }

    @Test
    fun bloquear_horario_livre_com_motivo_e_desfazer() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.dashboardVm()
        val free = vm.state.value.agenda.courts.flatMap { it.cells }.first { it.status == AgendaCellStatus.Free && it.hour >= 20 }
        vm.onCellClick(free)
        val sheet = assertNotNull(vm.state.value.block)
        assertEquals(free.slotId, sheet.slotId)
        assertEquals(free.courtId, sheet.courtId)
        assertTrue(!sheet.canConfirm) // motivo obrigatorio
        vm.selectBlockReason(BlockReason.Class)
        assertTrue(vm.state.value.block!!.canConfirm)
        vm.confirmBlock("Aula")

        assertNull(vm.state.value.block)
        val msg = assertIs<ArenaMessage.Blocked>(vm.state.value.message)
        val slot = env.arenas.slot(free.slotId!!)!!
        assertEquals(SlotStatus.BLOQUEADO, slot.status)
        assertEquals("Aula", slot.motivoBloqueio)
        val cell = vm.state.value.agenda.cell(free.slotId!!)!!
        assertEquals(AgendaCellStatus.Blocked, cell.status)
        assertEquals("Aula", cell.detail)

        vm.consumeMessage()
        vm.undoBlock(msg.slotId)
        assertEquals(SlotStatus.LIVRE, env.arenas.slot(free.slotId!!)!!.status)
    }

    @Test
    fun bloquear_horario_com_reserva_abre_aviso() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.dashboardVm()
        val booked = vm.state.value.agenda.courts.flatMap { it.cells }.first { it.isBooking }
        vm.openBlock(slotId = booked.slotId)
        assertNull(vm.state.value.block)
        assertEquals(ArenaMessage.BlockHasBooking(booked.slotId!!), vm.state.value.message)
    }

    @Test
    fun selecionar_reserva_preenche_painel_lateral_e_navegar_de_dia() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.dashboardVm()
        vm.nextDay()
        val s = vm.state.value
        assertEquals(LocalDate(2026, 11, 20), s.date)
        assertTrue(!s.isToday)
        val futsal = s.agenda.courts.first { it.courtId == "q-vm-futsal" }.cells.first { it.hour == 19 }
        assertEquals(AgendaCellStatus.Confirmed, futsal.status)
        assertEquals(DemoData.JOGO_FUTSAL_CONFIRMADO, futsal.jogoId)
        assertEquals(10, futsal.paid)
        vm.onCellClick(futsal)
        val detail = assertNotNull(vm.state.value.selected)
        assertEquals(StatusReserva.CONFIRMADO, detail.gameStatus)
        assertEquals(24_000L, detail.gross.cents)
        assertEquals(960L, detail.fee.cents)
        assertEquals(23_040L, detail.net.cents)
        vm.today()
        assertEquals(LocalDate(2026, 11, 19), vm.state.value.date)
        assertNull(vm.state.value.selected)
    }

    @Test
    fun linha_agora_reflete_jogo_em_andamento() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val slotId = env.seed.slotId("q-vm-futsal", LocalDate(2026, 11, 19), 19)
        env.arenas.atualizarSlot(slotId) { it.copy(status = SlotStatus.RESERVADO, reservaExterna = "Mensalista") }
        val vm = env.dashboardVm()
        val row = vm.state.value.nowRows.single { it.courtId == "q-vm-futsal" }
        assertEquals(AgendaCellStatus.InProgress, row.cell?.status)
        assertEquals("19h–20h", row.cell?.rangeLabel)
    }
}
