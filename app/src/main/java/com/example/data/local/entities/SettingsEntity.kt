package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: String = "default",
    val userWeightKg: Float,
    val userAge: Int,
    val metabolismFactor: Float,
    val themeMode: String,
    val accentPalette: String,
    val privacyMode: Boolean,
    val financeMode: Boolean,
    val warningsEnabled: Boolean,
    val compactMode: Boolean,
    val hideFinanceMode: Boolean,
    val firstDayOfWeek: Int,
    val currency: String,
    val defaultRoute: String,
    val defaultUnit: String,
    val createdAt: Long,
    val updatedAt: Long
)
