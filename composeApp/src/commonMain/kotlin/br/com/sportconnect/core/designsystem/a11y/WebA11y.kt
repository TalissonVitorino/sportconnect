package br.com.sportconnect.core.designsystem.a11y

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.com.sportconnect.core.platform.isWebApp
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.a11y_state_checked
import br.com.sportconnect.resources.a11y_state_selected
import br.com.sportconnect.resources.a11y_state_unchecked
import org.jetbrains.compose.resources.stringResource

/**
 * A2 (doc 13): a web do Compose so gera `role=button`/`heading`, sem `aria-selected` nem `aria-checked`,
 * e ignora `stateDescription`. Na web o estado vai no nome ("Inicio, selecionada"); no Android e no iOS
 * nada muda (Role.Tab/Role.Checkbox com estado ja chegam ao TalkBack e ao VoiceOver).
 */
@Composable
fun Modifier.webStateLabel(label: String, vararg states: String?): Modifier {
    if (!isWebApp) return this
    val text = (listOf(label) + states.filterNotNull().filter { it.isNotBlank() }).joinToString(", ")
    return this.semantics { contentDescription = text }
}

/** Aba, segmento ou item de navegacao: "selecionada" quando ativo. */
@Composable
fun Modifier.webSelectedLabel(label: String, selected: Boolean, vararg extra: String?): Modifier =
    webStateLabel(label, if (selected) stringResource(Res.string.a11y_state_selected) else null, *extra)

/** Caixa de selecao (chip de esporte, filtro liga/desliga): "marcado"/"nao marcado". */
@Composable
fun Modifier.webCheckedLabel(label: String, checked: Boolean): Modifier =
    webStateLabel(label, stringResource(if (checked) Res.string.a11y_state_checked else Res.string.a11y_state_unchecked))
