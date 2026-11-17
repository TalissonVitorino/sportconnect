package br.com.sportconnect.core.designsystem.map

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Tiles do mapa em memoria (LRU de [MAX] imagens) com ate 6 downloads simultaneos. Um so por app (Koin),
 * para que voltar a busca nao baixe tudo de novo.
 */
class MapTileCache(private val urlTemplate: String, private val client: HttpClient = HttpClient()) {
    /** Estado observavel: o mapa recompoe quando um tile chega. */
    val tiles = mutableStateMapOf<TileKey, ImageBitmap>()
    private val order = ArrayDeque<TileKey>()
    private val inFlight = mutableSetOf<TileKey>()
    private val failed = mutableMapOf<TileKey, Int>()
    private val gate = Semaphore(6)
    private var loggedErrors = 0

    /** Quantos dos [keys] falharam (para o aviso de "mapa indisponivel"). */
    fun failedCount(keys: Collection<TileKey>): Int = keys.count { (failed[it] ?: 0) > 0 && it !in tiles }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(key: TileKey) {
        if (key in tiles || key in inFlight || (failed[key] ?: 0) >= 3) return
        inFlight += key
        try {
            gate.withPermit {
                val resp = client.get(MapMath.tileUrl(urlTemplate, key)) {
                    // Politica de uso do OpenStreetMap: identificar o app (o navegador ignora e usa o dele).
                    header("User-Agent", "SportConnect/1.0 (app; sportconnect.app)")
                }
                check(resp.status.isSuccess()) { "HTTP ${resp.status.value}" }
                val bmp = resp.readRawBytes().decodeToImageBitmap()
                tiles[key] = bmp
                failed.remove(key)
                order.remove(key)
                order.addLast(key)
                while (order.size > MAX) tiles.remove(order.removeFirst())
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Throwable) {
            if ((failed[key] ?: 0) == 0 && loggedErrors++ < 3) println("SCMap: tile $key falhou: $e")
            failed[key] = (failed[key] ?: 0) + 1
        } finally {
            inFlight -= key
        }
    }

    /** Ao voltar a conexao: tenta de novo os que falharam. */
    fun resetFailures() = failed.clear()

    private companion object { const val MAX = 300 }
}
