package com.example.data.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.BioTrackDatabase
import com.example.data.repository.BioTrackRepositoryImpl
import com.example.domain.repository.BioTrackRepository

/**
 * Manual Dependency Injection container to keep the setup robust in this environment.
 */
class AppContainer(private val context: Context) {

    private val database: BioTrackDatabase by lazy {
        Room.databaseBuilder(
            context,
            BioTrackDatabase::class.java,
            "biotrack_database"
        ).build()
    }

    val bioTrackRepository: BioTrackRepository by lazy {
        BioTrackRepositoryImpl(database)
    }
}
