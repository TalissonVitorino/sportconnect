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
                // Não inventa um código de erro novo (ex.: "USUARIO_NAO_ENCONTRADO") fora do
                // catálogo (docs/15 §12, regra de docs/14 §5): um token com assinatura/issuer/tipo
                // válidos cujo usuarioId não resolve mais (usuário deletado/anonimizado via LGPD,
                // ou claim malformada) é tratado como a mesma sessão inválida do caso "sem token" -
                // o cliente só sabe reagir ao 401 NAO_AUTENTICADO universal.
                call.respond(
                    CatalogoErros.statusPara("NAO_AUTENTICADO"),
                    ErroEnvelope(ErroCorpo(codigo = "NAO_AUTENTICADO", mensagem = "Token sem usuario correspondente.")),
                )
                return@get
            }
            call.respond(HttpStatusCode.OK, usuario)
        }
    }
}
