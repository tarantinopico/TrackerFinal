package com.example.domain.pk

import kotlin.math.ln

/**
 * Pure PK mathematical constants and default values.
 */
object PkConstants {
    val LN2 = ln(2.0)
    
    // Default bioavailability fractions (F) based on standard clinical averages
    const val BIOAVAILABILITY_ORAL = 0.5
    const val BIOAVAILABILITY_SUBLINGUAL = 0.6
    const val BIOAVAILABILITY_INTRANASAL = 0.75
    const val BIOAVAILABILITY_INHALATION = 0.9
    const val BIOAVAILABILITY_IV = 1.0

    // Default absorption rate constants (ka) [per hour]
    // A higher ka means faster absorption
    const val KA_ORAL_DEFAULT = 1.5
    const val KA_SUBLINGUAL_DEFAULT = 3.0
    const val KA_INTRANASAL_DEFAULT = 6.0
    const val KA_INHALATION_DEFAULT = 12.0
    const val KA_IV_DEFAULT = 120.0 // Almost instant
}
