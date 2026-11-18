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
import br.com.sportconnect.server.http.configurarTratamentoDeErros

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    configurarTratamentoDeErros()
    install(ContentNegotiation) { json() }
    routing {
        get("/saude") { call.respondText("""{"status":"ok"}""", ContentType.Application.Json) }
    }
}
