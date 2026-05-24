package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.QuickDoseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickDoseDao {
    @Query("SELECT * FROM quick_doses ORDER BY orderIndex ASC")
    fun getAllQuickDoses(): Flow<List<QuickDoseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickDose(quickDose: QuickDoseEntity)

    @Update
    suspend fun updateQuickDose(quickDose: QuickDoseEntity)

    @Query("DELETE FROM quick_doses WHERE id = :id")
    suspend fun deleteQuickDose(id: String)
}
