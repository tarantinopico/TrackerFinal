package com.example.domain.pk

import kotlin.math.ln
import kotlin.math.abs
import kotlin.math.max

object TimeToClearCalculator {
    
    /**
     * Estimates hours remaining until the substance mathematically drops below a trace threshold.
     */
    fun estimateTimeToClear(
        params: PkCompoundParams,
        userData: PkUserData,
        clearanceThreshold: Double = 0.01 // Default considered cleared
    ): Double {
        var ke = params.ke * userData.metabolismFactor
        val ageFactor = if (userData.age > 40) 1.0 - ((userData.age - 40) * 0.005) else 1.0
        ke *= max(0.5, ageFactor)
        
        if (ke <= 0.0) return Double.MAX_VALUE

        val dF = (params.doseAmount * params.bioavailability) / userData.weightKg
        val ka = params.ka
        if (ka <= 0.0) return 0.0
        
        // In the terminal phase (t >> Tmax), the equation approximately reduces to the elimination arm:
        // C(t) ≈ dF * (ka / |ka - ke|) * exp(-ke * t)
        val interceptA = dF * (ka / abs(ka - ke))
        
        if (interceptA <= clearanceThreshold) return 0.0
        
        // clearanceThreshold = a * exp(-ke * t)
        // t = ln(a / clearanceThreshold) / ke
        val tHours = ln(interceptA / clearanceThreshold) / ke
        return max(0.0, tHours)
    }

}
