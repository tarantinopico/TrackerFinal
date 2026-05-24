package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "quick_doses",
    foreignKeys = [
        ForeignKey(
            entity = SubstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["substanceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["substanceId"]),
        Index(value = ["variantId"]),
        Index(value = ["orderIndex"])
    ]
)
data class QuickDoseEntity(
    @PrimaryKey val id: String,
    val substanceId: String?,
    val variantId: String?,
    val label: String,
    val defaultAmount: Float,
    val defaultUnit: String,
    val defaultRoute: String,
    val defaultPrice: Float?,
    val pinned: Boolean,
    val orderIndex: Int,
    val createdAt: Long,
    val updatedAt: Long
)
