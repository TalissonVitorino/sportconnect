package br.com.sportconnect

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import br.com.sportconnect.navigation.ExternalUriHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash do sistema (tema Theme.SportConnect.Starting). A sessão e o onboarding são lidos de forma
        // síncrona das preferências, então não é preciso segurar a splash na tela.
        val splash = installSplashScreen()
        splash.setOnExitAnimationListener { provider ->
            // Saída curta: o símbolo encolhe e a splash some, revelando a primeira tela.
            provider.iconView.animate().scaleX(0.6f).scaleY(0.6f).setDuration(SPLASH_EXIT_MS).start()
            provider.view.animate().alpha(0f).setDuration(SPLASH_EXIT_MS)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { provider.remove() }
                .start()
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    /** Link aberto com o app ja rodando (singleTask). Na abertura a frio o NavController le o intent sozinho. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.toString()?.let(ExternalUriHandler::onNewUri)
    }

    private companion object {
        const val SPLASH_EXIT_MS = 220L
    }
}
