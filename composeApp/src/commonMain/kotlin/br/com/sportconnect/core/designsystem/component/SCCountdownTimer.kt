package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.LocalSCClock
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.cd_countdown
import br.com.sportconnect.resources.ds_countdown_expired
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class SCCountdownVariant { Inline, Chip, Large }

/** Faixa de urgencia (06 §5.12). */
enum class SCCountdownUrgency { Relaxed, Warning, Critical, Expired }

/** Formata segundos restantes: "3h 20min" (> 1h) ou "42:10" (<= 1h). */
fun formatCountdown(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    return if (s > 3600) {
        val h = s / 3600
        val m = (s % 3600) / 60
        if (m == 0L) "${h}h" else "${h}h ${m}min"
    } else {
        val m = s / 60
        val sec = s % 60
        "${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
    }
}

/** Duracao em linguagem natural para leitores de tela, com granularidade de minuto. */
fun spokenDuration(totalSeconds: Long): String {
    val minutesTotal = (totalSeconds.coerceAtLeast(0) + 59) / 60
    val h = minutesTotal / 60
    val m = minutesTotal % 60
    val hs = when (h) { 0L -> null; 1L -> "1 hora"; else -> "$h horas" }
    val ms = when (m) { 0L -> null; 1L -> "1 minuto"; else -> "$m minutos" }
    return listOfNotNull(hs, ms).joinToString(" e ").ifEmpty { "menos de 1 minuto" }
}

fun countdownUrgency(totalSeconds: Long): SCCountdownUrgency = when {
    totalSeconds <= 0 -> SCCountdownUrgency.Expired
    totalSeconds < 5 * 60 -> SCCountdownUrgency.Critical
    totalSeconds <= 3600 -> SCCountdownUrgency.Warning
    else -> SCCountdownUrgency.Relaxed
}

/**
 * Contagem regressiva (06 §5.12). Digitos tabulares (sem "tremida"), icone muda ao esgotar,
 * cor nunca e o unico sinal (o formato muda por faixa). O texto acessivel e atualizado por
 * MINUTO, nao por segundo. Atualiza 1x/s apenas este composable (nao a tela).
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SCCountdownTimer(
    deadline: Instant,
    modifier: Modifier = Modifier,
    variant: SCCountdownVariant = SCCountdownVariant.Inline,
    label: String? = null,
    clock: Clock = LocalSCClock.current,
    onExpired: () -> Unit = {},
) {
    var remaining by remember(deadline, clock) { mutableStateOf((deadline - clock.now()).inWholeSeconds) }
    val expiredCallback by rememberUpdatedState(onExpired)
    LaunchedEffect(deadline, clock) {
        var notified = false
        while (true) {
            remaining = (deadline - clock.now()).inWholeSeconds
            if (remaining <= 0) {
                if (!notified) { notified = true; expiredCallback() }
                break
            }
            delay(1000)
        }
    }

    val c = SportConnectTheme.colors
    val ext = SportConnectTheme.extendedColors
    val urgency = countdownUrgency(remaining)
    val expiredText = stringResource(Res.string.ds_countdown_expired)
    val timeText = if (urgency == SCCountdownUrgency.Expired) expiredText else formatCountdown(remaining)
    val minuteBucket = (remaining.coerceAtLeast(0) + 59) / 60
    val spoken = remember(minuteBucket, label, urgency) {
        if (urgency == SCCountdownUrgency.Expired) null else spokenDuration(remaining)
    }
    val a11y = when {
        spoken == null -> expiredText
        label != null -> "Faltam $spoken $label"
        else -> null
    } ?: stringResource(Res.string.cd_countdown, spoken!!)

    val chip = variant == SCCountdownVariant.Chip
    val (fg, bg) = when (urgency) {
        SCCountdownUrgency.Relaxed -> c.onSurfaceVariant to c.surfaceContainerHigh
        SCCountdownUrgency.Warning -> (if (chip) ext.onWarningContainer else ext.warning) to ext.warningContainer
        SCCountdownUrgency.Critical -> (if (chip) c.onErrorContainer else c.error) to c.errorContainer
        SCCountdownUrgency.Expired -> c.onSurfaceVariant to c.surfaceContainerHigh
    }
    val icon = if (urgency == SCCountdownUrgency.Expired) Icons.Rounded.TimerOff else Icons.Rounded.Timer
    val digitsStyle = SportConnectTheme.typographyExt.numericScore
    val t = MaterialTheme.typography

    when (variant) {
        SCCountdownVariant.Large -> Column(
            modifier = modifier.clearAndSetSemantics { contentDescription = a11y },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = fg, modifier = Modifier.size(32.dp))
                Text(
                    timeText,
                    style = if (urgency == SCCountdownUrgency.Expired) t.headlineSmall else digitsStyle,
                    color = fg,
                )
            }
            if (label != null && urgency != SCCountdownUrgency.Expired) {
                Text(label, style = t.bodyMedium, color = c.onSurfaceVariant)
            }
        }
        else -> Row(
            modifier = modifier
                .then(
                    if (chip) {
                        Modifier.heightIn(min = 32.dp).background(bg, SCShape.chip).padding(horizontal = 10.dp, vertical = 4.dp)
                    } else {
                        Modifier
                    },
                )
                .clearAndSetSemantics { contentDescription = a11y },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            Text(
                timeText,
                style = t.labelLarge.copy(fontFeatureSettings = "tnum"),
                color = fg,
            )
            if (label != null && urgency != SCCountdownUrgency.Expired) {
                Text(label, style = t.bodySmall, color = if (chip) fg else c.onSurfaceVariant)
            }
        }
    }
}
