package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Stadium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCReservationTone
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SCShareState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Sport
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class SCGameCardVariant { Organizer, Participant, Open }

/** Dados de exibicao do card de jogo. Datas chegam formatadas ("Sáb, 23/11 · 19h–20h"). */
@OptIn(ExperimentalTime::class)
@Immutable
data class GameCardUi(
    val sport: Sport,
    val title: String,
    val arenaName: String,
    val courtName: String,
    val dateTimeLabel: String,
    val status: SCReservationTone,
    val paidCount: Int,
    val totalShares: Int,
    val shareValue: Money,
    val participants: List<AvatarUi> = emptyList(),
    val holdExpiresAt: Instant? = null,
    val coveredCount: Int = 0,
    val processingCount: Int = 0,
    /** Copy contextual do selo (ex.: `home_card_status_pending`); null = rotulo padrao do estado. */
    val statusLabel: String? = null,
    /** Variante Participant: estado da minha cota. */
    val myShare: SCShareState? = null,
    /** Rotulos de rodape ("Sua cota", "3 vagas"...) vindos da feature. */
    val shareCaption: String? = null,
    val countdownLabel: String? = null,
    /** Tag de papel no canto superior ("Você organiza", `home_role_organizer`, I17). */
    val roleTag: String? = null,
)

/**
 * Card de jogo (06 §5.5). Faixa lateral 4dp no acento do esporte + chip (nunca o fundo inteiro).
 * Card inteiro clicavel e mesclado para leitura; [primaryAction] e alvo separado e focavel.
 * Estado passado (`Completed`): conteudo secundario a 72%.
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SCGameCard(
    game: GameCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SCGameCardVariant = SCGameCardVariant.Participant,
    primaryAction: SCAction? = null,
    /** Chave do titulo para a transicao compartilhada com o detalhe ([sharedGameTitleKey]). */
    sharedTitleKey: String? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val sc = SportConnectTheme.extendedColors.sport(game.sport)
    val interaction = remember { MutableInteractionSource() }
    val past = game.status == SCReservationTone.Completed || game.status == SCReservationTone.Expired ||
        game.status == SCReservationTone.Cancelled
    val secondaryAlpha = if (past) 0.72f else 1f

    Surface(
        onClick = onClick,
        modifier = modifier.scFocusRing(interaction, SCShape.card),
        shape = SCShape.card,
        color = c.surfaceContainerLowest,
        border = BorderStroke(1.dp, c.outlineVariant),
        interactionSource = interaction,
    ) {
        val stripe = sc.accent
        Row(Modifier.drawBehind { drawRect(stripe, size = Size(4.dp.toPx(), size.height)) }) {
            Column(Modifier.weight(1f).padding(start = 18.dp, top = 14.dp, end = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // C4: esporte (icone + faixa lateral), status e papel na mesma linha quando cabem. FlowRow:
                // o selo nunca e espremido; se "Voce organiza" nao couber, desce para a linha de baixo.
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    SCSportChip(game.sport, variant = SCSportChipVariant.Compact)
                    if (game.statusLabel != null) SCStatusBadge(game.status, label = game.statusLabel) else SCStatusBadge(game.status)
                    if (game.roleTag != null) {
                        Text(
                            game.roleTag,
                            style = t.labelMedium,
                            color = c.onSecondaryContainer,
                            maxLines = 1,
                            modifier = Modifier.background(c.secondaryContainer, SCShape.badge).padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
                Text(game.title, style = t.titleMedium, color = c.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.scSharedBounds(sharedTitleKey))
                Column(Modifier.alpha(secondaryAlpha), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoLine(Icons.Rounded.Stadium, "${game.arenaName} · ${game.courtName}")
                    InfoLine(Icons.Rounded.Schedule, game.dateTimeLabel)
                }
                if (variant != SCGameCardVariant.Open) {
                    SCSplitProgress(
                        totalShares = game.totalShares,
                        paid = game.paidCount,
                        covered = game.coveredCount,
                        processing = game.processingCount,
                        variant = SCSplitProgressVariant.Compact,
                        expired = game.status == SCReservationTone.Expired,
                        // O selo contextual ja traz "8 de 12 pagas": nao repete o rotulo da barra.
                        showLabel = game.statusLabel == null,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (game.participants.isNotEmpty()) {
                        SCAvatarStack(game.participants, paymentAware = variant == SCGameCardVariant.Organizer, size = 28.dp)
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (game.shareCaption != null) {
                                Text(game.shareCaption, style = t.labelSmall, color = c.onSurfaceVariant)
                            }
                            if (variant == SCGameCardVariant.Participant && game.myShare != null) {
                                if (game.shareCaption != null) Text("·", style = t.labelSmall, color = c.onSurfaceVariant)
                                Icon(game.myShare.icon, null, tint = game.myShare.color(), modifier = Modifier.size(14.dp))
                                Text(stringResource(game.myShare.labelRes), style = t.labelSmall, color = c.onSurfaceVariant)
                            }
                        }
                        Text(game.shareValue.formatBrl(), style = SportConnectTheme.typographyExt.numericPriceSmall, color = c.onSurface)
                    }
                    if (game.status == SCReservationTone.AwaitingPayment && game.holdExpiresAt != null) {
                        SCCountdownTimer(game.holdExpiresAt, label = game.countdownLabel)
                    }
                }
                if (primaryAction != null) {
                    SCButton(
                        text = primaryAction.label,
                        onClick = primaryAction.onClick,
                        variant = if (variant == SCGameCardVariant.Organizer) SCButtonVariant.Tonal else SCButtonVariant.Primary,
                        size = SCButtonSize.Small,
                        leadingIcon = primaryAction.icon,
                        loading = primaryAction.loading,
                        enabled = primaryAction.enabled,
                        accessibilityLabel = primaryAction.accessibilityLabel,
                        fullWidth = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    val c = SportConnectTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, null, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
