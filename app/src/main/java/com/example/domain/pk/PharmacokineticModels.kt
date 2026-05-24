package com.example.domain.pk

import com.example.domain.model.AppSettings

data class PkUserData(
    val weightKg: Double,
    val age: Int,
    val metabolismFactor: Double
) {
    companion object {
        fun from(settings: AppSettings?): PkUserData {
            return PkUserData(
                weightKg = (settings?.userWeightKg?.toDouble() ?: 70.0),
                age = settings?.userAge ?: 30,
                metabolismFactor = (settings?.metabolismFactor?.toDouble() ?: 1.0)
            )
        }
    }
}

data class PkCompoundParams(
    val compoundId: String,
    val doseAmount: Double,
    val ka: Double,
    val ke: Double,
    val bioavailability: Double,
    val timestampMs: Long
)

data class PkPoint(
    val timeMs: Long,
    val concentration: Double,
    val isPeak: Boolean = false,
    val isOnset: Boolean = false
)

data class ThresholdStatus(
    val currentLoad: Double,
    val thresholdDose: Double?,
    val isActive: Boolean,
    val warningLevel: Int // 0: under threshold, 1: common, 2: strong
)
