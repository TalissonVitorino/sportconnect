package br.com.sportconnect.server.auth

import br.com.sportconnect.core.domain.repository.AuthRules
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
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

    // java.security.SecureRandom: o código OTP é um segredo de autenticação, não um valor
    // "qualquer" — kotlin.random.Random (PRNG não-criptográfico) seria previsível.
    private val rng = SecureRandom()

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

    /**
     * Toda a leitura-decisão-escrita roda dentro do remapeamento de [ConcurrentHashMap.compute],
     * que é atômico por chave (o `ConcurrentHashMap` trava o bin daquela chave durante a função
     * de remapeamento). Isso evita duas corridas reais sob concorrência: (a) duas tentativas
     * erradas simultâneas lendo o mesmo `tentativas` e cada uma incrementando a partir do mesmo
     * valor (permitindo mais de [AuthRules.MAX_TENTATIVAS] tentativas reais antes de bloquear) e
     * (b) duas tentativas corretas simultâneas lendo `usado = false` e ambas devolvendo `Ok`
     * (quebrando o uso único). O resultado da validação é capturado em [resultado], atribuído
     * dentro da função de remapeamento (que o `ConcurrentHashMap` garante invocar uma única vez
     * por chamada de `compute`).
     */
    override fun validar(celular: String, codigo: String): OtpResultado {
        var resultado: OtpResultado = OtpResultado.Invalido
        entradas.compute(celular) compute@{ _, atual ->
            if (atual == null) {
                resultado = OtpResultado.Invalido
                return@compute null
            }

            val agora = clock.now()
            val bloqueadoAte = atual.bloqueadoAte
            if (bloqueadoAte != null && agora < bloqueadoAte) {
                resultado = OtpResultado.Bloqueado(bloqueadoAte)
                return@compute atual
            }
            // Sem bloqueio ativo (nunca houve, ou já expirou): libera e zera tentativas antes de
            // seguir a validação normal.
            val entrada = if (bloqueadoAte != null) atual.copy(tentativas = 0, bloqueadoAte = null) else atual

            if (entrada.usado) {
                resultado = OtpResultado.Invalido
                return@compute entrada
            }
            if (agora - entrada.geradoEm > AuthRules.OTP_VALIDADE_MIN.minutes) {
                resultado = OtpResultado.Expirado
                return@compute entrada
            }

            if (codigo != entrada.codigo) {
                val tentativas = entrada.tentativas + 1
                return@compute if (tentativas >= AuthRules.MAX_TENTATIVAS) {
                    val ate = agora + AuthRules.BLOQUEIO_MIN.minutes
                    resultado = OtpResultado.Bloqueado(ate)
                    entrada.copy(tentativas = tentativas, bloqueadoAte = ate)
                } else {
                    resultado = OtpResultado.Invalido
                    entrada.copy(tentativas = tentativas)
                }
            }

            resultado = OtpResultado.Ok
            // Sucesso encerra a janela de erros: "5 erros SEGUIDOS" (AuthRules/US-002) não deve
            // acumular através de um login bem-sucedido.
            entrada.copy(usado = true, tentativas = 0)
        }
        return resultado
    }

    /**
     * Acesso exclusivo para testes: lê o último código gerado para [celular] sem passar por um
     * canal de SMS/WhatsApp real (este esqueleto não integra um provedor de verdade — ver
     * `authRoutes`). Nunca é exposto fora de testes: `authRoutes` não loga nem devolve o código
     * em nenhuma resposta ou log.
     */
    fun ultimoCodigoPara(celular: String): String? = entradas[celular]?.codigo

    private fun gerarCodigoAleatorio(): String = "%06d".format(rng.nextInt(1_000_000))
}
