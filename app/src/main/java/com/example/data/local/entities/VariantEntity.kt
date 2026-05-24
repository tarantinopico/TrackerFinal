package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "variants",
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
data class VariantEntity(
    @PrimaryKey val id: String,
    val substanceId: String,
    val name: String,
    val colorHex: String,
    val pricePerUnit: Float?,
    val unitLabel: String,
    val ratioJson: String,
    val roaDefault: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
