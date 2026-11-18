package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.TaxaConveniencia

/** Resultado do rateio (RN-04) com a taxa de conveniencia por cota (RN-07). */
data class Rateio(
    val valorTotal: Money,
    val numeroVagas: Int,
    val cotaBase: Money,
    val sobra: Money,
    val cotaOrganizador: Money,
    val cotaConvidado: Money,
    val taxaConveniencia: Money,
) {
    val totalConvidado: Money get() = cotaConvidado + taxaConveniencia
    val totalOrganizador: Money get() = cotaOrganizador + taxaConveniencia
    /** RN-06: cota base minima de R$ 5,00. */
    val respeitaCotaMinima: Boolean get() = cotaBase >= SplitCostUseCase.COTA_MINIMA

    /** Lista das N cotas; indice 0 = organizador (com a sobra). Soma = V. */
    val cotas: List<Money> get() = List(numeroVagas) { if (it == 0) cotaOrganizador else cotaConvidado }
}

/**
 * Rateio (RN-04): `cota_base = floor(V/N)`; a sobra de centavos (0 <= sobra < N) vai SEMPRE para a cota
 * do organizador, que e paga primeiro. Invariante: soma das cotas == V.
 */
class SplitCostUseCase {

    /** Cotas em ordem; a primeira e a do organizador. */
    operator fun invoke(total: Money, slots: Int): List<Money> = calcular(total, slots).cotas

    fun calcular(total: Money, vagas: Int, metodo: MetodoPagamento = MetodoPagamento.PIX): Rateio {
        require(vagas > 0) { "O numero de vagas deve ser maior que zero." }
        require(total.cents >= 0) { "O valor total nao pode ser negativo." }
        val base = total.cents / vagas
        val sobra = total.cents - base * vagas
        return Rateio(
            valorTotal = total,
            numeroVagas = vagas,
            cotaBase = Money(base),
            sobra = Money(sobra),
            cotaOrganizador = Money(base + sobra),
            cotaConvidado = Money(base),
            taxaConveniencia = TaxaConveniencia.para(metodo),
        )
    }

    /** Maior N que respeita a cota minima (RN-06) — usado para sugerir reduzir vagas. */
    fun maxVagasParaCotaMinima(total: Money): Int = (total.cents / COTA_MINIMA.cents).toInt()

    companion object {
        val COTA_MINIMA = Money(500)
    }
}
