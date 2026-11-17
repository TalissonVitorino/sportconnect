@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.game

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import br.com.sportconnect.core.designsystem.component.scSharedBounds
import br.com.sportconnect.core.designsystem.component.sharedGameTitleKey
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.AvatarUi
import br.com.sportconnect.core.designsystem.component.ParticipantRowSkeleton
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCBadgeSize
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCountdownTimer
import br.com.sportconnect.core.designsystem.component.SCCountdownVariant
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCOpenSpotRow
import br.com.sportconnect.core.designsystem.component.SCParticipantRow
import br.com.sportconnect.core.designsystem.component.labelRes
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCSplitProgress
import br.com.sportconnect.core.designsystem.component.SCSplitProgressVariant
import br.com.sportconnect.core.designsystem.component.SCSportChip
import br.com.sportconnect.core.designsystem.component.SCStatusBadge
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SCShareState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.MotivoConfirmacao
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.feature.common.CenteredContent
import br.com.sportconnect.feature.common.CourtLinesBackdrop
import br.com.sportconnect.feature.common.LoadErrorState
import br.com.sportconnect.feature.common.NoticeCard
import br.com.sportconnect.feature.common.ParticipantUi
import br.com.sportconnect.feature.common.SectionHeader
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.inviteUrl
import br.com.sportconnect.feature.common.labelRes
import br.com.sportconnect.feature.common.mapsUrl
import br.com.sportconnect.feature.common.rememberShareActions
import br.com.sportconnect.feature.common.tone
import br.com.sportconnect.feature.common.urlEncode
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cancel_by_arena_body
import br.com.sportconnect.resources.cancel_dialog_body
import br.com.sportconnect.resources.cancel_dialog_confirm
import br.com.sportconnect.resources.cancel_dialog_keep
import br.com.sportconnect.resources.cancel_dialog_title
import br.com.sportconnect.resources.cancel_done_title
import br.com.sportconnect.resources.cancel_game_cta
import br.com.sportconnect.resources.cancel_leave_confirm
import br.com.sportconnect.resources.cancel_leave_dialog_body
import br.com.sportconnect.resources.cancel_leave_dialog_title
import br.com.sportconnect.resources.cancel_leave_game
import br.com.sportconnect.resources.cancel_leave_refund_value
import br.com.sportconnect.resources.cancel_organizer_cannot_leave
import br.com.sportconnect.resources.cancel_outside_window
import br.com.sportconnect.resources.cd_menu_more
import br.com.sportconnect.resources.cd_remind_person
import br.com.sportconnect.resources.cd_share_game
import br.com.sportconnect.resources.common_close
import br.com.sportconnect.resources.confirmed_add_calendar
import br.com.sportconnect.resources.confirmed_body
import br.com.sportconnect.resources.confirmed_brand_word
import br.com.sportconnect.resources.confirmed_covered_body
import br.com.sportconnect.resources.confirmed_directions
import br.com.sportconnect.resources.confirmed_share
import br.com.sportconnect.resources.confirmed_title
import br.com.sportconnect.resources.countdown_processing
import br.com.sportconnect.resources.empty_paid_list_body
import br.com.sportconnect.resources.empty_paid_list_cta
import br.com.sportconnect.resources.empty_paid_list_title
import br.com.sportconnect.resources.expiry_cancel_body
import br.com.sportconnect.resources.expiry_slot_released
import br.com.sportconnect.resources.game_create_rule_cancel_body
import br.com.sportconnect.resources.game_create_rule_cancel_title
import br.com.sportconnect.resources.game_create_rule_covers_body
import br.com.sportconnect.resources.game_create_rule_covers_title
import br.com.sportconnect.resources.game_create_rule_resplit_body
import br.com.sportconnect.resources.game_create_rule_resplit_title
import br.com.sportconnect.resources.game_invite_more
import br.com.sportconnect.resources.game_participants_title
import br.com.sportconnect.resources.game_resolution_banner
import br.com.sportconnect.resources.game_resolution_open
import br.com.sportconnect.resources.game_your_payment_paid
import br.com.sportconnect.resources.game_your_payment_title
import br.com.sportconnect.resources.home_pay_now
import br.com.sportconnect.resources.invite_share_whatsapp
import br.com.sportconnect.resources.pix_paid_receipt
import br.com.sportconnect.resources.split_organizer_exposure
import br.com.sportconnect.resources.split_progress
import br.com.sportconnect.resources.split_remind_all
import br.com.sportconnect.resources.split_remind_limit
import br.com.sportconnect.resources.split_remind_one
import br.com.sportconnect.resources.split_remind_sent
import br.com.sportconnect.resources.split_role_organizer
import br.com.sportconnect.resources.split_rule_row
import br.com.sportconnect.resources.split_status_paid_at
import br.com.sportconnect.resources.split_time_left
import br.com.sportconnect.resources.wa_game_confirmed
import br.com.sportconnect.resources.wa_group_reminder
import br.com.sportconnect.resources.wa_payment_reminder_2h
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import br.com.sportconnect.navigation.Route

data class GameDetailActions(
    val back: () -> Unit = {},
    val pay: (String) -> Unit = {},
    val invite: (String) -> Unit = {},
    val resolve: (String) -> Unit = {},
    val receipt: (String) -> Unit = {},
)

@Composable
fun GameDetailRoute(route: Route.GameDetail, actions: GameDetailActions) {
    val viewModel: GameDetailViewModel = koinViewModel(key = "game-${route.id}") { parametersOf(route) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val share = rememberShareActions()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { ev ->
            when (ev) {
                is GameDetailEvent.RemindAll -> {
                    val j = ev.jogo
                    val prazo = j.reserva.prazoCotas?.let { PtBrFormat.relative(it, kotlin.time.Clock.System.now()) } ?: ""
                    share.whatsapp(getString(Res.string.wa_group_reminder, ev.pending, j.titulo, prazo, inviteUrl(j.codigoConvite)))
                    snackbar.showSC(getString(Res.string.split_remind_sent, ev.pending), SCSnackbarKind.Success)
                }
                is GameDetailEvent.RemindOne -> {
                    val j = ev.jogo
                    val prazo = j.reserva.prazoCotas?.let { PtBrFormat.relative(it, kotlin.time.Clock.System.now()) } ?: ""
                    val cota = (j.valorCotaConvidado + TaxaConveniencia.PIX).format()
                    share.whatsapp(getString(Res.string.wa_payment_reminder_2h, ev.name.substringBefore(' '), j.titulo, cota, prazo, inviteUrl(j.codigoConvite)))
                }
                is GameDetailEvent.RemindLimited ->
                    snackbar.showSC(getString(Res.string.split_remind_limit, ev.name.substringBefore(' '), PtBrFormat.hour(ev.nextAt)))
                GameDetailEvent.Cancelled -> snackbar.showSC(getString(Res.string.cancel_done_title), SCSnackbarKind.Success)
                GameDetailEvent.Left -> snackbar.showSC(getString(Res.string.cancel_leave_game), SCSnackbarKind.Success)
                is GameDetailEvent.Failure -> snackbar.showSC(ev.message, SCSnackbarKind.Error)
            }
        }
    }
    GameDetailScreen(
        state = state,
        snackbar = snackbar,
        actions = actions,
        onRetry = viewModel::retry,
        onRemindAll = viewModel::remindAll,
        onRemindOne = viewModel::remindOne,
        onShareInvite = { j ->
            viewModel.onInviteShared("whatsapp")
            actions.invite(j)
        },
        onCancel = { viewModel.cancel() },
        onLeave = viewModel::leave,
    )
}

/** Detalhe do jogo + rateio (10 §2.9, redline §4.1) e momento "Fechou!" (§3.1). */
@Composable
fun GameDetailScreen(
    state: GameDetailUiState,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    actions: GameDetailActions = GameDetailActions(),
    onRetry: () -> Unit = {},
    onRemindAll: () -> Unit = {},
    onRemindOne: (ParticipantUi) -> Unit = {},
    onShareInvite: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    onLeave: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    var showCancel by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }
    var showRule by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val content = state as? GameDetailUiState.Content
    val celebrating = content?.celebrate == true
    val share = rememberShareActions()
    // A7: "Fechou!" com toque de confirmacao (uma vez, quando a celebracao aparece).
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(celebrating) { if (celebrating) haptics.performHapticFeedback(HapticFeedbackType.Confirm) }

    Scaffold(
        containerColor = c.surface,
        snackbarHost = { SCSnackbarHost(snackbar) },
        topBar = {
            if (!celebrating) {
                SCTopBar(
                    title = "",
                    onNavigationClick = actions.back,
                    actions = {
                        if (content != null) {
                            IconButton(onClick = { onShareInvite(content.jogo.id) }) {
                                Icon(Icons.Rounded.Share, stringResource(Res.string.cd_share_game))
                            }
                            Box {
                                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, stringResource(Res.string.cd_menu_more)) }
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    if (content.isOrganizer && content.cancelAllowed) {
                                        DropdownMenuItem(text = { Text(stringResource(Res.string.cancel_game_cta)) }, onClick = { menu = false; showCancel = true })
                                    }
                                    if (content.canLeave) {
                                        DropdownMenuItem(text = { Text(stringResource(Res.string.cancel_leave_game)) }, onClick = { menu = false; showLeave = true })
                                    }
                                    DropdownMenuItem(text = { Text(stringResource(Res.string.common_close)) }, onClick = { menu = false })
                                }
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (content != null && content.canPay) {
                br.com.sportconnect.feature.common.BottomActionBar {
                    SCButton(
                        stringResource(Res.string.home_pay_now),
                        { actions.pay(content.jogo.id) },
                        size = SCButtonSize.Large,
                        fullWidth = true,
                        modifier = Modifier.testTag("game_pay_now"),
                    )
                }
            }
        },
    ) { padding ->
        when (state) {
            GameDetailUiState.Loading -> GameDetailSkeleton(Modifier.padding(padding))
            GameDetailUiState.Error, GameDetailUiState.NotFound -> LoadErrorState(onRetry, Modifier.padding(padding))
            is GameDetailUiState.Content -> BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
                val wc = widthClassOf(maxWidth)
                val scroll = rememberScrollState()
                if (wc == WidthClass.Expanded && !celebrating) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    ) {
                        Column(Modifier.weight(7f).widthIn(max = 600.dp).verticalScroll(scroll).padding(bottom = 24.dp)) {
                            MainColumn(state, actions, onRemindAll, onShareInvite, { showRule = true })
                        }
                        Column(Modifier.weight(5f).verticalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 24.dp)) {
                            ParticipantsSection(state, onRemindOne, onShareInvite, onCancel = { showCancel = true }, onLeave = { showLeave = true })
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                        if (celebrating) {
                            CelebrationHeader(state, actions.back, onShare = {
                                share.whatsapp(it)
                            })
                        }
                        CenteredContent(Modifier.padding(horizontal = if (wc == WidthClass.Compact) 16.dp else 24.dp)) {
                            if (celebrating) {
                                CelebrationBody(state, share)
                            } else {
                                MainColumn(state, actions, onRemindAll, onShareInvite, { showRule = true })
                            }
                            Spacer(Modifier.size(24.dp))
                            ParticipantsSection(state, onRemindOne, onShareInvite, onCancel = { showCancel = true }, onLeave = { showLeave = true })
                            Spacer(Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }

    if (content != null && showCancel) {
        SCDialog(
            title = stringResource(Res.string.cancel_dialog_title, content.jogo.titulo),
            body = stringResource(Res.string.cancel_dialog_body),
            onDismissRequest = { showCancel = false },
            variant = SCDialogVariant.Destructive,
            confirm = SCAction(stringResource(Res.string.cancel_dialog_confirm), { showCancel = false; onCancel() }),
            dismiss = SCAction(stringResource(Res.string.cancel_dialog_keep), { showCancel = false }),
        )
    }
    if (content != null && showLeave) {
        val refund = stringResource(Res.string.cancel_leave_refund_value, (content.myPaidShare?.valor ?: content.jogo.valorCotaConvidado).format())
        SCDialog(
            title = stringResource(Res.string.cancel_leave_dialog_title),
            body = stringResource(Res.string.cancel_leave_dialog_body, refund),
            onDismissRequest = { showLeave = false },
            variant = SCDialogVariant.Destructive,
            confirm = SCAction(stringResource(Res.string.cancel_leave_confirm), { showLeave = false; onLeave() }),
            dismiss = SCAction(stringResource(Res.string.cancel_dialog_keep), { showLeave = false }),
        )
    }
    if (content != null && showRule) {
        val (title, body) = ruleTexts(content.jogo.politica)
        SCDialog(
            title = stringResource(title),
            body = stringResource(body),
            onDismissRequest = { showRule = false },
            variant = SCDialogVariant.Info,
            icon = Icons.Rounded.Info,
            confirm = SCAction(stringResource(Res.string.common_close), { showRule = false }),
        )
    }
}

fun ruleTexts(p: PoliticaNaoPagamento): Pair<StringResource, StringResource> = when (p) {
    PoliticaNaoPagamento.ORGANIZADOR_COBRE -> Res.string.game_create_rule_covers_title to Res.string.game_create_rule_covers_body
    PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR -> Res.string.game_create_rule_cancel_title to Res.string.game_create_rule_cancel_body
    PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES -> Res.string.game_create_rule_resplit_title to Res.string.game_create_rule_resplit_body
}

@Composable
private fun GameDetailSkeleton(modifier: Modifier) {
    Column(modifier.fillMaxWidth().padding(16.dp).semantics { contentDescription = "Carregando jogo" }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SCSkeleton(SCSkeletonShape.Box(height = 32.dp, widthFraction = 0.55f))
        SCSkeleton(SCSkeletonShape.Line(widthFraction = 0.6f, height = 28.dp))
        SCSkeleton(SCSkeletonShape.Line(widthFraction = 0.8f))
        SCSkeleton(SCSkeletonShape.Line(widthFraction = 0.7f))
        SCSkeleton(SCSkeletonShape.Box(height = 120.dp))
        repeat(4) { ParticipantRowSkeleton() }
    }
}

@Composable
private fun MetaLine(icon: ImageVector, text: String, trailing: (@Composable () -> Unit)? = null) {
    val c = SportConnectTheme.colors
    Row(Modifier.fillMaxWidth().heightIn(min = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
private fun ColumnScope.MainColumn(
    s: GameDetailUiState.Content,
    actions: GameDetailActions,
    onRemindAll: () -> Unit,
    onShareInvite: (String) -> Unit,
    onRuleInfo: () -> Unit,
) {
    val c = SportConnectTheme.colors
    val j = s.jogo
    val share = rememberShareActions()
    Spacer(Modifier.size(8.dp))
    // Cabecalho: badge Large -> esporte -> nome -> meta.
    SCStatusBadge(
        tone = j.status.tone(),
        size = SCBadgeSize.Large,
        label = stringResource(j.status.labelRes),
        modifier = Modifier.testTag("game_status_badge"),
    )
    Spacer(Modifier.size(12.dp))
    SCSportChip(j.esporte)
    Spacer(Modifier.size(8.dp))
    Text(j.titulo, style = MaterialTheme.typography.headlineSmall, color = c.onSurface, maxLines = 2, modifier = Modifier.scSharedBounds(sharedGameTitleKey(j.id)).semantics { heading() })
    Spacer(Modifier.size(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MetaLine(Icons.Rounded.CalendarMonth, PtBrFormat.dateTimeRange(j.inicio, j.fim))
        MetaLine(Icons.Rounded.Stadium, "${j.arenaNome} · ${j.quadraNome}") {
            SCButton(
                stringResource(Res.string.confirmed_directions),
                { share.openUrl(mapsUrl(j.arenaNome)) },
                variant = SCButtonVariant.Text,
                size = SCButtonSize.Small,
            )
        }
    }

    when (j.status) {
        StatusReserva.AGUARDANDO_PAGAMENTOS -> {
            if (s.resolving) {
                Spacer(Modifier.size(16.dp))
                NoticeCard(
                    stringResource(Res.string.game_resolution_banner),
                    trailing = {
                        SCButton(stringResource(Res.string.game_resolution_open), { actions.resolve(j.id) }, variant = SCButtonVariant.Text, size = SCButtonSize.Small)
                    },
                )
            } else {
                Spacer(Modifier.size(24.dp))
                DeadlineCard(s)
            }
        }
        StatusReserva.EXPIRADO -> {
            Spacer(Modifier.size(16.dp))
            NoticeCard(stringResource(Res.string.expiry_cancel_body) + " " + stringResource(Res.string.expiry_slot_released), container = c.surfaceContainerHigh, content = c.onSurfaceVariant)
        }
        StatusReserva.CANCELADO -> {
            Spacer(Modifier.size(16.dp))
            val reason = j.reserva.motivoCancelamento
            val text = if (reason != null) stringResource(Res.string.cancel_by_arena_body, j.titulo, reason) else stringResource(Res.string.cancel_done_title)
            NoticeCard(text, container = c.errorContainer, content = c.onErrorContainer)
        }
        else -> Unit
    }

    Spacer(Modifier.size(24.dp))
    SplitBlock(s, Modifier.testTag("game_split_progress"))

    // Acoes
    if (s.isOrganizer && s.collecting && s.pendingCount > 0) {
        Spacer(Modifier.size(24.dp))
        if (s.counts.paid <= 1) {
            SCButton(
                stringResource(Res.string.invite_share_whatsapp),
                { onShareInvite(j.id) },
                fullWidth = true,
                leadingIcon = Icons.Rounded.Share,
                modifier = Modifier.testTag("game_invite_whatsapp"),
            )
        } else {
            SCButton(
                stringResource(Res.string.split_remind_all),
                onRemindAll,
                fullWidth = true,
                leadingIcon = Icons.Rounded.NotificationsActive,
                modifier = Modifier.testTag("game_remind_all"),
            )
            // C13: um primario por tela. "Convidar mais gente" e secundario (antes Tonal neon, competia
            // com o primario) e so aparece quando o principal nao e o proprio convite.
            Spacer(Modifier.size(8.dp))
            SCButton(
                stringResource(Res.string.game_invite_more),
                { onShareInvite(j.id) },
                variant = SCButtonVariant.Secondary,
                fullWidth = true,
                leadingIcon = Icons.Rounded.PersonAdd,
                modifier = Modifier.testTag("game_invite_more"),
            )
        }
    }
    if (j.status == StatusReserva.CONFIRMADO) {
        Spacer(Modifier.size(24.dp))
        ConfirmedActions(s, share)
    }

    // Regra
    Spacer(Modifier.size(24.dp))
    val (ruleTitle, _) = ruleTexts(j.politica)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onRuleInfo).testTag("game_rule_info"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.split_rule_row, stringResource(ruleTitle)), style = MaterialTheme.typography.bodyLarge, color = c.onSurface, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.Info, null, tint = c.onSurfaceVariant, modifier = Modifier.padding(12.dp).size(20.dp))
    }
    HorizontalDivider(color = c.outlineVariant)

    // Seu pagamento (atleta)
    val mine = s.myPaidShare
    if (mine != null && !s.isOrganizer) {
        Spacer(Modifier.size(24.dp))
        SectionHeader(stringResource(Res.string.game_your_payment_title))
        Spacer(Modifier.size(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.game_your_payment_paid, mine.totalPago.format(), mine.pagaEm?.let { PtBrFormat.hour(it) } ?: ""),
                style = MaterialTheme.typography.bodyLarge,
                color = c.onSurface,
                modifier = Modifier.weight(1f),
            )
            mine.pagamentoId?.let { pid ->
                SCButton(stringResource(Res.string.pix_paid_receipt), { actions.receipt(pid) }, variant = SCButtonVariant.Text, size = SCButtonSize.Small)
            }
        }
    }
}

@Composable
private fun DeadlineCard(s: GameDetailUiState.Content) {
    val c = SportConnectTheme.colors
    val j = s.jogo
    val deadline = j.reserva.prazoCotas ?: return
    val clock = LocalSCClock.current
    val now by produceState(clock.now(), deadline) {
        while (true) {
            value = clock.now()
            delay(30_000)
        }
    }
    val remaining = deadline - now
    Column(
        Modifier.fillMaxWidth().background(c.surfaceContainerLow, MaterialTheme.shapes.large).padding(16.dp).testTag("game_countdown"),
    ) {
        if (remaining.isPositive()) {
            SCCountdownTimer(deadline, variant = SCCountdownVariant.Large, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.size(4.dp))
            Text(
                stringResource(Res.string.split_time_left, PtBrFormat.duration(remaining)) + " · até " + PtBrFormat.relative(deadline, now),
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Timer, null, tint = c.onSurfaceVariant)
                Text(stringResource(Res.string.countdown_processing), style = MaterialTheme.typography.titleMedium, color = c.onSurfaceVariant)
            }
        }
        if (s.isOrganizer && j.politica == PoliticaNaoPagamento.ORGANIZADOR_COBRE && j.valorPendente.cents > 0) {
            Spacer(Modifier.size(12.dp))
            HorizontalDivider(color = c.outlineVariant)
            Spacer(Modifier.size(12.dp))
            val value = j.valorPendente.format()
            val full = stringResource(Res.string.split_organizer_exposure, value)
            Text(
                buildAnnotatedString {
                    val i = full.indexOf(value)
                    if (i < 0) append(full) else {
                        append(full.substring(0, i))
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.onSurface)) { append(value) }
                        append(full.substring(i + value.length))
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
        }
    }
}

/** "8 de 12 cotas pagas" com numeros em numericScore (DS-PATCH do 10 §6.3) + valores + trilho. */
@Composable
private fun SplitBlock(s: GameDetailUiState.Content, modifier: Modifier = Modifier, showLegend: Boolean = true) {
    val j = s.jogo
    val counts = s.counts
    SCSplitProgress(
        totalShares = counts.total,
        paid = counts.paid,
        processing = counts.processing,
        covered = counts.covered,
        refunded = counts.refunded,
        collected = j.valorArrecadado,
        total = j.valorTotal,
        variant = SCSplitProgressVariant.Full,
        showLegend = showLegend,
        expired = j.status == StatusReserva.EXPIRADO,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun SplitHeadline(paid: Int, total: Int, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    val score = SportConnectTheme.typographyExt.numericScore
    val full = stringResource(Res.string.split_progress, paid, total)
    Text(
        buildAnnotatedString {
            // "8 de 12 cotas pagas": numeros em placar, resto titleMedium.
            val parts = full.split(" ")
            parts.forEachIndexed { i, p ->
                if (i > 0) append(" ")
                if (p == paid.toString() || p == total.toString()) {
                    withStyle(score.toSpanStyle()) { append(p) }
                } else append(p)
            }
        },
        style = MaterialTheme.typography.titleMedium,
        color = c.onSurface,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun ConfirmedActions(s: GameDetailUiState.Content, share: br.com.sportconnect.feature.common.ShareActions) {
    val j = s.jogo
    val calendarUrl = "https://calendar.google.com/calendar/render?action=TEMPLATE&text=" + urlEncode(j.titulo) +
        "&location=" + urlEncode("${j.arenaNome} · ${j.quadraNome}")
    val waText = stringResource(Res.string.wa_game_confirmed, "${j.arenaNome} · ${j.quadraNome}", PtBrFormat.dateTimeRange(j.inicio, j.fim), inviteUrl(j.codigoConvite))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SCButton(
            stringResource(Res.string.confirmed_add_calendar),
            { share.openUrl(calendarUrl) },
            fullWidth = true,
            leadingIcon = Icons.Rounded.EditCalendar,
            modifier = Modifier.testTag("celebration_add_calendar"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SCButton(
                stringResource(Res.string.confirmed_directions),
                { share.openUrl(mapsUrl(j.arenaNome)) },
                variant = SCButtonVariant.Secondary,
                leadingIcon = Icons.Rounded.Directions,
                modifier = Modifier.weight(1f).testTag("celebration_directions"),
                fullWidth = true,
            )
            SCButton(
                stringResource(Res.string.confirmed_share),
                { share.whatsapp(waText) },
                variant = SCButtonVariant.Text,
                leadingIcon = Icons.Rounded.Share,
                modifier = Modifier.testTag("celebration_share"),
            )
        }
    }
}

/** Faixa "Fechou!" (10 §3.1): primaryContainer, linhas de quadra a 16%, palavra de marca em displayLarge. */
@Composable
private fun CelebrationHeader(s: GameDetailUiState.Content, onBack: () -> Unit, onShare: (String) -> Unit) {
    val c = SportConnectTheme.colors
    val j = s.jogo
    val body = if (j.reserva.motivoConfirmacao == MotivoConfirmacao.COBERTURA) {
        stringResource(Res.string.confirmed_covered_body, j.reserva.cotas.filter { it.origem == br.com.sportconnect.core.model.OrigemPagamento.COBERTURA }.sumOf { it.valor.cents }.let { br.com.sportconnect.core.model.Money(it).format() })
    } else {
        stringResource(Res.string.confirmed_body, PtBrFormat.dateTime(j.inicio))
    }
    val shareText = stringResource(Res.string.wa_game_confirmed, "${j.arenaNome} · ${j.quadraNome}", PtBrFormat.dateTimeRange(j.inicio, j.fim), inviteUrl(j.codigoConvite))
    val reduced = SportConnectTheme.reducedMotion
    var visible by remember { mutableStateOf(reduced) }
    LaunchedEffect(Unit) { visible = true }
    Box(Modifier.fillMaxWidth().heightIn(min = 280.dp).background(c.primaryContainer).testTag("celebration_banner")) {
        CourtLinesBackdrop(c.onPrimaryContainer.copy(alpha = 0.16f), Modifier.matchParentSize(), lineYFraction = 0.4f)
        Column(Modifier.fillMaxWidth().padding(bottom = 56.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = c.onPrimaryContainer) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onShare(shareText) }) { Icon(Icons.Rounded.Share, stringResource(Res.string.cd_share_game), tint = c.onPrimaryContainer) }
            }
            CenteredContent(Modifier.padding(horizontal = 16.dp)) {
                AnimatedVisibility(visible, enter = fadeIn() + slideInVertically { it / 4 }) {
                    Column(Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive }) {
                        Text(
                            stringResource(Res.string.confirmed_brand_word),
                            style = MaterialTheme.typography.displayLarge,
                            color = c.onPrimaryContainer,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(stringResource(Res.string.confirmed_title), style = MaterialTheme.typography.headlineMedium, color = c.onPrimaryContainer)
                        Spacer(Modifier.size(8.dp))
                        Text(body, style = MaterialTheme.typography.bodyLarge, color = c.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.CelebrationBody(s: GameDetailUiState.Content, share: br.com.sportconnect.feature.common.ShareActions) {
    val c = SportConnectTheme.colors
    val j = s.jogo
    // Cartao do rateio sobrepoe a faixa em 24dp (profundidade sem sombra).
    Column(
        Modifier.offset(y = (-40).dp).fillMaxWidth()
            .background(c.surfaceContainerLowest, MaterialTheme.shapes.large)
            .border(1.dp, c.outlineVariant, MaterialTheme.shapes.large)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SCStatusBadge(j.status.tone(), size = SCBadgeSize.Large, label = stringResource(j.status.labelRes), modifier = Modifier.testTag("game_status_badge"))
            Spacer(Modifier.weight(1f))
            SCSportChip(j.esporte)
        }
        Spacer(Modifier.size(12.dp))
        SplitBlock(s, showLegend = false)
    }
    Column(Modifier.offset(y = (-24).dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MetaLine(Icons.Rounded.Stadium, "${j.arenaNome} · ${j.quadraNome}")
        MetaLine(Icons.Rounded.CalendarMonth, PtBrFormat.dateTimeRange(j.inicio, j.fim))
        Spacer(Modifier.size(16.dp))
        ConfirmedActions(s, share)
    }
}

@Composable
private fun ParticipantsSection(
    s: GameDetailUiState.Content,
    onRemindOne: (ParticipantUi) -> Unit,
    onShareInvite: (String) -> Unit,
    onCancel: () -> Unit,
    onLeave: () -> Unit,
) {
    val c = SportConnectTheme.colors
    val j = s.jogo
    val roleOrg = stringResource(Res.string.split_role_organizer)
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(Res.string.game_participants_title))
        Spacer(Modifier.size(8.dp))
        val named = s.participants.filter { !it.openSpot }
        val open = s.participants.filter { it.openSpot }
        named.forEach { p ->
            val paidStatus = if (p.state == SCShareState.Paid && p.paidAt != null) stringResource(Res.string.split_status_paid_at, PtBrFormat.hour(p.paidAt)) else null
            val canRemind = s.isOrganizer && s.collecting && p.state == SCShareState.Pending && !p.isSelf
            SCParticipantRow(
                name = p.name,
                shareState = p.state,
                avatar = AvatarUi(p.name, paid = p.state == SCShareState.Paid),
                role = if (p.isOrganizer) roleOrg else null,
                isSelf = p.isSelf,
                statusLabel = paidStatus ?: stringResource(p.state.labelRes),
                action = if (canRemind) {
                    SCAction(
                        stringResource(Res.string.split_remind_one),
                        { onRemindOne(p) },
                        accessibilityLabel = stringResource(Res.string.cd_remind_person, p.name),
                    )
                } else null,
                modifier = Modifier.testTag("game_participant_${p.userId ?: p.key}"),
            )
        }
        if (s.isOrganizer && s.collecting && named.size <= 1) {
            SCEmptyState(
                title = stringResource(Res.string.empty_paid_list_title),
                description = stringResource(Res.string.empty_paid_list_body),
                primaryAction = SCAction(stringResource(Res.string.empty_paid_list_cta), { onShareInvite(j.id) }),
                modifier = Modifier.padding(top = 0.dp),
            )
        }
        val showOpen = open.take(if (open.size > 4) 3 else open.size)
        showOpen.forEachIndexed { i, _ ->
            SCOpenSpotRow(
                action = if (s.isOrganizer && s.collecting) SCAction(stringResource(Res.string.invite_share_whatsapp), { onShareInvite(j.id) }) else null,
                modifier = Modifier.testTag("game_open_spot_$i"),
            )
        }
        if (open.size > showOpen.size) {
            Text(
                "+ ${open.size - showOpen.size} vagas abertas",
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        Spacer(Modifier.size(16.dp))
        if (s.isOrganizer && (j.status == StatusReserva.AGUARDANDO_PAGAMENTOS || j.status == StatusReserva.CONFIRMADO)) {
            if (s.cancelAllowed) {
                CancelTextButton(stringResource(Res.string.cancel_game_cta), onCancel, "game_cancel")
            } else {
                Text(
                    stringResource(Res.string.cancel_outside_window, "${s.cancelWindowHours}h"),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant,
                )
            }
            if (s.collecting) {
                Text(stringResource(Res.string.cancel_organizer_cannot_leave), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
        }
        if (s.canLeave) CancelTextButton(stringResource(Res.string.cancel_leave_game), onLeave, "game_leave")
    }
}

@Composable
private fun CancelTextButton(text: String, onClick: () -> Unit, tag: String) {
    val c = SportConnectTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = c.error,
        modifier = Modifier.heightIn(min = 48.dp).clickable(onClick = onClick).padding(vertical = 14.dp, horizontal = 0.dp).testTag(tag),
    )
}

