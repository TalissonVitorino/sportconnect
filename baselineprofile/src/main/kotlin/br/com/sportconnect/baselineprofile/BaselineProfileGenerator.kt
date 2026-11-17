package br.com.sportconnect.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Jornada critica do app para o Baseline Profile (A3): abertura, Home, detalhe do jogo e as abas.
 * Entra como o organizador da demo (celular do DemoData + OTP 123456) para as telas terem conteudo.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        device.passOnboardingAndLogin()

        // Home -> detalhe do jogo (transicao compartilhada do titulo) -> volta.
        device.wait(Until.hasObject(By.textStartsWith("E aí")), TIMEOUT)
        device.findObject(By.text("Racha dos amigos"))?.let {
            it.click()
            device.wait(Until.hasObject(By.text("Quem está no jogo")), TIMEOUT)
            device.findObject(By.scrollable(true))?.fling(Direction.DOWN)
            device.waitForIdle()
            device.pressBack()
        }

        // Abas: Buscar (lista com imagens) -> Jogos -> Perfil -> Inicio.
        device.tab("Buscar")
        device.wait(Until.hasObject(By.textContains("livres")), TIMEOUT)
        device.findObject(By.scrollable(true))?.let { list ->
            list.fling(Direction.DOWN)
            device.waitForIdle()
            list.fling(Direction.UP)
        }
        device.tab("Jogos")
        device.tab("Perfil")
        device.tab("Início")
    }

    private fun UiDevice.passOnboardingAndLogin() {
        if (wait(Until.hasObject(By.text("Pular")), SHORT)) {
            findObject(By.text("Pular")).click()
            wait(Until.hasObject(By.text("Começar")), SHORT)
            findObject(By.text("Começar"))?.click()
        }
        // Visitante (instalacao com dados de antes, sem sessao): entra pelo Perfil.
        if (!wait(Until.hasObject(By.text("Entre com seu celular")), SHORT) && !hasObject(By.text("Racha dos amigos"))) {
            findObject(By.text("Perfil"))?.click()
            wait(Until.hasObject(By.text("Entrar com celular")), SHORT)
            findObject(By.text("Entrar com celular"))?.click()
        }
        if (wait(Until.hasObject(By.text("Entre com seu celular")), SHORT)) {
            findObject(By.clazz("android.widget.EditText")).text = "11987654321"
            findObject(By.text("Receber código")).click()
            wait(Until.hasObject(By.text("Digite o código")), TIMEOUT)
            findObject(By.clazz("android.widget.EditText")).text = "123456"
            wait(Until.hasObject(By.text("Início")), TIMEOUT)
            findObject(By.text("Início"))?.click()
        }
    }

    private fun UiDevice.tab(label: String) {
        findObject(By.text(label))?.click()
        waitForIdle()
    }

    private companion object {
        const val PACKAGE = "br.com.sportconnect.app"
        const val TIMEOUT = 5_000L
        const val SHORT = 2_500L
    }
}
