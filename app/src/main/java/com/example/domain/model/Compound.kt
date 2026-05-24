package com.example.domain.model

data class CurvePoint(val t: Int, val c: Float)

data class Compound(
    val id: String,
    val substanceId: String,
    val name: String,
    val halfLifeHours: Float? = null,
    val onsetMin: Int? = null,
    val peakMin: Int? = null,
    val durationHours: Float? = null,
    val thresholdDose: Float? = null,
    val commonDose: Float? = null,
    val strongDose: Float? = null,
    val molecularWeight: Float? = null,
    val potencyMultiplier: Double = 1.0,
    val colorHex: String = "#FFFFFF",
    val active: Boolean = true,
    val useCurve: Boolean = false,
    val curve: List<CurvePoint> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
