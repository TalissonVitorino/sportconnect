package br.com.sportconnect.core.domain.usecase

import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.StatusCota
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Clock
import kotlin.time.Instant

/** Resultado da exclusao (US-003): imediata ou agendada para depois dos jogos ativos pagos. */
data class ResultadoExclusao(val agendadaApos: Instant?)

/**
 * Exclusao de conta (US-003, App Store 5.1.1(v), politica do Google Play, LGPD art. 18).
 * Com cota PAGA em jogo ainda nao concluido, a exclusao so acontece depois desse jogo (ou do reembolso).
 */
class DeleteAccountUseCase(
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val analytics: AnalyticsTracker,
) {
    /** Jogos que seguram a exclusao: cota paga por mim e jogo nao terminado. */
    fun jogosQueSeguram(): Flow<List<Jogo>> = auth.currentUser.flatMapLatest { u ->
        if (u == null) flowOf(emptyList())
        else games.observeGames().map { todos ->
            todos.filter { j -> !j.status.terminal && j.reserva.cotas.any { it.atletaId == u.id && it.status == StatusCota.PAGA } }
                .sortedBy { it.fim }
        }
    }

    suspend operator fun invoke(): Result<ResultadoExclusao> {
        val pendentes = jogosQueSeguram().first()
        val apos = pendentes.maxOfOrNull { it.fim }
        return auth.excluirConta(apos).map {
            analytics.track(
                AnalyticsEvents.CONTA_EXCLUSAO_SOLICITADA,
                mapOf("agendada" to (apos != null), "qtd_jogos_pendentes" to pendentes.size),
            )
            ResultadoExclusao(apos)
        }
    }
}

/**
 * "Baixar meus dados" (RNF-11: portabilidade em JSON). Junta o perfil, os consentimentos, os jogos e os
 * pagamentos da pessoa. Nada de dados de outras pessoas alem do nome do jogo e da arena.
 */
class ExportMyDataUseCase(
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<String> {
        val u = auth.currentUser.value ?: return Result.failure(IllegalStateException("Sem sessão"))
        val todos = games.observeGames().first()
        val meus = todos.filter { j -> j.participa(u.id) || j.reserva.cotas.any { it.atletaId == u.id } }
        val pagamentos = meus.flatMap { j -> j.reserva.cotas.filter { it.atletaId == u.id && it.pagaEm != null }.map { j to it } }
        val json = buildJsonObject {
            put("gerado_em", clock.now().toString())
            putJsonObject("perfil") {
                put("id", u.id)
                put("nome", u.nome)
                put("celular", u.celular)
                put("email", u.email)
                put("criado_em", u.criadoEm.toString())
                putJsonArray("papeis") { u.papeis.forEach { add(kotlinx.serialization.json.JsonPrimitive(it.name)) } }
            }
            putJsonArray("consentimentos") {
                u.consentimentos.forEach { c ->
                    add(buildJsonObject { put("tipo", c.tipo); put("concedido", c.concedido); put("em", c.em.toString()) })
                }
            }
            putJsonArray("jogos") {
                meus.forEach { j ->
                    add(
                        buildJsonObject {
                            put("id", j.id)
                            put("titulo", j.titulo)
                            put("arena", j.arenaNome)
                            put("quadra", j.quadraNome)
                            put("inicio", j.inicio.toString())
                            put("status", j.status.name)
                            put("organizador", j.organizadorId == u.id)
                        },
                    )
                }
            }
            put(
                "pagamentos",
                buildJsonArray {
                    pagamentos.forEach { (j, c) ->
                        add(
                            buildJsonObject {
                                put("jogo_id", j.id)
                                put("valor_centavos", c.valor.cents)
                                put("status", c.status.name)
                                put("pago_em", c.pagaEm.toString())
                            },
                        )
                    }
                },
            )
        }
        analytics.track(
            AnalyticsEvents.DADOS_EXPORTADOS,
            mapOf("formato" to "json", "qtd_jogos" to meus.size, "qtd_pagamentos" to pagamentos.size),
        )
        return Result.success(JSON.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), json))
    }

    private companion object {
        val JSON = Json { prettyPrint = true }
    }
}
