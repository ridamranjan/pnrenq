package com.first.pnrenq.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrMigrationTest {

    @Test
    fun migrationFromVersionOnePreservesSavedPnrAndPassengers() = runBlocking {
        val context = InstrumentationRegistry
            .getInstrumentation().targetContext

        val name =
            "pnr_migration_test_${UUID.randomUUID()}.db"

        val pnr = "1234567890"

        try {
            // Schema and identity hash taken from the exported version 1 JSON.
            context.openOrCreateDatabase(
                name,
                Context.MODE_PRIVATE,
                null
            ).use { db ->

                db.setForeignKeyConstraintsEnabled(true)
                db.beginTransaction()

                try {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `saved_pnrs` (
                            `pnrNumber` TEXT NOT NULL,
                            `trainNumber` TEXT NOT NULL,
                            `trainName` TEXT NOT NULL,
                            `journeyDate` TEXT NOT NULL,
                            `fromStation` TEXT NOT NULL,
                            `toStation` TEXT NOT NULL,
                            `bookingStatus` TEXT NOT NULL,
                            `currentStatus` TEXT NOT NULL,
                            `chartStatus` TEXT,
                            `lastCheckedAtMillis` INTEGER NOT NULL,
                            `createdAtMillis` INTEGER NOT NULL,
                            PRIMARY KEY(`pnrNumber`)
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `passengers` (
                            `passengerId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `pnrNumber` TEXT NOT NULL,
                            `passengerIndex` INTEGER NOT NULL,
                            `bookingStatus` TEXT NOT NULL,
                            `currentStatus` TEXT NOT NULL,
                            `coach` TEXT,
                            `berth` TEXT,
                            FOREIGN KEY(`pnrNumber`) REFERENCES `saved_pnrs`(`pnrNumber`)
                                ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS " +
                                "`index_passengers_pnrNumber` " +
                                "ON `passengers` (`pnrNumber`)"
                    )

                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS " +
                                "`index_passengers_pnrNumber_passengerIndex` " +
                                "ON `passengers` (`pnrNumber`, `passengerIndex`)"
                    )

                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS room_master_table " +
                                "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                    )

                    db.execSQL(
                        "INSERT OR REPLACE INTO room_master_table " +
                                "(id,identity_hash) VALUES" +
                                "(42, '5394dd803a368be72e3c6577a47fc89e')"
                    )

                    db.execSQL(
                        """
                        INSERT INTO saved_pnrs (
                            pnrNumber,
                            trainNumber,
                            trainName,
                            journeyDate,
                            fromStation,
                            toStation,
                            bookingStatus,
                            currentStatus,
                            chartStatus,
                            lastCheckedAtMillis,
                            createdAtMillis
                        ) VALUES (
                            ?,
                            '12345',
                            'Migration Test',
                            '2026-12-01',
                            'NDLS',
                            'HWH',
                            'WL 5',
                            'CNF',
                            'Not Prepared',
                            1000,
                            123
                        )
                        """.trimIndent(),
                        arrayOf<Any>(pnr)
                    )

                    db.execSQL(
                        """
                        INSERT INTO passengers (
                            pnrNumber,
                            passengerIndex,
                            bookingStatus,
                            currentStatus,
                            coach,
                            berth
                        ) VALUES (
                            ?,
                            1,
                            'WL 5',
                            'CNF',
                            'B1',
                            '22'
                        )
                        """.trimIndent(),
                        arrayOf<Any>(pnr)
                    )

                    db.version = 1

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            val database = Room.databaseBuilder(
                context,
                PnrDatabase::class.java,
                name
            )
                .addMigrations(
                    PnrDatabaseProvider.MIGRATION_1_2
                )
                .build()

            try {
                val dao = database.pnrDao()

                val repository =
                    LocalPnrRepository(dao)

                val migrated =
                    repository.getByPnr(pnr)!!

                assertEquals(pnr, migrated.pnrNumber)
                assertEquals("12345", migrated.trainNumber)
                assertEquals("Migration Test", migrated.trainName)

                assertEquals(
                    LocalDate.of(2026, 12, 1),
                    migrated.journeyDate
                )

                assertEquals("NDLS", migrated.fromStation)
                assertEquals("HWH", migrated.toStation)
                assertEquals("WL 5", migrated.bookingStatus)
                assertEquals("CNF", migrated.currentStatus)
                assertEquals("Not Prepared", migrated.chartStatus)

                assertEquals(
                    1000L,
                    migrated.lastCheckedAtMillis
                )

                assertEquals(
                    123L,
                    dao.getCreatedAt(pnr)
                )

                assertEquals(
                    listOf(
                        PassengerStatus(
                            index = 1,
                            bookingStatus = "WL 5",
                            currentStatus = "CNF",
                            coach = "B1",
                            berth = "22"
                        )
                    ),
                    migrated.passengers
                )

                assertEquals(
                    PnrDetails(),
                    migrated.details
                )

                assertNull(
                    dao.getByPnr(pnr)!!.pnr.detailsJson
                )

                assertEquals(
                    2,
                    database.openHelper.readableDatabase.version
                )

                database.openHelper.readableDatabase
                    .query("PRAGMA foreign_key_check")
                    .use {
                        assertFalse(it.moveToFirst())
                    }

                // Verify migrated data supports normal version 2 writes.
                val updated = migrated.copy(
                    details = PnrDetails(
                        ticketFare = 123.45
                    )
                )

                assertTrue(
                    repository.saveIfExists(updated)
                )

                assertEquals(
                    updated,
                    repository.getByPnr(pnr)
                )

                assertEquals(
                    123L,
                    dao.getCreatedAt(pnr)
                )

                assertNotNull(
                    dao.getByPnr(pnr)!!.pnr.detailsJson
                )

                repository.delete(pnr)

                assertNull(
                    dao.getByPnr(pnr)
                )

                database.openHelper.readableDatabase
                    .query("SELECT COUNT(*) FROM passengers")
                    .use {
                        assertTrue(it.moveToFirst())
                        assertEquals(0, it.getInt(0))
                    }
            } finally {
                database.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}