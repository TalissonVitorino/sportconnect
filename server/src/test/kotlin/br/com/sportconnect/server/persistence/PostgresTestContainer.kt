package br.com.sportconnect.server.persistence

import org.testcontainers.containers.PostgreSQLContainer

/**
 * Container Postgres compartilhado por todas as classes de teste do módulo que precisam de um
 * banco real (Tasks 4, 5 e 6). O container e a migração ficam atrás de `by lazy`: a primeira
 * classe de teste que referencia [instancia] sobe o container uma única vez; as demais
 * reaproveitam a mesma instância já em execução, evitando o custo de subir um Testcontainers por
 * classe de teste.
 *
 * Cada classe de teste que grava em `usuarios`/`refresh_tokens` é responsável por limpar suas
 * próprias linhas antes de cada teste (ver `ExposedUsuarioRepositoryTest`), já que o container e
 * o schema são compartilhados entre toda a suíte do módulo.
 */
object PostgresTestContainer {
    val instancia: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:16-alpine").apply { start() }
    }
}
