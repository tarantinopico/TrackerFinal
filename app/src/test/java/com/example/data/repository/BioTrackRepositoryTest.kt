package com.example.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BioTrackDatabase
import com.example.domain.model.Substance
import com.example.domain.model.SubstanceCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BioTrackRepositoryTest {

    private lateinit var db: BioTrackDatabase
    private lateinit var repository: BioTrackRepositoryImpl

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BioTrackDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = BioTrackRepositoryImpl(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveSubstance() = runBlocking {
        val substance = Substance(
            id = "test_sub_1",
            name = "Caffeine",
            category = SubstanceCategory.STIMULANT,
            defaultUnit = "mg",
            active = true
        )
        
        repository.saveSubstance(substance)

        val retrieved = repository.getAllSubstances().first()
        assertEquals(1, retrieved.size)
        assertEquals("Caffeine", retrieved[0].name)
    }
}
