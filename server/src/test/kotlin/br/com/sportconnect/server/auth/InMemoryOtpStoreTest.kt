package br.com.sportconnect.server.auth

import br.com.sportconnect.core.domain.repository.AuthRules
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InMemoryOtpStoreTest {
    @Test
    fun codigo_gerado_valida_uma_unica_vez() {
        val store = InMemoryOtpStore()
        val codigo = store.gerar("+5511988887777")
        assertEquals(OtpResultado.Ok, store.validar("+5511988887777", codigo))
        assertEquals(OtpResultado.Invalido, store.validar("+5511988887777", codigo)) // uso unico
    }

    @Test
    fun bloqueia_exatamente_na_quinta_tentativa_errada() {
        val store = InMemoryOtpStore()
        store.gerar("+5511988887777")
        repeat(4) { assertEquals(OtpResultado.Invalido, store.validar("+5511988887777", "000000")) }
        val quinta = store.validar("+5511988887777", "000000")
        assertIs<OtpResultado.Bloqueado>(quinta)
    }

    @Test
    fun codigo_gerado_tem_seis_digitos_numericos_e_varia_entre_chamadas() {
        val store = InMemoryOtpStore()
        val codigos = (1..50).map { store.gerar("+5511900000000") }

        codigos.forEach { codigo ->
            assertEquals(6, codigo.length)
            assertTrue(codigo.all(Char::isDigit), "codigo '$codigo' deveria ter só dígitos")
        }
        // Não é uma prova de qualidade criptográfica do RNG, mas pega regressões óbvias (ex.: um
        // gerador que sempre devolve o mesmo valor). Com SecureRandom sobre 10^6 valores, a chance
        // de 50 sorteios darem todos o mesmo valor é, na prática, zero.
        assertTrue(codigos.toSet().size > 1, "esperava códigos diferentes entre chamadas")
    }

    @Test
    fun tentativas_zeram_apos_sucesso_mesmo_apos_erros_anteriores() {
        val store = InMemoryOtpStore()
        val celular = "+5511900000005"

        val codigo1 = store.gerar(celular)
        val errado1 = codigoGarantidamenteErrado(codigo1)
        repeat(4) { assertEquals(OtpResultado.Invalido, store.validar(celular, errado1)) }
        assertEquals(OtpResultado.Ok, store.validar(celular, codigo1)) // 4 erros + sucesso: zera o contador

        // Nova rodada: se o sucesso anterior não tivesse zerado as tentativas, a 1ª tentativa
        // errada aqui já estaria "contando" a partir de 4 e bloquearia na 2ª, não na 5ª.
        val codigo2 = store.gerar(celular)
        val errado2 = codigoGarantidamenteErrado(codigo2)
        repeat(4) { assertEquals(OtpResultado.Invalido, store.validar(celular, errado2)) }
        assertIs<OtpResultado.Bloqueado>(store.validar(celular, errado2))
    }

    @Test
    fun validar_concorrente_com_codigo_errado_bloqueia_com_exatamente_quatro_invalidos() {
        val store = InMemoryOtpStore()
        val celular = "+5511900000006"
        store.gerar(celular)
        val totalChamadas = 20

        val resultados = executarEmParalelo(totalChamadas) { store.validar(celular, "aaaaaa") }

        val invalidos = resultados.count { it == OtpResultado.Invalido }
        val bloqueados = resultados.count { it is OtpResultado.Bloqueado }
        // Com a leitura-decisão-escrita atômica por chave (ConcurrentHashMap.compute), o resultado
        // sob concorrência é determinístico: só as 4 primeiras tentativas (de qualquer thread)
        // incrementam até MAX_TENTATIVAS - 1; a que leva o contador a MAX_TENTATIVAS bloqueia, e
        // todas as demais (que chegam depois, já bloqueadas) também veem Bloqueado — nenhuma
        // "rouba" um incremento de outra.
        assertEquals(AuthRules.MAX_TENTATIVAS - 1, invalidos)
        assertEquals(totalChamadas - (AuthRules.MAX_TENTATIVAS - 1), bloqueados)
    }

    @Test
    fun validar_concorrente_com_codigo_correto_so_uma_chamada_recebe_ok() {
        val store = InMemoryOtpStore()
        val celular = "+5511900000007"
        val codigo = store.gerar(celular)
        val totalChamadas = 20

        val resultados = executarEmParalelo(totalChamadas) { store.validar(celular, codigo) }

        // Uso único sob concorrência: mesmo com N threads validando o MESMO código correto ao
        // mesmo tempo, só uma pode "consumir" o OTP.
        assertEquals(1, resultados.count { it == OtpResultado.Ok })
        assertEquals(totalChamadas - 1, resultados.count { it == OtpResultado.Invalido })
    }

    /** Dispara [acao] simultaneamente em [totalChamadas] threads e devolve todos os resultados. */
    private fun executarEmParalelo(totalChamadas: Int, acao: () -> OtpResultado): List<OtpResultado> {
        val pool = Executors.newFixedThreadPool(totalChamadas)
        val largada = CountDownLatch(1)
        val resultados = Collections.synchronizedList(mutableListOf<OtpResultado>())
        try {
            val tarefas = (1..totalChamadas).map {
                pool.submit {
                    largada.await()
                    resultados.add(acao())
                }
            }
            largada.countDown()
            tarefas.forEach { it.get(10, TimeUnit.SECONDS) }
        } finally {
            pool.shutdown()
        }
        return resultados.toList()
    }

    /** Troca o primeiro dígito de [codigoReal] para garantir um código diferente (sem depender de sorte). */
    private fun codigoGarantidamenteErrado(codigoReal: String): String =
        ((codigoReal[0].digitToInt() + 1) % 10).toString() + codigoReal.substring(1)
}
