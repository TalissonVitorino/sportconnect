package br.com.sportconnect.server.auth

import br.com.sportconnect.core.domain.repository.AuthRules
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

/**
 * Implementação em memória de [OtpStore] — a única usada por este esqueleto (sem Redis/estado
 * distribuído). A interface [OtpStore] isola essa troca para quando o backend tiver mais de uma
 * instância.
 *
 * Números de regra (validade, tentativas, bloqueio) vêm de [AuthRules] — não são reimplementados
 * aqui.
 */
class InMemoryOtpStore(private val clock: Clock = Clock.System) : OtpStore {

    private data class EntradaOtp(
        val codigo: String,
        val geradoEm: Instant,
        val tentativas: Int = 0,
        val bloqueadoAte: Instant? = null,
        val usado: Boolean = false,
    )

    private val entradas = ConcurrentHashMap<String, EntradaOtp>()

    override fun gerar(celular: String): String {
        val codigo = gerarCodigoAleatorio()
        entradas.compute(celular) { _, existente ->
            // Um novo pedido para o MESMO celular não reinicia a janela de tentativas (só a
            // reinicia se o celular mudar — docs/15-contrato-api.md §1, espelhando
            // `FakeAuthRepository.solicitarOtp`); como o mapa já é por celular, isso significa
            // preservar tentativas/bloqueio existentes e só trocar o código e o instante de
            // geração.
            EntradaOtp(
                codigo = codigo,
                geradoEm = clock.now(),
                tentativas = existente?.tentativas ?: 0,
                bloqueadoAte = existente?.bloqueadoAte,
            )
        }
        return codigo
    }

    override fun validar(celular: String, codigo: String): OtpResultado {
        val agora = clock.now()
        var entrada = entradas[celular] ?: return OtpResultado.Invalido

        val bloqueadoAte = entrada.bloqueadoAte
        if (bloqueadoAte != null) {
            if (agora < bloqueadoAte) return OtpResultado.Bloqueado(bloqueadoAte)
            // Bloqueio expirado: libera e zera as tentativas antes de seguir a validação normal.
            entrada = entrada.copy(tentativas = 0, bloqueadoAte = null)
            entradas[celular] = entrada
        }

        if (entrada.usado) return OtpResultado.Invalido
        if (agora - entrada.geradoEm > AuthRules.OTP_VALIDADE_MIN.minutes) return OtpResultado.Expirado

        if (codigo != entrada.codigo) {
            val tentativas = entrada.tentativas + 1
            return if (tentativas >= AuthRules.MAX_TENTATIVAS) {
                val ate = agora + AuthRules.BLOQUEIO_MIN.minutes
                entradas[celular] = entrada.copy(tentativas = tentativas, bloqueadoAte = ate)
                OtpResultado.Bloqueado(ate)
            } else {
                entradas[celular] = entrada.copy(tentativas = tentativas)
                OtpResultado.Invalido
            }
        }

        entradas[celular] = entrada.copy(usado = true)
        return OtpResultado.Ok
    }

    /**
     * Acesso exclusivo para testes: lê o último código gerado para [celular] sem passar por um
     * canal de SMS/WhatsApp real (este esqueleto não integra um provedor de verdade — ver
     * `authRoutes`).
     */
    fun ultimoCodigoPara(celular: String): String? = entradas[celular]?.codigo

    private fun gerarCodigoAleatorio(): String =
        (1..6).joinToString("") { Random.nextInt(0, 10).toString() }
}
