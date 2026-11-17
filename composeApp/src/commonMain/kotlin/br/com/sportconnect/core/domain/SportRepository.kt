package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.Sport

/** Contrato de dados consumido pelas features; implementacoes ficam em core/data. */
interface SportRepository {
    suspend fun availableSports(): List<Sport>
}
