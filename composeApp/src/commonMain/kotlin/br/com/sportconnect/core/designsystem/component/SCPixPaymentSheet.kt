package br.com.sportconnect.core.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_qr_code
import br.com.sportconnect.resources.ds_pix_copied
import br.com.sportconnect.resources.ds_pix_expired
import br.com.sportconnect.resources.ds_pix_expires_label
import br.com.sportconnect.resources.ds_pix_paid
import br.com.sportconnect.resources.ds_pix_pay_with_card
import br.com.sportconnect.resources.ds_pix_processing
import br.com.sportconnect.resources.ds_pix_steps
import br.com.sportconnect.resources.ds_pix_waiting
import br.com.sportconnect.resources.ds_show_less
import br.com.sportconnect.resources.ds_show_more
import br.com.sportconnect.resources.common_retry
import br.com.sportconnect.resources.pix_copy_code
import br.com.sportconnect.resources.pix_new_code
import br.com.sportconnect.resources.pix_title
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Estado do pagamento Pix (06 §5.11). */
@Stable
sealed interface SCPixState {
    data object Waiting : SCPixState
    data object Processing : SCPixState
    data object Paid : SCPixState
    data object Expired : SCPixState
    data class Error(val message: String) : SCPixState
}

/**
 * Bottom sheet modal do Pix (06 §5.11). O conteudo esta em [SCPixPaymentContent] (reutilizavel
 * em tela cheia/Desktop e no catalogo).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun SCPixPaymentSheet(
    amount: Money,
    pixCopyPaste: String,
    expiresAt: Instant,
    state: SCPixState,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onPayWithCard: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    qrBitmap: ImageBitmap? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SCShape.sheet,
        containerColor = SportConnectTheme.colors.surfaceContainerLow,
        scrimColor = SportConnectTheme.colors.scrim.copy(alpha = 0.32f),
        modifier = modifier,
    ) {
        SCPixPaymentContent(
            amount = amount,
            pixCopyPaste = pixCopyPaste,
            expiresAt = expiresAt,
            state = state,
            onCopy = onCopy,
            onRegenerate = onRegenerate,
            onPayWithCard = onPayWithCard,
            qrBitmap = qrBitmap,
            modifier = Modifier.verticalScroll(rememberScrollState()),
        )
    }
}

/**
 * Conteudo do Pix: titulo, valor, contador, QR 200dp em fundo BRANCO fixo (qualquer tema),
 * "Copiar codigo Pix" (primeiro foco), copia-e-cola truncado/expansivel, passos e status ao vivo.
 * [qrBitmap] = QR real gerado pelo backend/lib; se null, desenha [SCQrPlaceholder] deterministico.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SCPixPaymentContent(
    amount: Money,
    pixCopyPaste: String,
    expiresAt: Instant,
    state: SCPixState,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onPayWithCard: () -> Unit,
    modifier: Modifier = Modifier,
    qrBitmap: ImageBitmap? = null,
    requestInitialFocus: Boolean = true,
) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val t = MaterialTheme.typography
    var copied by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(copied) { if (copied) { delay(2000); copied = false } }
    if (requestInitialFocus) {
        LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    }
    val expired = state == SCPixState.Expired

    Column(
        modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(Res.string.pix_title), style = t.titleLarge, color = c.onSurface, modifier = Modifier.semantics { heading() })
        Text(
            amount.formatBrl(),
            style = t.displaySmall.copy(fontFeatureSettings = "tnum"),
            color = c.onSurface,
            modifier = Modifier.semantics { contentDescription = spokenBrl(amount.cents) },
        )
        if (state == SCPixState.Waiting || state == SCPixState.Processing) {
            SCCountdownTimer(expiresAt, variant = SCCountdownVariant.Chip, label = stringResource(Res.string.ds_pix_expires_label))
        }

        val qrCd = stringResource(Res.string.cd_qr_code, spokenBrl(amount.cents))
        Box(
            Modifier
                .background(ext.qrContainer, SCShape.small)
                .padding(16.dp)
                .semantics { contentDescription = qrCd },
            contentAlignment = Alignment.Center,
        ) {
            val qrMod = Modifier.size(200.dp).alpha(if (expired) 0.12f else 1f)
            if (qrBitmap != null) {
                Image(qrBitmap, contentDescription = null, modifier = qrMod)
            } else {
                SCQrPlaceholder(pixCopyPaste, qrMod)
            }
            if (expired) {
                Text(stringResource(Res.string.ds_pix_expired), style = t.titleMedium, color = ext.qrModule)
            }
        }

        when (state) {
            SCPixState.Expired -> SCButton(
                stringResource(Res.string.pix_new_code), onRegenerate,
                size = SCButtonSize.Large, leadingIcon = Icons.Rounded.Refresh, fullWidth = true,
                modifier = Modifier.focusRequester(focus),
            )
            is SCPixState.Error -> SCButton(
                stringResource(Res.string.common_retry), onRegenerate,
                size = SCButtonSize.Large, leadingIcon = Icons.Rounded.Refresh, fullWidth = true,
                modifier = Modifier.focusRequester(focus),
            )
            else -> SCButton(
                text = if (copied) stringResource(Res.string.ds_pix_copied) else stringResource(Res.string.pix_copy_code),
                onClick = { copied = true; onCopy() },
                size = SCButtonSize.Large,
                leadingIcon = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                fullWidth = true,
                enabled = state != SCPixState.Paid,
                modifier = Modifier.focusRequester(focus),
            )
        }

        Text(
            pixCopyPaste,
            style = SportConnectTheme.typographyExt.pixCode,
            color = c.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().animateContentSize()
                .background(c.surfaceContainerHighest.copy(alpha = 0.6f), SCShape.chip)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
        Text(
            stringResource(if (expanded) Res.string.ds_show_less else Res.string.ds_show_more),
            style = t.labelLarge,
            color = c.primary,
            modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button) { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 14.dp),
        )
        Text(stringResource(Res.string.ds_pix_steps), style = t.bodySmall, color = c.onSurfaceVariant, textAlign = TextAlign.Center)

        PixStatusLine(state)

        if (state == SCPixState.Waiting || state is SCPixState.Error) {
            SCButton(stringResource(Res.string.ds_pix_pay_with_card), onPayWithCard, variant = SCButtonVariant.Text)
        }
    }
}

@Composable
private fun PixStatusLine(state: SCPixState) {
    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val t = MaterialTheme.typography
    val assertive = state == SCPixState.Paid || state == SCPixState.Expired
    Row(
        Modifier.semantics { liveRegion = if (assertive) LiveRegionMode.Assertive else LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (state) {
            SCPixState.Waiting -> {
                CircularProgressIndicator(Modifier.size(16.dp), color = c.onSurfaceVariant, strokeWidth = 2.dp)
                Text(stringResource(Res.string.ds_pix_waiting), style = t.bodyMedium, color = c.onSurfaceVariant)
            }
            SCPixState.Processing -> {
                CircularProgressIndicator(Modifier.size(16.dp), color = c.primary, strokeWidth = 2.dp)
                Text(stringResource(Res.string.ds_pix_processing), style = t.bodyMedium, color = c.onSurface)
            }
            SCPixState.Paid -> {
                Icon(Icons.Rounded.CheckCircle, null, tint = ext.success, modifier = Modifier.size(24.dp))
                Text(stringResource(Res.string.ds_pix_paid), style = t.titleMedium, color = ext.success)
            }
            SCPixState.Expired -> {
                Icon(Icons.Rounded.ErrorOutline, null, tint = c.error, modifier = Modifier.size(20.dp))
                Text(stringResource(Res.string.ds_pix_expired), style = t.bodyMedium, color = c.error)
            }
            is SCPixState.Error -> {
                Icon(Icons.Rounded.ErrorOutline, null, tint = c.error, modifier = Modifier.size(20.dp))
                Text(state.message, style = t.bodyMedium, color = c.error)
            }
        }
    }
}

/**
 * QR "placeholder" desenhado em Canvas a partir de um padrao DETERMINISTICO do [payload]
 * (25x25 modulos, 3 padroes de localizacao, linhas de timing, dados de um PRNG semeado pelo hash).
 * Visualmente igual a um QR, mas NAO e decodificavel — trocar por bitmap real gerado no backend.
 */
@Composable
fun SCQrPlaceholder(payload: String, modifier: Modifier = Modifier) {
    val module = SportConnectTheme.extendedColors.qrModule
    val quiet = SportConnectTheme.extendedColors.qrContainer
    val matrix = remember(payload) { qrMatrix(payload, 25) }
    Canvas(modifier) {
        val n = matrix.size
        val cell = size.minDimension / n
        for (y in 0 until n) for (x in 0 until n) {
            if (matrix[y][x]) drawRect(module, Offset(x * cell, y * cell), Size(cell + 0.5f, cell + 0.5f))
        }
        // suaviza os finders com cantos arredondados (estetica; mantem contraste)
        val r = CornerRadius(cell * 1.2f)
        listOf(0 to 0, n - 7 to 0, 0 to n - 7).forEach { (fx, fy) ->
            drawRoundRect(module, Offset(fx * cell, fy * cell), Size(7 * cell, 7 * cell), r)
            drawRoundRect(quiet, Offset((fx + 1) * cell, (fy + 1) * cell), Size(5 * cell, 5 * cell), CornerRadius(cell * 0.8f))
            drawRoundRect(module, Offset((fx + 2) * cell, (fy + 2) * cell), Size(3 * cell, 3 * cell), CornerRadius(cell * 0.6f))
        }
    }
}

internal fun qrMatrix(payload: String, n: Int): Array<BooleanArray> {
    val m = Array(n) { BooleanArray(n) }
    val reserved = Array(n) { BooleanArray(n) }
    fun finder(ox: Int, oy: Int) {
        for (y in -1..7) for (x in -1..7) {
            val px = ox + x
            val py = oy + y
            if (px !in 0 until n || py !in 0 until n) continue
            reserved[py][px] = true
            val inRing = x in 0..6 && y in 0..6 && (x == 0 || x == 6 || y == 0 || y == 6)
            val inCore = x in 2..4 && y in 2..4
            m[py][px] = inRing || inCore
        }
    }
    finder(0, 0); finder(n - 7, 0); finder(0, n - 7)
    for (i in 8 until n - 8) {
        m[6][i] = i % 2 == 0; reserved[6][i] = true
        m[i][6] = i % 2 == 0; reserved[i][6] = true
    }
    // alinhamento (QR versao 2)
    val a = n - 7
    for (y in -2..2) for (x in -2..2) {
        reserved[a + y][a + x] = true
        m[a + y][a + x] = maxOf(kotlin.math.abs(x), kotlin.math.abs(y)) != 1
    }
    // PRNG xorshift semeado por FNV-1a do payload
    var seed = 0x811C9DC5L
    payload.forEach { ch -> seed = ((seed xor ch.code.toLong()) * 0x01000193L) and 0xFFFFFFFFL }
    var s = if (seed == 0L) 0x9E3779B9L else seed
    fun next(): Long { s = s xor (s shl 13) and 0xFFFFFFFFL; s = s xor (s ushr 17); s = s xor (s shl 5) and 0xFFFFFFFFL; return s }
    for (y in 0 until n) for (x in 0 until n) {
        if (!reserved[y][x]) m[y][x] = next() % 100 < 47
    }
    return m
}
