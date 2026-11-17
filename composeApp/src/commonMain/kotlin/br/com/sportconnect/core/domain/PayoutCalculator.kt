package br.com.sportconnect.core.domain

import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Plano
import br.com.sportconnect.core.model.Repasse
import br.com.sportconnect.core.model.StatusRepasse
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class CalculoRepasse(val bruto: Money, val comissao: Money, val liquido: Money, val percentual: Int)

/**
 * Repasse a arena (RN-30): `liquido = V − comissao`, comissao = % do plano sobre V (RN-07a: Basico 8%, Pro 4%).
 * A taxa de conveniencia NAO entra (RN-07). Arredondamento da comissao: meio centavo para cima (half-up),
 * sempre em inteiros. Liberacao em D+1 util apos CONCLUIDO (fins de semana pulados; feriados fora do MVP).
 */
class PayoutCalculator {

    fun comissao(valorTotal: Money, plano: Plano): Money {
        require(valorTotal.cents >= 0) { "Valor negativo." }
        return Money((valorTotal.cents * plano.comissaoPercentual + 50) / 100)
    }

    fun calcular(valorTotal: Money, plano: Plano): CalculoRepasse {
        val c = comissao(valorTotal, plano)
        return CalculoRepasse(valorTotal, c, valorTotal - c, plano.comissaoPercentual)
    }

    fun proximoDiaUtil(data: LocalDate): LocalDate {
        var d = data.plus(DatePeriod(days = 1))
        while (d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY) d = d.plus(DatePeriod(days = 1))
        return d
    }

    fun gerarRepasse(
        id: String,
        arenaId: String,
        jogoId: String,
        valorTotal: Money,
        plano: Plano,
        dataConclusao: LocalDate,
        descricao: String = "",
    ): Repasse {
        val calc = calcular(valorTotal, plano)
        return Repasse(
            id = id,
            arenaId = arenaId,
            jogoId = jogoId,
            valorBruto = calc.bruto,
            comissao = calc.comissao,
            valorLiquido = calc.liquido,
            dataPrevista = proximoDiaUtil(dataConclusao),
            status = StatusRepasse.AGENDADO,
            descricao = descricao,
        )
    }
}
