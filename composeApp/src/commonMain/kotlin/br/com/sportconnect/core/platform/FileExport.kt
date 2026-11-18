package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

/**
 * X2 (doc 13): salva um arquivo gerado pelo app (extrato CSV, "baixar meus dados" em JSON).
 * Android, iOS e desktop abrem o dialogo de salvar do sistema; a web baixa direto para Downloads.
 * [onDone] recebe true quando o arquivo foi gravado e false se a pessoa cancelou ou houve erro.
 */
@Composable
expect fun rememberFileExport(onDone: (saved: Boolean) -> Unit): (fileName: String, extension: String, bytes: ByteArray) -> Unit
