package br.com.sportconnect.feature.arenaadmin.common

import br.com.sportconnect.core.designsystem.theme.SCSelection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCButton
import br.com.sportconnect.core.designsystem.component.SCButtonSize
import br.com.sportconnect.core.designsystem.component.SCDialog
import br.com.sportconnect.core.designsystem.component.SCDialogVariant
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.component.SCSnackbarKind
import br.com.sportconnect.core.designsystem.component.showSC
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.aadm_action_failed
import br.com.sportconnect.resources.aadm_block_court_label
import br.com.sportconnect.resources.aadm_block_no_free
import br.com.sportconnect.resources.aadm_block_time_label
import br.com.sportconnect.resources.aadm_no_arena_body
import br.com.sportconnect.resources.aadm_no_arena_cta
import br.com.sportconnect.resources.aadm_no_arena_title
import br.com.sportconnect.resources.aadm_unblocked_snackbar
import br.com.sportconnect.resources.arena_block_reason_class
import br.com.sportconnect.resources.arena_block_reason_maintenance
import br.com.sportconnect.resources.arena_block_reason_other
import br.com.sportconnect.resources.arena_block_reason_phone
import br.com.sportconnect.resources.arena_blocked_snackbar
import br.com.sportconnect.resources.arena_booking_cancel_cta
import br.com.sportconnect.resources.arena_schedule_block_cta
import br.com.sportconnect.resources.arena_schedule_block_reason
import br.com.sportconnect.resources.common_close
import br.com.sportconnect.resources.common_retry
import br.com.sportconnect.resources.common_undo
import br.com.sportconnect.resources.error_block_active_booking_body
import br.com.sportconnect.resources.error_block_active_booking_title
import br.com.sportconnect.resources.error_server_body
import br.com.sportconnect.resources.error_server_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/** Titulo de secao `titleSmall` com heading() (leitores de tela navegam por cabecalho). */
@Composable
fun ArenaSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = SportConnectTheme.colors.onSurfaceVariant,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
fun ArenaErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    SCEmptyState(
        title = stringResource(Res.string.error_server_title),
        description = stringResource(Res.string.error_server_body),
        illustration = SCIllustration.Error,
        primaryAction = SCAction(stringResource(Res.string.common_retry), onRetry),
        modifier = modifier.testTag("arena_error"),
    )
}

@Composable
fun ArenaNoArenaState(onOnboarding: () -> Unit, modifier: Modifier = Modifier) {
    SCEmptyState(
        title = stringResource(Res.string.aadm_no_arena_title),
        description = stringResource(Res.string.aadm_no_arena_body),
        illustration = SCIllustration.Court,
        primaryAction = SCAction(stringResource(Res.string.aadm_no_arena_cta), onOnboarding),
        modifier = modifier.testTag("arena_no_arena"),
    )
}

val BlockReason.labelRes: StringResource
    get() = when (this) {
        BlockReason.Phone -> Res.string.arena_block_reason_phone
        BlockReason.Maintenance -> Res.string.arena_block_reason_maintenance
        BlockReason.Class -> Res.string.arena_block_reason_class
        BlockReason.Other -> Res.string.arena_block_reason_other
    }

/** Grupo de chips de escolha unica (radio semantico). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    tag: (T) -> String? = { null },
) {
    FlowRow(
        modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { opt ->
            val isSel = opt == selected
            FilterChip(
                selected = isSel,
                onClick = { onSelect(opt) },
                label = { Text(label(opt), style = MaterialTheme.typography.labelLarge) },
                leadingIcon = if (isSel) ({ Icon(Icons.Rounded.Check, null, Modifier.padding(0.dp)) }) else null,
                shape = SCShape.chip,
                colors = SCSelection.filterChipColors(),
                modifier = Modifier
                    .then(tag(opt)?.let { Modifier.testTag(it) } ?: Modifier)
                    .semantics { role = Role.RadioButton },
            )
        }
    }
}

/** Conteudo do sheet "Bloquear horario" (quadra, horario e motivo obrigatorio). */
@Composable
fun BlockSheetContent(
    sheet: BlockSheetUi,
    onCourt: (String) -> Unit,
    onSlot: (String) -> Unit,
    onReason: (BlockReason) -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = MaterialTheme.typography
    val c = SportConnectTheme.colors
    val reasonLabel = sheet.reason?.let { stringResource(it.labelRes) }
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.arena_schedule_block_cta), style = t.titleLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
        ArenaSectionTitle(stringResource(Res.string.aadm_block_court_label))
        ChoiceChips(sheet.courts.map { it.first }, sheet.courtId, { id -> sheet.courts.first { it.first == id }.second }, onCourt, tag = { "arena_block_court_$it" })
        ArenaSectionTitle(stringResource(Res.string.aadm_block_time_label))
        if (sheet.slots.isEmpty()) {
            Text(stringResource(Res.string.aadm_block_no_free), style = t.bodyMedium, color = c.onSurfaceVariant)
        } else {
            ChoiceChips(sheet.slots.map { it.slotId }, sheet.slotId, { id -> sheet.slots.first { it.slotId == id }.label }, onSlot, tag = { "arena_block_slot_$it" })
        }
        ArenaSectionTitle(stringResource(Res.string.arena_schedule_block_reason))
        ChoiceChips(BlockReason.entries, sheet.reason, { stringResource(it.labelRes) }, onReason, tag = { "arena_block_reason_${it.id}" })
        SCButton(
            text = stringResource(Res.string.arena_schedule_block_cta),
            onClick = { reasonLabel?.let(onConfirm) },
            size = SCButtonSize.Large,
            leadingIcon = Icons.Rounded.Block,
            enabled = sheet.canConfirm,
            loading = sheet.submitting,
            fullWidth = true,
            modifier = Modifier.testTag("arena_block_confirm"),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockSheet(
    sheet: BlockSheetUi?,
    onDismiss: () -> Unit,
    onCourt: (String) -> Unit,
    onSlot: (String) -> Unit,
    onReason: (BlockReason) -> Unit,
    onConfirm: (String) -> Unit,
) {
    if (sheet == null) return
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        shape = SCShape.sheet,
        containerColor = SportConnectTheme.colors.surfaceContainerLow,
    ) {
        BlockSheetContent(sheet, onCourt, onSlot, onReason, onConfirm)
    }
}

/**
 * Trata [ArenaMessage]: snackbar "Horario bloqueado" + Desfazer (10s), erro generico, e o dialogo
 * `error_block_active_booking_*` com acao para abrir a reserva.
 */
@Composable
fun ArenaMessageHandler(
    message: ArenaMessage?,
    snackbar: SnackbarHostState,
    onConsumed: () -> Unit,
    onUndoBlock: (String) -> Unit,
    onOpenBooking: (String) -> Unit,
) {
    var dialogFor by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(message) {
        when (message) {
            null -> Unit
            is ArenaMessage.Blocked -> {
                onConsumed()
                val r = snackbar.showSC(getString(Res.string.arena_blocked_snackbar), SCSnackbarKind.Undo, getString(Res.string.common_undo))
                if (r == SnackbarResult.ActionPerformed) onUndoBlock(message.slotId)
            }
            ArenaMessage.Unblocked -> {
                onConsumed()
                snackbar.showSC(getString(Res.string.aadm_unblocked_snackbar), SCSnackbarKind.Success)
            }
            ArenaMessage.Failure -> {
                onConsumed()
                snackbar.showSC(getString(Res.string.aadm_action_failed), SCSnackbarKind.Error)
            }
            is ArenaMessage.BlockHasBooking -> {
                dialogFor = message.slotId
                onConsumed()
            }
        }
    }
    dialogFor?.let { slotId ->
        SCDialog(
            title = stringResource(Res.string.error_block_active_booking_title),
            body = stringResource(Res.string.error_block_active_booking_body),
            onDismissRequest = { dialogFor = null },
            variant = SCDialogVariant.Info,
            confirm = SCAction(stringResource(Res.string.arena_booking_cancel_cta), {
                dialogFor = null
                onOpenBooking(slotId)
            }),
            dismiss = SCAction(stringResource(Res.string.common_close), { dialogFor = null }),
        )
    }
}

/** Linha chave-valor simples (resumo do dia). */
@Composable
fun ArenaKeyValue(text: String, modifier: Modifier = Modifier) {
    Row(modifier) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = SportConnectTheme.colors.onSurface)
    }
}
