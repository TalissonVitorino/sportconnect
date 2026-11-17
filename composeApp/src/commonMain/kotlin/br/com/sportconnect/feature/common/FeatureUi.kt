package br.com.sportconnect.feature.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCAction
import br.com.sportconnect.core.designsystem.component.SCEmptyState
import br.com.sportconnect.core.designsystem.component.SCIllustration
import br.com.sportconnect.core.designsystem.theme.SCBreakpoints
import br.com.sportconnect.core.designsystem.theme.SCReservationTone
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.common_retry
import br.com.sportconnect.resources.error_server_body
import br.com.sportconnect.resources.error_server_title
import br.com.sportconnect.resources.game_status_awaiting
import br.com.sportconnect.resources.game_status_cancelled
import br.com.sportconnect.resources.game_status_confirmed
import br.com.sportconnect.resources.game_status_done
import br.com.sportconnect.resources.game_status_expired
import br.com.sportconnect.resources.game_status_in_progress
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Largura de coluna unica em medium/expanded (10 §1.1). */
val ContentMaxWidth: Dp = 640.dp

/** Classe de largura simplificada (05 §9) calculada pela largura disponivel. */
enum class WidthClass { Compact, Medium, Expanded }

fun widthClassOf(width: Dp): WidthClass = when {
    width < SCBreakpoints.medium -> WidthClass.Compact
    width < SCBreakpoints.expanded -> WidthClass.Medium
    else -> WidthClass.Expanded
}

/** Mapeia o estado da reserva para o tom do DS. */
fun StatusReserva.tone(): SCReservationTone = when (this) {
    StatusReserva.RASCUNHO, StatusReserva.AGUARDANDO_PAGAMENTOS -> SCReservationTone.AwaitingPayment
    StatusReserva.CONFIRMADO, StatusReserva.EM_ANDAMENTO -> SCReservationTone.Confirmed
    StatusReserva.CONCLUIDO -> SCReservationTone.Completed
    StatusReserva.CANCELADO -> SCReservationTone.Cancelled
    StatusReserva.EXPIRADO -> SCReservationTone.Expired
}

/** Rotulo do badge Large / push / e-mail (I11). */
val StatusReserva.labelRes: StringResource
    get() = when (this) {
        StatusReserva.RASCUNHO, StatusReserva.AGUARDANDO_PAGAMENTOS -> Res.string.game_status_awaiting
        StatusReserva.CONFIRMADO -> Res.string.game_status_confirmed
        StatusReserva.EM_ANDAMENTO -> Res.string.game_status_in_progress
        StatusReserva.CONCLUIDO -> Res.string.game_status_done
        StatusReserva.CANCELADO -> Res.string.game_status_cancelled
        StatusReserva.EXPIRADO -> Res.string.game_status_expired
    }

/** Cabecalho de secao: titleSmall onSurfaceVariant, caixa de sentenca, heading() (I17). */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, color: Color = SportConnectTheme.colors.onSurfaceVariant) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = modifier.semantics { heading() },
    )
}

/**
 * Coluna centralizada com largura maxima (640dp por padrao) — compact ocupa tudo.
 */
@Composable
fun CenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = ContentMaxWidth,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = maxWidth).fillMaxWidth(), content = content)
    }
}

/**
 * CTA fixo inferior (10 §1.1): surface, borda superior 1dp outlineVariant, padding 16 (24 medium+),
 * respeita a barra de navegacao do sistema.
 */
@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = SportConnectTheme.colors
    Surface(color = c.surface, modifier = modifier.fillMaxWidth()) {
        Column {
            HorizontalDivider(color = c.outlineVariant, thickness = 1.dp)
            BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                val pad = if (maxWidth < SCBreakpoints.medium) 16.dp else 24.dp
                Column(
                    Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = pad, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content,
                )
            }
        }
    }
}

/** Erro de carregamento padrao (10 §1.3). */
@Composable
fun LoadErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    SCEmptyState(
        title = stringResource(Res.string.error_server_title),
        description = stringResource(Res.string.error_server_body),
        illustration = SCIllustration.Error,
        primaryAction = SCAction(stringResource(Res.string.common_retry), onRetry),
        modifier = modifier,
    )
}

/** Cartao de aviso tonal (warningContainer / errorContainer / tertiaryContainer) com icone. */
@Composable
fun NoticeCard(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = SportConnectTheme.extendedColors.warningContainer,
    content: Color = SportConnectTheme.extendedColors.onWarningContainer,
    icon: ImageVector = Icons.Rounded.Info,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().background(container, SCShape.control).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, tint = content, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = content, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun VSpace(height: Dp) = Spacer(Modifier.height(height))

/** Ponto unico de compartilhamento: copiar (ClipboardManager) e WhatsApp (`wa.me`, 09 §6.5). */
class ShareActions(
    val copy: (String) -> Unit,
    val whatsapp: (String) -> Unit,
    val openUrl: (String) -> Unit,
)

@Suppress("DEPRECATION")
@Composable
fun rememberShareActions(): ShareActions {
    val clipboard = LocalClipboardManager.current
    val uri = LocalUriHandler.current
    return remember(clipboard, uri) {
        ShareActions(
            copy = { clipboard.setText(AnnotatedString(it)) },
            whatsapp = { text -> runCatching { uri.openUri("https://wa.me/?text=" + urlEncode(text)) } },
            openUrl = { url -> runCatching { uri.openUri(url) } },
        )
    }
}

/** Percent-encoding UTF-8 (RFC 3986), sem APIs so-JVM. */
fun urlEncode(text: String): String = buildString {
    for (b in text.encodeToByteArray()) {
        val ch = b.toInt() and 0xFF
        val c = ch.toChar()
        if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '-' || c == '_' || c == '.' || c == '~') append(c)
        else {
            append('%')
            append("0123456789ABCDEF"[ch shr 4])
            append("0123456789ABCDEF"[ch and 0x0F])
        }
    }
}

/** Link publico do convite (09 §2.5). */
fun inviteUrl(code: String): String = "https://sportconnect.app/j/$code"

/** Link do mapa para "Como chegar". */
fun mapsUrl(query: String): String = "https://www.google.com/maps/search/?api=1&query=" + urlEncode(query)

@Composable
fun FullscreenCentered(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
