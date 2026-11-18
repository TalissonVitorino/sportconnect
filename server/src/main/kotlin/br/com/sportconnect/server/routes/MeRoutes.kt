package br.com.sportconnect.server.routes

import br.com.sportconnect.server.http.CatalogoErros
import br.com.sportconnect.server.http.ErroCorpo
import br.com.sportconnect.server.http.ErroEnvelope
import br.com.sportconnect.server.persistence.UsuarioRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * `GET /me`: devolve o [br.com.sportconnect.core.model.Usuario] do dono do token de acesso usado
 * na requisição. Protegida pelo provider `"jwt"` (ver [br.com.sportconnect.server.auth.jwtAuth]) —
 * esta rota em si não repete nenhuma checagem de assinatura/expiração/tipo de token, que já
 * aconteceu no provider antes do handler ser chamado.
 */
fun Route.meRoutes(usuarios: UsuarioRepository) {
    authenticate("jwt") {
        get("/me") {
            val principal = call.principal<JWTPrincipal>()
            val usuarioId = principal?.payload?.getClaim("usuarioId")?.asString()
            if (usuarioId.isNullOrBlank()) {
                call.respond(
                    CatalogoErros.statusPara("NAO_AUTENTICADO"),
                    ErroEnvelope(ErroCorpo(codigo = "NAO_AUTENTICADO", mensagem = "Token sem usuarioId.")),
                )
                return@get
            }
            val usuario = usuarios.buscarPorId(usuarioId)
            if (usuario == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErroEnvelope(ErroCorpo(codigo = "USUARIO_NAO_ENCONTRADO", mensagem = "Usuario nao encontrado.")),
                )
                return@get
            }
            call.respond(HttpStatusCode.OK, usuario)
        }
    }
}
