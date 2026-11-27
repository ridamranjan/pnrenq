package com.first.pnrenq.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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
        assertTrue(
            repository.observeAll().first().isEmpty()
        )
    }

    @Test
    fun saveAndReadPnr() = runBlocking {
        repository.save(sample())

        val saved =
            repository.getByPnr("1234567890")

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

        val saved =
            repository.getByPnr("1234567890")

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

        val other = sample(
            pnr = "9876543210",
            status = "CNF"
        )

        repository.save(original)
        repository.save(other)

        val otherPassenger =
            dao.getByPnr(other.pnrNumber)!!
                .passengers.first()

        val conflictingPassenger =
            original.toPassengerEntities()
                .first()
                .copy(
                    passengerId = otherPassenger.passengerId
                )

        var failed = false

        try {
            dao.savePnr(
                sample(status = "CNF").toSavedEntity(),
                listOf(conflictingPassenger)
            )
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            failed = true
        } catch (_: androidx.sqlite.SQLiteException) {
            failed = true
        }

        assertTrue(
            "Expected a passenger primary-key conflict",
            failed
        )

        assertEquals(
            original,
            repository.getByPnr(original.pnrNumber)
        )

        assertEquals(
            other,
            repository.getByPnr(other.pnrNumber)
        )
    }

    @Test
    fun rejectsPassengersBelongingToAnotherPnr() = runBlocking {
        val dao = database.pnrDao()

        val original = sample()

        val other = sample(
            pnr = "9876543210",
            status = "CNF"
        )

        repository.save(original)
        repository.save(other)

        for (
        wrongPnr in listOf(
            other.pnrNumber,
            "9999999999"
        )
        ) {
            var rejected = false

            try {
                dao.savePnr(
                    original.copy(
                        currentStatus = "CNF"
                    ).toSavedEntity(),
                    listOf(
                        original.toPassengerEntities()
                            .first()
                            .copy(pnrNumber = wrongPnr)
                    )
                )
            } catch (_: IllegalArgumentException) {
                rejected = true
            }

            assertTrue(rejected)

            assertEquals(
                original,
                repository.getByPnr(original.pnrNumber)
            )

            assertEquals(
                other,
                repository.getByPnr(other.pnrNumber)
            )
        }
    }

    @Test
    fun daoRejectsInvalidPassengerIndices() = runBlocking {
        val dao = database.pnrDao()
        val original = sample()

        val passenger =
            original.toPassengerEntities().first()

        val invalidLists = listOf(
            listOf(passenger, passenger),
            listOf(
                passenger.copy(passengerIndex = 0)
            )
        )

        for (invalid in invalidLists) {
            var rejected = false

            try {
                dao.savePnr(
                    original.toSavedEntity(),
                    invalid
                )
            } catch (_: IllegalArgumentException) {
                rejected = true
            }

            assertTrue(rejected)
            assertNull(dao.getByPnr(original.pnrNumber))
        }
    }

    @Test
    fun conditionalSaveUpdatesExistingRecordAndPreservesCreationTime() = runBlocking {
        val dao = database.pnrDao()
        val original = sample()

        dao.savePnr(
            original.toSavedEntity(123L),
            original.toPassengerEntities()
        )

        val updated = sample(
            status = "CNF",
            passengers = 1
        ).copy(
            lastCheckedAtMillis = 2000L
        )

        assertTrue(repository.saveIfExists(updated))

        assertEquals(
            updated,
            repository.getByPnr(original.pnrNumber)
        )

        assertEquals(
            123L,
            dao.getCreatedAt(original.pnrNumber)
        )
    }

    @Test
    fun conditionalSaveDoesNotRecreateDeletedOrMissingRecord() = runBlocking {
        val original = sample()

        assertFalse(
            repository.saveIfExists(original)
        )

        repository.save(original)
        repository.delete(original.pnrNumber)

        assertFalse(
            repository.saveIfExists(
                original.copy(currentStatus = "CNF")
            )
        )

        assertNull(
            database.pnrDao().getByPnr(original.pnrNumber)
        )

        assertTrue(
            repository.observeAll().first().isEmpty()
        )
    }

    @Test
    fun unreadableDateDoesNotStopObservationAndCanBeRepaired() = runBlocking {
        val dao = database.pnrDao()

        val damaged = sample()

        val valid = sample(
            pnr = "9876543210"
        )

        dao.savePnr(
            damaged.toSavedEntity(123L).copy(
                journeyDate = "2026-02-31"
            ),
            damaged.toPassengerEntities()
        )

        repository.save(valid)

        val observations =
            Channel<SavedPnrReadResult>(
                Channel.UNLIMITED
            )

        val job = launch {
            repository.observeAllWithIssues().collect {
                observations.send(it)
            }
        }

        try {
            val before = withTimeout(10_000L) {
                observations.receive()
            }

            assertEquals(
                listOf(valid),
                before.pnrs
            )

            assertEquals(
                listOf(damaged.pnrNumber),
                before.unreadablePnrs
            )

            assertNotNull(
                dao.getByPnr(damaged.pnrNumber)
            )

            assertNull(
                repository.getByPnr(damaged.pnrNumber)
            )

            assertNull(
                repository.observeByPnr(
                    damaged.pnrNumber
                ).first()
            )

            repository.save(damaged)

            val after = withTimeout(10_000L) {
                var result = observations.receive()

                while (result.unreadablePnrs.isNotEmpty()) {
                    result = observations.receive()
                }

                result
            }

            assertEquals(
                setOf(valid, damaged),
                after.pnrs.toSet()
            )

            assertEquals(
                123L,
                dao.getCreatedAt(damaged.pnrNumber)
            )
        } finally {
            job.cancelAndJoin()
            observations.close()
        }
    }

    @Test
    fun allUnreadableRecordsAreReportedAndNotDeleted() = runBlocking {
        val dao = database.pnrDao()
        val original = sample()

        dao.savePnr(
            original.toSavedEntity().copy(
                journeyDate = "invalid"
            ),
            original.toPassengerEntities()
        )

        val result =
            repository.observeAllWithIssues().first()

        assertTrue(result.pnrs.isEmpty())

        assertEquals(
            listOf(original.pnrNumber),
            result.unreadablePnrs
        )

        assertEquals(
            2,
            dao.getByPnr(original.pnrNumber)!!
                .passengers.size
        )
    }
}