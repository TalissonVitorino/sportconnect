package br.com.sportconnect

import androidx.compose.ui.window.ComposeUIViewController
import br.com.sportconnect.di.initKoin

@Suppress("FunctionName", "unused") // Chamado a partir do Swift (ContentView.swift)
fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() },
) { App() }
