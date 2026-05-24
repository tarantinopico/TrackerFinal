package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "compounds",
    foreignKeys = [
        ForeignKey(
            entity = SubstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["substanceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["substanceId"]),
        Index(value = ["createdAt"])
    ]
)
data class CompoundEntity(
    @PrimaryKey val id: String,
    val substanceId: String,
    val name: String,
    val halfLifeHours: Float?,
    val onsetMin: Int?,
    val peakMin: Int?,
    val durationHours: Float?,
    val thresholdDose: Float?,
    val commonDose: Float?,
    val strongDose: Float?,
    val molecularWeight: Float?,
    val potencyMultiplier: Double,
    val colorHex: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
