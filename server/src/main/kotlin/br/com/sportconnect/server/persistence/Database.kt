package br.com.sportconnect.server.persistence

import br.com.sportconnect.server.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

/**
 * Conecta ao Postgres via HikariCP e devolve o [Database] do Exposed usado pelos repositórios.
 *
 * O pool HikariCP criado aqui vive pelo tempo de vida da aplicação (processo de servidor) e não
 * é fechado por esta função. Em testes — que têm um ciclo de vida mais curto e tendem a criar
 * várias instâncias — prefira [criarDataSource] diretamente, guardando a referência para chamar
 * `.close()` quando o pool não for mais necessário (ver `ExposedUsuarioRepositoryTest`).
 */
fun conectar(config: AppConfig): Database = Database.connect(criarDataSource(config))

/**
 * Monta o [HikariDataSource] usado por [conectar]. Exposta separadamente para que quem precisa
 * encerrar o pool explicitamente (testes que compartilham o [PostgresTestContainer] entre várias
 * classes) possa guardar a referência em vez de deixar o pool vazar.
 */
fun criarDataSource(config: AppConfig): HikariDataSource = HikariDataSource(
    HikariConfig().apply {
        jdbcUrl = config.databaseUrl
        username = config.databaseUsuario
        password = config.databaseSenha
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
    },
)

/**
 * Executa as migrações Flyway pendentes (`classpath:db/migration`) no banco apontado por [config].
 */
fun migrar(config: AppConfig) {
    Flyway.configure()
        .dataSource(config.databaseUrl, config.databaseUsuario, config.databaseSenha)
        .load()
        .migrate()
}
