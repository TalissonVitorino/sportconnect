package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Estado da Reserva, exposto pelo Jogo (PRD §9.1). */
@Serializable
enum class StatusReserva(val rotulo: String, val terminal: Boolean) {
    RASCUNHO("Rascunho", false),
    AGUARDANDO_PAGAMENTOS("Pré-reservado · aguardando pagamentos", false),
    CONFIRMADO("Confirmado", false),
    EM_ANDAMENTO("Em andamento", false),
    CONCLUIDO("Concluído", true),
    CANCELADO("Cancelado", true),
    EXPIRADO("Expirado", true),
}

/** Sub-etapa de AGUARDANDO_PAGAMENTOS (campo `etapa_prazo`). */
@Serializable
enum class EtapaPrazo { COLETANDO_COTAS, JANELA_RESOLUCAO }

/** Politicas de nao pagamento (RN-14..RN-16). */
@Serializable
enum class PoliticaNaoPagamento(val rotulo: String, val descricao: String) {
    ORGANIZADOR_COBRE("Eu cubro a diferença", "Se faltar alguém no prazo, o organizador paga as cotas restantes."),
    CANCELAR_E_REEMBOLSAR("Cancelar e reembolsar", "Se faltar alguém no prazo, o jogo é cancelado e todos recebem o dinheiro de volta."),
    RERATEAR_ENTRE_PAGANTES("Dividir entre quem pagou", "Se faltar alguém, quem pagou pode aceitar pagar um pouco a mais."),
}

/** Garantia da politica A (RN-14). NAO_SE_APLICA para B/C. */
@Serializable
enum class GarantiaCobertura { PIX, CARTAO, NAO_SE_APLICA }

@Serializable
enum class Visibilidade { PRIVADO, VAGAS_ABERTAS }

@Serializable
enum class MotivoConfirmacao(val analytics: String) {
    TODAS_PAGAS("todas_pagas"), COBERTURA("cobertura"), RERRATEIO("rerrateio"), INTEGRAL("integral")
}

@Serializable
enum class MotivoExpiracao(val analytics: String) {
    POLITICA_B("politica_b"), COBERTURA_NAO_PAGA("cobertura_nao_paga"), RERRATEIO_FALHOU("rerrateio_falhou")
}

/** Registro imutavel de transicao (RN-34). */
@Serializable
data class RegistroTransicao(
    val de: StatusReserva,
    val para: StatusReserva,
    val em: Instant,
    val ator: Ator,
    val motivo: String? = null,
)

@Serializable
data class Reserva(
    val id: String,
    /** V — congelado no inicio do checkout (RN-02). */
    val valorTotal: Money,
    val status: StatusReserva = StatusReserva.RASCUNHO,
    val etapaPrazo: EtapaPrazo? = null,
    val ativadaEm: Instant? = null,
    /** P — prazo das cotas (RN-11). */
    val prazoCotas: Instant? = null,
    /** H — prazo do hold (RN-10). */
    val prazoHold: Instant? = null,
    val cotas: List<Cota> = emptyList(),
    val motivoCancelamento: String? = null,
    val canceladoPor: Ator? = null,
    val motivoConfirmacao: MotivoConfirmacao? = null,
    val motivoExpiracao: MotivoExpiracao? = null,
    val confirmadaEm: Instant? = null,
    val historico: List<RegistroTransicao> = emptyList(),
    val version: Long = 0,
)
