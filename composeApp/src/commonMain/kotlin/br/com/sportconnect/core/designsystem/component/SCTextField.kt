package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme

enum class SCTextFieldType { Text, Money, Phone, Cpf, Search, Multiline }

/**
 * Campo de texto (06 §5.2). Rotulo SEMPRE visivel acima do campo (nome acessivel),
 * container 56dp, raio 12, borda 1dp outline (2dp primary em foco, 2dp error em erro).
 *
 * Valores por tipo (o `value` guarda so o que importa ao dominio):
 * - `Money`: apenas digitos = centavos ("2150" -> exibe "R$ 21,50").
 * - `Phone`/`Cpf`: apenas digitos; mascara so visual (colar continua funcionando).
 */
@Composable
fun SCTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    type: SCTextFieldType = SCTextFieldType.Text,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val isError = errorText != null
    val shape = if (type == SCTextFieldType.Search) CircleShape else SCShape.control

    val digitLimit = when (type) {
        SCTextFieldType.Phone -> 11
        SCTextFieldType.Cpf -> 11
        SCTextFieldType.Money -> 11
        else -> null
    }
    val effectiveMax = maxLength ?: digitLimit
    val digitsOnly = type == SCTextFieldType.Money || type == SCTextFieldType.Phone || type == SCTextFieldType.Cpf
    val transformation: VisualTransformation = when (type) {
        SCTextFieldType.Money -> MoneyVisualTransformation
        SCTextFieldType.Phone -> MaskVisualTransformation("(##) # ####-####")
        SCTextFieldType.Cpf -> MaskVisualTransformation("###.###.###-##")
        else -> VisualTransformation.None
    }
    val kbOptions = when (type) {
        SCTextFieldType.Money, SCTextFieldType.Cpf -> keyboardOptions.copy(keyboardType = KeyboardType.Number)
        SCTextFieldType.Phone -> keyboardOptions.copy(keyboardType = KeyboardType.Phone)
        else -> keyboardOptions
    }

    val borderColor = when {
        !enabled -> c.onSurface.copy(alpha = 0.12f)
        isError -> c.error
        focused -> c.primary
        else -> c.outline
    }
    val borderWidth = if ((focused || isError) && enabled) 2.dp else 1.dp
    val container = if (readOnly) c.surfaceContainer else c.surfaceContainerLowest
    val spoken = if (type == SCTextFieldType.Money && value.isNotEmpty()) spokenBrl(value.toLongOrNull() ?: 0) else null

    Column(modifier = modifier.alpha(if (enabled) 1f else 0.38f)) {
        Text(
            text = label,
            style = t.labelLarge,
            color = if (isError) c.error else c.onSurface,
            modifier = Modifier.padding(bottom = 6.dp).clearAndSetSemantics { },
        )
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                var v = if (digitsOnly) raw.filter { it.isDigit() } else raw
                if (type == SCTextFieldType.Money) v = v.trimStart('0')
                if (effectiveMax != null) v = v.take(effectiveMax)
                onValueChange(v)
            },
            enabled = enabled,
            readOnly = readOnly,
            singleLine = type != SCTextFieldType.Multiline,
            minLines = if (type == SCTextFieldType.Multiline) 3 else 1,
            textStyle = t.bodyLarge.copy(color = c.onSurface),
            cursorBrush = SolidColor(if (isError) c.error else c.primary),
            visualTransformation = transformation,
            keyboardOptions = kbOptions,
            keyboardActions = keyboardActions,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = label
                    if (isError) {
                        error(errorText)
                    }
                    if (spoken != null) stateDescription = spoken
                },
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .heightIn(min = 56.dp)
                        .background(container, shape)
                        .then(if (readOnly) Modifier else Modifier.border(borderWidth, borderColor, shape))
                        .padding(horizontal = if (type == SCTextFieldType.Search) 20.dp else 16.dp),
                    verticalAlignment = if (type == SCTextFieldType.Multiline) Alignment.Top else Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val icon = leadingIcon ?: if (type == SCTextFieldType.Search) Icons.Rounded.Search else null
                    if (icon != null) {
                        Icon(icon, null, tint = c.onSurfaceVariant, modifier = Modifier.size(24.dp))
                    }
                    if (type == SCTextFieldType.Money) {
                        Text("R$", style = t.bodyLarge, color = c.onSurfaceVariant)
                    }
                    Box(
                        Modifier.weight(1f).padding(vertical = if (type == SCTextFieldType.Multiline) 16.dp else 0.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(placeholder, style = t.bodyLarge, color = c.onSurfaceVariant)
                        }
                        inner()
                    }
                    if (isError) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = c.error, modifier = Modifier.size(24.dp))
                    }
                    trailing?.invoke()
                }
            },
        )
        val helper = errorText ?: supportingText
        val showCounter = maxLength != null && !digitsOnly
        if (helper != null || showCounter) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp, start = 16.dp, end = 16.dp)) {
                if (helper != null) {
                    Text(
                        text = helper,
                        style = t.bodySmall,
                        color = if (isError) c.error else c.onSurfaceVariant,
                        modifier = Modifier.weight(1f).semantics {
                            if (isError) liveRegion = LiveRegionMode.Polite
                        },
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (showCounter) {
                    Spacer(Modifier.width(8.dp))
                    Text("${value.length}/$maxLength", style = t.bodySmall, color = c.onSurfaceVariant)
                }
            }
        }
    }
}

/** "2150" -> "21,50"; "123456" -> "1.234,56". Cursor sempre ao final (entrada de dinheiro). */
private object MoneyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val out = formatBrl(text.text.toLongOrNull() ?: 0L, withSymbol = false)
        return TransformedText(
            AnnotatedString(out),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = out.length
                override fun transformedToOriginal(offset: Int): Int = text.text.length
            },
        )
    }
}

/** Mascara visual com `#` = digito. Mapeamento de offset exato (edicao no meio funciona). */
private class MaskVisualTransformation(private val mask: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = StringBuilder()
        val origToTrans = IntArray(digits.length + 1)
        var di = 0
        for (ch in mask) {
            if (di >= digits.length) break
            if (ch == '#') {
                origToTrans[di] = out.length
                out.append(digits[di])
                di++
            } else {
                out.append(ch)
            }
        }
        // digitos alem da mascara (nao deveria acontecer por causa do limite) vao ao final
        while (di < digits.length) {
            origToTrans[di] = out.length
            out.append(digits[di]); di++
        }
        origToTrans[digits.length] = out.length
        val transToOrig = IntArray(out.length + 1)
        // os literais da mascara nao sao digitos: posicao original = digitos antes de i
        var count = 0
        for (i in 0..out.length) {
            transToOrig[i] = count
            if (i < out.length && out[i].isDigit()) count++
        }
        return TransformedText(
            AnnotatedString(out.toString()),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = origToTrans[offset.coerceIn(0, digits.length)]
                override fun transformedToOriginal(offset: Int): Int = transToOrig[offset.coerceIn(0, out.length)]
            },
        )
    }
}
