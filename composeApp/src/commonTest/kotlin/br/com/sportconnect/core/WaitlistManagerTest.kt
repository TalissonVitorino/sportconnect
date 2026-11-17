package br.com.sportconnect.core

import br.com.sportconnect.core.Fixtures.SABADO_20H
import br.com.sportconnect.core.Fixtures.SEXTA_10H
import br.com.sportconnect.core.Fixtures.ativo
import br.com.sportconnect.core.Fixtures.pagar
import br.com.sportconnect.core.domain.ListaEsperaException
import br.com.sportconnect.core.domain.ReservationStateMachine
import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.ListaEspera
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusListaEspera
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class WaitlistManagerTest {
    private val sm = ReservationStateMachine()
    private val wl = WaitlistManager()
    private val agora = SEXTA_10H + 1.hours

    private fun confirmado(): Jogo = pagar(sm, ativo(sm), 11)

    private fun filaCom(jogo: Jogo, n: Int): List<ListaEspera> {
        var fila = emptyList<ListaEspera>()
        repeat(n) { i -> fila = wl.entrar(jogo, fila, "w-$i", "Espera $i", agora + i.minutes) }
        return fila
    }

    @Test
    fun nao_entra_na_lista_se_ha_vaga_livre() {
        val j = pagar(sm, ativo(sm), 5)
        assertFalse(wl.podeEntrar(j, emptyList(), agora))
        assertFailsWith<ListaEsperaException> { wl.entrar(j, emptyList(), "w", "W", agora) }
    }

    @Test
    fun entra_em_ordem_fifo_quando_lotado() {
        val fila = filaCom(confirmado(), 3)
        assertEquals(listOf(1, 2, 3), fila.map { it.posicao })
        assertTrue(fila.all { it.status == StatusListaEspera.AGUARDANDO })
    }

    @Test
    fun limite_de_10_e_sem_duplicados() {
        val j = confirmado()
        val fila = filaCom(j, 10)
        assertFailsWith<ListaEsperaException> { wl.entrar(j, fila, "w-novo", "Novo", agora) }
        assertFailsWith<ListaEsperaException> { wl.entrar(j, filaCom(j, 2), "w-0", "Dup", agora) }
    }

    @Test
    fun lista_encerra_em_t_menos_1h() {
        assertFailsWith<ListaEsperaException> { wl.entrar(confirmado(), emptyList(), "w", "W", SABADO_20H - 1.hours) }
    }

    @Test
    fun vaga_liberada_notifica_primeiro_com_15_min_de_exclusividade() {
        val j = confirmado()
        val notificada = wl.notificarProximo(j, filaCom(j, 3), agora)
        val primeiro = notificada.first { it.usuarioId == "w-0" }
        assertEquals(StatusListaEspera.NOTIFICADO, primeiro.status)
        assertEquals(agora + 15.minutes, primeiro.exclusividadeAte)
        assertEquals("w-0", wl.exclusivo(notificada, agora)?.usuarioId)
        // Nao notifica um segundo enquanto o primeiro tem exclusividade.
        assertEquals(notificada, wl.notificarProximo(j, notificada, agora + 1.minutes))
    }

    @Test
    fun exclusividade_limitada_por_p_enquanto_coleta_cotas() {
        // Jogo lotado ainda em AGUARDANDO (todas travadas em checkout).
        val base = pagar(sm, ativo(sm), 10)
        val travado = base.copy(reserva = base.reserva.copy(cotas = base.reserva.cotas.map {
            if (it.status == StatusCota.PENDENTE) it.copy(checkoutAte = base.reserva.prazoCotas, atletaId = "x") else it
        }))
        val quasePrazo = travado.reserva.prazoCotas!! - 5.minutes
        val fila = wl.entrar(travado, emptyList(), "w", "W", quasePrazo - 1.minutes)
        val n = wl.notificarProximo(travado, fila, quasePrazo)
        assertEquals(travado.reserva.prazoCotas, n.single().exclusividadeAte)
    }

    @Test
    fun exclusividade_vencida_passa_ao_proximo() {
        val j = confirmado()
        val notificada = wl.notificarProximo(j, filaCom(j, 3), agora)
        val depois = wl.expirarNotificacoes(j, notificada, agora + 16.minutes)
        assertEquals(StatusListaEspera.EXPIRADO, depois.first { it.usuarioId == "w-0" }.status)
        assertEquals(StatusListaEspera.NOTIFICADO, depois.first { it.usuarioId == "w-1" }.status)
    }

    @Test
    fun sair_e_converter_reposicionam_a_fila() {
        val j = confirmado()
        val fila = wl.sair(filaCom(j, 3), "w-0")
        assertEquals(1, fila.first { it.usuarioId == "w-1" }.posicao)
        val convertida = wl.converter(wl.notificarProximo(j, fila, agora), "w-1")
        assertEquals(StatusListaEspera.CONVERTIDO, convertida.first { it.usuarioId == "w-1" }.status)
        assertEquals(1, convertida.first { it.usuarioId == "w-2" }.posicao)
    }

    @Test
    fun substituicao_reembolsa_original_sem_taxa_e_mantem_repasse_rn26() {
        val j = confirmado()
        val r = wl.substituir(j, "r-c3", "u-sub", "Substituto", agora)
        assertEquals(StatusCota.REEMBOLSADA, r.jogo.reserva.cotas.first { it.id == "r-c3" }.status)
        val nova = r.jogo.reserva.cotas.last()
        assertEquals("u-sub", nova.atletaId)
        assertEquals(StatusCota.PAGA, nova.status)
        assertEquals(Money(2_000), r.reembolso.total)
        assertEquals(Money.ZERO, r.reembolso.taxaDevolvida)
        assertEquals(12, r.jogo.cotasPagas)
        assertEquals(j.valorTotal, r.jogo.valorTotal)
        assertEquals(Money(24_000), r.jogo.valorArrecadado)
    }

    @Test
    fun substituicao_invalida_apos_t_menos_1h_para_organizador_ou_nao_confirmado() {
        val j = confirmado()
        assertFailsWith<ListaEsperaException> { wl.substituir(j, "r-c3", "u-sub", "S", SABADO_20H - 30.minutes) }
        assertFailsWith<ListaEsperaException> { wl.substituir(j, "r-c0", "u-sub", "S", agora) }
        assertFailsWith<ListaEsperaException> { wl.substituir(pagar(sm, ativo(sm), 3), "r-c1", "u-sub", "S", agora) }
    }
}
