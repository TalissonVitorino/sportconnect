package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import br.com.sportconnect.core.model.GeoPonto

/** Resultado do pedido de localizacao (B10, US-003: finalidade explicita, so para buscar arenas). */
sealed interface LocationResult {
    data class Found(val point: GeoPonto) : LocationResult
    /** A pessoa negou (ou o sistema bloqueou): mostrar `error_location_denied_*` e a cidade digitada. */
    data object Denied : LocationResult
    /** Permitiu, mas o aparelho nao conseguiu a posicao (GPS desligado, sem sinal). */
    data object Unavailable : LocationResult
}

/** true quando a plataforma consegue pedir a localizacao (Android e iOS). Desktop/Web: so cidade digitada. */
expect val deviceLocationSupported: Boolean

/**
 * Devolve uma funcao que pede a permissao (se preciso) e entrega a posicao aproximada em [onResult].
 * A precisao pedida e "aproximada" (~1 km): basta para ordenar arenas por distancia.
 */
@Composable
expect fun rememberLocationRequest(onResult: (LocationResult) -> Unit): () -> Unit

/** Centros das cidades atendidas, para quem prefere digitar a cidade (`onboarding_location_manual`). */
object KnownCities {
    val all: Map<String, GeoPonto> = mapOf(
        "São Paulo" to GeoPonto(-23.5505, -46.6333),
        "Rio de Janeiro" to GeoPonto(-22.9068, -43.1729),
        "Belo Horizonte" to GeoPonto(-19.9167, -43.9345),
        "Curitiba" to GeoPonto(-25.4284, -49.2733),
        "Porto Alegre" to GeoPonto(-30.0346, -51.2177),
        "Brasília" to GeoPonto(-15.7939, -47.8828),
        "Campinas" to GeoPonto(-22.9099, -47.0626),
        "Santos" to GeoPonto(-23.9608, -46.3336),
    )

    /** Busca sem acento e sem diferenciar maiusculas ("sao paulo" acha "São Paulo"). */
    fun match(query: String): List<String> {
        val q = query.normalize()
        if (q.isBlank()) return emptyList()
        return all.keys.filter { it.normalize().contains(q) }
    }

    private fun String.normalize(): String = lowercase().trim()
        .replace('á', 'a').replace('â', 'a').replace('ã', 'a').replace('é', 'e').replace('ê', 'e')
        .replace('í', 'i').replace('ó', 'o').replace('ô', 'o').replace('õ', 'o').replace('ú', 'u').replace('ç', 'c')
}
