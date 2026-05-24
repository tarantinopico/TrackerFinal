package com.example.domain.pk

enum class PkRoute {
    ORAL, SUBLINGUAL, INTRANASAL, INHALATION, INTRAVENOUS, OTHER
}

data class RouteCorrection(
    val bioavailabilityF: Double,
    val ka: Double
)

fun parseRoute(routeStr: String?): PkRoute {
    return when (routeStr?.lowercase()?.trim()) {
        "oral" -> PkRoute.ORAL
        "sublingual" -> PkRoute.SUBLINGUAL
        "intranasal" -> PkRoute.INTRANASAL
        "inhalation" -> PkRoute.INHALATION
        "intravenous", "iv" -> PkRoute.INTRAVENOUS
        else -> PkRoute.OTHER
    }
}

fun getRouteCorrection(route: PkRoute): RouteCorrection {
    return when (route) {
        PkRoute.ORAL -> RouteCorrection(
            bioavailabilityF = PkConstants.BIOAVAILABILITY_ORAL, 
            ka = PkConstants.KA_ORAL_DEFAULT
        )
        PkRoute.SUBLINGUAL -> RouteCorrection(
            bioavailabilityF = PkConstants.BIOAVAILABILITY_SUBLINGUAL, 
            ka = PkConstants.KA_SUBLINGUAL_DEFAULT
        )
        PkRoute.INTRANASAL -> RouteCorrection(
            bioavailabilityF = PkConstants.BIOAVAILABILITY_INTRANASAL, 
            ka = PkConstants.KA_INTRANASAL_DEFAULT
        )
        PkRoute.INHALATION -> RouteCorrection(
            bioavailabilityF = PkConstants.BIOAVAILABILITY_INHALATION, 
            ka = PkConstants.KA_INHALATION_DEFAULT
        )
        PkRoute.INTRAVENOUS -> RouteCorrection(
            bioavailabilityF = PkConstants.BIOAVAILABILITY_IV, 
            ka = PkConstants.KA_IV_DEFAULT
        )
        PkRoute.OTHER -> RouteCorrection(
            bioavailabilityF = PkConstants.BIOAVAILABILITY_ORAL, 
            ka = PkConstants.KA_ORAL_DEFAULT
        )
    }
}
