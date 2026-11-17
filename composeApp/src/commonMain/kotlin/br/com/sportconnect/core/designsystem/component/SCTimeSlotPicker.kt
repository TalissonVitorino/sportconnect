package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCBreakpoints
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SCSlotState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.ds_slot_available
import br.com.sportconnect.resources.ds_slot_busy
import br.com.sportconnect.resources.ds_slot_on_hold
import br.com.sportconnect.resources.ds_slot_releases_at
import br.com.sportconnect.resources.ds_slot_selected
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Horario para a grade. Textos de hora chegam formatados pela feature ("19:00").
 * [state] e o estado visual; `Selected` e derivado de `selection` pelo picker.
 */
@Immutable
data class SlotUi(
    val id: String,
    val startLabel: String,
    val endLabel: String,
    val price: Money? = null,
    val state: SCSlotState = SCSlotState.Available,
    /** Para `OnHold`: quando libera ("18:40"). */
    val holdReleasesAtLabel: String? = null,
)

val SCSlotState.labelRes: StringResource
    get() = when (this) {
        SCSlotState.Available -> Res.string.ds_slot_available
        SCSlotState.Busy -> Res.string.ds_slot_busy
        SCSlotState.OnHold -> Res.string.ds_slot_on_hold
        SCSlotState.Selected -> Res.string.ds_slot_selected
    }

val SCSlotState.icon: ImageVector?
    get() = when (this) {
        SCSlotState.Available -> null
        SCSlotState.Busy -> Icons.Rounded.Block
        SCSlotState.OnHold -> Icons.Rounded.HourglassTop
        SCSlotState.Selected -> Icons.Rounded.Check
    }

object SCTimeSlotPickerDefaults {
    /** 2 colunas compact / 4 medium / 6 expanded (06 §5.7). */
    fun columns(widthDp: Float): Int = when {
        widthDp < SCBreakpoints.medium.value -> 2
        widthDp < SCBreakpoints.expanded.value -> 4
        else -> 6
    }

    private val shortDays = mapOf(
        DayOfWeek.MONDAY to "Seg", DayOfWeek.TUESDAY to "Ter", DayOfWeek.WEDNESDAY to "Qua",
        DayOfWeek.THURSDAY to "Qui", DayOfWeek.FRIDAY to "Sex", DayOfWeek.SATURDAY to "Sáb", DayOfWeek.SUNDAY to "Dom",
    )

    /** Rotulo pt-BR do dia ("Sáb" / "23"). */
    fun dayLabel(date: LocalDate): Pair<String, String> = shortDays.getValue(date.dayOfWeek) to date.day.toString()

    /**
     * Regra de selecao contigua: toque num slot livre adjacente estende a selecao (ate [max]);
     * nao adjacente comeca uma nova selecao; toque num selecionado remove ele e os seguintes.
     * Retorna null quando a selecao excederia [max].
     */
    fun toggle(slots: List<SlotUi>, selection: Set<String>, tapped: String, max: Int): Set<String>? {
        val order = slots.map { it.id }
        val idx = order.indexOf(tapped)
        if (idx < 0) return selection
        if (tapped in selection) {
            return order.filterIndexed { i, id -> id in selection && i < idx }.toSet()
        }
        val selIdx = order.withIndex().filter { it.value in selection }.map { it.index }
        if (selIdx.isEmpty()) return setOf(tapped)
        val adjacent = idx == selIdx.min() - 1 || idx == selIdx.max() + 1
        if (!adjacent) return setOf(tapped)
        if (selIdx.size + 1 > max) return null
        return selection + tapped
    }
}

/**
 * Seletor de horarios (06 §5.7): faixa de dias, legenda dos 4 estados e grade de slots de 56dp.
 * Estado sempre com cor + icone/hachura + texto. Ocupado e pre-reservado continuam focaveis
 * para leitura (`disabled()`), mas nao acionaveis.
 *
 * Desvio: em vez de `windowSizeClass`, [columns] = null calcula pelas larguras disponiveis.
 */
@Composable
fun SCTimeSlotPicker(
    days: List<LocalDate>,
    selectedDay: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    slots: List<SlotUi>,
    selection: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    maxContiguous: Int = 3,
    columns: Int? = null,
    showLegend: Boolean = true,
    onSelectionRejected: (() -> Unit)? = null,
) {
    val spacing = SportConnectTheme.spacing
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            days.forEach { d -> DayChip(d, d == selectedDay) { onDaySelected(d) } }
        }
        if (showLegend) SlotLegend()
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cols = columns ?: SCTimeSlotPickerDefaults.columns(maxWidth.value)
            val rows = slots.chunked(cols)
            Column(
                Modifier.semantics { collectionInfo = CollectionInfo(rows.size, cols) },
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                rows.forEachIndexed { r, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        row.forEachIndexed { ci, slot ->
                            val selected = slot.id in selection
                            SlotCell(
                                slot = slot,
                                selected = selected,
                                modifier = Modifier.weight(1f).semantics {
                                    collectionItemInfo = CollectionItemInfo(r, 1, ci, 1)
                                },
                                onToggle = {
                                    val next = SCTimeSlotPickerDefaults.toggle(slots, selection, slot.id, maxContiguous)
                                    if (next == null) onSelectionRejected?.invoke() else onSelectionChange(next)
                                },
                            )
                        }
                        repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayChip(date: LocalDate, selected: Boolean, onClick: () -> Unit) {
    val c = SportConnectTheme.colors
    val (dow, num) = SCTimeSlotPickerDefaults.dayLabel(date)
    val interaction = remember { MutableInteractionSource() }
    Column(
        Modifier
            .widthIn(min = 56.dp)
            .heightIn(min = 64.dp)
            .scFocusRing(interaction, SCShape.control)
            .clip(SCShape.control)
            .background(if (selected) c.primary else c.surfaceContainerLowest)
            .border(1.dp, if (selected) c.primary else c.outlineVariant, SCShape.control)
            .selectable(selected, interactionSource = interaction, indication = ripple(), role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(dow, style = MaterialTheme.typography.labelMedium, color = if (selected) c.onPrimary else c.onSurfaceVariant)
        Text(num, style = SportConnectTheme.typographyExt.numericPriceSmall, color = if (selected) c.onPrimary else c.onSurface)
    }
}

@Composable
fun SlotLegend(modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SCSlotState.entries.forEach { s ->
            val colors = SportConnectTheme.extendedColors.slot(s)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier.size(16.dp).clip(SCShape.badge).background(colors.container)
                        .then(if (s == SCSlotState.Busy) Modifier.scHatch(colors.content.copy(alpha = 0.5f), 4.dp) else Modifier)
                        .border(1.dp, colors.border, SCShape.badge),
                    contentAlignment = Alignment.Center,
                ) {
                    s.icon?.let { Icon(it, null, tint = colors.content, modifier = Modifier.size(12.dp)) }
                }
                Text(stringResource(s.labelRes), style = MaterialTheme.typography.labelMedium, color = SportConnectTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SlotCell(slot: SlotUi, selected: Boolean, modifier: Modifier, onToggle: () -> Unit) {
    val state = if (selected) SCSlotState.Selected else slot.state
    val colors = SportConnectTheme.extendedColors.slot(state)
    val interactive = slot.state == SCSlotState.Available || selected
    val interaction = remember { MutableInteractionSource() }
    val stateLabel = stringResource(state.labelRes)
    val secondary = when (state) {
        SCSlotState.Busy -> stateLabel
        SCSlotState.OnHold -> slot.holdReleasesAtLabel?.let { stringResource(Res.string.ds_slot_releases_at, it) } ?: stateLabel
        else -> slot.price?.formatBrl()
    }
    val a11yState = listOfNotNull(
        stateLabel,
        if (state == SCSlotState.Available || state == SCSlotState.Selected) slot.price?.let { spokenBrl(it.cents) } else null,
        if (state == SCSlotState.OnHold) slot.holdReleasesAtLabel?.let { "libera às $it" } else null,
    ).joinToString(", ")

    Row(
        modifier
            .heightIn(min = 56.dp)
            .scFocusRing(interaction, SCShape.control)
            .clip(SCShape.control)
            .background(colors.container)
            .then(if (state == SCSlotState.Busy) Modifier.scHatch(colors.content.copy(alpha = 0.18f)) else Modifier)
            .border(1.dp, colors.border, SCShape.control)
            .toggleable(
                value = selected,
                enabled = interactive,
                role = Role.Checkbox,
                interactionSource = interaction,
                indication = ripple(),
                onValueChange = { onToggle() },
            )
            .semantics {
                stateDescription = a11yState
                if (!interactive) disabled()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${slot.startLabel}–${slot.endLabel}",
                style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (secondary != null) {
                Text(secondary, style = MaterialTheme.typography.bodySmall, color = colors.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        state.icon?.let {
            Spacer(Modifier.width(4.dp))
            Icon(it, null, tint = colors.content, modifier = Modifier.size(18.dp))
        }
    }
}
