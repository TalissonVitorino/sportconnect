package br.com.sportconnect.core.model

import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.UtcOffset

/**
 * Fuso de referencia (America/Sao_Paulo). Usamos offset fixo −03:00 (sem horario de verao desde 2019)
 * para nao depender do banco de fusos no alvo Wasm.
 */
val FusoSaoPaulo: FixedOffsetTimeZone = FixedOffsetTimeZone(UtcOffset(hours = -3))
