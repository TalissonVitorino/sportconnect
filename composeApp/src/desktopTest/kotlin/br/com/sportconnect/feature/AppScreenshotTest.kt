package br.com.sportconnect.feature

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.di.initKoin
import br.com.sportconnect.feature.creategame.CreateGameScreen
import br.com.sportconnect.feature.creategame.CreateGameViewModel
import br.com.sportconnect.feature.creategame.CreateStep
import br.com.sportconnect.feature.invite.GuestInviteScreen
import br.com.sportconnect.feature.invite.GuestInviteUiState
import br.com.sportconnect.feature.invite.GuestInviteViewModel
import br.com.sportconnect.feature.payment.PaymentScreen
import br.com.sportconnect.feature.payment.PaymentViewModel
import br.com.sportconnect.feature.payment.PixPhase
import br.com.sportconnect.feature.slots.SlotCellState
import br.com.sportconnect.feature.slots.SlotPickerScreen
import br.com.sportconnect.feature.slots.SlotPickerViewModel
import br.com.sportconnect.navigation.AppNavHost
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.skia.EncodedImageFormat
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import java.io.File
import javax.swing.SwingUtilities
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * Screenshots das telas do modo Atleta/Organizador para revisao visual (comparar com docs/assets/mockups).
 * Saida: <raiz>/build-screens/app-*.png. Compact = 400dp (densidade 2), expanded = 1280x800dp.
 * Rodar: ./gradlew :composeApp:desktopTest --tests '*AppScreenshotTest*' -PdisableAndroid=true
 */
class AppScreenshotTest {
    private val outDir: File = run {
        var dir = File(System.getProperty("user.dir")).absoluteFile
        while (!File(dir, "settings.gradle.kts").exists() && dir.parentFile != null) dir = dir.parentFile
        File(dir, "build-screens").apply { mkdirs() }
    }

    private val koin get() = KoinPlatform.getKoin()

    @BeforeTest
    fun startKoin() = initKoin()

    /** Outros testes (DiModulesTest) iniciam o proprio Koin: nao deixar o global vivo. */
    @AfterTest
    fun stopKoinAfter() = stopKoin()
    private val auth: AuthRepository get() = koin.get()

    private fun asOrganizer() = runBlocking { auth.entrarComo(DemoData.ID_ORGANIZADOR) }

    /** Dono de ViewModel/ciclo de vida para o NavHost fora de uma janela real. */
    private class HostOwner : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        private val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle get() = registry
    }

    /** O NavHost exige a thread principal (EDT no desktop): cena criada/renderizada no EDT, espera fora dele. */
    private fun <T> onEdt(block: () -> T): T {
        var result: Result<T>? = null
        SwingUtilities.invokeAndWait { result = runCatching(block) }
        return result!!.getOrThrow()
    }

    private fun render(name: String, widthDp: Int, heightDp: Int, density: Float = 2f, dark: Boolean = false, content: @Composable () -> Unit) {
        val owner = onEdt { HostOwner() }
        val scene = onEdt {
            ImageComposeScene(
                width = (widthDp * density).toInt(),
                height = (heightDp * density).toInt(),
                density = Density(density),
            ) {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalLifecycleOwner provides owner) {
                    SportConnectTheme(darkTheme = dark) {
                        Surface(Modifier.fillMaxSize(), color = SportConnectTheme.colors.surface) { content() }
                    }
                }
            }
        }
        try {
            var t = 0L
            repeat(14) {
                onEdt { scene.render(t) }
                Thread.sleep(250)
                t += 100_000_000L
            }
            val data = onEdt { scene.render(t).encodeToData(EncodedImageFormat.PNG) } ?: error("encode falhou")
            val file = File(outDir, name)
            file.writeBytes(data.bytes)
            assertTrue(file.length() > 0)
            println("screenshot: ${file.absolutePath}")
        } finally {
            onEdt {
                scene.close()
                owner.viewModelStore.clear()
            }
        }
    }

    private fun compact(name: String, dark: Boolean = false, height: Int = 880, content: @Composable () -> Unit) =
        render(name, 400, height, 2f, dark, content)

    private fun expanded(name: String, content: @Composable () -> Unit) = render(name, 1280, 800, 1.25f, false, content)

    // ------------------------------------------------------------------ via casca (navegacao real)

    @Test
    fun home() {
        asOrganizer()
        compact("app-home-compact-light.png") { AppNavHost(startDestination = Route.Home) }
        compact("app-home-compact-dark.png", dark = true) { AppNavHost(startDestination = Route.Home) }
        expanded("app-home-expanded.png") { AppNavHost(startDestination = Route.Home) }
    }

    @Test
    fun auth() {
        compact("app-auth-phone-compact-light.png", height = 800) {
            br.com.sportconnect.feature.auth.AuthScreen(br.com.sportconnect.feature.auth.AuthUiState(phone = "11987654321"))
        }
        compact("app-auth-code-error-compact-light.png", height = 800) {
            br.com.sportconnect.feature.auth.AuthScreen(
                br.com.sportconnect.feature.auth.AuthUiState(
                    step = br.com.sportconnect.feature.auth.AuthStep.Code,
                    phone = "11987654321",
                    resendIn = 21,
                    error = br.com.sportconnect.feature.auth.AuthError.CodeExpired,
                ),
            )
        }
        compact("app-auth-name-compact-light.png", height = 800) {
            br.com.sportconnect.feature.auth.AuthScreen(
                br.com.sportconnect.feature.auth.AuthUiState(step = br.com.sportconnect.feature.auth.AuthStep.Name, name = "Talisson"),
            )
        }
    }

    @Test
    fun explore() {
        asOrganizer()
        compact("app-explore-compact-light.png") { AppNavHost(startDestination = Route.Explore) }
    }

    @Test
    fun arena() {
        asOrganizer()
        compact("app-arena-compact-light.png", height = 1100) { AppNavHost(startDestination = Route.ArenaDetail(DemoData.ARENA_VILA_MADALENA)) }
    }

    @Test
    fun gameDetail() {
        asOrganizer()
        compact("app-game-compact-light.png", height = 1300) { AppNavHost(startDestination = Route.GameDetail(DemoData.JOGO_RACHA)) }
        compact("app-game-compact-dark.png", dark = true, height = 1300) { AppNavHost(startDestination = Route.GameDetail(DemoData.JOGO_RACHA)) }
        expanded("app-game-expanded.png") { AppNavHost(startDestination = Route.GameDetail(DemoData.JOGO_RACHA)) }
    }

    @Test
    fun fechou() {
        asOrganizer()
        compact("app-fechou-compact-light.png", height = 1100) { AppNavHost(startDestination = Route.GameDetail(DemoData.JOGO_FUTSAL_CONFIRMADO)) }
    }

    // ------------------------------------------------------------------ estados especificos (ViewModel direto)

    @Test
    fun slots() {
        asOrganizer()
        val today = Clock.System.now().toLocalDateTime(FusoSaoPaulo).date
        val vm: SlotPickerViewModel = koin.get { parametersOf(Route.SlotPicker("q-vm-society1", today.plus(DatePeriod(days = 2)).toString())) }
        runBlocking { withTimeout(10_000) { vm.uiState.first { !it.loading && it.cells.isNotEmpty() } } }
        // Seleciona o horario livre mais proximo das 19h para mostrar o estado "Selecionado".
        vm.uiState.value.cells.filter { it.state == SlotCellState.Available }
            .minByOrNull { kotlin.math.abs(it.slot.inicio.toLocalDateTime(FusoSaoPaulo).hour - 19) }
            ?.let { vm.toggle(it.slot.id) }
        compact("app-slots-compact-light.png", height = 1000) {
            val s by vm.uiState.collectAsState()
            SlotPickerScreen(s)
        }
    }

    @Test
    fun createGameRuleStep() {
        asOrganizer()
        val today = Clock.System.now().toLocalDateTime(FusoSaoPaulo).date
        val day = today.plus(DatePeriod(days = 3))
        val repo: br.com.sportconnect.core.domain.repository.ArenaRepository = koin.get()
        val slot = runBlocking {
            repo.observeSlots(DemoData.ARENA_VILA_MADALENA, day).first().first {
                it.quadraId == "q-vm-society1" && it.status == br.com.sportconnect.core.model.SlotStatus.LIVRE
            }
        }
        val vm: CreateGameViewModel = koin.get { parametersOf(Route.GameCreate(slot.id)) }
        runBlocking { withTimeout(10_000) { vm.uiState.first { it.rateio != null && it.viable.isNotEmpty() } } }
        vm.setSpots(12)
        vm.next()
        vm.next()
        check(vm.uiState.value.step == CreateStep.Rule)
        compact("app-create-rule-compact-light.png", height = 1100) {
            val s by vm.uiState.collectAsState()
            CreateGameScreen(s)
        }
    }

    @Test
    fun pix() {
        asOrganizer()
        val vm: PaymentViewModel = koin.get { parametersOf(Route.GamePay(DemoData.JOGO_VOLEI)) }
        runBlocking { withTimeout(10_000) { vm.uiState.first { !it.loading } } }
        vm.pay()
        val waiting = runBlocking { withTimeout(10_000) { vm.uiState.first { it.phase is PixPhase.Waiting } } }
        // Estado congelado em "Aguardando" (o Pix fake confirma sozinho em ~3s).
        compact("app-pix-compact-light.png", height = 1000) { PaymentScreen(waiting) }
        val paid = runBlocking { withTimeout(15_000) { vm.uiState.first { it.phase is PixPhase.Paid } } }
        compact("app-pix-paid-compact-light.png") { PaymentScreen(paid) }
    }

    @Test
    fun guestInvite() {
        // Convidado que ainda nao pagou o racha (o organizador seria redirecionado para game/{id}).
        runBlocking { auth.entrarComo("u-atleta-10") }
        try {
            val vm: GuestInviteViewModel = koin.get { parametersOf(Route.Invite("RACHA8")) }
            runBlocking { withTimeout(10_000) { vm.uiState.first { it is GuestInviteUiState.Content } } }
            compact("app-invite-compact-light.png", height = 1100) {
                val s by vm.uiState.collectAsState()
                GuestInviteScreen(s)
            }
        } finally {
            asOrganizer()
        }
    }
}
