package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.BioTrackDatabase
import com.example.domain.backup.BioTrackBackup
import com.example.domain.repository.BioTrackRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction

class BackupManager(
    private val context: Context,
    private val repository: BioTrackRepository,
    private val db: BioTrackDatabase
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        
    private val adapter = moshi.adapter(BioTrackBackup::class.java).indent("  ")

    suspend fun exportBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = BioTrackBackup(
                settings = repository.getSettings().first(),
                substances = repository.getAllSubstances().first(),
                compounds = repository.getAllCompounds().first(),
                variants = repository.getAllVariants().first(),
                doses = repository.getAllDoses().first(),
                quickDoses = repository.getAllQuickDoses().first()
            )
            
            val json = adapter.toJson(backup)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(uri: Uri, overwrite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return@withContext Result.failure(Exception("Could not read backup file"))
            
            val backup = adapter.fromJson(json) ?: return@withContext Result.failure(Exception("Corrupted backup file"))
            
            db.withTransaction {
                if (overwrite) {
                    db.clearAllTables()
                }
                
                // Re-insert 
                if (backup.settings != null && overwrite) {
                     // only restore settings on overwrite mode to prevent chaos
                     // wait, settings may be safely merged if we want, but let's just overwrite them on overwrite
                     repository.saveSettings(backup.settings)
                }
                
                backup.substances.forEach { repository.saveSubstance(it) }
                backup.compounds.forEach { repository.saveCompound(it) }
                backup.variants.forEach { repository.saveVariant(it) }
                backup.doses.forEach { repository.saveDose(it) }
                backup.quickDoses.forEach { repository.saveQuickDose(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
