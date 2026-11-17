package br.com.sportconnect.core.data.fake

import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.model.Arena
import br.com.sportconnect.core.model.Ator
import br.com.sportconnect.core.model.Convite
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.Endereco
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.GeoPonto
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.MotivoConfirmacao
import br.com.sportconnect.core.model.MotivoExpiracao
import br.com.sportconnect.core.model.OrigemPagamento
import br.com.sportconnect.core.model.PapelUsuario
import br.com.sportconnect.core.model.Plano
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.QuadraEsporte
import br.com.sportconnect.core.model.RegistroTransicao
import br.com.sportconnect.core.model.Repasse
import br.com.sportconnect.core.model.Reserva
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusRepasse
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.core.model.TipoQuadra
import br.com.sportconnect.core.model.Usuario
import br.com.sportconnect.core.model.Visibilidade
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Dados de demonstracao realistas (Sao Paulo), gerados relativos ao [clock] para que a demo
 * sempre tenha jogos "proximos". Deterministico (Random com semente fixa).
 */
class DemoData(private val clock: Clock) {

    val agora: Instant = clock.now()
    val hoje: LocalDate = agora.toLocalDateTime(FusoSaoPaulo).date

    private val split = SplitCostUseCase()
    private val hold = HoldDeadlineCalculator()
    private val payout = PayoutCalculator()

    // ------------------------------------------------------------------ usuarios

    val organizador = Usuario(
        id = ID_ORGANIZADOR, nome = "Rafael Souza", celular = "+5511987654321", email = "rafael.souza@email.com",
        papeis = setOf(PapelUsuario.ATLETA, PapelUsuario.ORGANIZADOR), criadoEm = agora - 120.days,
    )
    val gestor = Usuario(
        id = ID_GESTOR, nome = "Marina Costa", celular = "+5511976543210", email = "marina@arenavilamadalena.com.br",
        papeis = setOf(PapelUsuario.GESTOR_ARENA), arenaId = ARENA_VILA_MADALENA, criadoEm = agora - 200.days,
    )
    val atletas: List<Usuario> = listOf(
        "Bruno Lima", "Carla Mendes", "Diego Alves", "Eduarda Rocha", "Felipe Martins", "Gabriel Nunes",
        "Helena Dias", "Igor Pereira", "Juliana Freitas", "Lucas Barbosa", "Mariana Teixeira", "Nicolas Cardoso",
        "Paula Ribeiro", "Renato Gomes", "Sofia Araújo", "Thiago Moreira", "Vanessa Castro", "André Silva",
    ).mapIndexed { i, nome ->
        Usuario(
            id = "u-atleta-${i + 1}", nome = nome, celular = "+55119${(81000000 + i * 137).toString().padStart(8, '0')}",
            criadoEm = agora - (30 + i * 3).days,
        )
    }
    val usuarios: List<Usuario> = listOf(organizador, gestor) + atletas
    private fun atleta(nome: String) = atletas.first { it.nome.startsWith(nome) }

    // ------------------------------------------------------------------ arenas e quadras

    val arenas: List<Arena> = listOf(
        Arena(
            id = ARENA_VILA_MADALENA, nome = "Arena Vila Madalena Sports", documento = "12.345.678/0001-90",
            endereco = Endereco("Rua Fradique Coutinho", "1200", "Vila Madalena", cep = "05416-001"),
            localizacao = GeoPonto(-23.5568, -46.6880), plano = Plano.PRO, duracaoHoldHoras = 24, janelaCancelamentoHoras = 24,
            comodidades = listOf("Vestiário", "Estacionamento", "Bar", "Churrasqueira", "Wi-Fi"),
            gestorId = ID_GESTOR, telefone = "(11) 3031-4455", subcontaGatewayId = "acc_vm_001",
            descricao = "Dois campos de society com grama sintética nova e quadra coberta de futsal. Bar com telão.",
            avaliacaoMedia = 4.8, totalAvaliacoes = 312,
        ),
        Arena(
            id = ARENA_PRAIA_PAULISTA, nome = "Praia Paulista Beach Club", documento = "23.456.789/0001-01",
            endereco = Endereco("Rua dos Pinheiros", "870", "Pinheiros", cep = "05422-001"),
            localizacao = GeoPonto(-23.5655, -46.6838), plano = Plano.PRO, duracaoHoldHoras = 6, janelaCancelamentoHoras = 12,
            comodidades = listOf("Vestiário", "Duchas", "Aluguel de raquetes", "Lanchonete"),
            telefone = "(11) 3062-7788", subcontaGatewayId = "acc_pp_002",
            descricao = "Três quadras de areia tratada com iluminação de LED. Beach tennis, vôlei de praia e futevôlei.",
            avaliacaoMedia = 4.7, totalAvaliacoes = 198,
        ),
        Arena(
            id = ARENA_MOOCA, nome = "Quadra Central Mooca", documento = "34.567.890/0001-12",
            endereco = Endereco("Rua da Mooca", "2100", "Mooca", cep = "03104-002"),
            localizacao = GeoPonto(-23.5590, -46.5990), plano = Plano.BASICO, duracaoHoldHoras = 8,
            comodidades = listOf("Vestiário", "Arquibancada", "Bebedouro"),
            telefone = "(11) 2692-1020", subcontaGatewayId = "acc_mc_003",
            descricao = "Ginásio poliesportivo coberto para futsal, vôlei, basquete e handebol, mais quadra externa de basquete.",
            avaliacaoMedia = 4.4, totalAvaliacoes = 87,
        ),
        Arena(
            id = ARENA_MOEMA, nome = "Padel Point Moema", documento = "45.678.901/0001-23",
            endereco = Endereco("Avenida Ibirapuera", "2900", "Moema", cep = "04029-200"),
            localizacao = GeoPonto(-23.6005, -46.6650), plano = Plano.PRO, duracaoHoldHoras = 8, janelaCancelamentoHoras = 24,
            comodidades = listOf("Vestiário", "Estacionamento com manobrista", "Pro shop", "Café"),
            telefone = "(11) 5051-3344", subcontaGatewayId = "acc_pm_004",
            descricao = "Quadras de padel panorâmicas com vidro temperado e uma quadra de tênis de saibro.",
            avaliacaoMedia = 4.9, totalAvaliacoes = 241,
        ),
        Arena(
            id = ARENA_TATUAPE, nome = "Complexo Esportivo Tatuapé", documento = "56.789.012/0001-34",
            endereco = Endereco("Rua Tuiuti", "1500", "Tatuapé", cep = "03081-000"),
            localizacao = GeoPonto(-23.5405, -46.5760), plano = Plano.BASICO, duracaoHoldHoras = 12,
            comodidades = listOf("Vestiário", "Estacionamento", "Lanchonete", "Wi-Fi"),
            telefone = "(11) 2295-6060", subcontaGatewayId = "acc_tt_005",
            descricao = "Complexo com dois campos de society e quadra coberta. Ótimo para rachas de fim de semana.",
            avaliacaoMedia = 4.5, totalAvaliacoes = 156,
        ),
        Arena(
            id = ARENA_BUTANTA, nome = "Areia Butantã Beach Sports", documento = "67.890.123/0001-45",
            endereco = Endereco("Rua Alvarenga", "1400", "Butantã", cep = "05509-002"),
            localizacao = GeoPonto(-23.5718, -46.7100), plano = Plano.BASICO, duracaoHoldHoras = 12,
            comodidades = listOf("Duchas", "Aluguel de raquetes", "Estacionamento"),
            telefone = "(11) 3726-9090", subcontaGatewayId = "acc_bt_006",
            descricao = "Areia fofa perto da USP. Aulas de beach tennis pela manhã e quadras livres à noite.",
            avaliacaoMedia = 4.6, totalAvaliacoes = 74,
        ),
    )

    val quadras: List<Quadra> = listOf(
        Quadra("q-vm-society1", ARENA_VILA_MADALENA, "Society 1", TipoQuadra.SOCIETY_SINTETICO, "Grama sintética", false,
            listOf(QuadraEsporte(Sport.FUTEBOL_SOCIETY, 14)), Money.reais(280), Money.reais(360)),
        Quadra("q-vm-society2", ARENA_VILA_MADALENA, "Society 2 (coberta)", TipoQuadra.SOCIETY_SINTETICO, "Grama sintética", true,
            listOf(QuadraEsporte(Sport.FUTEBOL_SOCIETY, 16)), Money.reais(320), Money.reais(400)),
        Quadra("q-vm-futsal", ARENA_VILA_MADALENA, "Quadra Futsal", TipoQuadra.QUADRA_POLIESPORTIVA, "Piso emborrachado", true,
            listOf(QuadraEsporte(Sport.FUTSAL, 12), QuadraEsporte(Sport.VOLEI, 12)), Money.reais(180), Money.reais(240)),

        Quadra("q-pp-areia1", ARENA_PRAIA_PAULISTA, "Areia 1", TipoQuadra.AREIA, "Areia tratada", false,
            listOf(QuadraEsporte(Sport.BEACH_TENNIS, 8), QuadraEsporte(Sport.VOLEI_DE_PRAIA, 12), QuadraEsporte(Sport.FUTEVOLEI, 8)),
            Money.reais(140), Money.reais(180)),
        Quadra("q-pp-areia2", ARENA_PRAIA_PAULISTA, "Areia 2", TipoQuadra.AREIA, "Areia tratada", false,
            listOf(QuadraEsporte(Sport.BEACH_TENNIS, 4)), Money.reais(120), Money.reais(160)),
        Quadra("q-pp-areia3", ARENA_PRAIA_PAULISTA, "Areia 3 (iluminada)", TipoQuadra.AREIA, "Areia tratada", false,
            listOf(QuadraEsporte(Sport.FUTEVOLEI, 8), QuadraEsporte(Sport.VOLEI_DE_PRAIA, 12)), Money.reais(130), Money.reais(170)),

        Quadra("q-mc-poli", ARENA_MOOCA, "Poliesportiva", TipoQuadra.QUADRA_POLIESPORTIVA, "Taco de madeira", true,
            listOf(QuadraEsporte(Sport.FUTSAL, 14), QuadraEsporte(Sport.VOLEI, 16), QuadraEsporte(Sport.BASQUETE, 12), QuadraEsporte(Sport.HANDEBOL, 16)),
            Money.reais(150), Money.reais(220)),
        Quadra("q-mc-basquete", ARENA_MOOCA, "Quadra de Basquete", TipoQuadra.PISO_RAPIDO, "Piso de concreto", false,
            listOf(QuadraEsporte(Sport.BASQUETE, 10)), Money.reais(120), Money.reais(160)),

        Quadra("q-pm-padel1", ARENA_MOEMA, "Padel 1 (panorâmica)", TipoQuadra.PADEL, "Grama sintética com areia", true,
            listOf(QuadraEsporte(Sport.PADEL, 4)), Money.reais(180), Money.reais(240)),
        Quadra("q-pm-padel2", ARENA_MOEMA, "Padel 2", TipoQuadra.PADEL, "Grama sintética com areia", true,
            listOf(QuadraEsporte(Sport.PADEL, 4)), Money.reais(160), Money.reais(220)),
        Quadra("q-pm-tenis", ARENA_MOEMA, "Tênis saibro", TipoQuadra.SAIBRO, "Saibro", false,
            listOf(QuadraEsporte(Sport.TENIS, 4)), Money.reais(150), Money.reais(200)),

        Quadra("q-tt-society1", ARENA_TATUAPE, "Society A", TipoQuadra.SOCIETY_SINTETICO, "Grama sintética", false,
            listOf(QuadraEsporte(Sport.FUTEBOL_SOCIETY, 16)), Money.reais(220), Money.reais(300)),
        Quadra("q-tt-society2", ARENA_TATUAPE, "Society B", TipoQuadra.SOCIETY_SINTETICO, "Grama sintética", false,
            listOf(QuadraEsporte(Sport.FUTEBOL_SOCIETY, 14)), Money.reais(240), Money.reais(320)),
        Quadra("q-tt-futsal", ARENA_TATUAPE, "Futsal coberta", TipoQuadra.QUADRA_POLIESPORTIVA, "Piso emborrachado", true,
            listOf(QuadraEsporte(Sport.FUTSAL, 12), QuadraEsporte(Sport.HANDEBOL, 14)), Money.reais(160), Money.reais(220)),

        Quadra("q-bt-areia1", ARENA_BUTANTA, "Areia 1", TipoQuadra.AREIA, "Areia fofa", false,
            listOf(QuadraEsporte(Sport.BEACH_TENNIS, 4)), Money.reais(120), Money.reais(150)),
        Quadra("q-bt-areia2", ARENA_BUTANTA, "Areia 2", TipoQuadra.AREIA, "Areia fofa", false,
            listOf(QuadraEsporte(Sport.BEACH_TENNIS, 8), QuadraEsporte(Sport.VOLEI_DE_PRAIA, 12)), Money.reais(130), Money.reais(170)),
        Quadra("q-bt-futevolei", ARENA_BUTANTA, "Futevôlei", TipoQuadra.AREIA, "Areia fofa", false,
            listOf(QuadraEsporte(Sport.FUTEVOLEI, 8)), Money.reais(120), Money.reais(160)),
    )

    // ------------------------------------------------------------------ helpers de tempo

    fun dia(offset: Int): LocalDate = hoje.plus(DatePeriod(days = offset))
    fun em(data: LocalDate, hora: Int, minuto: Int = 0): Instant = LocalDateTime(data, LocalTime(hora, minuto)).toInstant(FusoSaoPaulo)

    fun ehPico(data: LocalDate, hora: Int): Boolean {
        val fimDeSemana = data.dayOfWeek == DayOfWeek.SATURDAY || data.dayOfWeek == DayOfWeek.SUNDAY
        return if (fimDeSemana) hora in 8..19 else hora >= 18
    }

    fun slotId(quadraId: String, data: LocalDate, hora: Int) = "slot-$quadraId-$data-${hora.toString().padStart(2, '0')}"

    // ------------------------------------------------------------------ slots (hoje + 6 dias, 7h–23h)

    private fun gerarSlots(): MutableMap<String, Slot> {
        val mapa = LinkedHashMap<String, Slot>()
        for (q in quadras) for (d in 0 until DIAS_DE_SLOTS) for (h in PRIMEIRA_HORA..ULTIMA_HORA) {
            val data = dia(d)
            val id = slotId(q.id, data, h)
            mapa[id] = Slot(
                id = id, arenaId = q.arenaId, quadraId = q.id,
                inicio = em(data, h), fim = em(data, h) + 60.minutes,
                preco = if (ehPico(data, h)) q.precoHoraPico else q.precoHora,
            )
        }
        return mapa
    }

    // ------------------------------------------------------------------ jogos

    private fun quadra(id: String) = quadras.first { it.id == id }
    private fun arena(id: String) = arenas.first { it.id == id }

    private fun cotasPagas(reservaId: String, valor: Money, vagas: Int, pagantes: List<Usuario>, de: Instant, ate: Instant): List<Cota> {
        val valores = split(valor, vagas)
        val passo = if (pagantes.size <= 1) 0L else (ate - de).inWholeSeconds / pagantes.size
        return valores.mapIndexed { i, v ->
            val u = pagantes.getOrNull(i)
            if (u != null) {
                val metodo = if (i % 4 == 3) MetodoPagamento.CARTAO else MetodoPagamento.PIX
                Cota(
                    id = "$reservaId-c$i", valor = v, taxaConveniencia = TaxaConveniencia.para(metodo),
                    origem = OrigemPagamento.ATLETA, status = StatusCota.PAGA, atletaId = u.id, atletaNome = u.nome,
                    organizador = i == 0, pagaEm = de + (passo * i).seconds, metodo = metodo,
                    pagamentoId = "pg-seed-$reservaId-$i",
                )
            } else Cota(id = "$reservaId-c$i", valor = v)
        }
    }

    private data class Spec(
        val id: String,
        val titulo: String,
        val organizador: Usuario,
        val esporte: Sport,
        val quadraId: String,
        val dia: Int,
        val hora: Int,
        val vagas: Int,
        val pagantes: List<Usuario>,
        val status: StatusReserva,
        val codigo: String,
        val politica: PoliticaNaoPagamento = PoliticaNaoPagamento.ORGANIZADOR_COBRE,
        val visibilidade: Visibilidade = Visibilidade.PRIVADO,
        val ativadaHaHoras: Int = 2,
        val observacoes: String? = null,
    )

    private fun montar(spec: Spec, slots: Map<String, Slot>): Jogo {
        val q = quadra(spec.quadraId)
        val a = arena(q.arenaId)
        val data = dia(spec.dia)
        val sid = slotId(q.id, data, spec.hora)
        val inicio = em(data, spec.hora)
        val fim = inicio + 60.minutes
        val preco = slots[sid]?.preco ?: if (ehPico(data, spec.hora)) q.precoHoraPico else q.precoHora
        val reservaId = "res-${spec.id}"
        val garantia = if (spec.politica == PoliticaNaoPagamento.ORGANIZADOR_COBRE) GarantiaCobertura.PIX else GarantiaCobertura.NAO_SE_APLICA
        val ativadaEm = if (spec.dia < 0) inicio - 30.hours else agora - spec.ativadaHaHoras.hours
        val prazos = hold.calcular(ativadaEm, inicio, spec.politica, garantia, a.duracaoHoldHoras)
        val ultimoPagamento = if (spec.dia < 0) minOf(prazos.cotas, inicio) - 10.minutes else agora - 5.minutes
        var cotas = cotasPagas(reservaId, preco, spec.vagas, spec.pagantes, ativadaEm, ultimoPagamento)
        val historico = mutableListOf(RegistroTransicao(StatusReserva.RASCUNHO, StatusReserva.AGUARDANDO_PAGAMENTOS, ativadaEm, Ator.ORGANIZADOR))
        var reserva = Reserva(
            id = reservaId, valorTotal = preco, status = spec.status, ativadaEm = ativadaEm,
            prazoCotas = prazos.cotas, prazoHold = prazos.hold,
        )
        when (spec.status) {
            StatusReserva.AGUARDANDO_PAGAMENTOS -> reserva = reserva.copy(etapaPrazo = EtapaPrazo.COLETANDO_COTAS)
            StatusReserva.CONFIRMADO, StatusReserva.CONCLUIDO -> {
                val confirmadaEm = cotas.mapNotNull { it.pagaEm }.maxOrNull() ?: ativadaEm
                historico += RegistroTransicao(StatusReserva.AGUARDANDO_PAGAMENTOS, StatusReserva.CONFIRMADO, confirmadaEm, Ator.SISTEMA, "todas_pagas")
                if (spec.status == StatusReserva.CONCLUIDO) {
                    historico += RegistroTransicao(StatusReserva.CONFIRMADO, StatusReserva.EM_ANDAMENTO, inicio, Ator.SISTEMA)
                    historico += RegistroTransicao(StatusReserva.EM_ANDAMENTO, StatusReserva.CONCLUIDO, fim, Ator.SISTEMA)
                }
                reserva = reserva.copy(motivoConfirmacao = MotivoConfirmacao.TODAS_PAGAS, confirmadaEm = confirmadaEm)
            }
            StatusReserva.EXPIRADO -> {
                cotas = cotas.map { if (it.status == StatusCota.PAGA) it.copy(status = StatusCota.REEMBOLSADA) else it.copy(status = StatusCota.EXPIRADA) }
                historico += RegistroTransicao(StatusReserva.AGUARDANDO_PAGAMENTOS, StatusReserva.EXPIRADO, prazos.cotas, Ator.SISTEMA, "politica_b")
                reserva = reserva.copy(motivoExpiracao = MotivoExpiracao.POLITICA_B)
            }
            StatusReserva.CANCELADO -> {
                cotas = cotas.map { if (it.status == StatusCota.PAGA) it.copy(status = StatusCota.REEMBOLSADA) else it.copy(status = StatusCota.EXPIRADA) }
                historico += RegistroTransicao(StatusReserva.AGUARDANDO_PAGAMENTOS, StatusReserva.CANCELADO, agora - 1.hours, Ator.ARENA, spec.observacoes)
                reserva = reserva.copy(canceladoPor = Ator.ARENA, motivoCancelamento = spec.observacoes)
            }
            else -> Unit
        }
        reserva = reserva.copy(cotas = cotas, historico = historico, version = historico.size.toLong())
        return Jogo(
            id = spec.id, titulo = spec.titulo, organizadorId = spec.organizador.id, organizadorNome = spec.organizador.nome,
            esporte = spec.esporte, arenaId = a.id, arenaNome = a.nome, quadraId = q.id, quadraNome = q.nome, slotId = sid,
            inicio = inicio, fim = fim, numeroVagas = spec.vagas, politica = spec.politica, garantia = garantia,
            visibilidade = spec.visibilidade, observacoes = if (spec.status == StatusReserva.CANCELADO) null else spec.observacoes,
            codigoConvite = spec.codigo, reserva = reserva, criadoEm = ativadaEm - 15.minutes,
        )
    }

    private fun specs(): List<Spec> {
        val a = atletas
        return listOf(
            Spec(JOGO_RACHA, "Racha dos amigos", organizador, Sport.FUTEBOL_SOCIETY, "q-vm-society1", 2, 20, 12,
                listOf(organizador) + a.take(7), StatusReserva.AGUARDANDO_PAGAMENTOS, "RACHA8",
                observacoes = "Levar colete claro e escuro. Bar depois do jogo!"),
            Spec(JOGO_VOLEI, "Vôlei misto da Mooca", atleta("Carla"), Sport.VOLEI, "q-mc-poli", 1, 20, 12,
                listOf(atleta("Carla"), atleta("Helena"), atleta("Igor"), atleta("Paula"), atleta("Renato")),
                StatusReserva.AGUARDANDO_PAGAMENTOS, "VOLEI5", politica = PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES, ativadaHaHoras = 1),
            Spec(JOGO_FUTSAL_CONFIRMADO, "Futsal da firma", organizador, Sport.FUTSAL, "q-vm-futsal", 1, 19, 10,
                listOf(organizador) + a.drop(5).take(9), StatusReserva.CONFIRMADO, "FUTSAL", ativadaHaHoras = 20),
            Spec(JOGO_BEACH_CONCLUIDO, "Beach tennis de domingo", atleta("Juliana"), Sport.BEACH_TENNIS, "q-pp-areia2", -3, 9, 4,
                listOf(atleta("Juliana"), organizador, atleta("Mariana"), atleta("Felipe")), StatusReserva.CONCLUIDO, "BEACH9"),
            Spec(JOGO_SOCIETY_CONCLUIDO, "Society de terça", organizador, Sport.FUTEBOL_SOCIETY, "q-vm-society2", -2, 21, 14,
                listOf(organizador) + a.take(13), StatusReserva.CONCLUIDO, "TERCA1"),
            Spec(JOGO_PADEL_EXPIRADO, "Padel ranking", organizador, Sport.PADEL, "q-pm-padel1", -1, 18, 4,
                listOf(organizador, atleta("Bruno"), atleta("Diego")), StatusReserva.EXPIRADO, "PADEL4",
                politica = PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR),
            Spec(JOGO_SOCIETY_ABERTO, "Society aberto — falta gente!", atleta("Thiago"), Sport.FUTEBOL_SOCIETY, "q-tt-society1", 1, 21, 14,
                listOf(atleta("Thiago")) + a.take(8), StatusReserva.AGUARDANDO_PAGAMENTOS, "SOC14",
                visibilidade = Visibilidade.VAGAS_ABERTAS, ativadaHaHoras = 3, observacoes = "Nível intermediário. Chegue 10 min antes."),
            Spec(JOGO_BASQUETE_ABERTO, "Basquete 5x5", atleta("Lucas"), Sport.BASQUETE, "q-mc-basquete", 3, 19, 10,
                listOf(atleta("Lucas"), atleta("Nicolas"), atleta("Gabriel"), atleta("Igor"), atleta("André"), atleta("Diego")),
                StatusReserva.AGUARDANDO_PAGAMENTOS, "BASQ10", visibilidade = Visibilidade.VAGAS_ABERTAS, ativadaHaHoras = 1),
            Spec(JOGO_BEACH_ABERTO, "Beach tennis duplas", atleta("Sofia"), Sport.BEACH_TENNIS, "q-bt-areia1", 1, 18, 4,
                listOf(atleta("Sofia"), atleta("Vanessa")), StatusReserva.AGUARDANDO_PAGAMENTOS, "BEACH4",
                visibilidade = Visibilidade.VAGAS_ABERTAS, ativadaHaHoras = 1, observacoes = "Duplas mistas, nível iniciante."),
            Spec(JOGO_FUTEVOLEI_CANCELADO, "Futevôlei de sábado", organizador, Sport.FUTEVOLEI, "q-pp-areia3", 2, 8, 4,
                listOf(organizador, atleta("Eduarda")), StatusReserva.CANCELADO, "FUTV04", observacoes = "Manutenção da areia"),
        )
    }

    // ------------------------------------------------------------------ montagem final

    val slots: Map<String, Slot>
    val jogos: List<Jogo>
    val convites: List<Convite>
    val repasses: List<Repasse>

    init {
        val mapa = gerarSlots()
        jogos = specs().map { montar(it, mapa) }
        // Ocupa os slots dos jogos nao terminais.
        jogos.filter { it.status == StatusReserva.AGUARDANDO_PAGAMENTOS || it.status == StatusReserva.CONFIRMADO }.forEach { j ->
            mapa[j.slotId]?.let { mapa[j.slotId] = it.copy(status = if (j.status == StatusReserva.CONFIRMADO) SlotStatus.RESERVADO else SlotStatus.PRE_RESERVADO, jogoId = j.id) }
        }
        // Reservas externas (mensalistas) e bloqueios para dar realismo a agenda.
        val rnd = Random(42)
        for ((id, s) in mapa.entries.toList()) {
            if (s.status != SlotStatus.LIVRE) continue
            val hora = s.inicio.toLocalDateTime(FusoSaoPaulo).hour
            val data = s.inicio.toLocalDateTime(FusoSaoPaulo).date
            val r = rnd.nextInt(100)
            mapa[id] = when {
                ehPico(data, hora) && r < 30 -> s.copy(status = SlotStatus.RESERVADO, reservaExterna = if (r < 15) "Mensalista" else "Reserva balcão")
                !ehPico(data, hora) && r < 8 -> s.copy(status = SlotStatus.RESERVADO, reservaExterna = "Aula / escolinha")
                r in 95..96 -> s.copy(status = SlotStatus.BLOQUEADO, motivoBloqueio = "Manutenção")
                else -> s
            }
        }
        slots = mapa
        convites = jogos.map { Convite(id = "conv-${it.id}", jogoId = it.id, codigoCurto = it.codigoConvite, url = "https://sportconnect.app/j/${it.codigoConvite}", aberturas = it.cotasPagas * 2 + 3) }

        val concluidos = jogos.filter { it.status == StatusReserva.CONCLUIDO }.map { j ->
            val r = payout.gerarRepasse("rep-${j.id}", j.arenaId, j.id, j.valorTotal, arena(j.arenaId).plano, j.fim.toLocalDateTime(FusoSaoPaulo).date, "${j.esporte.displayName} · ${j.quadraNome}")
            if (r.dataPrevista <= hoje) r.copy(status = StatusRepasse.LIQUIDADO, liquidadoEm = em(r.dataPrevista, 10)) else r
        }
        // Historico de repasses da arena do gestor (Vila Madalena).
        val historicos = (4..12 step 2).mapIndexed { i, diasAtras ->
            val q = quadras.filter { it.arenaId == ARENA_VILA_MADALENA }[i % 3]
            val data = dia(-diasAtras)
            val bruto = q.precoHoraPico
            val r = payout.gerarRepasse("rep-hist-$i", ARENA_VILA_MADALENA, "hist-$i", bruto, Plano.PRO, data, "${q.esportes.first().esporte.displayName} · ${q.nome}")
            r.copy(status = if (i == 1) StatusRepasse.RETIDO else StatusRepasse.LIQUIDADO, liquidadoEm = if (i == 1) null else em(r.dataPrevista, 10))
        }
        repasses = concluidos + historicos
    }

    companion object {
        const val ID_ORGANIZADOR = "u-organizador"
        const val ID_GESTOR = "u-gestor"

        const val ARENA_VILA_MADALENA = "arena-vila-madalena"
        const val ARENA_PRAIA_PAULISTA = "arena-praia-paulista"
        const val ARENA_MOOCA = "arena-mooca"
        const val ARENA_MOEMA = "arena-moema"
        const val ARENA_TATUAPE = "arena-tatuape"
        const val ARENA_BUTANTA = "arena-butanta"

        const val JOGO_RACHA = "jogo-racha"
        const val JOGO_VOLEI = "jogo-volei"
        const val JOGO_FUTSAL_CONFIRMADO = "jogo-futsal"
        const val JOGO_BEACH_CONCLUIDO = "jogo-beach-domingo"
        const val JOGO_SOCIETY_CONCLUIDO = "jogo-society-terca"
        const val JOGO_PADEL_EXPIRADO = "jogo-padel"
        const val JOGO_SOCIETY_ABERTO = "jogo-society-aberto"
        const val JOGO_BASQUETE_ABERTO = "jogo-basquete"
        const val JOGO_BEACH_ABERTO = "jogo-beach-duplas"
        const val JOGO_FUTEVOLEI_CANCELADO = "jogo-futevolei"

        const val DIAS_DE_SLOTS = 7
        const val PRIMEIRA_HORA = 7
        const val ULTIMA_HORA = 22
    }
}
