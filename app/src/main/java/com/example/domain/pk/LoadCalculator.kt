package com.example.domain.pk

import com.example.domain.model.Compound
import com.example.domain.model.Dose
import com.example.domain.model.Variant

object LoadCalculator {
    
    /**
     * Calculates the summed load of all active compounds across all valid doses at T.
     */
    fun calculateTotalLoadAtTime(
        timeMs: Long,
        doses: List<Dose>,
        compounds: List<Compound>,
        variants: List<Variant>,
        userData: PkUserData
    ): Double {
        var totalLoad = 0.0
        
        for (dose in doses) {
            // Ignore future doses just in case
            if (dose.timestamp > timeMs) continue

            val doseRoute = parseRoute(dose.route)
            val routeCorr = getRouteCorrection(doseRoute)
            
            val variant = variants.find { it.id == dose.variantId }
            val substanceCompounds = compounds.filter { it.substanceId == dose.substanceId }
            
            for (compound in substanceCompounds) {
                // Determine ratio, default 1.0 if not explicit in variant
                val ratio = variant?.ratio?.get(compound.id)?.toDouble() ?: 1.0
                
                val params = PharmacokineticEngine.extractCompoundParams(
                    compound = compound,
                    dose = dose,
                    variantRatio = ratio,
                    routeCorrection = routeCorr
                )
                
                totalLoad += PharmacokineticEngine.calculateConcentrationAtTime(timeMs, params, userData)
            }
        }
        
        return totalLoad
    }

    /**
     * A rough average daily load metric based on sampling the 24 hour rolling window
     */
    fun getDailyLoadEstimate(
        activeDoses: List<Dose>,
        compounds: List<Compound>,
        variants: List<Variant>,
        userData: PkUserData,
        nowMs: Long = System.currentTimeMillis()
    ): Double {
        val startMs = nowMs - (12 * 60 * 60 * 1000L)
        val endMs = nowMs + (12 * 60 * 60 * 1000L)
        
        var sum = 0.0
        var count = 0
        for (t in startMs..endMs step 3600000L) { // 1 hr steps
            sum += calculateTotalLoadAtTime(t, activeDoses, compounds, variants, userData)
            count++
        }
        return if (count > 0) sum / count else 0.0
    }
    
    /**
     * Generates an array of mathematically sound points for UI graphs
     */
    fun buildTrendCurve(
        activeDoses: List<Dose>,
        compounds: List<Compound>,
        variants: List<Variant>,
        userData: PkUserData,
        startMs: Long,
        endMs: Long,
        resolutionMs: Long = 15 * 60 * 1000L
    ): List<PkPoint> {
        val points = mutableListOf<PkPoint>()
        if (startMs >= endMs || resolutionMs <= 0) return points

        for (time in startMs..endMs step resolutionMs) {
            val load = calculateTotalLoadAtTime(time, activeDoses, compounds, variants, userData)
            points.add(PkPoint(time, load))
        }
        return points
    }

    /**
     * Threshold safety validation
     */
    fun checkThreshold(load: Double, compound: Compound): ThresholdStatus {
        val thresh = compound.thresholdDose?.toDouble() ?: 0.0
        val isAct = thresh > 0.0 && load >= thresh
        val common = compound.commonDose?.toDouble() ?: 0.0
        val strong = compound.strongDose?.toDouble() ?: 0.0
        
        var level = 0
        if (common > 0.0 && load >= common) level = 1
        if (strong > 0.0 && load >= strong) level = 2
        
        return ThresholdStatus(load, if (thresh > 0.0) thresh else null, isAct, level)
    }
}
