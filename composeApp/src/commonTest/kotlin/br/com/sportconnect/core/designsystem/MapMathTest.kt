package br.com.sportconnect.core.designsystem

import br.com.sportconnect.core.designsystem.map.MapMath
import br.com.sportconnect.core.designsystem.map.SCMapCamera
import br.com.sportconnect.core.designsystem.map.TileKey
import br.com.sportconnect.core.designsystem.map.WorldPoint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** M1: projecao, tiles visiveis, enquadramento e zoom do SCMap. */
class MapMathTest {
    @Test
    fun projecao_ida_e_volta_preserva_o_ponto() {
        val (lat, lon) = MapMath.unproject(MapMath.project(-23.5613, -46.6914))
        assertTrue(abs(lat + 23.5613) < 1e-9 && abs(lon + 46.6914) < 1e-9)
        assertEquals(WorldPoint(0.5, 0.5), MapMath.project(0.0, 0.0))
    }

    @Test
    fun tile_de_sao_paulo_no_zoom_12_e_o_do_padrao_osm() {
        val p = MapMath.project(-23.5505, -46.6333)
        val tiles = MapMath.visibleTiles(p, 12.0, 1f, 1, 1)
        assertEquals(listOf(TileKey(12, 1517, 2323)), tiles)
        assertEquals("https://t/12/1517/2323.png", MapMath.tileUrl("https://t/{z}/{x}/{y}.png", tiles.first()))
    }

    @Test
    fun tiles_visiveis_cobrem_a_janela_e_densidade_alta_usa_tiles_mais_nitidos() {
        val c = MapMath.project(-23.55, -46.63)
        val t1 = MapMath.visibleTiles(c, 13.0, 1f, 1024, 768)
        assertTrue(t1.size in 12..25)
        assertTrue(MapMath.visibleTiles(c, 13.0, 4f, 1024, 768).all { it.z == 14 })
        assertTrue(MapMath.visibleTiles(c, 13.0, 2.6f, 1024, 768).all { it.z == 13 })
    }

    @Test
    fun enquadrar_pontos_mostra_todos_com_folga() {
        val pts = listOf(MapMath.project(-23.56, -46.69), MapMath.project(-23.60, -46.66))
        val (center, zoom) = MapMath.fit(pts, 400, 600, 1f, 40f)
        val world = MapMath.worldSizePx(zoom, 1f)
        pts.forEach { p ->
            val x = (p.x - center.x) * world + 200
            val y = (p.y - center.y) * world + 300
            assertTrue(x in 39.0..361.0 && y in 39.0..561.0, "ponto fora: $x,$y")
        }
    }

    @Test
    fun zoom_mantem_o_ponto_sob_o_dedo() {
        val cam = SCMapCamera(MapMath.project(-23.55, -46.63), 12.0)
        val size = IntSize(400, 800)
        val focus = Offset(300f, 200f)
        fun under(): WorldPoint {
            val w = MapMath.worldSizePx(cam.zoom, 2f)
            return WorldPoint(cam.center.x + (focus.x - 200) / w, cam.center.y + (focus.y - 400) / w)
        }
        val before = under()
        cam.zoomBy(2f, focus, size, 2f)
        assertEquals(13.0, cam.zoom, 1e-9)
        val after = under()
        assertTrue(abs(before.x - after.x) < 1e-12 && abs(before.y - after.y) < 1e-12)
    }
}
