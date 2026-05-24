package com.example.domain.model

data class AppSettings(
    val id: String = "default",
    val userWeightKg: Float = 70.0f,
    val userAge: Int = 30,
    val metabolismFactor: Float = 1.0f,
    val themeMode: String = "Dark",
    val accentPalette: String = "Green",
    val privacyMode: Boolean = false,
    val financeMode: Boolean = true,
    val warningsEnabled: Boolean = true,
    val compactMode: Boolean = false,
    val hideFinanceMode: Boolean = false,
    val firstDayOfWeek: Int = 1,
    val currency: String = "USD",
    val defaultRoute: String = "Oral",
    val defaultUnit: String = "mg",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
