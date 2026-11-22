
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
class PnrDaoTest {

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

        repository = LocalPnrRepository(database.pnrDao())
    }

    @After
    fun cleanup() {
        database.close()
    }

    private fun sample(
        pnr: String = "1234567890",
        status: String = "WL 5",
        passengers: Int = 2
    ): PnrInfo = PnrInfo(
        pnrNumber = pnr,
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2026, 12, 1),
        fromStation = "NDLS",
        toStation = "HWH",
        bookingStatus = "WL 10",
        currentStatus = status,
        chartStatus = "Not Prepared",
        passengers = (1..passengers).map {
            PassengerStatus(
                index = it,
                bookingStatus = "WL 10",
                currentStatus = status,
                coach = null,
                berth = null
            )
        },
        lastCheckedAtMillis = 1000L
    )

    @Test
    fun databaseStartsEmpty() = runBlocking {
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun saveAndReadPnr() = runBlocking {
        repository.save(sample())

        val saved = repository.getByPnr("1234567890")

        assertNotNull(saved)
        assertEquals("Test Express", saved?.trainName)
        assertEquals("WL 5", saved?.currentStatus)
        assertEquals(2, saved?.passengers?.size)
    }

    @Test
    fun duplicatePnrUpdatesInsteadOfInserting() = runBlocking {
        repository.save(sample(status = "WL 5"))
        repository.save(sample(status = "CNF"))

        val all = repository.observeAll().first()

        assertEquals(1, all.size)
        assertEquals("CNF", all.first().currentStatus)
    }

    @Test
    fun passengerReplacementRemovesOldRows() = runBlocking {
        repository.save(sample(passengers = 3))
        repository.save(sample(passengers = 1))

        val saved = repository.getByPnr("1234567890")

        assertEquals(1, saved?.passengers?.size)
        assertEquals(1, saved?.passengers?.first()?.index)
    }

    @Test
    fun updatePreservesOriginalCreationTime() = runBlocking {
        val dao = database.pnrDao()
        val original = sample()

        dao.savePnr(
            original.toSavedEntity(createdAtMillis = 123L),
            original.toPassengerEntities()
        )

        val updated = sample(status = "CNF")

        dao.savePnr(
            updated.toSavedEntity(createdAtMillis = 999L),
            updated.toPassengerEntities()
        )

        assertEquals(
            123L,
            dao.getCreatedAt("1234567890")
        )
    }

    @Test
    fun deletingPnrRemovesPassengers() = runBlocking {
        repository.save(sample())

        repository.delete("1234567890")

        assertNull(repository.getByPnr("1234567890"))
        assertTrue(repository.observeAll().first().isEmpty())

        val cursor = database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM passengers"
        )

        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
    }

    @Test
    fun failedTransactionRollsBackChanges() = runBlocking {
        val dao = database.pnrDao()
        val original = sample(status = "WL 5")

        repository.save(original)

        val invalidPassenger = PassengerEntity(
            pnrNumber = "9999999999",
            passengerIndex = 1,
            bookingStatus = "CNF",
            currentStatus = "CNF",
            coach = "S1",
            berth = "10"
        )

        var failed = false

        try {
            dao.savePnr(
                sample(status = "CNF").toSavedEntity(),
                listOf(invalidPassenger)
            )
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            failed = true
        } catch (e: androidx.sqlite.SQLiteException) {
            failed = true
        }

        assertTrue(
            "Expected a foreign-key constraint failure",
            failed
        )

        val saved = repository.getByPnr("1234567890")

        assertEquals("WL 5", saved?.currentStatus)
        assertEquals(2, saved?.passengers?.size)
    }
}
