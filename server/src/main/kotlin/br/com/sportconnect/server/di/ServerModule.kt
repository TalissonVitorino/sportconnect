package br.com.sportconnect.server.di

import br.com.sportconnect.server.auth.InMemoryOtpStore
import br.com.sportconnect.server.auth.OtpStore
import br.com.sportconnect.server.auth.TokenService
import br.com.sportconnect.server.config.AppConfig
import br.com.sportconnect.server.persistence.ExposedUsuarioRepository
import br.com.sportconnect.server.persistence.UsuarioRepository
import br.com.sportconnect.server.persistence.criarDataSource
import br.com.sportconnect.server.persistence.migrar
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo Koin raiz do servidor. Expõe o [AppConfig] já resolvido para que outros módulos
 * (persistência, segurança, etc.) possam injetá-lo em vez de ler o ambiente diretamente.
 *
 * Também monta a persistência (Postgres via HikariCP/Exposed) e a autenticação (OTP/JWT): as
 * migrações Flyway rodam de forma síncrona aqui — antes de qualquer rota poder usar o banco —, e
 * o pool HikariCP é fechado quando a aplicação Ktor para (ver `Application.module`, que registra
 * o hook em `ApplicationStopping`; nos testes isso acontece ao final de cada `testApplication {}`,
 * evitando o vazamento de pool que `ExposedUsuarioRepositoryTest` já teve que corrigir).
 */
fun serverModule(config: AppConfig): Module {
    migrar(config)
    return module {
        single { config }
        single { criarDataSource(config) }
        single<Database> { Database.connect(get<HikariDataSource>()) }
        single<UsuarioRepository> { ExposedUsuarioRepository(get()) }
        single<OtpStore> { InMemoryOtpStore() }
        single { TokenService(get()) }
    }
}
