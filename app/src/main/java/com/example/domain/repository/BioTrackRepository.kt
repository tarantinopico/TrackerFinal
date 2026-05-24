package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow

interface BioTrackRepository {
    // Substance
    fun getActiveSubstances(): Flow<List<Substance>>
    fun getAllSubstances(): Flow<List<Substance>>
    suspend fun getSubstanceById(id: String): Substance?
    fun getSubstanceByIdFlow(id: String): Flow<Substance?>
    suspend fun saveSubstance(substance: Substance)
    suspend fun deleteSubstance(id: String)
    
    // Compound
    fun getCompoundsForSubstance(substanceId: String): Flow<List<Compound>>
    fun getAllCompounds(): Flow<List<Compound>>
    suspend fun saveCompound(compound: Compound)
    suspend fun deleteCompound(id: String)
    
    // Variant
    fun getVariantsForSubstance(substanceId: String): Flow<List<Variant>>
    fun getAllVariants(): Flow<List<Variant>>
    suspend fun saveVariant(variant: Variant)
    suspend fun deleteVariant(id: String)

    // Dose (former LogEntry)
    fun getAllDoses(): Flow<List<Dose>>
    fun getDosesForSubstance(substanceId: String): Flow<List<Dose>>
    suspend fun saveDose(dose: Dose)
    suspend fun deleteDose(id: String)
    
    // QuickDose
    fun getAllQuickDoses(): Flow<List<QuickDose>>
    suspend fun saveQuickDose(quickDose: QuickDose)
    suspend fun deleteQuickDose(id: String)
    
    // Settings
    fun getSettings(): Flow<AppSettings?>
    suspend fun saveSettings(settings: AppSettings)
}
