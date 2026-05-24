package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.SubstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubstanceDao {
    @Query("SELECT * FROM substances WHERE archivedAt IS NULL ORDER BY name ASC")
    fun getActiveSubstances(): Flow<List<SubstanceEntity>>

    @Query("SELECT * FROM substances ORDER BY name ASC")
    fun getAllSubstances(): Flow<List<SubstanceEntity>>

    @Query("SELECT * FROM substances WHERE id = :id")
    suspend fun getSubstanceById(id: String): SubstanceEntity?
    
    @Query("SELECT * FROM substances WHERE id = :id")
    fun getSubstanceByIdFlow(id: String): Flow<SubstanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstance(substance: SubstanceEntity)

    @Update
    suspend fun updateSubstance(substance: SubstanceEntity)

    @Query("DELETE FROM substances WHERE id = :id")
    suspend fun deleteSubstance(id: String)
}
