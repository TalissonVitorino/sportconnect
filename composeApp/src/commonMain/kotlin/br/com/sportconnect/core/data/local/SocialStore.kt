package br.com.sportconnect.core.data.local

import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.ListaEspera
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

/** Presenca marcada na avaliacao pos-jogo (E15, US-130). So o app ve; ninguem recebe a marcacao individual. */
enum class Attendance { ATTENDED, NO_SHOW, LATE }

data class GameRating(val attendance: Map<String, Attendance>, val courtStars: Int, val comment: String)

/**
 * Fase 2 no app (sem servidor): lista de espera (E14, US-110, regras no [WaitlistManager]) e
 * avaliacoes enviadas (E15). Com o back-end, a fila e as avaliacoes passam a viver la.
 */
class SocialStore(private val settings: Settings? = null, private val manager: WaitlistManager = WaitlistManager()) {
    private val _queues = MutableStateFlow<Map<String, List<ListaEspera>>>(emptyMap())
    val queues: StateFlow<Map<String, List<ListaEspera>>> = _queues.asStateFlow()

    private val _rated = MutableStateFlow(settings?.getStringOrNull(KEY_RATED)?.split(',')?.filter { it.isNotEmpty() }?.toSet().orEmpty())
    val rated: StateFlow<Set<String>> = _rated.asStateFlow()

    private fun queue(gameId: String) = _queues.value[gameId].orEmpty()

    fun canJoin(jogo: Jogo, now: Instant): Boolean = manager.podeEntrar(jogo, queue(jogo.id), now)

    /** Posicao (1-based) da pessoa na fila ativa, ou null. */
    fun position(gameId: String, userId: String): Int? =
        manager.ativos(queue(gameId)).indexOfFirst { it.usuarioId == userId }.takeIf { it >= 0 }?.plus(1)

    fun join(jogo: Jogo, userId: String, userName: String, now: Instant): Result<Int> = runCatching {
        val next = manager.entrar(jogo, queue(jogo.id), userId, userName, now)
        _queues.update { it + (jogo.id to next) }
        position(jogo.id, userId) ?: error("fila")
    }

    fun leave(gameId: String, userId: String) {
        _queues.update { it + (gameId to manager.sair(queue(gameId), userId)) }
    }

    fun submitRating(gameId: String, @Suppress("UNUSED_PARAMETER") rating: GameRating) {
        _rated.update { it + gameId }
        settings?.putString(KEY_RATED, _rated.value.joinToString(","))
    }

    private companion object { const val KEY_RATED = "sportconnect.rated_games" }
}
