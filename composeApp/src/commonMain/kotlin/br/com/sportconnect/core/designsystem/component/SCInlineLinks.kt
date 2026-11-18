package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme

/** Trecho clicavel dentro de um paragrafo: [label] precisa aparecer exatamente no texto. */
data class SCInlineLink(val label: String, val onClick: () -> Unit)

/**
 * A3 (doc 13): paragrafo com links que funcionam igual em todas as plataformas. Com `LinkAnnotation` a web
 * do Compose criava os links sem nome para o leitor de tela. Aqui cada palavra e um texto (a quebra de linha
 * continua natural) e cada link e um elemento proprio, com nome e papel de botao.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SCInlineLinks(
    text: String,
    links: List<SCInlineLink>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = SportConnectTheme.colors.onSurfaceVariant,
    linkColor: Color = SportConnectTheme.colors.primary,
) {
    // O paragrafo e lido de uma vez: a 1a palavra carrega a frase inteira e as outras ficam fora da arvore
    // (a web do Compose nao expoe descricao posta no conteiner). Os links continuam focaveis.
    FlowRow(modifier) {
        var first = true
        var rest = text
        while (rest.isNotEmpty()) {
            val next = links.mapNotNull { l -> rest.indexOf(l.label).takeIf { it >= 0 }?.let { it to l } }.minByOrNull { it.first }
            val plain = if (next == null) rest else rest.substring(0, next.first)
            // Palavras com o espaco depois: o FlowRow quebra a linha entre palavras, como num paragrafo.
            Regex("""\S+\s*|\s+""").findAll(plain).forEach {
                val sentence = if (first) text else null
                first = false
                Text(it.value, style = style, color = color, modifier = Modifier.clearAndSetSemantics { if (sentence != null) contentDescription = sentence })
            }
            if (next == null) break
            val link = next.second
            Text(
                link.label,
                style = style.copy(textDecoration = TextDecoration.Underline),
                color = linkColor,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = link.onClick)
                    .semantics { contentDescription = link.label },
            )
            rest = rest.substring(next.first + link.label.length)
        }
    }
}
