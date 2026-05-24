package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.CompoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompoundDao {
    @Query("SELECT * FROM compounds WHERE substanceId = :substanceId")
    fun getCompoundsForSubstance(substanceId: String): Flow<List<CompoundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompound(compound: CompoundEntity)

    @Update
    suspend fun updateCompound(compound: CompoundEntity)

    @Query("DELETE FROM compounds WHERE id = :id")
    suspend fun deleteCompound(id: String)
}
