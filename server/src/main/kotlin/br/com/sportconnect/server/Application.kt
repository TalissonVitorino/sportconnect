package br.com.sportconnect.server

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.ContentType
import br.com.sportconnect.server.config.AppConfig
import br.com.sportconnect.server.di.serverModule
import br.com.sportconnect.server.http.configurarTratamentoDeErros
import org.koin.ktor.plugin.Koin

fun main() {
    val config = AppConfig.fromEnv(System.getenv())
    embeddedServer(Netty, port = config.porta, module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = AppConfig.fromEnv(System.getenv())
    configurarTratamentoDeErros()
    install(ContentNegotiation) { json() }
    install(Koin) {
        modules(serverModule(config))
    }
    routing {
        get("/saude") { call.respondText("""{"status":"ok"}""", ContentType.Application.Json) }
    }
}
