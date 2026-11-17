package br.com.sportconnect.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute

/**
 * W1 (doc 13): URLs da versao web. A barra de endereco mostra a rota ("#/g/jogo-racha") e um link aberto
 * direto no navegador ("/j/RACHA8" ou "#/j/RACHA8") vira o mesmo link publico que o Android e o iOS tratam.
 */
object WebLinks {
    private const val BASE = "https://sportconnect.app"
    private val LINK = Regex("""^/?(j|g|a)/([A-Za-z0-9_-]+)/?$""")

    /** Fragmento da URL para a entrada atual da pilha ("" = nao muda a URL). */
    fun fragmentFor(entry: NavBackStackEntry): String {
        val d = entry.destination
        return when {
            d.hasRoute<Route.Home>() -> "#/"
            d.hasRoute<Route.Explore>() -> "#/buscar"
            d.hasRoute<Route.MyGames>() -> "#/jogos"
            d.hasRoute<Route.Profile>() -> "#/perfil"
            d.hasRoute<Route.Notifications>() -> "#/avisos"
            d.hasRoute<Route.GameDetail>() -> "#/g/" + entry.toRoute<Route.GameDetail>().id
            d.hasRoute<Route.Invite>() -> "#/j/" + entry.toRoute<Route.Invite>().code
            d.hasRoute<Route.ArenaDetail>() -> "#/a/" + entry.toRoute<Route.ArenaDetail>().id
            else -> ""
        }
    }

    /** Link publico a abrir a partir do caminho e do fragmento da URL da pagina, ou null (abre o inicio). */
    fun linkFromLocation(path: String, hash: String): String? {
        val candidates = listOf(hash.removePrefix("#"), path)
        for (c in candidates) {
            val m = LINK.find(c.trim()) ?: continue
            return "$BASE/${m.groupValues[1]}/${m.groupValues[2]}"
        }
        return null
    }
}
