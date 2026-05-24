package com.example.data.repository

import com.example.data.local.BioTrackDatabase
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.*
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BioTrackRepositoryImpl(
    private val db: BioTrackDatabase
) : BioTrackRepository {

    override fun getActiveSubstances(): Flow<List<Substance>> =
        db.substanceDao().getActiveSubstances().map { list -> list.map { it.toDomain() } }

    override fun getAllSubstances(): Flow<List<Substance>> =
        db.substanceDao().getAllSubstances().map { list -> list.map { it.toDomain() } }

    override suspend fun getSubstanceById(id: String): Substance? =
        db.substanceDao().getSubstanceById(id)?.toDomain()

    override fun getSubstanceByIdFlow(id: String): Flow<Substance?> =
        db.substanceDao().getSubstanceByIdFlow(id).map { it?.toDomain() }

    override suspend fun saveSubstance(substance: Substance) {
        db.substanceDao().insertSubstance(substance.toEntity())
    }

    override suspend fun deleteSubstance(id: String) {
        db.substanceDao().deleteSubstance(id)
    }

    override fun getCompoundsForSubstance(substanceId: String): Flow<List<Compound>> =
        db.compoundDao().getCompoundsForSubstance(substanceId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveCompound(compound: Compound) {
        db.compoundDao().insertCompound(compound.toEntity())
    }

    override suspend fun deleteCompound(id: String) {
        db.compoundDao().deleteCompound(id)
    }

    override fun getVariantsForSubstance(substanceId: String): Flow<List<Variant>> =
        db.variantDao().getVariantsForSubstance(substanceId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveVariant(variant: Variant) {
        db.variantDao().insertVariant(variant.toEntity())
    }

    override suspend fun deleteVariant(id: String) {
        db.variantDao().deleteVariant(id)
    }

    override fun getAllDoses(): Flow<List<Dose>> =
        db.doseDao().getAllDoses().map { list -> list.map { it.toDomain() } }

    override fun getDosesForSubstance(substanceId: String): Flow<List<Dose>> =
        db.doseDao().getDosesForSubstance(substanceId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveDose(dose: Dose) {
        db.doseDao().insertDose(dose.toEntity())
    }

    override suspend fun deleteDose(id: String) {
        db.doseDao().deleteDose(id)
    }

    override fun getAllQuickDoses(): Flow<List<QuickDose>> =
        db.quickDoseDao().getAllQuickDoses().map { list -> list.map { it.toDomain() } }

    override suspend fun saveQuickDose(quickDose: QuickDose) {
        db.quickDoseDao().insertQuickDose(quickDose.toEntity())
    }

    override suspend fun deleteQuickDose(id: String) {
        db.quickDoseDao().deleteQuickDose(id)
    }

    override fun getSettings(): Flow<AppSettings?> =
        db.settingsDao().getSettings().map { it?.toDomain() }

    override suspend fun saveSettings(settings: AppSettings) {
        db.settingsDao().insertSettings(settings.toEntity())
    }
}
