package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable

/**
 * Modalidades suportadas desde o MVP (multiesporte) — entidade `Esporte` do modelo de dominio (PRD §10).
 * [minVagas]/[maxVagas]/[vagasPadrao] seguem a RN-05; a arena pode reduzir o maximo por quadra, nunca ampliar.
 */
@Serializable
enum class Sport(
    val displayName: String,
    val minVagas: Int,
    val maxVagas: Int,
    val vagasPadrao: Int,
    val duracaoPadraoMinutos: Int,
) {
    FUTEBOL_SOCIETY("Futebol society", 10, 20, 14, 60),
    FUTSAL("Futsal", 8, 16, 12, 60),
    VOLEI("Vôlei", 8, 16, 12, 60),
    VOLEI_DE_PRAIA("Vôlei de praia", 4, 12, 4, 60),
    FUTEVOLEI("Futevôlei", 4, 8, 4, 60),
    BEACH_TENNIS("Beach tennis", 2, 8, 4, 60),
    PADEL("Padel", 2, 4, 4, 90),
    TENIS("Tênis", 2, 4, 2, 60),
    BASQUETE("Basquete", 6, 15, 10, 60),
    HANDEBOL("Handebol", 10, 18, 14, 60),
    ;

    /** RN-03/RN-05: N valido para a quadra considerando a capacidade definida pela arena. */
    fun aceitaVagas(n: Int, capacidadeQuadra: Int = maxVagas): Boolean =
        n >= minVagas && n <= minOf(maxVagas, capacidadeQuadra)
}

/** Nome do PRD para a mesma entidade. */
typealias Esporte = Sport
