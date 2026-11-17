package br.com.sportconnect.core.designsystem.theme

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable

/**
 * Cor unica de "selecionado" (C10 do backlog 11). Antes, chips e segmented buttons usavam o pessego
 * (`secondaryContainer`, padrao do M3) enquanto data, horario e aba usavam verde. Agora tudo que e
 * selecao usa `primaryContainer`, o mesmo verde do indicador da barra de navegacao; o pessego fica
 * para destaque ("Recomendado", "Voce organiza").
 */
object SCSelection {
    @Composable
    fun segmentedColors(): SegmentedButtonColors {
        val c = SportConnectTheme.colors
        return SegmentedButtonDefaults.colors(
            activeContainerColor = c.primaryContainer,
            activeContentColor = c.onPrimaryContainer,
            activeBorderColor = c.outline,
        )
    }

    @Composable
    fun filterChipColors(): SelectableChipColors {
        val c = SportConnectTheme.colors
        return FilterChipDefaults.filterChipColors(
            selectedContainerColor = c.primaryContainer,
            selectedLabelColor = c.onPrimaryContainer,
            selectedLeadingIconColor = c.onPrimaryContainer,
            selectedTrailingIconColor = c.onPrimaryContainer,
        )
    }
}
