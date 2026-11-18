package br.com.sportconnect.core.data.local

import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.ListaEspera
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/** Presenca marcada na avaliacao pos-jogo (E15, US-130). So o app ve; ninguem recebe a marcacao individual. */
@Serializable
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

    /** T7 (13): presenca marcada por jogo (`attendance[gameId][userId]`), para fechar o ciclo do E15 no perfil. */
    private val _attendance = MutableStateFlow(
        settings?.getStringOrNull(KEY_ATTENDANCE)?.let { runCatching { Json.decodeFromString<Map<String, Map<String, Attendance>>>(it) }.getOrNull() }.orEmpty(),
    )
    val attendance: StateFlow<Map<String, Map<String, Attendance>>> = _attendance.asStateFlow()

    /** Jogos em que [userId] foi marcado / quantos deles ele compareceu (ATTENDED ou LATE). Null = nunca avaliado. */
    fun attendanceOf(userId: String): Pair<Int, Int>? {
        val marks = _attendance.value.values.mapNotNull { it[userId] }
        if (marks.isEmpty()) return null
        return marks.count { it != Attendance.NO_SHOW } to marks.size
    }

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

    fun submitRating(gameId: String, rating: GameRating) {
        _rated.update { it + gameId }
        settings?.putString(KEY_RATED, _rated.value.joinToString(","))
        _attendance.update { it + (gameId to rating.attendance) }
        settings?.putString(KEY_ATTENDANCE, Json.encodeToString(_attendance.value))
    }

    private companion object {
        const val KEY_RATED = "sportconnect.rated_games"
        const val KEY_ATTENDANCE = "sportconnect.attendance"
    }
}
