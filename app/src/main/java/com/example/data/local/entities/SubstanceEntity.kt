package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "substances",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["archivedAt"])
    ]
)
data class SubstanceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val alias: String,
    val category: String,
    val iconKey: String,
    val defaultUnit: String,
    val active: Boolean,
    val notes: String,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
