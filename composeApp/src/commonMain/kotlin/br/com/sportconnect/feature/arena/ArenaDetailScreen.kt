@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package br.com.sportconnect.feature.arena

import br.com.sportconnect.core.platform.rememberNativeShare
import br.com.sportconnect.resources.cd_directions
import br.com.sportconnect.core.designsystem.map.rememberSCMapCamera
import br.com.sportconnect.core.designsystem.map.SCMapPin
import br.com.sportconnect.core.designsystem.map.SCMap
import br.com.sportconnect.core.designsystem.map.MapTileCache
import br.com.sportconnect.resources.arena_detail_amenity_lighting
import br.com.sportconnect.resources.arena_detail_amenity_bar
import br.com.sportconnect.resources.arena_detail_amenity_locker
import br.com.sportconnect.resources.arena_detail_amenity_parking
import br.com.sportconnect.resources.cd_arena_photo
import org.jetbrains.compose.resources.StringResource
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.OutdoorGrill
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import br.com.sportconnect.core.data.local.ArenaPhotoStore
import br.com.sportconnect.core.designsystem.component.scAlignTextButtonStart
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCourtPlaceholder
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCSportChipVariant
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.feature.common.LoadErrorState
import br.com.sportconnect.feature.common.SectionHeader
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.mapsUrl
import br.com.sportconnect.feature.common.rememberShareActions
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.feature.slots.SlotPickerRoute
import br.com.sportconnect.navigation.Route
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.arena_detail_amenities
import br.com.sportconnect.resources.arena_detail_call
import br.com.sportconnect.resources.arena_detail_cancellation_policy
import br.com.sportconnect.resources.arena_detail_court_surface
import br.com.sportconnect.resources.arena_detail_courts
import br.com.sportconnect.resources.arena_detail_no_slots_today
import br.com.sportconnect.resources.arena_detail_open_maps
import br.com.sportconnect.resources.arena_detail_price_range
import br.com.sportconnect.resources.arena_detail_rules
import br.com.sportconnect.resources.arena_detail_see_times
import br.com.sportconnect.resources.arena_detail_see_tomorrow
import br.com.sportconnect.resources.arena_detail_unpublished_cta
import br.com.sportconnect.resources.arena_detail_unpublished_title
import br.com.sportconnect.resources.cd_arena_share
import br.com.sportconnect.resources.cd_back
import br.com.sportconnect.resources.common_see_all
import br.com.sportconnect.resources.error_server_body
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

data class ArenaDetailActions(
    val back: () -> Unit = {},
    val openSlots: (courtId: String, date: String?) -> Unit = { _, _ -> },
    val createGame: (slotId: String) -> Unit = {},
)

@Composable
fun ArenaDetailRoute(route: Route.ArenaDetail, actions: ArenaDetailActions) {
    val vm: ArenaDetailViewModel = koinViewModel(key = "arena-${route.id}") { parametersOf(route) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    ArenaDetailScreen(state, actions, onSport = vm::selectSport)
}

/** Detalhe da arena (10 §2.5). Expanded: informacoes a esquerda e grade de horarios embutida a direita. */
@Composable
fun ArenaDetailScreen(state: ArenaDetailUiState, actions: ArenaDetailActions = ArenaDetailActions(), onSport: (Sport?) -> Unit = {}) {
    val c = SportConnectTheme.colors
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        val content = state as? ArenaDetailUiState.Content
        val firstCourt = content?.let { s -> s.detail.quadras.firstOrNull { s.sport == null || it.suporta(s.sport) } }
        Scaffold(
            containerColor = c.surface,
            bottomBar = {
                // C8: com varias quadras o CTA fixo era ambiguo ("Ver horarios" de qual?) e cada card ja tem horarios e
                // "Ver todos". Fica so quando ha uma quadra.
                val singleCourt = content?.detail?.quadras?.count { content.sport == null || it.suporta(content.sport) } == 1
                if (content != null && firstCourt != null && singleCourt && wc != WidthClass.Expanded) {
                    BottomActionBar {
                        SCButton(
                            stringResource(Res.string.arena_detail_see_times),
                            { actions.openSlots(firstCourt.id, null) },
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            modifier = Modifier.testTag("arena_see_times"),
                        )
                    }
                }
            },
        ) { padding ->
            when (state) {
                ArenaDetailUiState.Loading -> Column(Modifier.padding(padding)) {
                    SCSkeleton(SCSkeletonShape.Box(height = 220.dp, radius = 0.dp))
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SCSkeleton(SCSkeletonShape.Line(0.7f, 28.dp))
                        SCSkeleton(SCSkeletonShape.Box(height = 140.dp))
                        SCSkeleton(SCSkeletonShape.Box(height = 140.dp))
                    }
                }
                ArenaDetailUiState.NotFound -> LoadErrorState(actions.back, Modifier.padding(padding))
                ArenaDetailUiState.Unpublished -> SCEmptyState(
                    title = stringResource(Res.string.arena_detail_unpublished_title),
                    description = stringResource(Res.string.error_server_body),
                    primaryAction = SCAction(stringResource(Res.string.arena_detail_unpublished_cta), actions.back),
                    modifier = Modifier.padding(padding),
                )
                is ArenaDetailUiState.Content -> if (wc == WidthClass.Expanded && firstCourt != null) {
                    Row(Modifier.fillMaxSize().padding(padding), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(5f).verticalScroll(rememberScrollState()).padding(start = 24.dp, top = 16.dp, bottom = 24.dp)) {
                            Gallery(state, actions.back, rounded = true)
                            ArenaInfo(state, actions, onSport, compactCourts = true)
                        }
                        Box(Modifier.weight(7f).fillMaxSize()) {
                            SlotPickerRoute(
                                route = Route.SlotPicker(firstCourt.id),
                                onBack = actions.back,
                                onContinue = actions.createGame,
                                embedded = true,
                            )
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                        Gallery(state, actions.back, rounded = false)
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            Column(Modifier.widthIn(max = 760.dp).fillMaxWidth()) {
                                ArenaInfo(state, actions, onSport, compactCourts = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Gallery(state: ArenaDetailUiState.Content, onBack: () -> Unit, rounded: Boolean) {
    val nativeShare = rememberNativeShare()
    val share = rememberShareActions()
    val sport = state.detail.esportes.firstOrNull() ?: Sport.FUTEBOL_SOCIETY
    val scrim = Color.Black.copy(alpha = 0.56f)
    Box(
        Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .then(if (rounded) Modifier.clip(MaterialTheme.shapes.extraLarge) else Modifier)
            .testTag("arena_gallery"),
    ) {
        // E9: fotos enviadas pela arena (carrossel); sem fotos, o desenho da quadra (e sem contador falso).
        val store: ArenaPhotoStore = koinInject()
        val arenaId = state.detail.arena.id
        LaunchedEffect(arenaId) { store.load(arenaId) }
        val photos = store.photos.collectAsStateWithLifecycle().value[arenaId].orEmpty()
        val pager = rememberPagerState { photos.size.coerceAtLeast(1) }
        if (photos.isEmpty()) {
            SCCourtPlaceholder(sport, Modifier.fillMaxSize())
        } else {
            HorizontalPager(pager, Modifier.fillMaxSize()) { i ->
                Image(
                    photos[i], stringResource(Res.string.cd_arena_photo, state.detail.arena.nome),
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(scrim, CircleShape)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.cd_back), tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = {
                    val link = "https://sportconnect.app/a/${state.detail.arena.id}"
                    if (!nativeShare("${state.detail.arena.nome}\n$link")) share.copy(link)
                },
                modifier = Modifier.size(40.dp).background(scrim, CircleShape).testTag("arena_share"),
            ) { Icon(Icons.Rounded.Share, stringResource(Res.string.cd_arena_share), tint = Color.White) }
        }
        if (photos.size > 1) Text(
            "${pager.currentPage + 1}/${photos.size}",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).background(scrim, SCShape.chip).padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ArenaInfo(state: ArenaDetailUiState.Content, actions: ArenaDetailActions, onSport: (Sport?) -> Unit, compactCourts: Boolean) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val a = state.detail.arena
    val share = rememberShareActions()
    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Text(a.nome, style = t.headlineMedium, color = c.onSurface, maxLines = 2, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(4.dp))
        Text("${a.endereco.resumo} · ${a.endereco.cidade}", style = t.bodyMedium, color = c.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SCButton(
                stringResource(Res.string.arena_detail_open_maps),
                { share.openUrl(mapsUrl("${a.nome}, ${a.endereco.resumo}")) },
                variant = SCButtonVariant.Secondary,
                size = SCButtonSize.Small,
                leadingIcon = Icons.Rounded.Directions,
                modifier = Modifier.testTag("arena_open_maps"),
            )
            a.telefone?.let { phone ->
                SCButton(
                    stringResource(Res.string.arena_detail_call),
                    { share.openUrl("tel:" + phone.filter { it.isDigit() }) },
                    variant = SCButtonVariant.Secondary,
                    size = SCButtonSize.Small,
                    leadingIcon = Icons.Rounded.Call,
                    modifier = Modifier.testTag("arena_call"),
                )
            }
        }
        // M3: mini-mapa com o pino; tocar abre o app de mapas (mesmo destino do "Como chegar").
        val tiles: MapTileCache = koinInject()
        val camera = rememberSCMapCamera(a.localizacao.latitude, a.localizacao.longitude, 15.0)
        val pinDesc = stringResource(Res.string.cd_directions)
        Spacer(Modifier.height(12.dp))
        SCMap(
            camera, tiles,
            listOf(SCMapPin(a.id, a.localizacao.latitude, a.localizacao.longitude, a.endereco.bairro.ifBlank { a.nome }, pinDesc)),
            modifier = Modifier.fillMaxWidth().height(150.dp).clip(MaterialTheme.shapes.large).testTag("arena_minimap"),
            selectedPinId = a.id,
            onPinClick = { share.openUrl(mapsUrl("${a.nome}, ${a.endereco.resumo}")) },
            onMapClick = { share.openUrl(mapsUrl("${a.nome}, ${a.endereco.resumo}")) },
            interactive = false,
            fitPinsOnStart = false,
        )
        Spacer(Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.detail.esportes.sortedBy { it.ordinal }.forEach { s ->
                SCSportChip(
                    s,
                    variant = SCSportChipVariant.Filter,
                    selected = state.sport == s,
                    onClick = { onSport(s) },
                    modifier = Modifier.testTag("arena_sport_chip_${s.name.lowercase()}"),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        SectionHeader(stringResource(Res.string.arena_detail_courts))
        Spacer(Modifier.height(8.dp))
        val courts = state.detail.quadras.filter { state.sport == null || it.suporta(state.sport) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            courts.forEach { q -> CourtBlock(q, state.todaySlots[q.id].orEmpty(), actions, compactCourts) }
        }
        if (a.comodidades.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionHeader(stringResource(Res.string.arena_detail_amenities))
            Spacer(Modifier.height(8.dp))
            // E9: comodidades com icone e o rotulo do 07 quando reconhecida.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("arena_amenities")) {
                a.comodidades.forEach { raw ->
                    val (icon, label) = amenityOf(raw)
                    Row(
                        Modifier.heightIn(min = 36.dp).background(c.surfaceContainerLow, SCShape.chip).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(icon, null, tint = c.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(label?.let { stringResource(it) } ?: raw, style = t.labelLarge, color = c.onSurface)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        PolicyRow(stringResource(Res.string.arena_detail_rules))
        HorizontalDivider(color = c.outlineVariant)
        PolicyRow(stringResource(Res.string.arena_detail_cancellation_policy))
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PolicyRow(text: String) {
    val c = SportConnectTheme.colors
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button) { }, verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = c.onSurfaceVariant)
    }
}

@Composable
private fun CourtBlock(q: Quadra, slots: List<Slot>, actions: ArenaDetailActions, compact: Boolean) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(
        Modifier.fillMaxWidth()
            .background(c.surfaceContainerLowest, MaterialTheme.shapes.large)
            .border(1.dp, c.outlineVariant, MaterialTheme.shapes.large)
            .clickable { actions.openSlots(q.id, null) }
            .padding(16.dp)
            .testTag("arena_court_${q.id}"),
    ) {
        Text(q.nome, style = t.titleMedium, color = c.onSurface)
        Text(
            stringResource(Res.string.arena_detail_court_surface, q.piso) + if (q.coberta) " · Coberta" else "",
            style = t.bodyMedium,
            color = c.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(Res.string.arena_detail_price_range, q.precoHora.format(), q.precoHoraPico.format()),
            style = SportConnectTheme.typographyExt.numericPriceSmall,
            color = c.onSurface,
        )
        if (!compact) {
            Spacer(Modifier.height(12.dp))
            if (slots.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.arena_detail_no_slots_today), style = t.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.weight(1f))
                    SCButton(stringResource(Res.string.arena_detail_see_tomorrow), { actions.openSlots(q.id, "tomorrow") }, variant = SCButtonVariant.Text, size = SCButtonSize.Small, modifier = Modifier.scAlignTextButtonStart())
                }
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slots.take(4).forEach { s ->
                        Box(
                            Modifier.heightIn(min = 48.dp).widthIn(min = 64.dp).clip(SCShape.control)
                                .border(1.dp, c.outline, SCShape.control)
                                .clickable(role = Role.Button) { actions.createGame(s.id) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text(PtBrFormat.hour(s.inicio), style = t.labelLarge, color = c.onSurface) }
                    }
                    SCButton(stringResource(Res.string.common_see_all), { actions.openSlots(q.id, null) }, variant = SCButtonVariant.Text, size = SCButtonSize.Small, modifier = Modifier.scAlignTextButtonStart())
                }
            }
        }
    }
}

/** Icone e rotulo padrao (07) das comodidades conhecidas; desconhecida fica com o texto da arena. */
private fun amenityOf(raw: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, StringResource?> {
    val n = raw.lowercase()
    return when {
        "estacion" in n -> Icons.Rounded.LocalParking to Res.string.arena_detail_amenity_parking
        "vesti" in n -> Icons.Rounded.Checkroom to Res.string.arena_detail_amenity_locker
        "bar" in n || "lanchonete" in n -> Icons.Rounded.LocalBar to Res.string.arena_detail_amenity_bar
        "ilumina" in n -> Icons.Rounded.Lightbulb to Res.string.arena_detail_amenity_lighting
        "churras" in n -> Icons.Rounded.OutdoorGrill to null
        "wi-fi" in n || "wifi" in n -> Icons.Rounded.Wifi to null
        else -> Icons.Rounded.CheckCircle to null
    }
}
