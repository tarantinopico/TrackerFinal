package com.example.domain.model

data class Variant(
    val id: String,
    val substanceId: String,
    val name: String,
    val colorHex: String = "#FFFFFF",
    val pricePerUnit: Float? = null,
    val unitLabel: String = "mg",
    val ratio: Map<String, Float> = emptyMap(),
    val roaDefault: String = "Oral",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
