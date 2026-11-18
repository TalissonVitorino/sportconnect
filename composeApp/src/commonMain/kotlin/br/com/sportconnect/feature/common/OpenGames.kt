package br.com.sportconnect.feature.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.designsystem.component.SCGameCard
import br.com.sportconnect.core.designsystem.component.SCGameCardVariant
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.GetOpenGamesUseCase
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.guest_spots_left
import br.com.sportconnect.resources.guest_your_share
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * T3 (doc 13, US-100/101): jogos com vaga aberta para avulsos — os de outras pessoas, no futuro e ainda
 * coletando cotas. Usado na Home (ate 3) e na aba "Jogos com vaga" da busca.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OpenGamesViewModel(
    getOpenGames: GetOpenGamesUseCase,
    auth: AuthRepository,
    private val clock: Clock,
) : ViewModel() {
    private val sport = MutableStateFlow<Sport?>(null)

    val games: StateFlow<List<Jogo>> = combine(sport.flatMapLatest { getOpenGames(it) }, auth.currentUser) { jogos, u ->
        val uid = u?.id
        jogos.filter { uid == null || (it.organizadorId != uid && !it.participa(uid)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSport(s: Sport?) { sport.value = s }

    fun now(): Instant = clock.now()
}

/** Card de jogo aberto: "4 vagas abertas de 12", valor da cota e sem barra de rateio. */
@Composable
fun OpenGameCard(jogo: Jogo, now: Instant, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SCGameCard(
        game = jogo.toCardUi(
            userId = null,
            statusLabel = stringResource(Res.string.guest_spots_left, jogo.vagasRestantes, jogo.numeroVagas),
            shareCaption = stringResource(Res.string.guest_your_share),
            countdownLabel = null,
            now = now,
        ),
        onClick = onClick,
        variant = SCGameCardVariant.Open,
        modifier = modifier.testTag("open_game_${jogo.id}"),
    )
}
