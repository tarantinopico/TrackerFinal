package com.example.domain.backup

import com.example.domain.model.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupMetadata(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class BioTrackBackup(
    val metadata: BackupMetadata = BackupMetadata(),
    val settings: AppSettings? = null,
    val substances: List<Substance> = emptyList(),
    val compounds: List<Compound> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val doses: List<Dose> = emptyList(),
    val quickDoses: List<QuickDose> = emptyList()
)
