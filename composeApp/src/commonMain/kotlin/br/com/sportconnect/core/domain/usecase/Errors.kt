package br.com.sportconnect.core.domain.usecase

/** Erro de regra de negocio com codigo estavel (para `erro_exibido.codigo_erro` e mensagens de UX). */
class RegraNegocioException(val codigo: String, mensagem: String) : IllegalStateException(mensagem)

internal fun regra(codigo: String, mensagem: String): Nothing = throw RegraNegocioException(codigo, mensagem)
