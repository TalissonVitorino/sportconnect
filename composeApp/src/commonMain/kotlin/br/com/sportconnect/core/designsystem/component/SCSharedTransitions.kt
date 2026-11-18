@file:OptIn(ExperimentalSharedTransitionApi::class)

package br.com.sportconnect.core.designsystem.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme

/** Escopo do `SharedTransitionLayout` em volta do NavHost (A9 do backlog 11). */
val LocalSCSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** Escopo de animacao do destino atual do NavHost (o `this` de `composable<...> { }`). */
val LocalSCNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Chave do titulo do jogo: o mesmo texto no card (Home/Meus jogos) e no cabecalho do detalhe. */
fun sharedGameTitleKey(gameId: String) = "game-title-$gameId"

/**
 * Elemento compartilhado entre telas: o mesmo conteudo "viaja" de uma tela para a outra.
 * Sem escopo (previews, testes, telas fora do NavHost) ou com "reduzir movimento", nao faz nada.
 */
@Composable
fun Modifier.scSharedBounds(key: String?): Modifier {
    val shared = LocalSCSharedTransitionScope.current
    val animated = LocalSCNavAnimatedScope.current
    if (key == null || shared == null || animated == null || SportConnectTheme.reducedMotion) return this
    return with(shared) {
        this@scSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animated,
            enter = fadeIn(tween(SCMotion.Medium1)),
            exit = fadeOut(tween(SCMotion.Short4)),
            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
        )
    }
}
