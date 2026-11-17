package br.com.sportconnect.core.designsystem.map

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sinh
import kotlin.math.tan

/** Ponto no "mundo" Web Mercator normalizado: x e y de 0 a 1 (0,0 = noroeste). */
data class WorldPoint(val x: Double, val y: Double)

/** Tile raster no padrao z/x/y (OpenStreetMap, Google Map Tiles, MapTiler...). */
data class TileKey(val z: Int, val x: Int, val y: Int)

/** M1 (doc 13): projecao e enquadramento do [SCMap]. Funcoes puras (testadas em MapMathTest). */
object MapMath {
    const val TILE_SIZE = 256.0
    const val MIN_ZOOM = 3.0
    const val MAX_ZOOM = 18.0
    private const val MAX_LAT = 85.05112878

    fun project(lat: Double, lon: Double): WorldPoint {
        val la = lat.coerceIn(-MAX_LAT, MAX_LAT) * PI / 180.0
        val x = (lon + 180.0) / 360.0
        val y = (1.0 - ln(tan(la) + 1.0 / cos(la)) / PI) / 2.0
        return WorldPoint(x, y)
    }

    /** Inverso de [project]: (latitude, longitude). */
    fun unproject(p: WorldPoint): Pair<Double, Double> {
        val lon = p.x * 360.0 - 180.0
        val lat = atan(sinh(PI * (1.0 - 2.0 * p.y))) * 180.0 / PI
        return lat to lon
    }

    /** Tamanho do mundo em px no [zoom] (tiles de 256 "dp" multiplicados pela densidade). */
    fun worldSizePx(zoom: Double, density: Float): Double = TILE_SIZE * density * pow2(zoom)

    /**
     * Zoom dos tiles a baixar, ate 19. Em telas densas usa tiles um pouco mais detalhados, mas sem chegar
     * ao maximo: com tiles de 256 px, isso deixaria os nomes de ruas pequenos demais para ler.
     */
    fun tileZoom(zoom: Double, density: Float): Int =
        floor(zoom + max(0.0, log2(density.toDouble().coerceAtLeast(1.0)) - 0.6)).toInt().coerceIn(0, 19)

    /** Tiles visiveis numa janela [widthPx] x [heightPx] centrada em [center]. */
    fun visibleTiles(center: WorldPoint, zoom: Double, density: Float, widthPx: Int, heightPx: Int): List<TileKey> {
        if (widthPx <= 0 || heightPx <= 0) return emptyList()
        val world = worldSizePx(zoom, density)
        val z = tileZoom(zoom, density)
        val n = 1 shl z
        val tilePx = world / n
        val left = center.x * world - widthPx / 2.0
        val top = center.y * world - heightPx / 2.0
        val x0 = floor(left / tilePx).toInt()
        val x1 = floor((left + widthPx) / tilePx).toInt()
        val y0 = max(0, floor(top / tilePx).toInt())
        val y1 = min(n - 1, floor((top + heightPx) / tilePx).toInt())
        return buildList {
            for (ty in y0..y1) for (tx in x0..x1) add(TileKey(z, ((tx % n) + n) % n, ty))
        }
    }

    /** Centro e zoom que mostram todos os [points] com [paddingPx] de folga. */
    fun fit(points: List<WorldPoint>, widthPx: Int, heightPx: Int, density: Float, paddingPx: Float, maxZoom: Double = 15.0): Pair<WorldPoint, Double> {
        if (points.isEmpty()) return project(-23.5505, -46.6333) to 11.0
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val center = WorldPoint((minX + maxX) / 2, (minY + maxY) / 2)
        val dx = max(maxX - minX, 1e-9)
        val dy = max(maxY - minY, 1e-9)
        val w = (widthPx - 2 * paddingPx).coerceAtLeast(1f)
        val h = (heightPx - 2 * paddingPx).coerceAtLeast(1f)
        val zx = log2(w / (dx * TILE_SIZE * density))
        val zy = log2(h / (dy * TILE_SIZE * density))
        return center to min(min(zx, zy), maxZoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
    }

    /** Endereco do tile a partir do modelo "{z}/{x}/{y}". */
    fun tileUrl(template: String, key: TileKey): String =
        template.replace("{z}", key.z.toString()).replace("{x}", key.x.toString()).replace("{y}", key.y.toString())

    fun pow2(z: Double): Double = kotlin.math.exp(z * ln(2.0))
}
