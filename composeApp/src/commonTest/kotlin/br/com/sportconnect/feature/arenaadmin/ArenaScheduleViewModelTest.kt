package br.com.sportconnect.feature.arenaadmin

import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellStatus
import br.com.sportconnect.feature.arenaadmin.common.AgendaMode
import br.com.sportconnect.feature.arenaadmin.common.ArenaMessage
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ArenaScheduleViewModelTest {

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun abre_no_dia_de_hoje_com_primeira_quadra_selecionada() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val s = env.scheduleVm().state.value
        assertEquals(LoadState.Ready, s.load)
        assertEquals(AgendaMode.Day, s.mode)
        assertEquals(LocalDate(2026, 11, 19), s.date)
        assertTrue(s.isToday)
        assertEquals("Quadra Futsal", s.agenda.courts.first().name)
        assertEquals(s.agenda.courts.first().courtId, s.courtId)
        // Lista do compact: so horas com oferta (7h..22h) da quadra selecionada.
        assertEquals((7..22).toList(), s.courtCells.map { it.hour })
    }

    @Test
    fun navega_entre_dias_e_volta_para_hoje() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.scheduleVm()
        vm.next()
        assertEquals("sex, 20 nov", vm.state.value.dateLabel)
        val futsal = vm.state.value.agenda.courts.first { it.courtId == "q-vm-futsal" }.cells.first { it.hour == 19 }
        assertEquals(AgendaCellStatus.Confirmed, futsal.status)
        assertEquals(DemoData.JOGO_FUTSAL_CONFIRMADO, futsal.jogoId)
        vm.previous(); vm.previous()
        assertEquals(LocalDate(2026, 11, 18), vm.state.value.date)
        vm.today()
        assertEquals(LocalDate(2026, 11, 19), vm.state.value.date)
    }

    @Test
    fun semana_mostra_sete_dias_de_uma_quadra_a_partir_de_segunda() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.scheduleVm()
        vm.selectCourt("q-vm-society1")
        vm.setMode(AgendaMode.Week)
        val s = vm.state.value
        val week = assertNotNull(s.week)
        assertEquals("q-vm-society1", week.courtId)
        assertEquals(7, week.days.size)
        assertEquals(DayOfWeek.MONDAY, week.days.first().date.dayOfWeek)
        assertEquals(LocalDate(2026, 11, 16), week.days.first().date)
        assertTrue(week.days.single { it.isToday }.date == LocalDate(2026, 11, 19))
        assertEquals("16 nov – 22 nov", s.weekLabel)
        // Racha (sab 21/11 20h) aparece como pre-reserva na coluna de sabado.
        val sabado = week.days.indexOfFirst { it.date == LocalDate(2026, 11, 21) }
        val racha = week.cells[sabado].first { it.hour == 20 }
        assertEquals(AgendaCellStatus.Hold, racha.status)
        assertEquals(DemoData.JOGO_RACHA, racha.jogoId)
        // Dias antes de hoje nao tem slots no fake -> Fechado.
        assertTrue(week.cells[0].all { it.status == AgendaCellStatus.Closed })
        // Avancar na semana pula 7 dias.
        vm.next()
        assertEquals(LocalDate(2026, 11, 26), vm.state.value.date)
    }

    @Test
    fun tocar_reserva_seleciona_detalhe() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.scheduleVm()
        vm.next()
        val cell = vm.state.value.agenda.courts.first { it.courtId == "q-vm-futsal" }.cells.first { it.hour == 19 }
        vm.onCellClick(cell)
        val d = assertNotNull(vm.state.value.selected)
        assertEquals(cell.slotId, d.slotId)
        assertEquals(10, d.paid)
        assertEquals("19h–20h", d.rangeLabel)
        vm.clearSelection()
        assertNull(vm.state.value.selected)
    }

    @Test
    fun horario_bloqueado_pede_confirmacao_e_libera() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val slotId = env.seed.slotId("q-vm-society2", LocalDate(2026, 11, 19), 21)
        env.arenas.atualizarSlot(slotId) { it.copy(status = SlotStatus.BLOQUEADO, motivoBloqueio = "Manutenção") }
        val vm = env.scheduleVm()
        val cell = vm.state.value.agenda.cell(slotId)!!
        assertEquals(AgendaCellStatus.Blocked, cell.status)
        vm.onCellClick(cell)
        assertEquals(slotId, vm.state.value.unblock?.slotId)
        vm.confirmUnblock()
        assertNull(vm.state.value.unblock)
        assertEquals(ArenaMessage.Unblocked, vm.state.value.message)
        assertEquals(SlotStatus.LIVRE, env.arenas.slot(slotId)!!.status)
        assertEquals(AgendaCellStatus.Free, vm.state.value.agenda.cell(slotId)!!.status)
    }

    @Test
    fun bloqueio_pela_agenda_usa_quadra_da_aba() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.scheduleVm()
        vm.selectCourt("q-vm-society2")
        vm.openBlock()
        val sheet = assertNotNull(vm.state.value.block)
        assertEquals("q-vm-society2", sheet.courtId)
        assertTrue(sheet.slots.isNotEmpty())
        vm.selectBlockCourt("q-vm-futsal")
        assertEquals("q-vm-futsal", vm.state.value.block!!.courtId)
        vm.dismissBlock()
        assertNull(vm.state.value.block)
    }
}
