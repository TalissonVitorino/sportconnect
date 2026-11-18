package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

@Composable
actual fun rememberNativeShare(): (text: String) -> Boolean = remember {
    { text ->
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        val top = generateSequence(root) { it.presentedViewController }.lastOrNull()
        if (top == null) {
            false
        } else {
            val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
            // iPad: a folha precisa de uma ancora; usa o centro da tela.
            sheet.popoverPresentationController?.sourceView = top.view
            top.presentViewController(sheet, animated = true, completion = null)
            true
        }
    }
}

