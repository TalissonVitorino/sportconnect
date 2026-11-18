package br.com.sportconnect.server

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.ContentType
import br.com.sportconnect.server.auth.OtpStore
import br.com.sportconnect.server.auth.TokenService
import br.com.sportconnect.server.auth.authRoutes
import br.com.sportconnect.server.auth.jwtAuth
import br.com.sportconnect.server.config.AppConfig
import br.com.sportconnect.server.di.serverModule
import br.com.sportconnect.server.http.configurarTratamentoDeErros
import br.com.sportconnect.server.persistence.UsuarioRepository
import br.com.sportconnect.server.routes.meRoutes
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.auth.Authentication
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin

fun main() {
    val config = AppConfig.fromEnv(System.getenv())
    embeddedServer(Netty, port = config.porta, module = Application::module).start(wait = true)
}

/**
 * Monta a aplicação: tratamento de erros, serialização, DI (Koin) e rotas.
 *
 * [config] tem um default lido do ambiente (produção, via [main]). Testes que precisam apontar
 * para um banco diferente (ex.: Testcontainers) passam um [AppConfig] explícito para que esta
 * mesma função — e não uma montagem paralela — seja exercitada (ver `AuthRoutesTest`).
 */
fun Application.module(config: AppConfig = AppConfig.fromEnv(System.getenv())) {
    configurarTratamentoDeErros()
    install(ContentNegotiation) { json() }
    install(Authentication) { jwtAuth(config) }
    install(Koin) {
        modules(serverModule(config))
    }
    val koin = getKoin()
    // O pool HikariCP é fechado quando esta aplicação Ktor para (em produção, no shutdown do
    // processo; em testes, ao final de cada bloco `testApplication {}`) — evita o vazamento de
    // pool que `ExposedUsuarioRepositoryTest` já teve que corrigir.
    val dataSource = koin.get<HikariDataSource>()
    monitor.subscribe(ApplicationStopping) { dataSource.close() }
    val usuarios = koin.get<UsuarioRepository>()
    val otp = koin.get<OtpStore>()
    val tokens = koin.get<TokenService>()
    routing {
        get("/saude") { call.respondText("""{"status":"ok"}""", ContentType.Application.Json) }
        authRoutes(usuarios, otp, tokens)
        meRoutes(usuarios)
    }
}
