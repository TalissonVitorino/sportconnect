@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.invite

import br.com.sportconnect.core.designsystem.component.scAlignTextButtonStart
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.AvatarUi
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCAvatar
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCountdownTimer
import br.com.sportconnect.core.designsystem.component.SCCourtPlaceholder
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCIllustrationImage
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCSplitProgress
import br.com.sportconnect.core.designsystem.component.SCSplitProgressVariant
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCTextField
import br.com.sportconnect.core.designsystem.component.SCTextFieldType
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.icon.SCLogo
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.feature.common.CenteredContent
import br.com.sportconnect.feature.common.CourtLinesBackdrop
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.inviteUrl
import br.com.sportconnect.feature.common.mapsUrl
import br.com.sportconnect.feature.common.rememberShareActions
import br.com.sportconnect.feature.common.splitCounts
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.feature.game.ruleTexts
import br.com.sportconnect.navigation.Route
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.arena_detail_open_maps
import br.com.sportconnect.resources.cd_copy_link
import br.com.sportconnect.resources.common_close
import br.com.sportconnect.resources.error_game_cancelled_body
import br.com.sportconnect.resources.error_game_cancelled_title
import br.com.sportconnect.resources.error_invite_invalid_body
import br.com.sportconnect.resources.error_invite_invalid_title
import br.com.sportconnect.resources.expiry_cancel_body
import br.com.sportconnect.resources.expiry_title
import br.com.sportconnect.resources.guest_already_paid
import br.com.sportconnect.resources.guest_cta_decline
import br.com.sportconnect.resources.guest_cta_pay
import br.com.sportconnect.resources.guest_deadline
import br.com.sportconnect.resources.guest_declined
import br.com.sportconnect.resources.guest_declined_notify
import br.com.sportconnect.resources.guest_first_open_kicker
import br.com.sportconnect.resources.guest_game_full_body
import br.com.sportconnect.resources.guest_game_full_title
import br.com.sportconnect.resources.guest_invited_by
import br.com.sportconnect.resources.guest_leave_policy
import br.com.sportconnect.resources.guest_no_account_hint
import br.com.sportconnect.resources.guest_protected_payment
import br.com.sportconnect.resources.guest_rule_cancel
import br.com.sportconnect.resources.guest_rule_covers
import br.com.sportconnect.resources.guest_rule_label
import br.com.sportconnect.resources.guest_rule_resplit
import br.com.sportconnect.resources.guest_see_rules
import br.com.sportconnect.resources.guest_share_breakdown
import br.com.sportconnect.resources.guest_spots_left
import br.com.sportconnect.resources.guest_who_is_in
import br.com.sportconnect.resources.guest_who_is_in_more
import br.com.sportconnect.resources.guest_your_share
import br.com.sportconnect.resources.invite_body
import br.com.sportconnect.resources.invite_copy_link
import br.com.sportconnect.resources.invite_link_copied
import br.com.sportconnect.resources.invite_link_preview_label
import br.com.sportconnect.resources.invite_message_preview_label
import br.com.sportconnect.resources.invite_see_game
import br.com.sportconnect.resources.invite_share_other
import br.com.sportconnect.resources.invite_share_whatsapp
import br.com.sportconnect.resources.invite_title
import br.com.sportconnect.resources.pix_see_game
import br.com.sportconnect.resources.split_time_left
import br.com.sportconnect.resources.wa_invite_message
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// =================================================================== organizador

data class OrganizerInviteActions(
    val close: (String) -> Unit = {},
    val seeGame: (String) -> Unit = {},
)

@Composable
fun OrganizerInviteRoute(route: Route.GameInvite, actions: OrganizerInviteActions) {
    val vm: OrganizerInviteViewModel = koinViewModel(key = "invite-${route.id}") { parametersOf(route) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    OrganizerInviteScreen(state, actions, onEdit = vm::editMessage, onShared = vm::onShared)
}

/** Mensagem padrao do convite (`wa_invite_message`). */
@Composable
fun inviteMessage(j: Jogo, nowLabel: (kotlin.time.Instant) -> String): String {
    val share = (j.valorCotaConvidado + TaxaConveniencia.PIX).format()
    return stringResource(
        Res.string.wa_invite_message,
        j.titulo,
        "${j.arenaNome} · ${j.quadraNome}",
        PtBrFormat.dateTimeRange(j.inicio, j.fim),
        share,
        j.reserva.prazoCotas?.let(nowLabel) ?: "",
        inviteUrl(j.codigoConvite),
    )
}

/** Chamar a galera (10 §2.8). */
@Composable
fun OrganizerInviteScreen(
    state: OrganizerInviteUiState,
    actions: OrganizerInviteActions = OrganizerInviteActions(),
    onEdit: (String) -> Unit = {},
    onShared: (String) -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val share = rememberShareActions()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val j = state.jogo
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        Scaffold(
            containerColor = c.surface,
            snackbarHost = { SCSnackbarHost(snackbar) },
            topBar = { SCTopBar("", navigation = SCTopBarNavigation.Close, onNavigationClick = { j?.let { actions.close(it.id) } }) },
            bottomBar = {
                if (j != null) {
                    val message = state.editedMessage ?: inviteMessage(j) { PtBrFormat.relative(it, state.now) }
                    BottomActionBar {
                        SCButton(
                            stringResource(Res.string.invite_share_whatsapp),
                            { onShared("whatsapp"); share.whatsapp(message) },
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            leadingIcon = Icons.Rounded.Share,
                            modifier = Modifier.testTag("invite_share_whatsapp"),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SCButton(
                                stringResource(Res.string.invite_copy_link),
                                {
                                    onShared("copiado")
                                    share.copy(inviteUrl(j.codigoConvite))
                                    scope.launch { snackbar.showSC(getString(Res.string.invite_link_copied), SCSnackbarKind.Success) }
                                },
                                variant = SCButtonVariant.Secondary,
                                leadingIcon = Icons.Rounded.ContentCopy,
                                accessibilityLabel = stringResource(Res.string.cd_copy_link),
                                fullWidth = true,
                                modifier = Modifier.weight(1f).testTag("invite_copy_link"),
                            )
                            SCButton(
                                stringResource(Res.string.invite_share_other),
                                {
                                    onShared("nativo")
                                    share.copy(message)
                                    scope.launch { snackbar.showSC(getString(Res.string.invite_link_copied), SCSnackbarKind.Success) }
                                },
                                variant = SCButtonVariant.Secondary,
                                leadingIcon = Icons.Rounded.Share,
                                fullWidth = true,
                                modifier = Modifier.weight(1f).testTag("invite_share_other"),
                            )
                        }
                        SCButton(
                            stringResource(Res.string.invite_see_game),
                            { actions.seeGame(j.id) },
                            variant = SCButtonVariant.Text,
                            fullWidth = true,
                            modifier = Modifier.testTag("invite_see_game"),
                        )
                    }
                }
            },
        ) { padding ->
            if (j == null) {
                Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SCSkeleton(SCSkeletonShape.Box(height = 120.dp))
                    SCSkeleton(SCSkeletonShape.Line(0.6f, 32.dp))
                }
                return@Scaffold
            }
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                CenteredContent(Modifier.padding(horizontal = if (wc == WidthClass.Compact) 16.dp else 24.dp)) {
                    SCIllustrationImage(SCIllustration.Court, Modifier.size(120.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(Res.string.invite_title), style = t.headlineLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(Res.string.invite_body), style = t.bodyLarge, color = c.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    j.reserva.prazoCotas?.let { deadline ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SCCountdownTimer(deadline)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(Res.string.split_time_left, PtBrFormat.duration(deadline - state.now)),
                                style = t.bodyMedium,
                                color = c.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    val preview = @Composable {
                        Column(Modifier.fillMaxWidth()) {
                            Text(stringResource(Res.string.invite_link_preview_label), style = t.titleSmall, color = c.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Column(Modifier.fillMaxWidth().background(c.surfaceContainerLow, MaterialTheme.shapes.large).padding(12.dp)) {
                                SCCourtPlaceholder(j.esporte, Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium))
                                Spacer(Modifier.height(8.dp))
                                Text(j.titulo, style = t.titleSmall, color = c.onSurface)
                                Text(PtBrFormat.dateTimeRange(j.inicio, j.fim), style = t.bodySmall, color = c.onSurfaceVariant)
                                Text(
                                    stringResource(Res.string.guest_your_share) + ": " + (j.valorCotaConvidado + TaxaConveniencia.PIX).format(),
                                    style = t.bodySmall,
                                    color = c.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    val message = @Composable {
                        Column(Modifier.fillMaxWidth()) {
                            Text(stringResource(Res.string.invite_message_preview_label), style = t.titleSmall, color = c.onSurface)
                            Spacer(Modifier.height(8.dp))
                            SCTextField(
                                value = state.editedMessage ?: inviteMessage(j) { PtBrFormat.relative(it, state.now) },
                                onValueChange = onEdit,
                                label = "",
                                type = SCTextFieldType.Multiline,
                                modifier = Modifier.fillMaxWidth().testTag("invite_message_field"),
                            )
                        }
                    }
                    if (wc == WidthClass.Expanded) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Box(Modifier.weight(1f)) { preview() }
                            Box(Modifier.weight(1f)) { message() }
                        }
                    } else {
                        preview()
                        Spacer(Modifier.height(24.dp))
                        message()
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// =================================================================== convidado

data class GuestInviteActions(
    val pay: (gameId: String) -> Unit = {},
    val login: (gameId: String) -> Unit = {},
    val seeGame: (gameId: String) -> Unit = {},
    val close: () -> Unit = {},
)

@Composable
fun GuestInviteRoute(route: Route.Invite, actions: GuestInviteActions) {
    val vm: GuestInviteViewModel = koinViewModel(key = "guest-${route.code}") { parametersOf(route) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    // Organizador abrindo o proprio link -> game/{id} (10 §2.12).
    LaunchedEffect(state) {
        val s = state as? GuestInviteUiState.Content
        if (s?.isOrganizer == true) actions.seeGame(s.jogo.id)
    }
    GuestInviteScreen(state, actions, onDecline = vm::decline, onRetry = vm::retry)
}

/** Convite do convidado (10 §2.12, redline §4.3, momento de marca §3.2). */
@Composable
fun GuestInviteScreen(
    state: GuestInviteUiState,
    actions: GuestInviteActions = GuestInviteActions(),
    onDecline: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    var showRules by remember { mutableStateOf(false) }
    val share = rememberShareActions()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        val content = state as? GuestInviteUiState.Content
        val fixedFooter = wc == WidthClass.Compact && maxHeight >= 480.dp
        Scaffold(
            containerColor = if (wc == WidthClass.Compact) c.surface else c.surfaceContainerLow,
            topBar = { GuestTopBar() },
            bottomBar = {
                if (content != null && fixedFooter) {
                    BottomActionBar { GuestFooter(content, actions, onDecline, share) }
                }
            },
        ) { padding ->
            when (state) {
                GuestInviteUiState.Loading -> Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SCSkeleton(SCSkeletonShape.Line(0.5f, 20.dp))
                    SCSkeleton(SCSkeletonShape.Line(0.8f, 32.dp))
                    SCSkeleton(SCSkeletonShape.Box(height = 200.dp))
                }
                is GuestInviteUiState.Invalid -> SCEmptyState(
                    title = stringResource(Res.string.error_invite_invalid_title),
                    description = stringResource(Res.string.error_invite_invalid_body),
                    illustration = SCIllustration.Error,
                    primaryAction = SCAction(stringResource(Res.string.common_close), actions.close),
                    modifier = Modifier.padding(padding),
                )
                is GuestInviteUiState.Content -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                    CenteredContent(maxWidth = 560.dp) {
                        Column(Modifier.fillMaxWidth().background(c.surface)) {
                            GuestHero(state, share)
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                Spacer(Modifier.height(24.dp))
                                when {
                                    state.cancelled -> NoticeBlock(stringResource(Res.string.error_game_cancelled_title), stringResource(Res.string.error_game_cancelled_body))
                                    state.expired -> NoticeBlock(stringResource(Res.string.expiry_title), stringResource(Res.string.expiry_cancel_body))
                                    state.full && !state.alreadyPaid -> NoticeBlock(stringResource(Res.string.guest_game_full_title), stringResource(Res.string.guest_game_full_body))
                                    else -> ShareCard(state)
                                }
                                Spacer(Modifier.height(24.dp))
                                SplitInfo(state)
                                Spacer(Modifier.height(24.dp))
                                RuleInfo(state.jogo) { showRules = true }
                                Spacer(Modifier.height(24.dp))
                                if (!fixedFooter) {
                                    GuestFooter(state, actions, onDecline, share)
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    val content = state as? GuestInviteUiState.Content
    if (showRules && content != null) {
        val (title, body) = ruleTexts(content.jogo.politica)
        SCDialog(
            title = stringResource(title),
            body = stringResource(body),
            variant = SCDialogVariant.Info,
            icon = Icons.Rounded.Info,
            onDismissRequest = { showRules = false },
            confirm = SCAction(stringResource(Res.string.common_close), { showRules = false }),
        )
    }
}

@Composable
private fun GuestTopBar() {
    val c = SportConnectTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface)) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            SCLogo(Modifier.height(20.dp), onDark = isDarkSurface())
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.Lock, null, tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(Res.string.guest_protected_payment), style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
        }
        HorizontalDivider(color = c.outlineVariant)
    }
}

/** Escolhe a versao do logo pelo fundo atual (tema), nao pelo sistema. */
@Composable
private fun isDarkSurface(): Boolean {
    val bg = SportConnectTheme.colors.surface
    return (bg.red + bg.green + bg.blue) / 3f < 0.5f
}

@Composable
private fun GuestHero(state: GuestInviteUiState.Content, share: br.com.sportconnect.feature.common.ShareActions) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val j = state.jogo
    Box(
        Modifier.fillMaxWidth()
            .then(if (state.firstOpen) Modifier.background(c.surfaceContainerLow) else Modifier)
            .testTag("guest_hero"),
    ) {
        if (state.firstOpen) {
            CourtLinesBackdrop(c.outlineVariant, Modifier.matchParentSize().testTag("guest_hero_court_lines"), lineYFraction = 0.55f)
        }
        Column(Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
            if (state.firstOpen) {
                Text(
                    stringResource(Res.string.guest_first_open_kicker),
                    style = t.labelLarge,
                    color = c.secondary,
                    modifier = Modifier.testTag("guest_first_open_kicker"),
                )
                Spacer(Modifier.height(8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SCAvatar(AvatarUi(j.organizadorNome), size = 44.dp, ring = c.secondary)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(Res.string.guest_invited_by, j.organizadorNome.substringBefore(' ')),
                    style = t.titleLarge,
                    color = c.onSurface,
                    modifier = Modifier.semantics { heading() }.testTag("guest_invited_by"),
                )
            }
            Spacer(Modifier.height(16.dp))
            SCSportChip(j.esporte)
            Spacer(Modifier.height(8.dp))
            Text(PtBrFormat.dateTimeRange(j.inicio, j.fim), style = t.headlineSmall, color = c.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("${j.arenaNome} · ${j.quadraNome}", style = t.bodyLarge, color = c.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("São Paulo", style = t.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.weight(1f))
                SCButton(
                    stringResource(Res.string.arena_detail_open_maps),
                    { share.openUrl(mapsUrl(j.arenaNome)) },
                    variant = SCButtonVariant.Text,
                    size = SCButtonSize.Small,
                )
            }
        }
    }
}

@Composable
private fun ShareCard(state: GuestInviteUiState.Content) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val j = state.jogo
    val fee = TaxaConveniencia.PIX
    val total = j.valorCotaConvidado + fee
    Column(
        Modifier.fillMaxWidth()
            .background(c.surfaceContainerLowest, MaterialTheme.shapes.extraLarge)
            .border(1.dp, c.outlineVariant, MaterialTheme.shapes.extraLarge)
            .padding(20.dp),
    ) {
        if (state.alreadyPaid) {
            Text(stringResource(Res.string.guest_already_paid), style = t.titleMedium, color = SportConnectTheme.extendedColors.success)
            return@Column
        }
        Text(stringResource(Res.string.guest_your_share), style = t.titleSmall, color = c.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(total.format(), style = t.displaySmall, color = c.onSurface, modifier = Modifier.testTag("guest_share_value"))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.guest_share_breakdown, j.valorCotaConvidado.format(), fee.format()), style = t.bodyMedium, color = c.onSurfaceVariant)
            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) { Icon(Icons.Rounded.Info, null, tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
        }
        j.reserva.prazoCotas?.let { deadline ->
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = c.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.testTag("guest_deadline")) {
                Icon(Icons.Rounded.Schedule, null, tint = SportConnectTheme.extendedColors.warning, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(stringResource(Res.string.guest_deadline, PtBrFormat.relative(deadline, state.now)), style = t.bodyMedium, color = c.onSurface)
                    SCCountdownTimer(deadline)
                }
            }
        }
    }
}

@Composable
private fun NoticeBlock(title: String, body: String) {
    val c = SportConnectTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surfaceContainerHigh, MaterialTheme.shapes.extraLarge).padding(20.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
    }
}

@Composable
private fun SplitInfo(state: GuestInviteUiState.Content) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val j = state.jogo
    val counts = j.splitCounts(state.now)
    Text(stringResource(Res.string.guest_spots_left, j.vagasRestantes, j.numeroVagas), style = t.titleMedium, color = c.onSurface)
    Spacer(Modifier.height(8.dp))
    SCSplitProgress(
        counts.total, counts.paid, processing = counts.processing, covered = counts.covered,
        variant = SCSplitProgressVariant.Compact,
        modifier = Modifier.fillMaxWidth().testTag("guest_split_progress"),
    )
    Spacer(Modifier.height(12.dp))
    val payers = j.reserva.cotas.filter { it.status == StatusCota.PAGA && it.atletaNome != null }.map {
        val parts = it.atletaNome!!.split(' ')
        if (parts.size > 1) "${parts.first()} ${parts.last().first()}." else parts.first()
    }
    if (payers.isNotEmpty()) {
        Text(stringResource(Res.string.guest_who_is_in), style = t.titleSmall, color = c.onSurface)
        Spacer(Modifier.height(4.dp))
        val shown = payers.take(4).joinToString(", ")
        val more = payers.size - 4
        Text(
            if (more > 0) "$shown " + stringResource(Res.string.guest_who_is_in_more, more) else shown,
            style = t.bodyMedium,
            color = c.onSurfaceVariant,
            modifier = Modifier.testTag("guest_who_is_in"),
        )
    }
}

@Composable
private fun RuleInfo(j: Jogo, onSeeRules: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Text(stringResource(Res.string.guest_rule_label), style = t.titleSmall, color = c.onSurface)
    Spacer(Modifier.height(4.dp))
    Text(
        when (j.politica) {
            PoliticaNaoPagamento.ORGANIZADOR_COBRE -> stringResource(Res.string.guest_rule_covers, j.organizadorNome.substringBefore(' '))
            PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR -> stringResource(Res.string.guest_rule_cancel)
            PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES -> stringResource(Res.string.guest_rule_resplit)
        },
        style = t.bodyMedium,
        color = c.onSurfaceVariant,
        modifier = Modifier.testTag("guest_rule"),
    )
    Spacer(Modifier.height(8.dp))
    Text(stringResource(Res.string.guest_leave_policy), style = t.bodyMedium, color = c.onSurfaceVariant)
    SCButton(stringResource(Res.string.guest_see_rules), onSeeRules, variant = SCButtonVariant.Text, size = SCButtonSize.Small, modifier = Modifier.scAlignTextButtonStart().testTag("guest_see_rules"))
}

@Composable
private fun GuestFooter(
    state: GuestInviteUiState.Content,
    actions: GuestInviteActions,
    onDecline: () -> Unit,
    share: br.com.sportconnect.feature.common.ShareActions,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val j = state.jogo
    when {
        state.declined -> {
            val org = j.organizadorNome.substringBefore(' ')
            val declineText = stringResource(Res.string.guest_cta_decline)
            Text(stringResource(Res.string.guest_declined, org), style = t.bodyMedium, color = c.onSurface)
            SCButton(
                stringResource(Res.string.guest_declined_notify),
                { share.whatsapp(declineText) },
                variant = SCButtonVariant.Secondary,
                fullWidth = true,
            )
        }
        state.canPay -> {
            if (!state.loggedIn) Text(stringResource(Res.string.guest_no_account_hint), style = t.bodySmall, color = c.onSurfaceVariant)
            val total = (j.valorCotaConvidado + TaxaConveniencia.PIX).format()
            SCButton(
                stringResource(Res.string.guest_cta_pay, total),
                { if (state.loggedIn) actions.pay(j.id) else actions.login(j.id) },
                size = SCButtonSize.Large,
                fullWidth = true,
                modifier = Modifier.testTag("guest_cta_pay"),
            )
            SCButton(
                stringResource(Res.string.guest_cta_decline),
                onDecline,
                variant = SCButtonVariant.Text,
                fullWidth = true,
                modifier = Modifier.testTag("guest_cta_decline"),
            )
        }
        else -> SCButton(
            stringResource(Res.string.pix_see_game),
            { actions.seeGame(j.id) },
            variant = if (state.alreadyPaid) SCButtonVariant.Primary else SCButtonVariant.Secondary,
            size = SCButtonSize.Large,
            fullWidth = true,
        )
    }
}
