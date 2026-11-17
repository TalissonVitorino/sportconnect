package br.com.sportconnect.core

import br.com.sportconnect.core.Fixtures.ORG
import br.com.sportconnect.core.Fixtures.SABADO_20H
import br.com.sportconnect.core.Fixtures.SEXTA_10H
import br.com.sportconnect.core.Fixtures.ativo
import br.com.sportconnect.core.Fixtures.pagar
import br.com.sportconnect.core.Fixtures.rascunho
import br.com.sportconnect.core.domain.EfeitoReserva
import br.com.sportconnect.core.domain.EventoReserva
import br.com.sportconnect.core.domain.ReservationStateMachine
import br.com.sportconnect.core.domain.SituacaoReembolso
import br.com.sportconnect.core.domain.TransicaoInvalidaException
import br.com.sportconnect.core.model.Ator
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.MotivoConfirmacao
import br.com.sportconnect.core.model.MotivoExpiracao
import br.com.sportconnect.core.model.OrigemPagamento
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ReservationStateMachineTest {
    private val sm = ReservationStateMachine()
    private val p = SEXTA_10H + 3.hours + 30.minutes // P padrao (A-Pix, hold 4h)
    private val h = SEXTA_10H + 4.hours

    private fun invalida(jogo: Jogo, evento: EventoReserva) {
        assertFailsWith<TransicaoInvalidaException> { sm.aplicar(jogo, evento) }
        assertTrue(sm.tentarAplicar(jogo, evento).isFailure)
    }

    private fun Jogo.cotas(status: StatusCota) = reserva.cotas.filter { it.status == status }

    // ------------------------------------------------------------- ativacao

    @Test
    fun ativar_calcula_h_e_p_e_vai_para_aguardando() {
        val j = ativo(sm)
        assertEquals(StatusReserva.AGUARDANDO_PAGAMENTOS, j.status)
        assertEquals(EtapaPrazo.COLETANDO_COTAS, j.etapaPrazo)
        assertEquals(h, j.reserva.prazoHold)
        assertEquals(p, j.reserva.prazoCotas)
        assertEquals(1, j.cotasPagas)
    }

    @Test
    fun ativar_emite_slot_pre_reservado() {
        val pago = sm.aplicar(rascunho(), EventoReserva.CotaPaga("r-c0", SEXTA_10H, ORG)).jogo
        val r = sm.aplicar(pago, EventoReserva.Ativar(SEXTA_10H))
        assertTrue(EfeitoReserva.SlotAlterado(SlotStatus.PRE_RESERVADO) in r.efeitos)
        assertEquals(1, r.efeitos<EfeitoReserva.Ativada>().size)
    }

    @Test
    fun ativar_sem_cota_do_organizador_paga_e_invalido() {
        invalida(rascunho(), EventoReserva.Ativar(SEXTA_10H))
    }

    @Test
    fun ativar_a_cartao_sem_pre_autorizacao_e_invalido() {
        val base = rascunho(garantia = GarantiaCobertura.CARTAO)
        val pago = sm.aplicar(base, EventoReserva.CotaPaga("r-c0", SEXTA_10H, ORG)).jogo
        invalida(pago, EventoReserva.Ativar(SEXTA_10H, garantiaAprovada = false))
    }

    @Test
    fun ativar_duas_vezes_e_invalido() {
        invalida(ativo(sm), EventoReserva.Ativar(SEXTA_10H))
    }

    @Test
    fun ativar_com_janela_menor_que_30_min_e_invalido_rn12() {
        val base = rascunho(inicio = SEXTA_10H + 2.hours + 20.minutes)
        val pago = sm.aplicar(base, EventoReserva.CotaPaga("r-c0", SEXTA_10H, ORG)).jogo
        invalida(pago, EventoReserva.Ativar(SEXTA_10H))
    }

    @Test
    fun pagamento_integral_com_uma_vaga_nasce_confirmado() {
        val base = rascunho(vagas = 1, valor = Money(18_000))
        val pago = sm.aplicar(base, EventoReserva.CotaPaga("r-c0", SEXTA_10H, ORG)).jogo
        val r = sm.aplicar(pago, EventoReserva.Ativar(SEXTA_10H))
        assertEquals(StatusReserva.CONFIRMADO, r.jogo.status)
        assertEquals(MotivoConfirmacao.INTEGRAL, r.jogo.reserva.motivoConfirmacao)
    }

    @Test
    fun em_rascunho_so_a_cota_do_organizador_pode_ser_paga() {
        invalida(rascunho(), EventoReserva.CotaPaga("r-c1", SEXTA_10H, "u-x"))
    }

    // ------------------------------------------------------------- pagamento de cotas

    @Test
    fun cota_paga_atualiza_progresso_sem_confirmar() {
        val j = pagar(sm, ativo(sm), 7)
        assertEquals(8, j.cotasPagas)
        assertEquals(4, j.vagasRestantes)
        assertEquals(StatusReserva.AGUARDANDO_PAGAMENTOS, j.status)
    }

    @Test
    fun ultima_cota_paga_confirma_rn17() {
        val j = pagar(sm, ativo(sm), 10)
        val ultima = j.reserva.cotas.first { it.status == StatusCota.PENDENTE }
        val r = sm.aplicar(j, EventoReserva.CotaPaga(ultima.id, SEXTA_10H + 1.hours, "u-ultimo"))
        assertEquals(StatusReserva.CONFIRMADO, r.jogo.status)
        assertEquals(MotivoConfirmacao.TODAS_PAGAS, r.jogo.reserva.motivoConfirmacao)
        assertNull(r.jogo.etapaPrazo)
        assertTrue(EfeitoReserva.SlotAlterado(SlotStatus.RESERVADO) in r.efeitos)
        assertEquals(MotivoConfirmacao.TODAS_PAGAS, r.efeitos<EfeitoReserva.Confirmada>().single().motivo)
    }

    @Test
    fun taxa_de_conveniencia_registrada_por_metodo() {
        val j = ativo(sm)
        val pix = sm.aplicar(j, EventoReserva.CotaPaga("r-c1", SEXTA_10H, "u-1", metodo = MetodoPagamento.PIX)).jogo
        val cartao = sm.aplicar(pix, EventoReserva.CotaPaga("r-c2", SEXTA_10H, "u-2", metodo = MetodoPagamento.CARTAO)).jogo
        assertEquals(Money(150), cartao.reserva.cotas.first { it.id == "r-c1" }.taxaConveniencia)
        assertEquals(Money(199), cartao.reserva.cotas.first { it.id == "r-c2" }.taxaConveniencia)
    }

    @Test
    fun pagar_cota_ja_paga_e_invalido() {
        val j = pagar(sm, ativo(sm), 1)
        invalida(j, EventoReserva.CotaPaga("r-c1", SEXTA_10H, "u-outro"))
    }

    @Test
    fun pagar_depois_de_p_e_invalido_rn18() {
        invalida(ativo(sm), EventoReserva.CotaPaga("r-c1", p + 1.minutes, "u-1"))
    }

    @Test
    fun uma_cota_por_pessoa_rn20() {
        val j = sm.aplicar(ativo(sm), EventoReserva.CotaPaga("r-c1", SEXTA_10H, "u-1")).jogo
        invalida(j, EventoReserva.CotaPaga("r-c2", SEXTA_10H, "u-1"))
    }

    // ------------------------------------------------------------- politica A

    @Test
    fun politica_a_pix_no_prazo_abre_janela_de_resolucao_de_30_min() {
        val j = pagar(sm, ativo(sm), 7)
        val r = sm.aplicar(j, EventoReserva.PrazoCotasExpirou(p))
        assertEquals(StatusReserva.AGUARDANDO_PAGAMENTOS, r.jogo.status)
        assertEquals(EtapaPrazo.JANELA_RESOLUCAO, r.jogo.etapaPrazo)
        val cobranca = r.efeitos<EfeitoReserva.CobrarCobertura>().single()
        assertEquals(Money(8_000), cobranca.valor)
        assertEquals(h, cobranca.ate)
        assertEquals(30.minutes, h - p)
    }

    @Test
    fun politica_a_cobertura_paga_confirma_sem_taxa() {
        val janela = sm.aplicar(pagar(sm, ativo(sm), 7), EventoReserva.PrazoCotasExpirou(p)).jogo
        val r = sm.aplicar(janela, EventoReserva.CoberturaPaga(p + 10.minutes, "pg-cob"))
        assertEquals(StatusReserva.CONFIRMADO, r.jogo.status)
        assertEquals(MotivoConfirmacao.COBERTURA, r.jogo.reserva.motivoConfirmacao)
        val cobertas = r.jogo.reserva.cotas.filter { it.origem == OrigemPagamento.COBERTURA }
        assertEquals(4, cobertas.size)
        assertTrue(cobertas.all { it.atletaId == ORG && it.taxaConveniencia == Money.ZERO && it.status == StatusCota.PAGA })
    }

    @Test
    fun politica_a_sem_cobertura_ate_h_expira_e_reembolsa_tudo_com_taxa() {
        val janela = sm.aplicar(pagar(sm, ativo(sm), 7), EventoReserva.PrazoCotasExpirou(p)).jogo
        val r = sm.aplicar(janela, EventoReserva.PrazoResolucaoExpirou(h))
        assertEquals(StatusReserva.EXPIRADO, r.jogo.status)
        assertEquals(MotivoExpiracao.COBERTURA_NAO_PAGA, r.jogo.reserva.motivoExpiracao)
        assertEquals(8, r.jogo.cotas(StatusCota.REEMBOLSADA).size)
        assertEquals(4, r.jogo.cotas(StatusCota.EXPIRADA).size)
        val reembolsos = r.efeitos<EfeitoReserva.Reembolsar>()
        assertEquals(8, reembolsos.size)
        assertTrue(reembolsos.all { it.reembolso.devolveTaxa && it.reembolso.situacao == SituacaoReembolso.EXPIRACAO })
    }

    @Test
    fun politica_a_cartao_captura_no_prazo_e_confirma_sem_janela() {
        val base = rascunho(garantia = GarantiaCobertura.CARTAO)
        val j = pagar(sm, ativo(sm, base = base), 5)
        assertEquals(j.reserva.prazoHold, j.reserva.prazoCotas)
        val r = sm.aplicar(j, EventoReserva.PrazoCotasExpirou(h))
        assertEquals(StatusReserva.CONFIRMADO, r.jogo.status)
        assertEquals(MotivoConfirmacao.COBERTURA, r.jogo.reserva.motivoConfirmacao)
        assertEquals(Money(12_000), r.efeitos<EfeitoReserva.PoliticaAplicada>().single().valorCoberto)
    }

    // ------------------------------------------------------------- politica B

    @Test
    fun politica_b_expira_reembolsa_pagas_com_taxa_e_expira_pendentes() {
        val base = rascunho(politica = PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR)
        val j = pagar(sm, ativo(sm, base = base), 9)
        assertEquals(h, j.reserva.prazoCotas)
        val r = sm.aplicar(j, EventoReserva.PrazoCotasExpirou(h))
        assertEquals(StatusReserva.EXPIRADO, r.jogo.status)
        assertEquals(MotivoExpiracao.POLITICA_B, r.jogo.reserva.motivoExpiracao)
        assertEquals(10, r.jogo.cotas(StatusCota.REEMBOLSADA).size)
        assertEquals(2, r.jogo.cotas(StatusCota.EXPIRADA).size)
        assertTrue(EfeitoReserva.SlotAlterado(SlotStatus.LIVRE) in r.efeitos)
        // Atletas recebem cota + R$ 1,50; organizador (cota sem taxa registrada no fixture? paga via CotaPaga PIX) tambem.
        assertTrue(r.efeitos<EfeitoReserva.Reembolsar>().all { it.reembolso.total == Money(2_150) })
    }

    // ------------------------------------------------------------- politica C

    private fun baseC(valor: Money = Money(24_000), teto: Int = 50) =
        rascunho(politica = PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES, valor = valor, teto = teto)

    @Test
    fun politica_c_viavel_gera_complementos_e_janela_de_aceite() {
        val j = pagar(sm, ativo(sm, base = baseC()), 9) // 10 pagantes, 2 pendentes
        val r = sm.aplicar(j, EventoReserva.PrazoCotasExpirou(p))
        assertEquals(EtapaPrazo.JANELA_RESOLUCAO, r.jogo.etapaPrazo)
        val comps = r.jogo.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO }
        assertEquals(10, comps.size)
        assertTrue(comps.all { it.valor == Money(400) && it.status == StatusCota.PENDENTE })
        assertEquals(2, r.jogo.cotas(StatusCota.EXPIRADA).size)
        assertEquals(10, r.efeitos<EfeitoReserva.PropostaRerrateio>().size)
        assertTrue(r.efeitos<EfeitoReserva.PropostaRerrateio>().all { it.ate == h })
    }

    @Test
    fun politica_c_sobra_do_complemento_vai_para_o_organizador() {
        val j = pagar(sm, ativo(sm, base = baseC(valor = Money(25_000))), 10) // 11 pagantes, falta 1 cota de 20,83
        val r = sm.aplicar(j, EventoReserva.PrazoCotasExpirou(p))
        val comps = r.jogo.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO }
        assertEquals(Money(193), comps.single { it.organizador }.valor) // 189 + sobra 4
        assertTrue(comps.filterNot { it.organizador }.all { it.valor == Money(189) })
        assertEquals(2_083, comps.sumOf { it.valor.cents })
    }

    @Test
    fun politica_c_todos_aceitam_confirma_por_rerrateio_sem_taxa() {
        var j = sm.aplicar(pagar(sm, ativo(sm, base = baseC()), 9), EventoReserva.PrazoCotasExpirou(p)).jogo
        val comps = j.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO }
        comps.forEach { c -> j = sm.aplicar(j, EventoReserva.CotaPaga(c.id, p + 5.minutes, c.atletaId)).jogo }
        assertEquals(StatusReserva.CONFIRMADO, j.status)
        assertEquals(MotivoConfirmacao.RERRATEIO, j.reserva.motivoConfirmacao)
        assertTrue(j.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO }.all { it.taxaConveniencia == Money.ZERO })
        assertEquals(Money(24_000), j.valorArrecadado)
    }

    @Test
    fun politica_c_aceite_incompleto_ate_h_expira_e_reembolsa_complementos() {
        var j = sm.aplicar(pagar(sm, ativo(sm, base = baseC()), 9), EventoReserva.PrazoCotasExpirou(p)).jogo
        val comps = j.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO }
        comps.take(5).forEach { c -> j = sm.aplicar(j, EventoReserva.CotaPaga(c.id, p + 5.minutes, c.atletaId)).jogo }
        val r = sm.aplicar(j, EventoReserva.PrazoResolucaoExpirou(h))
        assertEquals(StatusReserva.EXPIRADO, r.jogo.status)
        assertEquals(MotivoExpiracao.RERRATEIO_FALHOU, r.jogo.reserva.motivoExpiracao)
        assertEquals(15, r.efeitos<EfeitoReserva.Reembolsar>().size) // 10 cotas + 5 complementos
    }

    @Test
    fun politica_c_organizador_pode_cobrir_o_residual() {
        var j = sm.aplicar(pagar(sm, ativo(sm, base = baseC()), 9), EventoReserva.PrazoCotasExpirou(p)).jogo
        val comps = j.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO }
        comps.take(8).forEach { c -> j = sm.aplicar(j, EventoReserva.CotaPaga(c.id, p + 5.minutes, c.atletaId)).jogo }
        val r = sm.aplicar(j, EventoReserva.CoberturaPaga(p + 20.minutes))
        assertEquals(StatusReserva.CONFIRMADO, r.jogo.status)
        assertEquals(Money(800), r.efeitos<EfeitoReserva.PoliticaAplicada>().single().valorCoberto)
    }

    @Test
    fun politica_c_com_pagantes_abaixo_do_minimo_cai_para_b() {
        val j = pagar(sm, ativo(sm, base = baseC()), 8) // k = 9 < 10 (min society)
        val r = sm.aplicar(j, EventoReserva.PrazoCotasExpirou(p))
        assertEquals(StatusReserva.EXPIRADO, r.jogo.status)
        assertEquals(MotivoExpiracao.POLITICA_B, r.jogo.reserva.motivoExpiracao)
    }

    @Test
    fun politica_c_complemento_acima_do_teto_cai_para_b() {
        // Volei (min 8), 12 vagas, 8 pagantes: F = 4 x 2000 = 8000; base = 1000 > teto 10% (200).
        val base = rascunho(politica = PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES, esporte = Sport.VOLEI, teto = 10)
        val j = pagar(sm, ativo(sm, base = base), 7)
        assertEquals(StatusReserva.EXPIRADO, sm.aplicar(j, EventoReserva.PrazoCotasExpirou(p)).jogo.status)
    }

    // ------------------------------------------------------------- prazos invalidos

    @Test
    fun prazo_das_cotas_antes_de_p_e_invalido() {
        invalida(ativo(sm), EventoReserva.PrazoCotasExpirou(p - 1.minutes))
    }

    @Test
    fun eventos_da_janela_em_coletando_cotas_sao_invalidos() {
        val j = ativo(sm)
        invalida(j, EventoReserva.PrazoResolucaoExpirou(h))
        invalida(j, EventoReserva.CoberturaPaga(p))
    }

    @Test
    fun cota_padrao_nao_pode_ser_paga_na_janela_de_resolucao() {
        val janela = sm.aplicar(pagar(sm, ativo(sm), 7), EventoReserva.PrazoCotasExpirou(p)).jogo
        val pendente = janela.reserva.cotas.first { it.status == StatusCota.PENDENTE }
        invalida(janela, EventoReserva.CotaPaga(pendente.id, p + 1.minutes, "u-atrasado"))
    }

    @Test
    fun prazo_expirou_despacha_conforme_a_etapa() {
        val j = pagar(sm, ativo(sm), 7)
        val janela = sm.prazoExpirou(j, p).jogo
        assertEquals(EtapaPrazo.JANELA_RESOLUCAO, janela.etapaPrazo)
        assertEquals(StatusReserva.EXPIRADO, sm.prazoExpirou(janela, h).jogo.status)
    }

    // ------------------------------------------------------------- cancelamento

    @Test
    fun organizador_cancela_aguardando_com_reembolso_integral() {
        val j = pagar(sm, ativo(sm), 3)
        val r = sm.aplicar(j, EventoReserva.CancelarPor(Ator.ORGANIZADOR, SEXTA_10H + 1.hours, "Choveu"))
        assertEquals(StatusReserva.CANCELADO, r.jogo.status)
        assertEquals(Ator.ORGANIZADOR, r.jogo.reserva.canceladoPor)
        assertEquals(4, r.efeitos<EfeitoReserva.Reembolsar>().size)
        assertTrue(r.efeitos<EfeitoReserva.Reembolsar>().all { it.reembolso.devolveTaxa })
        assertTrue(EfeitoReserva.SlotAlterado(SlotStatus.LIVRE) in r.efeitos)
    }

    @Test
    fun organizador_cancela_confirmado_so_dentro_da_janela_rn23() {
        val confirmado = pagar(sm, ativo(sm), 11)
        assertEquals(StatusReserva.CONFIRMADO, confirmado.status)
        val dentro = SABADO_20H - 25.hours
        assertEquals(StatusReserva.CANCELADO, sm.aplicar(confirmado, EventoReserva.CancelarPor(Ator.ORGANIZADOR, dentro, null, 24)).jogo.status)
        invalida(confirmado, EventoReserva.CancelarPor(Ator.ORGANIZADOR, SABADO_20H - 23.hours, null, 24))
    }

    @Test
    fun arena_cancela_confirmado_exige_motivo_rn24() {
        val confirmado = pagar(sm, ativo(sm), 11)
        val agora = SABADO_20H - 3.hours
        invalida(confirmado, EventoReserva.CancelarPor(Ator.ARENA, agora, " "))
        val r = sm.aplicar(confirmado, EventoReserva.CancelarPor(Ator.ARENA, agora, "Chuva forte"))
        assertEquals(StatusReserva.CANCELADO, r.jogo.status)
        assertEquals(12, r.efeitos<EfeitoReserva.Reembolsar>().size)
        assertTrue(r.efeitos<EfeitoReserva.Reembolsar>().all { it.reembolso.situacao == SituacaoReembolso.CANCELAMENTO_ARENA })
    }

    @Test
    fun atleta_ou_sistema_nao_cancelam_e_rascunho_nao_cancela() {
        val j = ativo(sm)
        invalida(j, EventoReserva.CancelarPor(Ator.ATLETA, SEXTA_10H, "x"))
        invalida(j, EventoReserva.CancelarPor(Ator.SISTEMA, SEXTA_10H, "x"))
        invalida(rascunho(), EventoReserva.CancelarPor(Ator.ORGANIZADOR, SEXTA_10H, "x"))
    }

    // ------------------------------------------------------------- saida voluntaria

    @Test
    fun atleta_sai_antes_da_confirmacao_reembolso_sem_taxa_e_vaga_reaberta_rn21() {
        val j = pagar(sm, ativo(sm), 3)
        val r = sm.aplicar(j, EventoReserva.AtletaSaiu("r-c1", SEXTA_10H + 1.hours))
        assertEquals(StatusCota.REEMBOLSADA, r.jogo.reserva.cotas.first { it.id == "r-c1" }.status)
        assertEquals(13, r.jogo.reserva.cotas.size)
        assertEquals(StatusCota.PENDENTE, r.jogo.reserva.cotas.last().status)
        assertEquals(Money(2_000), r.jogo.reserva.cotas.last().valor)
        val reembolso = r.efeitos<EfeitoReserva.Reembolsar>().single().reembolso
        assertEquals(Money(2_000), reembolso.total)
        assertEquals(3, r.jogo.cotasPagas)
        assertEquals(9, r.jogo.vagasRestantes)
    }

    @Test
    fun saida_invalida_para_organizador_e_apos_confirmacao() {
        invalida(ativo(sm), EventoReserva.AtletaSaiu("r-c0", SEXTA_10H))
        val confirmado = pagar(sm, ativo(sm), 11)
        invalida(confirmado, EventoReserva.AtletaSaiu("r-c1", SEXTA_10H + 1.hours))
    }

    // ------------------------------------------------------------- execucao

    @Test
    fun iniciar_e_concluir_nos_horarios_rn29() {
        val confirmado = pagar(sm, ativo(sm), 11)
        invalida(confirmado, EventoReserva.IniciarJogo(SABADO_20H - 1.minutes))
        val andamento = sm.aplicar(confirmado, EventoReserva.IniciarJogo(SABADO_20H)).jogo
        assertEquals(StatusReserva.EM_ANDAMENTO, andamento.status)
        invalida(andamento, EventoReserva.Concluir(SABADO_20H + 30.minutes))
        assertEquals(StatusReserva.CONCLUIDO, sm.aplicar(andamento, EventoReserva.Concluir(SABADO_20H + 1.hours)).jogo.status)
    }

    @Test
    fun iniciar_sem_confirmar_e_concluir_sem_iniciar_sao_invalidos() {
        invalida(ativo(sm), EventoReserva.IniciarJogo(SABADO_20H))
        invalida(pagar(sm, ativo(sm), 11), EventoReserva.Concluir(SABADO_20H + 2.hours))
    }

    @Test
    fun estados_terminais_rejeitam_qualquer_evento() {
        val confirmado = pagar(sm, ativo(sm), 11)
        val concluido = sm.aplicar(sm.aplicar(confirmado, EventoReserva.IniciarJogo(SABADO_20H)).jogo, EventoReserva.Concluir(SABADO_20H + 1.hours)).jogo
        val cancelado = sm.aplicar(ativo(sm), EventoReserva.CancelarPor(Ator.ORGANIZADOR, SEXTA_10H, null)).jogo
        val expirado = sm.aplicar(ativo(sm, base = rascunho(politica = PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR)), EventoReserva.PrazoCotasExpirou(h)).jogo
        val agora = SABADO_20H + 2.hours
        for (j in listOf(concluido, cancelado, expirado)) {
            assertTrue(j.status.terminal)
            listOf(
                EventoReserva.Ativar(agora), EventoReserva.CotaPaga("r-c1", agora, "u"), EventoReserva.PrazoCotasExpirou(agora),
                EventoReserva.CoberturaPaga(agora), EventoReserva.PrazoResolucaoExpirou(agora),
                EventoReserva.CancelarPor(Ator.ARENA, agora, "x"), EventoReserva.AtletaSaiu("r-c1", agora),
                EventoReserva.IniciarJogo(agora), EventoReserva.Concluir(agora),
            ).forEach { invalida(j, it) }
        }
    }

    // ------------------------------------------------------------- auditoria e slot

    @Test
    fun historico_registra_transicoes_e_versao_cresce_rn34() {
        val confirmado = pagar(sm, ativo(sm), 11)
        val trilha = confirmado.reserva.historico.map { it.de to it.para }
        assertEquals(
            listOf(
                StatusReserva.RASCUNHO to StatusReserva.AGUARDANDO_PAGAMENTOS,
                StatusReserva.AGUARDANDO_PAGAMENTOS to StatusReserva.CONFIRMADO,
            ),
            trilha,
        )
        assertTrue(confirmado.reserva.version > rascunho().reserva.version + 10)
    }

    @Test
    fun status_do_slot_por_estado_da_reserva() {
        assertEquals(SlotStatus.EM_CHECKOUT, ReservationStateMachine.slotStatusPara(StatusReserva.RASCUNHO))
        assertEquals(SlotStatus.PRE_RESERVADO, ReservationStateMachine.slotStatusPara(StatusReserva.AGUARDANDO_PAGAMENTOS))
        assertEquals(SlotStatus.RESERVADO, ReservationStateMachine.slotStatusPara(StatusReserva.CONFIRMADO))
        assertEquals(SlotStatus.RESERVADO, ReservationStateMachine.slotStatusPara(StatusReserva.EM_ANDAMENTO))
        assertEquals(SlotStatus.LIVRE, ReservationStateMachine.slotStatusPara(StatusReserva.EXPIRADO))
        assertEquals(SlotStatus.LIVRE, ReservationStateMachine.slotStatusPara(StatusReserva.CANCELADO))
    }
}
