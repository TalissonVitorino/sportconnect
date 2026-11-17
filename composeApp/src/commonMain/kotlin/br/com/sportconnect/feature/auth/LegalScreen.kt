@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.sportconnect.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.component.SCTopBar
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.domain.repository.AuthRules
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.auth_terms_link_privacy
import br.com.sportconnect.resources.auth_terms_link_terms
import br.com.sportconnect.resources.legal_pending_body
import br.com.sportconnect.resources.legal_version
import org.jetbrains.compose.resources.stringResource

/**
 * Termos de Uso / Politica de Privacidade (CT-05). O texto definitivo depende do Juridico; ate la a
 * tela mostra a versao vigente ([AuthRules.VERSAO_TERMOS]) e um resumo em linguagem simples.
 */
@Composable
fun LegalScreen(doc: LegalDoc, onBack: () -> Unit) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val title = stringResource(if (doc == LegalDoc.Terms) Res.string.auth_terms_link_terms else Res.string.auth_terms_link_privacy)
    Scaffold(containerColor = c.surface, topBar = { SCTopBar(title, onNavigationClick = onBack) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp).testTag("legal_${doc.name.lowercase()}")) {
                Text(stringResource(Res.string.legal_version, AuthRules.VERSAO_TERMOS), style = t.labelLarge, color = c.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(Res.string.legal_pending_body), style = t.bodyLarge, color = c.onSurface)
            }
        }
    }
}
