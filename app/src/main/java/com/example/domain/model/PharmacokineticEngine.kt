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
        return compounds.map { cmp ->
            val ratio = variant?.ratio?.get(cmp.id) ?: (1.0 / compounds.size)
            val baseAmountForCompound = dose.doseAmount.toDouble() * ratio
            
            val potency = cmp.potencyMultiplier
            val effectiveAmount = baseAmountForCompound * potency
            
            val hl = cmp.halfLifeHours?.toDouble() ?: 6.0
            val peakMin = cmp.peakMin ?: 30
            val peak = peakMin / 60.0
            
            val concentration = calculateSingleCurve(effectiveAmount, dtHours, hl, peak)
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
