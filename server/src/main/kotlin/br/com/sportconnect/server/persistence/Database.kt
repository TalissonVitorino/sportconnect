package br.com.sportconnect.server.persistence

import br.com.sportconnect.server.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

/**
 * Conecta ao Postgres via HikariCP e devolve o [Database] do Exposed usado pelos repositórios.
 */
fun conectar(config: AppConfig): Database {
    val dataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = config.databaseUrl
            username = config.databaseUsuario
            password = config.databaseSenha
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
        },
    )
    return Database.connect(dataSource)
}

/**
 * Executa as migrações Flyway pendentes (`classpath:db/migration`) no banco apontado por [config].
 */
fun migrar(config: AppConfig) {
    Flyway.configure()
        .dataSource(config.databaseUrl, config.databaseUsuario, config.databaseSenha)
        .load()
        .migrate()
}
