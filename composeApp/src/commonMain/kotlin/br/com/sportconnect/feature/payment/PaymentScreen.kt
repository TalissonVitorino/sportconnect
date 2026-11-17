@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.PriceLine
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCountdownTimer
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCPixPaymentContent
import br.com.sportconnect.core.designsystem.component.SCPixState
import br.com.sportconnect.core.designsystem.component.SCPriceBreakdown
import br.com.sportconnect.core.designsystem.component.SCPriceEmphasis
import br.com.sportconnect.core.designsystem.component.SCQrPlaceholder
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCSplitProgress
import br.com.sportconnect.core.designsystem.component.SCSplitProgressVariant
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.feature.common.BottomActionBar
import br.com.sportconnect.feature.common.CenteredContent
import br.com.sportconnect.feature.common.NoticeCard
import br.com.sportconnect.feature.common.WidthClass
import br.com.sportconnect.feature.common.rememberShareActions
import br.com.sportconnect.feature.common.splitCounts
import br.com.sportconnect.feature.common.widthClassOf
import br.com.sportconnect.navigation.Route
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.card_pay_cta
import br.com.sportconnect.resources.card_processing
import br.com.sportconnect.resources.cd_pix_code_expand
import br.com.sportconnect.resources.common_convenience_fee
import br.com.sportconnect.resources.error_already_paid_body
import br.com.sportconnect.resources.error_already_paid_title
import br.com.sportconnect.resources.error_game_full_body
import br.com.sportconnect.resources.error_game_full_title
import br.com.sportconnect.resources.error_generic_body
import br.com.sportconnect.resources.error_generic_title
import br.com.sportconnect.resources.error_pix_expired_body
import br.com.sportconnect.resources.error_pix_expired_title
import br.com.sportconnect.resources.invite_share_whatsapp
import br.com.sportconnect.resources.payment_card_fee_hint
import br.com.sportconnect.resources.payment_close_body
import br.com.sportconnect.resources.payment_close_confirm
import br.com.sportconnect.resources.payment_close_keep
import br.com.sportconnect.resources.payment_close_title
import br.com.sportconnect.resources.payment_generating_pix
import br.com.sportconnect.resources.payment_method_card
import br.com.sportconnect.resources.payment_method_pix
import br.com.sportconnect.resources.payment_method_pix_hint
import br.com.sportconnect.resources.payment_method_title
import br.com.sportconnect.resources.payment_no_fee_note
import br.com.sportconnect.resources.payment_pix_cta
import br.com.sportconnect.resources.payment_spot_held
import br.com.sportconnect.resources.payment_summary_share
import br.com.sportconnect.resources.payment_summary_total
import br.com.sportconnect.resources.payment_title
import br.com.sportconnect.resources.pix_code_copied
import br.com.sportconnect.resources.pix_code_copied_button
import br.com.sportconnect.resources.pix_copy_code
import br.com.sportconnect.resources.pix_expires_in
import br.com.sportconnect.resources.pix_new_code
import br.com.sportconnect.resources.pix_paid_body
import br.com.sportconnect.resources.pix_paid_receipt
import br.com.sportconnect.resources.pix_paid_title
import br.com.sportconnect.resources.pix_pay_with_card
import br.com.sportconnect.resources.pix_processing
import br.com.sportconnect.resources.pix_receiver
import br.com.sportconnect.resources.pix_see_game
import br.com.sportconnect.resources.pix_show_qr
import br.com.sportconnect.resources.pix_step_1
import br.com.sportconnect.resources.pix_step_2
import br.com.sportconnect.resources.pix_step_3
import br.com.sportconnect.resources.pix_title
import br.com.sportconnect.resources.pix_waiting_body
import br.com.sportconnect.resources.pix_waiting_check
import br.com.sportconnect.resources.pix_waiting_title
import br.com.sportconnect.resources.split_progress
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Instant

data class PaymentActions(
    val close: () -> Unit = {},
    val seeGame: (String) -> Unit = {},
    /** Depois de pagar a cota do organizador: segue para `game/{id}/invite`. */
    val invite: (String) -> Unit = {},
    val receipt: (String) -> Unit = {},
)

@Composable
fun PaymentRoute(route: Route.GamePay, actions: PaymentActions) {
    val vm: PaymentViewModel = koinViewModel(key = "pay-${route.id}") { parametersOf(route) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    PaymentScreen(
        state = state,
        actions = actions,
        onSelectMethod = vm::selectMethod,
        onPay = vm::pay,
        onCopied = vm::onCopied,
        onCheckNow = vm::checkNow,
        onRegenerate = vm::regenerate,
        onPayWithCard = vm::payWithCard,
    )
}

/** Pagamento (10 §2.10, redline §4.2). Compact: resumo + "sheet" do Pix; expanded: 2 colunas com QR primario. */
@Composable
fun PaymentScreen(
    state: PaymentUiState,
    actions: PaymentActions = PaymentActions(),
    onSelectMethod: (MetodoPagamento) -> Unit = {},
    onPay: () -> Unit = {},
    onCopied: () -> Unit = {},
    onCheckNow: () -> Unit = {},
    onRegenerate: () -> Unit = {},
    onPayWithCard: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val snackbar = remember { SnackbarHostState() }
    val share = rememberShareActions()
    var confirmClose by remember { mutableStateOf(false) }
    val waiting = state.phase is PixPhase.Waiting || state.phase is PixPhase.Processing || state.phase is PixPhase.Expired
    val close = { if (state.phase is PixPhase.Waiting) confirmClose = true else actions.close() }
    LaunchedEffect(state.copied) {
        if (state.copied) snackbar.showSC(getString(Res.string.pix_code_copied), SCSnackbarKind.Success)
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wc = widthClassOf(maxWidth)
        Scaffold(
            containerColor = c.surface,
            snackbarHost = { SCSnackbarHost(snackbar) },
            topBar = {
                SCTopBar(stringResource(Res.string.payment_title), navigation = SCTopBarNavigation.Close, onNavigationClick = close)
            },
            bottomBar = {
                if (!state.paid && !(waiting && wc != WidthClass.Expanded) && state.phase !is PixPhase.Error) {
                    BottomActionBar {
                        val label = if (state.method == MetodoPagamento.PIX) {
                            stringResource(Res.string.payment_pix_cta, state.total.format())
                        } else {
                            stringResource(Res.string.card_pay_cta, state.total.format())
                        }
                        SCButton(
                            if (state.phase is PixPhase.Generating) stringResource(Res.string.payment_generating_pix) else label,
                            onPay,
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            loading = state.phase is PixPhase.Generating,
                            enabled = !state.loading && !waiting,
                            modifier = Modifier.testTag("pay_pix_cta"),
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when {
                    state.paid -> PaidContent(state, actions)
                    state.phase is PixPhase.Error -> ErrorContent(state.phase, state, actions, onRegenerate)
                    wc == WidthClass.Expanded && waiting -> Row(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                    ) {
                        Column(Modifier.widthIn(max = 456.dp).weight(1f)) { Summary(state, onSelectMethod, enabled = false) }
                        Column(
                            Modifier.widthIn(max = 456.dp).weight(1f).fillMaxHeight()
                                .background(c.surfaceContainerLow, MaterialTheme.shapes.extraLarge)
                                .verticalScroll(rememberScrollState()).padding(top = 20.dp),
                        ) {
                            // Expanded: QR como primario (09 ficha 10) — conteudo do DS.
                            val cob = state.cobranca
                            if (cob != null) {
                                SCPixPaymentContent(
                                    amount = cob.total,
                                    pixCopyPaste = cob.pixCopiaECola.orEmpty(),
                                    expiresAt = cob.expiraEm,
                                    state = state.phase.toSc(),
                                    onCopy = { cob.pixCopiaECola?.let(share.copy); onCopied() },
                                    onRegenerate = onRegenerate,
                                    onPayWithCard = onPayWithCard,
                                    requestInitialFocus = false,
                                )
                            }
                        }
                    }
                    else -> {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            CenteredContent(Modifier.padding(horizontal = if (wc == WidthClass.Compact) 0.dp else 24.dp)) {
                                Summary(state, onSelectMethod, enabled = !waiting)
                            }
                        }
                        if (waiting) {
                            // "Sheet" do Pix sobre o resumo (scrim 32%, topo radius.xxl, alca 32x4).
                            Box(Modifier.fillMaxSize().background(c.scrim.copy(alpha = 0.32f)))
                            Column(
                                Modifier.align(Alignment.BottomCenter).widthIn(max = 560.dp).fillMaxWidth()
                                    .fillMaxHeight(0.94f)
                                    .background(c.surfaceContainerLow, SCShape.sheet)
                                    .verticalScroll(rememberScrollState())
                                    .testTag("pix_sheet"),
                            ) {
                                Box(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp), contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(32.dp, 4.dp).background(c.outlineVariant, CircleShape))
                                }
                                PixSheetContent(state, share, onCopied, onCheckNow, onRegenerate, onPayWithCard)
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClose) {
        SCDialog(
            title = stringResource(Res.string.payment_close_title),
            body = stringResource(Res.string.payment_close_body),
            onDismissRequest = { confirmClose = false },
            confirm = SCAction(stringResource(Res.string.payment_close_keep), { confirmClose = false }),
            dismiss = SCAction(stringResource(Res.string.payment_close_confirm), { confirmClose = false; actions.close() }),
        )
    }
}

private val PaymentUiState.cobranca
    get() = when (val p = phase) {
        is PixPhase.Waiting -> p.cobranca
        is PixPhase.Processing -> p.cobranca
        is PixPhase.Expired -> p.cobranca
        is PixPhase.Paid -> p.cobranca
        else -> null
    }

private fun PixPhase.toSc(): SCPixState = when (this) {
    is PixPhase.Processing -> SCPixState.Processing
    is PixPhase.Paid -> SCPixState.Paid
    is PixPhase.Expired -> SCPixState.Expired
    is PixPhase.Error -> SCPixState.Error(message)
    else -> SCPixState.Waiting
}

@Composable
private fun Summary(state: PaymentUiState, onSelectMethod: (MetodoPagamento) -> Unit, enabled: Boolean) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column {
        // Faixa da trava (RN-19).
        state.lockUntil?.let { lock ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 40.dp).background(c.surfaceContainer).padding(horizontal = 16.dp).testTag("pay_spot_held"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.LockClock, null, tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                LiveDuration(lock) { d -> Text(stringResource(Res.string.payment_spot_held, d), style = t.bodyMedium, color = c.onSurface) }
            }
        }
        Column(Modifier.padding(16.dp)) {
            state.jogo?.let { j ->
                Text(j.titulo, style = t.titleMedium, color = c.onSurface)
                Text("${PtBrFormat.dateTimeRange(j.inicio, j.fim)} · ${j.arenaNome}", style = t.bodyMedium, color = c.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
            }
            val lines = buildList {
                add(PriceLine(stringResource(Res.string.payment_summary_share), state.shareValue))
                if (!state.noFee) add(PriceLine(stringResource(Res.string.common_convenience_fee), state.fee, emphasis = SCPriceEmphasis.Muted))
            }
            SCPriceBreakdown(
                lines = lines,
                total = PriceLine(stringResource(Res.string.payment_summary_total), state.total),
                footnote = if (state.noFee) stringResource(Res.string.payment_no_fee_note) else null,
                modifier = Modifier.fillMaxWidth()
                    .background(c.surfaceContainerLowest, MaterialTheme.shapes.large)
                    .border(1.dp, c.outlineVariant, MaterialTheme.shapes.large)
                    .padding(16.dp)
                    .testTag("pay_breakdown"),
            )
            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.payment_method_title), style = t.titleMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(8.dp))
            Column(Modifier.selectableGroup()) {
                MethodRow(
                    selected = state.method == MetodoPagamento.PIX,
                    title = stringResource(Res.string.payment_method_pix),
                    hint = stringResource(Res.string.payment_method_pix_hint),
                    icon = Icons.Rounded.QrCode2,
                    enabled = enabled,
                    tag = "pay_method_pix",
                ) { onSelectMethod(MetodoPagamento.PIX) }
                MethodRow(
                    selected = state.method == MetodoPagamento.CARTAO,
                    title = stringResource(Res.string.payment_method_card),
                    hint = if (state.noFee) null else stringResource(Res.string.payment_card_fee_hint, TaxaConveniencia.CARTAO.format()),
                    icon = Icons.Rounded.CreditCard,
                    enabled = enabled,
                    tag = "pay_method_card",
                ) { onSelectMethod(MetodoPagamento.CARTAO) }
            }
        }
    }
}

@Composable
private fun MethodRow(
    selected: Boolean,
    title: String,
    hint: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    tag: String,
    onClick: () -> Unit,
) {
    val c = SportConnectTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled, colors = RadioButtonDefaults.colors(selectedColor = c.primary))
        Spacer(Modifier.width(16.dp))
        Icon(icon, null, tint = c.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = c.onSurface)
            if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
        }
    }
}

/** Texto que se atualiza com a duracao restante no formato do 07 ("9min", "45s"). */
@Composable
private fun LiveDuration(deadline: Instant, content: @Composable (String) -> Unit) {
    val clock = LocalSCClock.current
    var text by remember(deadline) { mutableStateOf(PtBrFormat.duration(deadline - clock.now())) }
    LaunchedEffect(deadline) {
        while (true) {
            text = PtBrFormat.duration(deadline - clock.now())
            delay(1_000)
        }
    }
    content(text)
}

/** Conteudo do sheet do Pix no compact (redline §4.2): copiar e o primario; QR sob demanda. */
@Composable
private fun PixSheetContent(
    state: PaymentUiState,
    share: br.com.sportconnect.feature.common.ShareActions,
    onCopied: () -> Unit,
    onCheckNow: () -> Unit,
    onRegenerate: () -> Unit,
    onPayWithCard: () -> Unit,
) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val t = MaterialTheme.typography
    val cob = state.cobranca ?: return
    val code = cob.pixCopiaECola.orEmpty()
    var expanded by remember { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    val expired = state.phase is PixPhase.Expired
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
        Text(stringResource(Res.string.pix_title), style = t.titleLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(4.dp))
        Text(cob.total.format(), style = t.displaySmall, color = c.onSurface)
        Spacer(Modifier.height(4.dp))
        if (!expired) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.LockClock, null, tint = c.onSurfaceVariant, modifier = Modifier.size(16.dp))
                LiveDuration(cob.expiraEm) { d -> Text(stringResource(Res.string.pix_expires_in, d), style = t.bodyMedium, color = c.onSurfaceVariant) }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (expired) {
            NoticeCard(
                stringResource(Res.string.error_pix_expired_title) + ". " + stringResource(Res.string.error_pix_expired_body),
                container = c.errorContainer,
                content = c.onErrorContainer,
            )
            Spacer(Modifier.height(12.dp))
            SCButton(stringResource(Res.string.pix_new_code), onRegenerate, size = SCButtonSize.Large, fullWidth = true, modifier = Modifier.testTag("pix_new_code"))
            return@Column
        }
        SCButton(
            if (state.copied) stringResource(Res.string.pix_code_copied_button) else stringResource(Res.string.pix_copy_code),
            { share.copy(code); onCopied() },
            size = SCButtonSize.Large,
            fullWidth = true,
            leadingIcon = if (state.copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
            modifier = Modifier.testTag("pix_copy_button"),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth()
                .background(c.surfaceContainerLowest, MaterialTheme.shapes.medium)
                .border(1.dp, c.outlineVariant, MaterialTheme.shapes.medium)
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                code,
                style = SportConnectTheme.typographyExt.pixCode,
                color = c.onSurface,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp).testTag("pix_code_text"),
            )
            IconButton(onClick = { expanded = !expanded }) {
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, stringResource(Res.string.cd_pix_code_expand))
            }
        }
        Spacer(Modifier.height(20.dp))
        // Passos numerados (lista ordenada real).
        listOf(Res.string.pix_step_1, Res.string.pix_step_2, Res.string.pix_step_3).forEachIndexed { i, res ->
            Row(Modifier.heightIn(min = 32.dp).padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).background(c.surfaceContainerHighest, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${i + 1}", style = t.labelMedium, color = c.onSurface)
                }
                Spacer(Modifier.width(12.dp))
                Text(stringResource(res), style = t.bodyMedium, color = c.onSurface)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.VerifiedUser, null, tint = c.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(Res.string.pix_receiver, "SportConnect · pagamento intermediado pelo gateway"), style = t.bodySmall, color = c.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        SCButton(
            stringResource(Res.string.pix_show_qr),
            { showQr = !showQr },
            variant = SCButtonVariant.Secondary,
            leadingIcon = Icons.Rounded.QrCode2,
            fullWidth = true,
            modifier = Modifier.testTag("pix_show_qr"),
        )
        AnimatedVisibility(showQr) {
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.background(ext.qrContainer, MaterialTheme.shapes.large).padding(16.dp).testTag("pix_qr_image")) {
                    SCQrPlaceholder(code, Modifier.size(200.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        // Status ao vivo.
        Column(
            Modifier.fillMaxWidth().background(c.surfaceContainer, MaterialTheme.shapes.large).padding(16.dp)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .testTag("pix_waiting_status"),
        ) {
            val processing = state.phase is PixPhase.Processing
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), color = c.primary, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        processing && state.method == MetodoPagamento.CARTAO -> stringResource(Res.string.card_processing)
                        processing -> stringResource(Res.string.pix_processing)
                        else -> stringResource(Res.string.pix_waiting_title)
                    },
                    style = t.titleSmall,
                    color = c.onSurface,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(stringResource(Res.string.pix_waiting_body), style = t.bodyMedium, color = c.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SCButton(stringResource(Res.string.pix_waiting_check), onCheckNow, variant = SCButtonVariant.Secondary)
            SCButton(stringResource(Res.string.pix_pay_with_card), onPayWithCard, variant = SCButtonVariant.Text)
        }
    }
}

@Composable
private fun PaidContent(state: PaymentUiState, actions: PaymentActions) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val t = MaterialTheme.typography
    val reduced = SportConnectTheme.reducedMotion
    var shown by remember { mutableStateOf(reduced) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(if (shown) 1f else 0.8f)
    val j = state.jogo
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CenteredContent(Modifier.padding(24.dp)) {
            Column(
                Modifier.fillMaxWidth().semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(32.dp))
                Icon(Icons.Rounded.CheckCircle, null, tint = ext.success, modifier = Modifier.size(64.dp).scale(scale))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.pix_paid_title), style = t.headlineLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(8.dp))
                Text(stringResource(Res.string.pix_paid_body, state.shareValue.format()), style = t.bodyLarge, color = c.onSurfaceVariant)
            }
            if (j != null) {
                Spacer(Modifier.height(24.dp))
                // O SCSplitProgress ja traz o rotulo "x de y cotas pagas" (antes aparecia duplicado).
                val counts = j.splitCounts(LocalSCClock.current.now())
                SCSplitProgress(counts.total, counts.paid, processing = counts.processing, covered = counts.covered, variant = SCSplitProgressVariant.Compact)
            }
            Spacer(Modifier.height(32.dp))
            val id = j?.id
            if (id != null) {
                if (state.afterCreate) {
                    SCButton(stringResource(Res.string.invite_share_whatsapp), { actions.invite(id) }, size = SCButtonSize.Large, fullWidth = true, modifier = Modifier.testTag("pay_success_see_game"))
                } else {
                    SCButton(stringResource(Res.string.pix_see_game), { actions.seeGame(id) }, size = SCButtonSize.Large, fullWidth = true, modifier = Modifier.testTag("pay_success_see_game"))
                }
            }
            (state.phase as? PixPhase.Paid)?.cobranca?.pagamentoId?.let { pid ->
                SCButton(
                    stringResource(Res.string.pix_paid_receipt),
                    { actions.receipt(pid) },
                    variant = SCButtonVariant.Text,
                    fullWidth = true,
                    modifier = Modifier.testTag("pay_success_receipt"),
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(phase: PixPhase.Error, state: PaymentUiState, actions: PaymentActions, onRetry: () -> Unit) {
    val (title, body) = when (phase.code) {
        "JA_PAGOU" -> Res.string.error_already_paid_title to Res.string.error_already_paid_body
        "SEM_VAGAS" -> Res.string.error_game_full_title to Res.string.error_game_full_body
        else -> Res.string.error_generic_title to Res.string.error_generic_body
    }
    val bodyText = if (phase.code == "JA_PAGOU" || phase.code == "SEM_VAGAS" || phase.message.isBlank()) stringResource(body) else phase.message
    br.com.sportconnect.core.designsystem.component.SCEmptyState(
        title = stringResource(title),
        description = bodyText,
        illustration = br.com.sportconnect.core.designsystem.component.SCIllustration.Error,
        primaryAction = state.jogo?.id?.let { id -> SCAction(stringResource(Res.string.pix_see_game), { actions.seeGame(id) }) },
        secondaryAction = if (phase.code == "JA_PAGOU" || phase.code == "SEM_VAGAS") null else SCAction(stringResource(Res.string.pix_new_code), onRetry),
        modifier = Modifier.testTag(if (phase.code == "JA_PAGOU") "pix_already_paid" else "pay_error"),
    )
}
