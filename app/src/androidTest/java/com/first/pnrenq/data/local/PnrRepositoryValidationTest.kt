
package com.first.pnrenq.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PnrRepositoryValidationTest {

    private lateinit var database: PnrDatabase
    private lateinit var repository: LocalPnrRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry
            .getInstrumentation().targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            PnrDatabase::class.java
        ).build()

        repository = LocalPnrRepository(
            database.pnrDao()
        )
    }

    @After
    fun cleanup() {
        database.close()
    }

    private fun sample(
        pnr: String = "1234567890",
        indices: List<Int> = listOf(1)
    ): PnrInfo = PnrInfo(
        pnrNumber = pnr,
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2026, 12, 1),
        fromStation = "NDLS",
        toStation = "HWH",
        bookingStatus = "WL 5",
        currentStatus = "CNF",
        chartStatus = "Not Prepared",
        passengers = indices.map {
            PassengerStatus(
                index = it,
                bookingStatus = "WL 5",
                currentStatus = "CNF",
                coach = "B1",
                berth = "20"
            )
        },
        lastCheckedAtMillis = 1000L
    )

    @Test
    fun rejectsInvalidPnrWithoutSaving() = runBlocking {
        var rejected = false

        try {
            repository.save(sample(pnr = "123"))
        } catch (e: IllegalArgumentException) {
            rejected = true
        }

        assertTrue(rejected)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun rejectsDuplicatePassengerIndices() = runBlocking {
        var rejected = false

        try {
            repository.save(sample(indices = listOf(1, 1)))
        } catch (e: IllegalArgumentException) {
            rejected = true
        }

        assertTrue(rejected)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun rejectsNonPositivePassengerIndex() = runBlocking {
        var rejected = false

        try {
            repository.save(sample(indices = listOf(0)))
        } catch (e: IllegalArgumentException) {
            rejected = true
        }

        assertTrue(rejected)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun acceptsValidPnr() = runBlocking {
        repository.save(sample())

        val saved = repository.getByPnr("1234567890")

        assertNotNull(saved)
        assertEquals(1, saved?.passengers?.size)
    }
}
