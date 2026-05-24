package com.example.domain.model

object PharmacokineticEngine {
    
    /**
     * Calculates the active load or concentration of a substance and its compounds at a given time.
     * Takes into account exact variant ratios, potencies, and half-lives.
     * Returns a list of compound concentrations if we need exact breakdown.
     */
    data class CompoundLoad(
        val compoundId: String,
        val compoundName: String,
        val colorHex: String,
        val concentration: Double
    )
    
    fun calculateCurrentLoad(
        dose: Dose,
        substance: Substance?,
        compounds: List<Compound>,
        variant: Variant?,
        timeNow: Long
    ): List<CompoundLoad> {
        if (substance == null) return emptyList()
        val dtHours = (timeNow - dose.timestamp) / 3600000.0
        if (dtHours < 0.0) return emptyList()
        
        // If no compounds, treat the substance as one homogeneous compound with default info
        if (compounds.isEmpty()) {
            val halfLife = when (substance.category) {
                SubstanceCategory.STIMULANT -> 5.0
                SubstanceCategory.DEPRESSANT -> 4.0
                SubstanceCategory.PSYCHEDELIC -> 12.0
                SubstanceCategory.SUPPLEMENT -> 24.0
                else -> 6.0
            }
            val peak = 0.5 
            val concentration = calculateSingleCurve(dose.doseAmount.toDouble(), dtHours, halfLife, peak)
            return listOf(CompoundLoad("default", substance.name, substance.colorHex, concentration))
        }
        
        // With compounds, distribute dose by ratio.
        // If variant has exact ratios, use them, otherwise default to equal split.
        val baseDoseRaw = dose.doseAmount.toDouble()
        val isMacroUnit = dose.unit.equals("g", ignoreCase = true) || dose.unit.equals("kg", ignoreCase = true)
        val doseForCompounds = if (isMacroUnit) {
            UnitConverter.toMg(baseDoseRaw, dose.unit)
        } else {
            baseDoseRaw
        }

        return compounds.map { cmp ->
            val ratio = variant?.ratio?.get(cmp.id) ?: (1.0 / compounds.size)
            val baseAmountForCompound = doseForCompounds * ratio
            
            val potency = cmp.potencyMultiplier
            val effectiveAmount = baseAmountForCompound * potency
            
            val concentration = if (cmp.useCurve && cmp.curve.isNotEmpty()) {
                val dtMin = dtHours * 60.0
                calculateCustomCurve(effectiveAmount, dtMin, cmp.curve)
            } else {
                val hl = cmp.halfLifeHours?.toDouble() ?: 6.0
                val peakMin = cmp.peakMin ?: 30
                val peak = peakMin / 60.0
                calculateSingleCurve(effectiveAmount, dtHours, hl, peak)
            }
            
            CompoundLoad(cmp.id, cmp.name, cmp.colorHex, concentration)
        }
    }
    
    fun calculateTotalConcentration(
        dose: Dose,
        substance: Substance?,
        compounds: List<Compound>,
        variant: Variant?,
        timeNow: Long
    ): Double {
        val loads = calculateCurrentLoad(dose, substance, compounds, variant, timeNow)
        return loads.sumOf { it.concentration }
    }
    
    private fun calculateCustomCurve(amount: Double, dtMin: Double, curve: List<CurvePoint>): Double {
        if (amount <= 0.0 || curve.isEmpty()) return 0.0
        val sorted = curve.sortedBy { it.t }
        val first = sorted.first()
        val last = sorted.last()
        
        if (dtMin <= first.t) return amount * (first.c / 100.0)
        if (dtMin >= last.t) return amount * (last.c / 100.0)
        
        for (i in 0 until sorted.size - 1) {
            val p1 = sorted[i]
            val p2 = sorted[i+1]
            if (dtMin >= p1.t && dtMin <= p2.t) {
                val fraction = (dtMin - p1.t) / (p2.t - p1.t)
                val c = p1.c + (p2.c - p1.c) * fraction
                return amount * (c / 100.0)
            }
        }
        return 0.0
    }
    
    private fun calculateSingleCurve(amount: Double, dtHours: Double, halfLifeHours: Double, peakHours: Double): Double {
        if (amount <= 0.0) return 0.0
        val peakH = if (peakHours <= 0.0) 0.1 else peakHours 
        val hl = if (halfLifeHours <= 0.0) 1.0 else halfLifeHours
        
        return if (dtHours <= peakH) {
            // Ascending to peak
            amount * (dtHours / peakH)
        } else {
            // Decay from peak
            val postPeak = dtHours - peakH
            val decay = Math.pow(0.5, postPeak / hl)
            amount * decay
        }
    }
}
