package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class StatusCota(val rotulo: String) {
    PENDENTE("Aguardando pagamento"),
    PAGA("Paga"),
    REEMBOLSADA("Reembolsada"),
    EXPIRADA("Expirada"),
}

@Serializable
enum class TipoCota { PADRAO, COMPLEMENTO }

@Serializable
enum class OrigemPagamento { ATLETA, COBERTURA }

@Serializable
enum class MetodoPagamento(val rotulo: String) { PIX("Pix"), CARTAO("Cartão") }

/** RN-07: taxa de conveniencia fixa por cota, fora do rateio. */
object TaxaConveniencia {
    val PIX = Money(150)
    val CARTAO = Money(199)
    fun para(metodo: MetodoPagamento): Money = when (metodo) {
        MetodoPagamento.PIX -> PIX
        MetodoPagamento.CARTAO -> CARTAO
    }
}

@Serializable
data class Cota(
    val id: String,
    val valor: Money,
    /** Cobrada alem da cota; 0 enquanto pendente, em complementos e coberturas (RN-07). */
    val taxaConveniencia: Money = Money.ZERO,
    val tipo: TipoCota = TipoCota.PADRAO,
    val origem: OrigemPagamento? = null,
    val status: StatusCota = StatusCota.PENDENTE,
    val atletaId: String? = null,
    val atletaNome: String? = null,
    /** Cota do organizador: recebe a sobra de centavos (RN-04) e e paga primeiro (RN-09). */
    val organizador: Boolean = false,
    /** RN-19: trava de checkout. */
    val checkoutAte: Instant? = null,
    val pagaEm: Instant? = null,
    val metodo: MetodoPagamento? = null,
    val pagamentoId: String? = null,
    /** Para COMPLEMENTO: cota original do pagante (RN-16). */
    val cotaOriginalId: String? = null,
    val preAceiteRerrateio: Boolean = false,
) {
    val totalPago: Money get() = valor + taxaConveniencia

    /** Livre para um novo checkout (nao travada por outra pessoa). */
    fun livreParaCheckout(agora: Instant, usuarioId: String? = null): Boolean =
        status == StatusCota.PENDENTE &&
            (checkoutAte == null || checkoutAte <= agora || (usuarioId != null && atletaId == usuarioId))
}
