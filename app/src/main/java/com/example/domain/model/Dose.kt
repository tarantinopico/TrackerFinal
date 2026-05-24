package com.example.domain.model

data class Dose(
    val id: String,
    val substanceId: String,
    val variantId: String? = null,
    val doseAmount: Float,
    val unit: String = "mg",
    val route: String = "Oral",
    val price: Float? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
