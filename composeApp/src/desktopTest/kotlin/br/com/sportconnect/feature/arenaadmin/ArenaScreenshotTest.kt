package br.com.sportconnect.feature.arenaadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.ReservationService
import br.com.sportconnect.core.domain.ReservationStateMachine
import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.data.fake.FakePaymentGateway
import br.com.sportconnect.core.domain.usecase.CancelGameUseCase
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.feature.arenaadmin.booking.ArenaBookingScreen
import br.com.sportconnect.feature.arenaadmin.booking.ArenaBookingViewModel
import br.com.sportconnect.feature.arenaadmin.booking.CancelBookingDialog
import br.com.sportconnect.feature.arenaadmin.booking.CancelReason
import br.com.sportconnect.feature.arenaadmin.common.AgendaMode
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtEditScreen
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtEditViewModel
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtsScreen
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtsViewModel
import br.com.sportconnect.feature.arenaadmin.dashboard.ArenaDashboardScreen
import br.com.sportconnect.feature.arenaadmin.onboarding.ArenaOnboardingScreen
import br.com.sportconnect.feature.arenaadmin.onboarding.ArenaOnboardingViewModel
import br.com.sportconnect.feature.arenaadmin.payouts.ArenaPayoutsScreen
import br.com.sportconnect.feature.arenaadmin.payouts.ArenaPayoutsViewModel
import br.com.sportconnect.feature.arenaadmin.schedule.ArenaScheduleScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Screenshots do modo "Minha Arena" para comparar com docs/assets/mockups/09-arena.png e 10-desk.png.
 * Saida: <raiz>/build-screens/arena-*.png
 * Rodar: ./gradlew :composeApp:desktopTest --tests '*ArenaScreenshotTest*' -PdisableAndroid=true
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaScreenshotTest {
    private val outDir: File = run {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (!File(dir, "settings.gradle.kts").exists() && dir.parentFile != null) dir = dir.parentFile
        File(dir, "build-screens").apply { mkdirs() }
    }

    @BeforeTest fun setUp() = Dispatchers.setMain(Dispatchers.Unconfined)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val hoje = LocalDate(2026, 11, 19)

    /** Ambiente com o gestor logado e uma noite movimentada hoje (como no mockup). */
    private fun env(): ArenaTestEnv = ArenaTestEnv().also { e ->
        runBlocking {
            e.loginGestor()
            val seed = e.seed
            // Futsal confirmado: amanha -> hoje 19h (em andamento as 19:32).
            val futsal = e.games.game(DemoData.JOGO_FUTSAL_CONFIRMADO)!!
            e.arenas.atualizarSlot(futsal.slotId) { it.copy(status = SlotStatus.LIVRE, jogoId = null) }
            val futsalSlot = seed.slotId("q-vm-futsal", hoje, 19)
            e.games.save(futsal.copy(slotId = futsalSlot, inicio = seed.em(hoje, 19), fim = seed.em(hoje, 20)))
            e.arenas.atualizarSlot(futsalSlot) { it.copy(status = SlotStatus.RESERVADO, jogoId = futsal.id, reservaExterna = null) }
            // Society confirmado hoje 20h (12/12) a partir do jogo concluido de terca.
            val base = e.games.game(DemoData.JOGO_SOCIETY_CONCLUIDO)!!
            val socSlot = seed.slotId("q-vm-society2", hoje, 20)
            val soc = base.copy(
                id = "jogo-demo-society", titulo = "Society da quinta", slotId = socSlot,
                inicio = seed.em(hoje, 20), fim = seed.em(hoje, 21),
                reserva = base.reserva.copy(id = "res-demo-society", status = StatusReserva.CONFIRMADO),
            )
            e.games.save(soc)
            e.arenas.atualizarSlot(socSlot) { it.copy(status = SlotStatus.RESERVADO, jogoId = soc.id, reservaExterna = null) }
            // Racha (pre-reserva) -> hoje 22h na Society 1, hold ate 20h.
            val racha = e.games.game(DemoData.JOGO_RACHA)!!
            e.arenas.atualizarSlot(racha.slotId) { it.copy(status = SlotStatus.LIVRE, jogoId = null) }
            val rachaSlot = seed.slotId("q-vm-society1", hoje, 22)
            e.games.save(racha.copy(slotId = rachaSlot, inicio = seed.em(hoje, 22), fim = seed.em(hoje, 23), reserva = racha.reserva.copy(prazoHold = seed.em(hoje, 20))))
            e.arenas.atualizarSlot(rachaSlot) { it.copy(status = SlotStatus.PRE_RESERVADO, jogoId = racha.id, reservaExterna = null) }
            // Bloqueio de aula as 18h na Society 2.
            e.arenas.atualizarSlot(seed.slotId("q-vm-society2", hoje, 18)) { it.copy(status = SlotStatus.BLOQUEADO, motivoBloqueio = "Aula", reservaExterna = null) }
        }
    }

    private fun render(name: String, widthDp: Int, heightDp: Int, dark: Boolean = false, density: Float = 1f, content: @Composable () -> Unit) {
        val e = ArenaTestEnv()
        ImageComposeScene(
            width = (widthDp * density).toInt(),
            height = (heightDp * density).toInt(),
            density = Density(density),
        ) {
            CompositionLocalProvider(LocalSCClock provides e.clock) {
                SportConnectTheme(darkTheme = dark) {
                    Box(Modifier.fillMaxSize().background(SportConnectTheme.colors.surface)) { content() }
                }
            }
        }.use { scene ->
            var t = 0L
            repeat(8) {
                scene.render(t)
                Thread.sleep(250)
                t += 100_000_000L
            }
            val image = scene.render(t)
            val data = image.encodeToData(EncodedImageFormat.PNG) ?: error("encode falhou")
            val file = File(outDir, name)
            file.writeBytes(data.bytes)
            assertTrue(file.length() > 0)
            println("screenshot: ${file.absolutePath}")
        }
    }

    @Test
    fun painelCompactLight() {
        val vm = env().dashboardVm()
        render("arena-dashboard-compact-light.png", 400, 1500) { ArenaDashboardScreen(ArenaAdminNavigator.Preview, vm, ticking = false) }
    }

    @Test
    fun painelCompactDark() {
        val vm = env().dashboardVm()
        render("arena-dashboard-compact-dark.png", 400, 1500, dark = true) { ArenaDashboardScreen(ArenaAdminNavigator.Preview, vm, ticking = false) }
    }

    @Test
    fun painelExpanded() {
        val e = env()
        val vm = e.dashboardVm()
        vm.select(e.seed.slotId("q-vm-society2", hoje, 20))
        render("arena-dashboard-expanded.png", 1280, 800) { ArenaDashboardScreen(ArenaAdminNavigator.Preview, vm, ticking = false) }
    }

    @Test
    fun painelMedium() {
        val vm = env().dashboardVm()
        render("arena-dashboard-medium.png", 720, 1300) { ArenaDashboardScreen(ArenaAdminNavigator.Preview, vm, ticking = false) }
    }

    @Test
    fun agendaCompact() {
        val vm = env().scheduleVm()
        vm.selectCourt("q-vm-society2")
        render("arena-schedule-compact.png", 400, 1100) { ArenaScheduleScreen(ArenaAdminNavigator.Preview, vm, ticking = false) }
    }

    @Test
    fun agendaSemanaExpanded() {
        val vm = env().scheduleVm()
        vm.selectCourt("q-vm-society1")
        vm.setMode(AgendaMode.Week)
        render("arena-schedule-week-expanded.png", 1280, 800) { ArenaScheduleScreen(ArenaAdminNavigator.Preview, vm, ticking = false) }
    }

    private fun bookingVm(e: ArenaTestEnv, id: String): ArenaBookingViewModel {
        val service = ReservationService(
            e.games, e.arenas, FakePaymentGateway(e.clock, CoroutineScope(Dispatchers.Unconfined)), e.analytics,
            ReservationStateMachine(), e.payout, WaitlistManager(), e.clock,
        )
        val cancel = CancelGameUseCase(service, e.games, e.arenas, e.auth, e.clock)
        return ArenaBookingViewModel(id, e.arenas, e.games, e.auth, e.payout, cancel, e.clock)
    }

    @Test
    fun reservaCompact() {
        val e = env()
        val vm = bookingVm(e, e.seed.slotId("q-vm-society2", hoje, 20))
        render("arena-booking-compact.png", 400, 1700) { ArenaBookingScreen("x", ArenaAdminNavigator.Preview, vm) }
    }

    @Test
    fun reservaCancelamento() {
        val e = env()
        val vm = bookingVm(e, DemoData.JOGO_RACHA)
        vm.openCancel()
        vm.selectReason(CancelReason.Weather)
        render("arena-booking-cancel-dialog.png", 400, 520) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                CancelBookingDialog(vm.state.value, vm, inline = true)
            }
        }
    }

    @Test
    fun repassesCompact() {
        val e = env()
        val vm = ArenaPayoutsViewModel(e.arenas, e.games, e.auth, e.payout, e.analytics, e.clock)
        vm.setPeriod(br.com.sportconnect.feature.arenaadmin.payouts.PayoutPeriod.All)
        render("arena-payouts-compact.png", 400, 1300) { ArenaPayoutsScreen(ArenaAdminNavigator.Preview, vm) }
    }

    @Test
    fun repassesExpanded() {
        val e = env()
        val vm = ArenaPayoutsViewModel(e.arenas, e.games, e.auth, e.payout, e.analytics, e.clock)
        vm.setPeriod(br.com.sportconnect.feature.arenaadmin.payouts.PayoutPeriod.All)
        vm.select(vm.state.value.items.firstOrNull()?.id)
        render("arena-payouts-expanded.png", 1280, 800) { ArenaPayoutsScreen(ArenaAdminNavigator.Preview, vm) }
    }

    @Test
    fun quadras() {
        val e = env()
        val vm = ArenaCourtsViewModel(e.arenas, e.auth, e.store, e.clock)
        render("arena-courts-compact.png", 400, 1300) { ArenaCourtsScreen(ArenaAdminNavigator.Preview, vm, br.com.sportconnect.core.data.local.ArenaPhotoStore(com.russhwolf.settings.MapSettings())) }
    }

    @Test
    fun edicaoDeQuadra() {
        val e = env()
        val vm = ArenaCourtEditViewModel("q-vm-society1", e.arenas, e.auth, e.store, e.analytics, e.clock)
        render("arena-court-edit-expanded.png", 1280, 1300) { ArenaCourtEditScreen("q-vm-society1", ArenaAdminNavigator.Preview, vm) }
    }

    @Test
    fun onboarding() {
        val vm = env().onboardingVm()
        vm.setName("Arena Pinheiros")
        render("arena-onboarding-compact.png", 400, 1000) { ArenaOnboardingScreen(ArenaAdminNavigator.Preview, vm) }
    }
}
