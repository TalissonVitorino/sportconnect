package br.com.sportconnect.core.designsystem.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_map_zoom_in
import br.com.sportconnect.resources.cd_map_zoom_out
import br.com.sportconnect.resources.map_attribution
import br.com.sportconnect.resources.map_unavailable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Pino do mapa: [label] curto (ex.: "R$ 160"), [description] lido pelo leitor de tela. */
data class SCMapPin(val id: String, val latitude: Double, val longitude: Double, val label: String, val description: String)

/** Camera do mapa: centro no mundo normalizado e zoom continuo. Sobrevive a rotacao. */
@Stable
class SCMapCamera(center: WorldPoint, zoom: Double) {
    var center by mutableStateOf(center)
    var zoom by mutableStateOf(zoom)

    fun panBy(dxPx: Float, dyPx: Float, density: Float) {
        val world = MapMath.worldSizePx(zoom, density)
        center = WorldPoint((center.x - dxPx / world).mod(1.0), (center.y - dyPx / world).coerceIn(0.0, 1.0))
    }

    /** Zoom mantendo fixo o ponto sob [focus] (pinca, roda do mouse, duplo toque). */
    fun zoomBy(factor: Float, focus: Offset, size: IntSize, density: Float) {
        val old = zoom
        val next = (old + kotlin.math.log2(factor.toDouble())).coerceIn(MapMath.MIN_ZOOM, MapMath.MAX_ZOOM)
        if (next == old) return
        val dx = focus.x - size.width / 2f
        val dy = focus.y - size.height / 2f
        val wOld = MapMath.worldSizePx(old, density)
        val wNew = MapMath.worldSizePx(next, density)
        val fx = center.x + dx / wOld
        val fy = center.y + dy / wOld
        center = WorldPoint(fx - dx / wNew, (fy - dy / wNew).coerceIn(0.0, 1.0))
        zoom = next
    }

    fun moveTo(lat: Double, lon: Double, zoom: Double = this.zoom) {
        center = MapMath.project(lat, lon)
        this.zoom = zoom.coerceIn(MapMath.MIN_ZOOM, MapMath.MAX_ZOOM)
    }

    companion object {
        fun saver() = androidx.compose.runtime.saveable.Saver<SCMapCamera, List<Double>>(
            save = { listOf(it.center.x, it.center.y, it.zoom) },
            restore = { SCMapCamera(WorldPoint(it[0], it[1]), it[2]) },
        )
    }
}

/** Camera inicial: Sao Paulo; [SCMap] enquadra os pinos na primeira medida se [fitPinsOnStart]. */
@Composable
fun rememberSCMapCamera(lat: Double = -23.5505, lon: Double = -46.6333, zoom: Double = 12.0): SCMapCamera =
    rememberSaveable(saver = SCMapCamera.saver()) { SCMapCamera(MapMath.project(lat, lon), zoom) }

/**
 * M1 (doc 13): mapa do design system, igual em Android, iOS, desktop e web. Tiles raster de
 * [MapTileCache]; arrastar, pinca, duplo toque e roda do mouse; botoes +/− (48 dp) e atribuicao visivel.
 */
@Composable
fun SCMap(
    camera: SCMapCamera,
    tiles: MapTileCache,
    pins: List<SCMapPin>,
    modifier: Modifier = Modifier,
    selectedPinId: String? = null,
    onPinClick: (SCMapPin) -> Unit = {},
    onMapClick: () -> Unit = {},
    fitPinsOnStart: Boolean = true,
    /** false = previa estatica (dentro de paginas com rolagem): sem gestos nem +/−; toque chama [onMapClick]. */
    interactive: Boolean = true,
    /** Zoom maximo ao enquadrar os pinos (um pino so: aproxima ate aqui). */
    fitMaxZoom: Double = 15.0,
) {
    val c = SportConnectTheme.colors
    val density = LocalDensity.current.density
    var size by remember { mutableStateOf(IntSize.Zero) }
    var fitted by rememberSaveable { mutableStateOf(!fitPinsOnStart) }
    LaunchedEffect(size, pins.isNotEmpty()) {
        if (!fitted && size.width > 0 && pins.isNotEmpty()) {
            val (center, zoom) = MapMath.fit(pins.map { MapMath.project(it.latitude, it.longitude) }, size.width, size.height, density, 56f * density, fitMaxZoom)
            camera.center = center
            camera.zoom = zoom
            fitted = true
        }
    }
    val visible by remember(size, density) {
        derivedStateOf { MapMath.visibleTiles(camera.center, camera.zoom, density, size.width, size.height) }
    }
    LaunchedEffect(visible) { coroutineScope { visible.forEach { k -> launch { tiles.load(k) } } } }
    val unavailable = visible.isNotEmpty() && tiles.failedCount(visible) == visible.size

    Box(
        modifier.clipToBounds().background(c.surfaceContainer).onSizeChanged { size = it }.testTag("sc_map")
            .then(if (interactive) Modifier else Modifier.clickable(onClick = onMapClick)),
    ) {
        Canvas(
            if (!interactive) Modifier.fillMaxSize() else Modifier.fillMaxSize()
                .pointerInput(density) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (zoom != 1f) camera.zoomBy(zoom, centroid, IntSize(this.size.width, this.size.height), density)
                        camera.panBy(pan.x, pan.y, density)
                    }
                }
                .pointerInput(density) {
                    detectTapGestures(
                        onDoubleTap = { camera.zoomBy(2f, it, IntSize(this.size.width, this.size.height), density) },
                        onTap = { onMapClick() },
                    )
                }
                .pointerInput(density) {
                    awaitPointerEventScope {
                        while (true) {
                            val e = awaitPointerEvent()
                            if (e.type == PointerEventType.Scroll) {
                                val ch = e.changes.first()
                                val f = if (ch.scrollDelta.y < 0) 1.25f else 0.8f
                                camera.zoomBy(f, ch.position, IntSize(this.size.width, this.size.height), density)
                                e.changes.forEach { it.consume() }
                            }
                        }
                    }
                },
        ) {
            val world = MapMath.worldSizePx(camera.zoom, density)
            val z = MapMath.tileZoom(camera.zoom, density)
            val n = 1 shl z
            val tilePx = world / n
            val left = camera.center.x * world - this.size.width / 2.0
            val top = camera.center.y * world - this.size.height / 2.0
            val x0 = kotlin.math.floor(left / tilePx).toInt()
            visible.forEach { k ->
                // Recupera a coluna "desenrolada" (o mundo repete na horizontal).
                var tx = k.x
                while (tx < x0) tx += n
                val dx = (tx * tilePx - left).roundToInt()
                val dy = (k.y * tilePx - top).roundToInt()
                val sizePx = kotlin.math.ceil(tilePx).toInt() + 1
                val bmp = tiles.tiles[k]
                if (bmp != null) {
                    drawImage(bmp, dstOffset = IntOffset(dx, dy), dstSize = IntSize(sizePx, sizePx))
                } else {
                    drawRect(c.surfaceContainerHigh, topLeft = Offset(dx.toFloat(), dy.toFloat()), size = androidx.compose.ui.geometry.Size(sizePx - 2f, sizePx - 2f))
                }
            }
        }
        // Pinos (selecionado por ultimo, por cima).
        val world = MapMath.worldSizePx(camera.zoom, density)
        pins.sortedBy { it.id == selectedPinId }.forEach { pin ->
            val p = MapMath.project(pin.latitude, pin.longitude)
            var px = (p.x - camera.center.x) * world + size.width / 2.0
            // O pino mais perto do centro entre as copias do mundo.
            if (px < -world / 2) px += world
            if (px > size.width + world / 2) px -= world
            val py = (p.y - camera.center.y) * world + size.height / 2.0
            if (px < -200 || py < -100 || px > size.width + 200 || py > size.height + 200) return@forEach
            MapPinChip(pin, pin.id == selectedPinId, { onPinClick(pin) }, Modifier.layout { m, cons ->
                val pl = m.measure(cons.copy(minWidth = 0, minHeight = 0))
                layout(cons.maxWidth, cons.maxHeight) { pl.place((px - pl.width / 2.0).roundToInt(), (py - pl.height).roundToInt()) }
            })
        }
        if (interactive) Column(Modifier.align(Alignment.TopEnd).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ZoomButton(Icons.Rounded.Add, stringResource(Res.string.cd_map_zoom_in), "map_zoom_in") {
                camera.zoomBy(2f, Offset(size.width / 2f, size.height / 2f), size, density)
            }
            ZoomButton(Icons.Rounded.Remove, stringResource(Res.string.cd_map_zoom_out), "map_zoom_out") {
                camera.zoomBy(0.5f, Offset(size.width / 2f, size.height / 2f), size, density)
            }
        }
        if (unavailable) {
            Text(
                stringResource(Res.string.map_unavailable), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center).background(c.surface, MaterialTheme.shapes.medium).padding(12.dp),
            )
        }
        Text(
            stringResource(Res.string.map_attribution), style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomEnd).background(c.surface.copy(alpha = 0.85f)).padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ZoomButton(icon: androidx.compose.ui.graphics.vector.ImageVector, cd: String, tag: String, onClick: () -> Unit) {
    val c = SportConnectTheme.colors
    Surface(
        onClick = onClick, shape = CircleShape, color = c.surface, contentColor = c.onSurface, shadowElevation = 3.dp,
        modifier = Modifier.size(48.dp).semantics { contentDescription = cd }.testTag(tag),
    ) { Box(contentAlignment = Alignment.Center) { Icon(icon, null) } }
}

@Composable
private fun MapPinChip(pin: SCMapPin, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = SportConnectTheme.colors
    val bg = if (selected) c.primary else c.surface
    // Chip com o preco e, logo abaixo, a ponta apontando para o local exato.
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            color = bg,
            contentColor = if (selected) c.onPrimary else c.onSurface,
            shadowElevation = if (selected) 6.dp else 2.dp,
            border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant),
            modifier = Modifier.heightIn(min = 32.dp)
                .semantics { contentDescription = pin.description; role = Role.Button; this.selected = selected }
                .testTag("map_pin_${pin.id}"),
        ) {
            Text(
                pin.label, style = if (selected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = if (selected) 12.dp else 8.dp, vertical = 6.dp),
            )
        }
        Box(Modifier.size(width = 2.dp, height = 6.dp).background(if (selected) c.primary else c.outline))
        Box(Modifier.size(8.dp).shadow(1.dp, CircleShape).background(bg, CircleShape).border(2.dp, if (selected) c.primary else c.outline, CircleShape))
    }
}
