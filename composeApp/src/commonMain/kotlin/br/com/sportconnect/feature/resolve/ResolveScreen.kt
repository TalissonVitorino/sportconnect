@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.resolve

import br.com.sportconnect.feature.common.LocalOnline
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.designsystem.component.AvatarUi
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCAvatarStack
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCButtonVariant
import br.com.sportconnect.core.designsystem.component.SCCountdownTimer
import br.com.sportconnect.core.designsystem.component.SCCountdownVariant
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCPixPaymentContent
import br.com.sportconnect.core.designsystem.component.SCPixState
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.PtBrFormat
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.TipoCota
import br.com.sportconnect.feature.common.CenteredContent
import br.com.sportconnect.feature.common.rememberShareActions
import br.com.sportconnect.navigation.Route
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cancel_dialog_keep
import br.com.sportconnect.resources.confirmed_title
import br.com.sportconnect.resources.error_resplit_expired_body
import br.com.sportconnect.resources.error_resplit_expired_title
import br.com.sportconnect.resources.expiry_covers_pay_cta
import br.com.sportconnect.resources.game_create_deadline_value
import br.com.sportconnect.resources.expiry_resplit_accept
import br.com.sportconnect.resources.expiry_resplit_body
import br.com.sportconnect.resources.expiry_resplit_decline
import br.com.sportconnect.resources.expiry_resplit_title
import br.com.sportconnect.resources.expiry_title
import br.com.sportconnect.resources.pix_see_game
import br.com.sportconnect.resources.refund_pix_eta
import br.com.sportconnect.resources.resolve_accepted_count
import br.com.sportconnect.resources.resolve_covers_consequence
import br.com.sportconnect.resources.split_organizer_exposure
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ResolveRoute(route: Route.GameResolve, onClose: () -> Unit, onSeeGame: (String) -> Unit) {
    val vm: ResolveViewModel = koinViewModel(key = "resolve-${route.id}") { parametersOf(route) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    ResolveScreen(state, onClose, onSeeGame, vm::payCover, vm::accept, vm::decline)
}

/** Decisao no fim do prazo (10 §2.11): A-Pix organizador, C pagante, C organizador. */
@Composable
fun ResolveScreen(
    state: ResolveUiState,
    onClose: () -> Unit = {},
    onSeeGame: (String) -> Unit = {},
    onCover: () -> Unit = {},
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val t = MaterialTheme.typography
    val share = rememberShareActions()
    var confirmDecline by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = c.surface,
        topBar = { SCTopBar("", navigation = SCTopBarNavigation.Close, onNavigationClick = onClose) },
    ) { padding ->
        val j = state.jogo
        if (state.loading || j == null) {
            Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SCSkeleton(SCSkeletonShape.Line(0.7f, 32.dp))
                SCSkeleton(SCSkeletonShape.Box(height = 160.dp))
            }
            return@Scaffold
        }
        if (state.paid) {
            SCEmptyState(
                title = stringResource(Res.string.confirmed_title),
                description = PtBrFormat.dateTimeRange(j.inicio, j.fim),
                primaryAction = SCAction(stringResource(Res.string.pix_see_game), { onSeeGame(j.id) }),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        if (state.declined) {
            SCEmptyState(
                title = stringResource(Res.string.expiry_resplit_decline),
                description = stringResource(Res.string.refund_pix_eta),
                primaryAction = SCAction(stringResource(Res.string.pix_see_game), { onSeeGame(j.id) }),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        if (state.mode == ResolveMode.None) {
            SCEmptyState(
                title = stringResource(Res.string.error_resplit_expired_title),
                description = stringResource(Res.string.error_resplit_expired_body),
                primaryAction = SCAction(stringResource(Res.string.pix_see_game), { onSeeGame(j.id) }),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        val hold = j.reserva.prazoHold
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            CenteredContent(Modifier.padding(16.dp), maxWidth = 560.dp) {
                val complement = state.complement
                val title = when (state.mode) {
                    ResolveMode.ResplitPayer -> stringResource(Res.string.expiry_resplit_title, complement?.valor?.format() ?: "")
                    else -> stringResource(Res.string.expiry_title)
                }
                Text(title, style = t.headlineMedium, color = c.onSurface, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier.fillMaxWidth().background(ext.warningContainer, MaterialTheme.shapes.large).padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    when (state.mode) {
                        ResolveMode.ResplitPayer -> {
                            val original = j.reserva.cotas.firstOrNull { it.id == complement?.cotaOriginalId }?.valor
                            val totalMine = (original ?: j.valorCotaConvidado) + (complement?.valor ?: j.valorCotaConvidado)
                            Text(
                                stringResource(Res.string.expiry_resplit_body, state.missingShares, totalMine.format(), hold?.let { PtBrFormat.relative(it, state.now) } ?: ""),
                                style = t.bodyLarge,
                                color = ext.onWarningContainer,
                            )
                        }
                        else -> Text(
                            stringResource(Res.string.split_organizer_exposure, state.missingValue.format()),
                            style = t.bodyLarge,
                            color = ext.onWarningContainer,
                        )
                    }
                    if (hold != null) {
                        Spacer(Modifier.height(12.dp))
                        SCCountdownTimer(hold, variant = SCCountdownVariant.Large)
                        Text(stringResource(Res.string.game_create_deadline_value, PtBrFormat.relative(hold, state.now)), style = t.bodyMedium, color = ext.onWarningContainer)
                    }
                }
                Spacer(Modifier.height(24.dp))
                val charge = state.charge
                if (charge != null) {
                    SCPixPaymentContent(
                        amount = charge.total,
                        pixCopyPaste = charge.pixCopiaECola.orEmpty(),
                        expiresAt = charge.expiraEm,
                        state = SCPixState.Waiting,
                        onCopy = { charge.pixCopiaECola?.let(share.copy) },
                        onRegenerate = { if (state.mode == ResolveMode.Cover) onCover() else onAccept() },
                        onPayWithCard = {},
                        requestInitialFocus = false,
                    )
                    return@CenteredContent
                }
                when (state.mode) {
                    ResolveMode.Cover -> {
                        hold?.let {
                            Text(stringResource(Res.string.resolve_covers_consequence, PtBrFormat.relative(it, state.now)), style = t.bodyMedium, color = c.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(16.dp))
                        SCButton(
                            stringResource(Res.string.expiry_covers_pay_cta, state.missingValue.format()),
                            onCover,
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            loading = state.paying,
                            enabled = LocalOnline.current,
                            modifier = Modifier.testTag("resolve_pay_cover"),
                        )
                    }
                    ResolveMode.ResplitPayer -> {
                        SCButton(
                            stringResource(Res.string.expiry_resplit_accept, complement?.valor?.format() ?: ""),
                            onAccept,
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            loading = state.paying,
                            modifier = Modifier.testTag("resolve_accept"),
                        )
                        Spacer(Modifier.height(8.dp))
                        SCButton(
                            stringResource(Res.string.expiry_resplit_decline),
                            { confirmDecline = true },
                            variant = SCButtonVariant.Secondary,
                            size = SCButtonSize.Large,
                            fullWidth = true,
                            modifier = Modifier.testTag("resolve_decline"),
                        )
                        Spacer(Modifier.height(16.dp))
                        AcceptedRow(state)
                    }
                    ResolveMode.ResplitOrganizer -> AcceptedRow(state)
                    ResolveMode.None -> Unit
                }
                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, style = t.bodyMedium, color = c.error)
                }
            }
        }
    }
    if (confirmDecline) {
        SCDialog(
            title = stringResource(Res.string.expiry_resplit_decline),
            body = stringResource(Res.string.refund_pix_eta),
            variant = SCDialogVariant.Destructive,
            onDismissRequest = { confirmDecline = false },
            confirm = SCAction(stringResource(Res.string.expiry_resplit_decline), { confirmDecline = false; onDecline() }),
            dismiss = SCAction(stringResource(Res.string.cancel_dialog_keep), { confirmDecline = false }),
        )
    }
}

@Composable
private fun AcceptedRow(state: ResolveUiState) {
    val c = SportConnectTheme.colors
    val j = state.jogo ?: return
    val accepted = j.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO && it.status == StatusCota.PAGA }
    Text(stringResource(Res.string.resolve_accepted_count, state.accepted, state.payers), style = MaterialTheme.typography.titleMedium, color = c.onSurface)
    Spacer(Modifier.height(8.dp))
    SCAvatarStack(accepted.map { AvatarUi(it.atletaNome ?: "?", paid = true) })
}
