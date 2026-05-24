package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.VariantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VariantDao {
    @Query("SELECT * FROM variants WHERE substanceId = :substanceId")
    fun getVariantsForSubstance(substanceId: String): Flow<List<VariantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: VariantEntity)

    @Update
    suspend fun updateVariant(variant: VariantEntity)

    @Query("DELETE FROM variants WHERE id = :id")
    suspend fun deleteVariant(id: String)
}
