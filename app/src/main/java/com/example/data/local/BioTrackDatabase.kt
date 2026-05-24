package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import com.example.data.local.db.Converters

@Database(
    entities = [
        SubstanceEntity::class,
        CompoundEntity::class,
        VariantEntity::class,
        DoseEntity::class,
        QuickDoseEntity::class,
        SettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BioTrackDatabase : RoomDatabase() {
    // New granular DAOs
    abstract fun substanceDao(): SubstanceDao
    abstract fun compoundDao(): CompoundDao
    abstract fun variantDao(): VariantDao
    abstract fun doseDao(): DoseDao
    abstract fun quickDoseDao(): QuickDoseDao
    abstract fun settingsDao(): SettingsDao
}
