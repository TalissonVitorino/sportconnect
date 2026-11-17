package br.com.sportconnect.feature.arenaadmin.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.ParticipantRowSkeleton
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCDialogContent
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCSkeleton
import br.com.sportconnect.core.designsystem.component.SCSkeletonShape
import br.com.sportconnect.core.designsystem.component.SCSnackbarHost
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.component.SCTopBarNavigation
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.feature.arenaadmin.ArenaAdminNavigator
import br.com.sportconnect.feature.arenaadmin.common.ArenaErrorState
import br.com.sportconnect.feature.arenaadmin.common.ArenaSectionTitle
import br.com.sportconnect.feature.arenaadmin.common.BookingDetailContent
import br.com.sportconnect.feature.arenaadmin.common.ChoiceChips
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_action_failed
import br.com.sportconnect.resources.aadm_booking_cancelled
import br.com.sportconnect.resources.aadm_booking_not_found_body
import br.com.sportconnect.resources.aadm_booking_not_found_title
import br.com.sportconnect.resources.aadm_booking_released
import br.com.sportconnect.resources.arena_booking_cancel_body
import br.com.sportconnect.resources.arena_booking_cancel_confirm
import br.com.sportconnect.resources.arena_booking_cancel_reason
import br.com.sportconnect.resources.arena_booking_cancel_title
import br.com.sportconnect.resources.arena_booking_keep
import br.com.sportconnect.resources.arena_booking_reason_force
import br.com.sportconnect.resources.arena_booking_reason_maintenance
import br.com.sportconnect.resources.arena_booking_reason_other
import br.com.sportconnect.resources.arena_booking_reason_weather
import br.com.sportconnect.resources.arena_booking_title
import br.com.sportconnect.resources.common_back
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

val CancelReason.labelRes: StringResource
    get() = when (this) {
        CancelReason.Weather -> Res.string.arena_booking_reason_weather
        CancelReason.Maintenance -> Res.string.arena_booking_reason_maintenance
        CancelReason.Force -> Res.string.arena_booking_reason_force
        CancelReason.Other -> Res.string.arena_booking_reason_other
    }

/** `arena-admin/booking/{id}` — Detalhe da reserva (10 §2.19). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaBookingScreen(
    bookingId: String,
    nav: ArenaAdminNavigator,
    viewModel: ArenaBookingViewModel = koinViewModel(key = "booking-$bookingId") { parametersOf(bookingId) },
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val uri = LocalUriHandler.current
    LaunchedEffect(state.message) {
        val m = state.message ?: return@LaunchedEffect
        viewModel.consumeMessage()
        when (m) {
            BookingMessage.Cancelled -> snackbar.showSC(getString(Res.string.aadm_booking_cancelled), SCSnackbarKind.Success)
            BookingMessage.Released -> snackbar.showSC(getString(Res.string.aadm_booking_released), SCSnackbarKind.Success)
            is BookingMessage.Failed -> snackbar.showSC(m.text ?: getString(Res.string.aadm_action_failed), SCSnackbarKind.Error)
        }
    }
    if (state.cancelDialog) {
        CancelBookingDialog(state, viewModel, inline = false)
    }
    Scaffold(
        topBar = {
            SCTopBar(
                title = stringResource(Res.string.arena_booking_title),
                navigation = SCTopBarNavigation.Back,
                onNavigationClick = nav::back,
            )
        },
        snackbarHost = { SCSnackbarHost(snackbar) },
        containerColor = SportConnectTheme.colors.surface,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val content = Modifier.widthIn(max = 640.dp).fillMaxWidth()
            when (state.load) {
                LoadState.Loading -> Column(content.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SCSkeleton(SCSkeletonShape.Box(32.dp, widthFraction = 0.5f))
                    SCSkeleton(SCSkeletonShape.Line(0.7f, 24.dp))
                    repeat(4) { ParticipantRowSkeleton() }
                }
                LoadState.Error -> ArenaErrorState(viewModel::retry, content)
                LoadState.NoArena -> SCEmptyState(
                    stringResource(Res.string.aadm_booking_not_found_title),
                    stringResource(Res.string.aadm_booking_not_found_body),
                    content.testTag("booking_not_found"),
                    illustration = SCIllustration.NoResults,
                    primaryAction = SCAction(stringResource(Res.string.common_back), nav::back),
                )
                LoadState.Ready -> state.detail?.let { d ->
                    BookingDetailContent(
                        d,
                        modifier = content.verticalScroll(rememberScrollState()).padding(16.dp).padding(bottom = 24.dp),
                        showFinancials = state.showFinancials,
                        onCancel = viewModel::openCancel,
                        onRelease = viewModel::release,
                        onContact = { d.organizerPhone?.let { p -> runCatching { uri.openUri("tel:$p") } } },
                    )
                }
            }
        }
    }
}

/**
 * Confirmacao destrutiva (SCDialog.Destructive): motivo obrigatorio (`arena_booking_reason_*`),
 * "Manter reserva" com foco inicial e "Cancelar reserva" desabilitado ate escolher o motivo.
 * [inline] = renderiza so o conteudo (screenshots).
 */
@Composable
fun CancelBookingDialog(state: ArenaBookingUiState, viewModel: ArenaBookingViewModel, inline: Boolean) {
    val reasonLabel = state.reason?.let { stringResource(it.labelRes) }
    val title = stringResource(Res.string.arena_booking_cancel_title)
    val body = stringResource(Res.string.arena_booking_cancel_body)
    val confirm = SCAction(
        stringResource(Res.string.arena_booking_cancel_confirm),
        { reasonLabel?.let(viewModel::confirmCancel) },
        enabled = state.reason != null,
        loading = state.cancelling,
    )
    val dismiss = SCAction(stringResource(Res.string.arena_booking_keep), viewModel::dismissCancel)
    val content: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ArenaSectionTitle(stringResource(Res.string.arena_booking_cancel_reason))
            ChoiceChips(CancelReason.entries, state.reason, { stringResource(it.labelRes) }, viewModel::selectReason, tag = { "booking_cancel_reason_${it.id}" })
        }
    }
    if (inline) {
        SCDialogContent(title, confirm, Modifier.testTag("booking_cancel_dialog"), dismiss, variant = SCDialogVariant.Destructive, body = body, requestInitialFocus = false, content = content)
    } else {
        SCDialog(title, viewModel::dismissCancel, confirm, Modifier.testTag("booking_cancel_dialog"), dismiss, variant = SCDialogVariant.Destructive, body = body, content = content)
    }
}
