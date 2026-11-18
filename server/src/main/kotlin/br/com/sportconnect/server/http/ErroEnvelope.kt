package br.com.sportconnect.server.http

import kotlinx.serialization.Serializable

@Serializable
data class ErroCorpo(val codigo: String, val mensagem: String)

@Serializable
data class ErroEnvelope(val erro: ErroCorpo)
