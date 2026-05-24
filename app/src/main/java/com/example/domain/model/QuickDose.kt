package com.example.domain.model

data class QuickDose(
    val id: String,
    val substanceId: String? = null,
    val variantId: String? = null,
    val label: String,
    val defaultAmount: Float,
    val defaultUnit: String = "mg",
    val defaultRoute: String = "Oral",
    val defaultPrice: Float? = null,
    val pinned: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
