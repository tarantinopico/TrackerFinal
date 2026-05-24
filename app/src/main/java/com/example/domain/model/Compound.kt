package com.example.domain.model

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
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
