package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Endereco(
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String = "São Paulo",
    val uf: String = "SP",
    val cep: String,
) {
    val resumo: String get() = "$logradouro, $numero · $bairro"
}

@Serializable
data class GeoPonto(val latitude: Double, val longitude: Double)

/** Planos da arena (§13.2) — define a comissao da plataforma (RN-07a). */
@Serializable
enum class Plano(val displayName: String, val comissaoPercentual: Int) {
    BASICO("Básico", 8),
    PRO("Pro", 4),
}

@Serializable
enum class StatusArena { EM_CADASTRO, ATIVA, SUSPENSA }

@Serializable
data class Arena(
    val id: String,
    val nome: String,
    val documento: String,
    val endereco: Endereco,
    val localizacao: GeoPonto,
    val fotos: List<String> = emptyList(),
    val comodidades: List<String> = emptyList(),
    val plano: Plano = Plano.BASICO,
    /** RN-10: duracao do hold configuravel entre 1h e 48h (padrao 4h). */
    val duracaoHoldHoras: Int = 4,
    /** RN-23: janela de cancelamento gratuito para o organizador (padrao 24h, 0..72h). */
    val janelaCancelamentoHoras: Int = 24,
    val subcontaGatewayId: String? = null,
    val status: StatusArena = StatusArena.ATIVA,
    val gestorId: String? = null,
    val telefone: String? = null,
    val descricao: String = "",
    val avaliacaoMedia: Double? = null,
    val totalAvaliacoes: Int = 0,
)

@Serializable
enum class TipoQuadra { SOCIETY_SINTETICO, QUADRA_POLIESPORTIVA, AREIA, PADEL, SAIBRO, PISO_RAPIDO }

/** Capacidade maxima de atletas da quadra para um esporte (<= RN-05). */
@Serializable
data class QuadraEsporte(val esporte: Sport, val capacidadeMax: Int = esporte.maxVagas)

@Serializable
data class Quadra(
    val id: String,
    val arenaId: String,
    val nome: String,
    val tipo: TipoQuadra,
    val piso: String,
    val coberta: Boolean,
    val esportes: List<QuadraEsporte>,
    /** Preco/hora fora do horario de pico. */
    val precoHora: Money,
    /** Preco/hora no pico (seg–sex 18h–23h, sab/dom 8h–20h). */
    val precoHoraPico: Money = precoHora,
    val ativa: Boolean = true,
) {
    fun suporta(esporte: Sport): Boolean = esportes.any { it.esporte == esporte }
    fun capacidadePara(esporte: Sport): Int? = esportes.firstOrNull { it.esporte == esporte }?.capacidadeMax
}
