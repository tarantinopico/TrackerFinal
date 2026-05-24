package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "doses",
    foreignKeys = [
        ForeignKey(
            entity = SubstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["substanceId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = VariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variantId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["substanceId"]),
        Index(value = ["variantId"]),
        Index(value = ["timestamp"])
    ]
)
data class DoseEntity(
    @PrimaryKey val id: String,
    val substanceId: String,
    val variantId: String?,
    val doseAmount: Float,
    val unit: String,
    val route: String,
    val price: Float?,
    val timestamp: Long,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)
