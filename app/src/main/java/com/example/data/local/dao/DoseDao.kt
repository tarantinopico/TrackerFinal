package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.DoseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseDao {
    @Query("SELECT * FROM doses ORDER BY timestamp DESC")
    fun getAllDoses(): Flow<List<DoseEntity>>

    @Query("SELECT * FROM doses WHERE substanceId = :substanceId ORDER BY timestamp DESC")
    fun getDosesForSubstance(substanceId: String): Flow<List<DoseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDose(dose: DoseEntity)

    @Update
    suspend fun updateDose(dose: DoseEntity)

    @Query("DELETE FROM doses WHERE id = :id")
    suspend fun deleteDose(id: String)
}
