package br.com.sportconnect.core.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Presenca marcada na avaliacao pos-jogo (E15, US-130). So o app ve; ninguem recebe a marcacao individual. */
@Serializable
enum class Attendance { ATTENDED, NO_SHOW, LATE }

@Serializable
data class GameRating(val attendance: Map<String, Attendance>, val courtStars: Int, val comment: String)

/** Nota (1-5) e comentario sobre a quadra, por jogo avaliado. */
@Serializable
data class CourtRating(val stars: Int, val comment: String)

/**
 * Fase 2 no app (sem servidor): avaliacoes enviadas (E15). A lista de espera (E14, US-110) vive em
 * [GameRepository] via [JoinWaitlistUseCase]/[LeaveWaitlistUseCase]; com o back-end, as avaliacoes passam a viver la tambem.
 */
class SocialStore(private val settings: Settings? = null) {
    private val _rated = MutableStateFlow(settings?.getStringOrNull(KEY_RATED)?.split(',')?.filter { it.isNotEmpty() }?.toSet().orEmpty())
    val rated: StateFlow<Set<String>> = _rated.asStateFlow()

    /** T7 (13): presenca marcada por jogo (`attendance[gameId][userId]`), para fechar o ciclo do E15 no perfil. */
    private val _attendance = MutableStateFlow(
        settings?.getStringOrNull(KEY_ATTENDANCE)?.let { runCatching { Json.decodeFromString<Map<String, Map<String, Attendance>>>(it) }.getOrNull() }.orEmpty(),
    )
    val attendance: StateFlow<Map<String, Map<String, Attendance>>> = _attendance.asStateFlow()

    /** Nota e comentario da quadra por jogo avaliado; antes era descartado em [submitRating]. */
    private val _courtRatings = MutableStateFlow(
        settings?.getStringOrNull(KEY_COURT_RATINGS)?.let { runCatching { Json.decodeFromString<Map<String, CourtRating>>(it) }.getOrNull() }.orEmpty(),
    )
    val courtRatings: StateFlow<Map<String, CourtRating>> = _courtRatings.asStateFlow()

    /** Jogos em que [userId] foi marcado / quantos deles ele compareceu (ATTENDED ou LATE). Null = nunca avaliado. */
    fun attendanceOf(userId: String): Pair<Int, Int>? {
        val marks = _attendance.value.values.mapNotNull { it[userId] }
        if (marks.isEmpty()) return null
        return marks.count { it != Attendance.NO_SHOW } to marks.size
    }

    fun submitRating(gameId: String, rating: GameRating) {
        _rated.update { it + gameId }
        settings?.putString(KEY_RATED, _rated.value.joinToString(","))
        _attendance.update { it + (gameId to rating.attendance) }
        settings?.putString(KEY_ATTENDANCE, Json.encodeToString(_attendance.value))
        _courtRatings.update { it + (gameId to CourtRating(rating.courtStars, rating.comment)) }
        settings?.putString(KEY_COURT_RATINGS, Json.encodeToString(_courtRatings.value))
    }

    private companion object {
        const val KEY_RATED = "sportconnect.rated_games"
        const val KEY_ATTENDANCE = "sportconnect.attendance"
        const val KEY_COURT_RATINGS = "sportconnect.court_ratings"
    }
}
