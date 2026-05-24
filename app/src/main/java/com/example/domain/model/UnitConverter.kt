package com.example.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

enum class MassUnit(val label: String, val multiplierToMg: BigDecimal) {
    NG("ng", BigDecimal("0.000001")),
    MCG("µg", BigDecimal("0.001")),
    MG("mg", BigDecimal("1")),
    G("g", BigDecimal("1000")),
    KG("kg", BigDecimal("1000000"));

    companion object {
        fun fromLabel(label: String): MassUnit {
            return values().find { it.label.equals(label, ignoreCase = true) || it.name.equals(label, ignoreCase = true) } ?: MG
        }
    }
}

object UnitConverter {
    fun convert(amount: Double, from: MassUnit, to: MassUnit): Double {
        if (from == to) return amount
        if (amount == 0.0) return 0.0
        val baseAmountInMg = BigDecimal(amount.toString()).multiply(from.multiplierToMg)
        val targetAmount = baseAmountInMg.divide(to.multiplierToMg, 8, RoundingMode.HALF_UP)
        return targetAmount.toDouble()
    }

    fun toMg(amount: Double, fromLabel: String): Double {
        val unit = MassUnit.fromLabel(fromLabel)
        return convert(amount, unit, MassUnit.MG)
    }

    fun fromMg(amountMg: Double, toLabel: String): Double {
        val unit = MassUnit.fromLabel(toLabel)
        return convert(amountMg, MassUnit.MG, unit)
    }
}
