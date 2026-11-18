package br.com.sportconnect.server.auth

import br.com.sportconnect.core.domain.usecase.RegraNegocioException
import br.com.sportconnect.core.model.Usuario
import br.com.sportconnect.server.http.ErroCorpo
import br.com.sportconnect.server.persistence.UsuarioRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

/** Formato E.164 simples: `+` seguido de 10 a 15 dígitos. */
private val FORMATO_E164 = Regex("^\\+\\d{10,15}$")

private val logger = LoggerFactory.getLogger("br.com.sportconnect.server.auth.AuthRoutes")

@Serializable
data class SolicitarOtpRequest(val celular: String, val canal: String = "SMS")

@Serializable
data class ConfirmarOtpRequest(val celular: String, val codigo: String)

@Serializable
data class TokensResponse(val acesso: String, val renovacao: String, val expiraEm: Instant)

@Serializable
data class ConfirmarOtpResponse(val usuario: Usuario, val tokens: TokensResponse)

/** Corpo de erro de `TOO_MANY_ATTEMPTS`: além do envelope padrão, inclui `tentarApos` (docs/15 §1). */
@Serializable
private data class ErroBloqueadoResponse(val erro: ErroCorpo, val tentarApos: Instant)

/**
 * Rotas de autenticação por OTP (docs/15-contrato-api.md §1): solicitar código e confirmar.
 *
 * Este esqueleto não integra um provedor de SMS/WhatsApp real: `POST /auth/otp` apenas gera o
 * código via [otp]. O valor do código **nunca** é logado nem devolvido em nenhuma resposta HTTP —
 * isso seria uma falha de segurança real caso este código fosse ao ar como está, independente do
 * nível de log configurado em produção. Testes leem o código diretamente do [OtpStore]
 * (`InMemoryOtpStore.ultimoCodigoPara`), nunca de um log. A integração com um provedor real é item
 * de um plano futuro, não deste esqueleto.
 */
fun Route.authRoutes(usuarios: UsuarioRepository, otp: OtpStore, tokens: TokenService) {
    post("/auth/otp") {
        val requisicao = call.receive<SolicitarOtpRequest>()
        if (!FORMATO_E164.matches(requisicao.celular)) {
            throw RegraNegocioException("PHONE_INVALID", "Celular inválido. Use DDD + número.")
        }
        otp.gerar(requisicao.celular)
        logger.debug("OTP solicitado para {}", requisicao.celular)
        call.respond(HttpStatusCode.NoContent)
    }

    post("/auth/otp/confirmar") {
        val requisicao = call.receive<ConfirmarOtpRequest>()
        when (val resultado = otp.validar(requisicao.celular, requisicao.codigo)) {
            is OtpResultado.Invalido -> throw RegraNegocioException("CODE_INVALID", "Código inválido.")
            is OtpResultado.Expirado -> throw RegraNegocioException("CODE_EXPIRED", "O código expirou.")
            is OtpResultado.Bloqueado -> call.respond(
                HttpStatusCode.TooManyRequests,
                ErroBloqueadoResponse(
                    erro = ErroCorpo(codigo = "TOO_MANY_ATTEMPTS", mensagem = "Muitas tentativas seguidas."),
                    tentarApos = resultado.ate,
                ),
            )

            is OtpResultado.Ok -> {
                val usuario = usuarios.buscarPorCelular(requisicao.celular)
                    ?: usuarios.criar(celular = requisicao.celular, nome = "Convidado")
                val par = tokens.emitirRefresh(usuario.id)
                call.respond(
                    HttpStatusCode.OK,
                    ConfirmarOtpResponse(
                        usuario = usuario,
                        tokens = TokensResponse(acesso = par.acesso, renovacao = par.renovacao, expiraEm = par.expiraEm),
                    ),
                )
            }
        }
    }
}
