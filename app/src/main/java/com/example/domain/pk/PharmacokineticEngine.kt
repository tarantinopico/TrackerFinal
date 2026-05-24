package com.example.domain.pk

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.abs

/**
 * Pure Deterministic Pharmacokinetic Mathematical Engine.
 * 
 * Utilizes a one-compartment, first-order absorption & elimination Bateman structure.
 * Completely decoupled from UI logic.
 */
object PharmacokineticEngine {

    /**
     * Elimination Rate Constant (ke)
     */
    fun calculateEliminationConstant(halfLifeHours: Double): Double {
        if (halfLifeHours <= 0.0) return 0.0
        return PkConstants.LN2 / halfLifeHours
    }

    /**
     * Mathematical Tmax representing the exact time (in hours) of peak concentration.
     */
    fun calculateTMax(ka: Double, ke: Double): Double {
        if (ka <= 0.0 || ke <= 0.0) return 0.0
        if (abs(ka - ke) < 0.0001) {
            // L'Hopital's limit as ka approaches ke
            return 1.0 / ke
        }
        return ln(ka / ke) / (ka - ke)
    }

    /**
     * Calculates concentration at a specific point in time (Bateman Function).
     */
    fun calculateConcentrationAtTime(
        timeMs: Long,
        params: PkCompoundParams,
        userData: PkUserData
    ): Double {
        val dtMs = timeMs - params.timestampMs
        if (dtMs < 0) return 0.0 // Pre-ingestion

        val tHours = dtMs / (1000.0 * 60 * 60)
        
        val ka = params.ka
        var ke = params.ke
        
        // Subject Modifiers
        ke *= userData.metabolismFactor

        // Age factor: gentle functional decline after 40
        val ageFactor = if (userData.age > 40) 1.0 - ((userData.age - 40) * 0.005) else 1.0
        ke *= max(0.5, ageFactor) // Don't drop elimination below 50% purely from age
        
        if (ka <= 0.0 || ke <= 0.0) return 0.0

        // Volume of distribution roughly approx by weight in simple one-compartment (1L/kg)
        val vd = userData.weightKg 
        val dF = (params.doseAmount * params.bioavailability) / vd

        if (abs(ka - ke) < 0.0001) {
            // Edge case where Absorption Rate == Elimination Rate
            return dF * ka * tHours * exp(-ke * tHours)
        }

        return dF * (ka / (ka - ke)) * (exp(-ke * tHours) - exp(-ka * tHours))
    }

    /**
     * Molds domain data into cleanly solvable math parameters
     */
    fun extractCompoundParams(
        compound: com.example.domain.model.Compound,
        dose: com.example.domain.model.Dose,
        variantRatio: Double,
        routeCorrection: RouteCorrection
    ): PkCompoundParams {
        val baseHalfLife = (compound.halfLifeHours?.toDouble() ?: 6.0).coerceAtLeast(0.1)
        val ke = calculateEliminationConstant(baseHalfLife)
        
        // Absorption driven by route
        val ka = routeCorrection.ka
        
        // The actual mg quantity entering the path
        val effectiveAmount = dose.doseAmount.toDouble() * variantRatio
        
        return PkCompoundParams(
            compoundId = compound.id,
            doseAmount = effectiveAmount,
            ka = ka,
            ke = ke,
            bioavailability = routeCorrection.bioavailabilityF,
            timestampMs = dose.timestamp
        )
    }
}
