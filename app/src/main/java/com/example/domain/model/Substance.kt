package com.example.domain.model

data class Substance(
    val id: String,
    val name: String,
    val alias: String = "",
    val category: SubstanceCategory,
    val iconKey: String = "ic_pill",
    val defaultUnit: String = "mg",
    val active: Boolean = true,
    val notes: String = "",
    val archivedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
