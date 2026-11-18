package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.layout.BoxWithConstraints
import br.com.sportconnect.resources.Res
import org.jetbrains.compose.resources.stringResource
import br.com.sportconnect.resources.cd_court_distance
import br.com.sportconnect.resources.cd_slot_chip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCElevation
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SCSlotState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Sport

enum class SCCourtCardVariant { List, Horizontal, Map, Owner }

@Immutable
data class CourtCardUi(
    val arenaName: String,
    val courtName: String,
    /** "Grama sintética · Coberta" (pre-formatado pela feature). */
    val detailsLabel: String,
    val sports: List<Sport>,
    val priceFrom: Money,
    /** "a partir de" / "/h" ficam na feature; aqui so o sufixo exibido apos o preco. */
    val priceSuffix: String = "/h",
    val priceFromPrefix: String = "a partir de",
    val distanceLabel: String? = null,
    val nextSlots: List<SlotUi> = emptyList(),
    val photoUrl: String? = null,
    /** Exibido quando nao ha [nextSlots] ("Sem horários livres hoje"). */
    val unavailableLabel: String? = null,
    /** Variante Owner: ocupacao do dia 0..1 e rotulo ("72% ocupada hoje"). */
    val occupancy: Float? = null,
    val occupancyLabel: String? = null,
    /** Estimativa por pessoa (`common_per_person_estimate`, "≈ R$ 20,00 por pessoa com 12 vagas"). */
    val estimateLabel: String? = null,
)

/**
 * Card de quadra (06 §5.6). Foto 16:9 (placeholder com linhas da quadra do esporte principal,
 * pois ainda nao ha carregador de imagem), selo de distancia sobre scrim, esportes `Compact`,
 * preco tabular e ate 3 mini-slots com alvo de 48dp proprio.
 */
@Composable
fun SCCourtCard(
    court: CourtCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SCCourtCardVariant = SCCourtCardVariant.List,
    onSlotClick: ((SlotUi) -> Unit)? = null,
    ownerAction: SCAction? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val interaction = remember { MutableInteractionSource() }
    val mainSport = court.sports.firstOrNull() ?: Sport.FUTEBOL_SOCIETY
    val priceCd = "${court.priceFromPrefix} ${spokenBrl(court.priceFrom.cents)} por hora"
    // A1 (doc 13): nome da arena, quadra, piso e distancia numa frase (a web perdia os textos do card).
    val distanceCd = court.distanceLabel?.let { stringResource(Res.string.cd_court_distance, it) }
    val headline = listOfNotNull(court.arenaName, "${court.courtName} · ${court.detailsLabel}", distanceCd).joinToString(", ")

    Surface(
        onClick = onClick,
        modifier = modifier.scFocusRing(interaction, SCShape.card),
        shape = SCShape.card,
        color = if (variant == SCCourtCardVariant.Map) c.surfaceContainerLow else c.surfaceContainerLowest,
        border = if (variant == SCCourtCardVariant.Map) null else BorderStroke(1.dp, c.outlineVariant),
        shadowElevation = if (variant == SCCourtCardVariant.Map) SCElevation.level1 else 0.dp,
        interactionSource = interaction,
    ) {
        when (variant) {
            SCCourtCardVariant.Horizontal, SCCourtCardVariant.Map -> Row(
                Modifier.padding(12.dp).heightIn(min = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SCCourtPlaceholder(mainSport, Modifier.width(96.dp).height(96.dp).clip(SCShape.small))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(court.arenaName, style = t.titleMedium, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { contentDescription = headline })
                    Text("${court.courtName} · ${court.detailsLabel}", style = t.bodyMedium, color = c.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.clearAndSetSemantics { })
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        court.sports.take(4).forEach { SCSportChip(it, variant = SCSportChipVariant.Compact) }
                        if (court.distanceLabel != null) {
                            Icon(Icons.Rounded.LocationOn, null, tint = c.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp).size(16.dp))
                            Text(court.distanceLabel, style = t.labelMedium, color = c.onSurfaceVariant, modifier = Modifier.clearAndSetSemantics { })
                        }
                    }
                    PriceFrom(court, Modifier.semantics { contentDescription = priceCd })
                    if (court.estimateLabel != null) Text(court.estimateLabel, style = t.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.semantics { contentDescription = court.estimateLabel })
                }
            }
            else -> Column {
                // C6: 16:9 ocupava ~50% da tela no celular (1 resultado por vez). 2.4:1 mostra ~2 cards.
                // T4: em telas largas a proporcao passava de 250 dp de altura; limita a 180 dp sem estreitar.
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(minOf(maxWidth / 2.4f, 180.dp))) {
                    SCCourtPlaceholder(mainSport, Modifier.fillMaxWidth().fillMaxHeight())
                    if (court.distanceLabel != null) {
                        Row(
                            Modifier.align(Alignment.TopStart).padding(12.dp)
                                .background(c.scrim.copy(alpha = 0.56f), SCShape.chip)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Rounded.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(court.distanceLabel, style = t.labelMedium, color = Color.White, modifier = Modifier.clearAndSetSemantics { })
                        }
                    }
                }
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(court.arenaName, style = t.titleMedium, color = c.onSurface, modifier = Modifier.semantics { contentDescription = headline })
                    Text("${court.courtName} · ${court.detailsLabel}", style = t.bodyMedium, color = c.onSurfaceVariant, modifier = Modifier.clearAndSetSemantics { })
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            court.sports.take(5).forEach { SCSportChip(it, variant = SCSportChipVariant.Compact) }
                        }
                        PriceFrom(court, Modifier.semantics { contentDescription = priceCd })
                    }
                    if (court.estimateLabel != null) {
                        Text(court.estimateLabel, style = t.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.fillMaxWidth().semantics { contentDescription = court.estimateLabel }, textAlign = TextAlign.End)
                    }
                    if (variant == SCCourtCardVariant.Owner && court.occupancy != null) {
                        LinearProgressIndicator(
                            progress = { court.occupancy },
                            modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 4.dp),
                            color = c.primary,
                            trackColor = c.surfaceContainerHighest,
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {},
                        )
                        if (court.occupancyLabel != null) Text(court.occupancyLabel, style = t.bodySmall, color = c.onSurfaceVariant)
                        if (ownerAction != null) {
                            SCButton(ownerAction.label, ownerAction.onClick, variant = SCButtonVariant.Secondary, size = SCButtonSize.Small, leadingIcon = ownerAction.icon)
                        }
                    } else if (court.nextSlots.isNotEmpty()) {
                        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            court.nextSlots.take(3).forEach { slot -> MiniSlot(slot, onSlotClick) }
                        }
                    } else if (court.unavailableLabel != null) {
                        Text(court.unavailableLabel, style = t.bodyMedium, color = c.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceFrom(court: CourtCardUi, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    Row(modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(court.priceFromPrefix, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.alignByBaseline())
        Text(court.priceFrom.formatBrl(), style = SportConnectTheme.typographyExt.numericPriceSmall, color = c.onSurface, modifier = Modifier.alignByBaseline())
        Text(court.priceSuffix, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.alignByBaseline())
    }
}

@Composable
private fun MiniSlot(slot: SlotUi, onSlotClick: ((SlotUi) -> Unit)?) {
    // A1: "Reservar das 21h as 22h, R$ 240,00" em vez de so "21h".
    val slotCd = slot.price?.let { stringResource(Res.string.cd_slot_chip, slot.startLabel, slot.endLabel, it.formatBrl()) } ?: slot.startLabel
    val colors = SportConnectTheme.extendedColors.slot(slot.state)
    val enabled = onSlotClick != null && slot.state == SCSlotState.Available
    Box(
        Modifier
            .heightIn(min = 48.dp)
            .widthIn(min = 64.dp)
            .clip(SCShape.control)
            .background(colors.container)
            .border(1.dp, colors.border, SCShape.control)
            .then(if (enabled) Modifier.clickable(role = Role.Button) { onSlotClick(slot) } else Modifier)
            .semantics { contentDescription = slotCd }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(slot.startLabel, style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"), color = colors.content)
    }
}
