package br.com.sportconnect.feature.arenaadmin.courts

import br.com.sportconnect.resources.cd_remove_photo
import br.com.sportconnect.resources.arena_photos_add
import br.com.sportconnect.resources.arena_photos_hint
import br.com.sportconnect.resources.arena_photos_title
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.grid.GridItemSpan
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCButton
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.IconButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import kotlinx.coroutines.launch
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import br.com.sportconnect.core.data.local.ArenaPhotoStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.CourtCardSkeleton
import br.com.sportconnect.core.designsystem.component.CourtCardUi
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCCourtCard
import br.com.sportconnect.core.designsystem.component.SCCourtCardVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.TipoQuadra
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.ArenaTab
import br.com.sportconnect.feature.arenaadmin.common.ArenaAdminScaffold
import br.com.sportconnect.feature.arenaadmin.common.ArenaErrorState
import br.com.sportconnect.feature.arenaadmin.common.ArenaNoArenaState
import br.com.sportconnect.feature.arenaadmin.common.ArenaWidth
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_court_covered_no
import br.com.sportconnect.resources.aadm_court_covered_yes
import br.com.sportconnect.resources.aadm_court_type_clay
import br.com.sportconnect.resources.aadm_court_type_hard
import br.com.sportconnect.resources.aadm_court_type_padel
import br.com.sportconnect.resources.aadm_court_type_poli
import br.com.sportconnect.resources.aadm_court_type_sand
import br.com.sportconnect.resources.aadm_court_type_society
import br.com.sportconnect.resources.aadm_courts_occupancy
import br.com.sportconnect.resources.arena_court_add
import br.com.sportconnect.resources.arena_tab_courts
import br.com.sportconnect.resources.common_edit
import br.com.sportconnect.resources.empty_arena_courts_body
import br.com.sportconnect.resources.empty_arena_courts_cta
import br.com.sportconnect.resources.empty_arena_courts_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

val TipoQuadra.labelRes: StringResource
    get() = when (this) {
        TipoQuadra.SOCIETY_SINTETICO -> Res.string.aadm_court_type_society
        TipoQuadra.QUADRA_POLIESPORTIVA -> Res.string.aadm_court_type_poli
        TipoQuadra.AREIA -> Res.string.aadm_court_type_sand
        TipoQuadra.PADEL -> Res.string.aadm_court_type_padel
        TipoQuadra.SAIBRO -> Res.string.aadm_court_type_clay
        TipoQuadra.PISO_RAPIDO -> Res.string.aadm_court_type_hard
    }

/** `arena-admin/courts` — Quadras (10 §2.20). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaCourtsScreen(
    nav: ArenaAdminNavigator,
    viewModel: ArenaCourtsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    ArenaAdminScaffold(
        selected = ArenaTab.Courts,
        nav = nav,
        topBar = { SCTopBar(stringResource(Res.string.arena_tab_courts), navigation = null) },
    ) { padding, width ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.load) {
                LoadState.Loading -> CourtCardSkeleton(Modifier.padding(16.dp))
                LoadState.Error -> ArenaErrorState(viewModel::retry)
                LoadState.NoArena -> ArenaNoArenaState(nav::openOnboarding)
                LoadState.Ready -> if (state.courts.isEmpty()) {
                    SCEmptyState(
                        stringResource(Res.string.empty_arena_courts_title),
                        stringResource(Res.string.empty_arena_courts_body),
                        primaryAction = SCAction(stringResource(Res.string.empty_arena_courts_cta), { nav.openCourt(null) }).takeIf { state.canEdit },
                    )
                } else {
                    val margin = if (width == ArenaWidth.Compact) 16.dp else 24.dp
                    val covered = stringResource(Res.string.aadm_court_covered_yes)
                    val uncovered = stringResource(Res.string.aadm_court_covered_no)
                    val edit = stringResource(Res.string.common_edit)
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(320.dp),
                        contentPadding = PaddingValues(start = margin, end = margin, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize().testTag("courts_list"),
                    ) {
                        val photosArenaId = state.arenaId
                        if (state.canEdit && photosArenaId != null) {
                            item(span = { GridItemSpan(maxLineSpan) }, key = "arena_photos") { ArenaPhotosSection(photosArenaId) }
                        }
                        items(state.courts, key = { it.id }) { court ->
                            val occ = stringResource(Res.string.aadm_courts_occupancy, court.occupancyPct)
                            SCCourtCard(
                                court = CourtCardUi(
                                    arenaName = court.name,
                                    courtName = stringResource(court.tipo.labelRes),
                                    detailsLabel = listOf(court.surface, if (court.covered) covered else uncovered).filter { it.isNotBlank() }.joinToString(" · "),
                                    sports = court.sports,
                                    priceFrom = court.priceFrom,
                                    occupancy = court.occupancyPct / 100f,
                                    occupancyLabel = occ,
                                ),
                                onClick = { if (state.canEdit) nav.openCourt(court.id) },
                                variant = SCCourtCardVariant.Owner,
                                ownerAction = if (state.canEdit) SCAction(edit, { nav.openCourt(court.id) }, icon = Icons.Rounded.Edit) else null,
                                modifier = Modifier.testTag("courts_card_${court.id}"),
                            )
                        }
                    }
                }
            }
            if (state.load == LoadState.Ready && state.canEdit && state.courts.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { nav.openCourt(null) },
                    icon = { Icon(Icons.Rounded.Add, null) },
                    text = { Text(stringResource(Res.string.arena_court_add)) },
                    containerColor = SportConnectTheme.colors.primaryContainer,
                    contentColor = SportConnectTheme.colors.onPrimaryContainer,
                    shape = SCShape.small,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("courts_add"),
                )
            }
        }
    }
}

/** E9 (CT-01): fotos da arena — aparecem no carrossel da pagina da arena para os atletas. */
@Composable
private fun ArenaPhotosSection(arenaId: String) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val store: ArenaPhotoStore = koinInject()
    val photos = store.photos.collectAsStateWithLifecycle().value[arenaId].orEmpty()
    val scope = rememberCoroutineScope()
    LaunchedEffect(arenaId) { store.load(arenaId) }
    val picker = rememberFilePickerLauncher(type = FileKitType.Image, mode = FileKitMode.Multiple(maxItems = ArenaPhotoStore.MAX)) { files ->
        if (!files.isNullOrEmpty()) scope.launch { store.add(arenaId, files.map { it.readBytes() }) }
    }
    Column(Modifier.fillMaxWidth().testTag("arena_photos"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.arena_photos_title), style = t.titleMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Text(stringResource(Res.string.arena_photos_hint), style = t.bodySmall, color = c.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            photos.forEachIndexed { i, img ->
                Box(Modifier.size(112.dp, 72.dp).clip(MaterialTheme.shapes.medium)) {
                    Image(img, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    val cd = stringResource(Res.string.cd_remove_photo, i + 1)
                    IconButton(
                        onClick = { scope.launch { store.remove(arenaId, i) } },
                        modifier = Modifier.align(Alignment.TopEnd).size(32.dp).semantics { contentDescription = cd },
                    ) {
                        Icon(Icons.Rounded.Cancel, null, tint = Color.White, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape))
                    }
                }
            }
            if (photos.size < ArenaPhotoStore.MAX) {
                SCButton(
                    stringResource(Res.string.arena_photos_add), { picker.launch() },
                    variant = SCButtonVariant.Secondary, leadingIcon = Icons.Rounded.AddAPhoto,
                    modifier = Modifier.height(72.dp).testTag("arena_photos_add"),
                )
            }
        }
    }
}
