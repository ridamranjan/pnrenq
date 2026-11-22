
package com.first.pnrenq.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PnrPersistenceTest {

    @Test
    fun savedPnrSurvivesDatabaseReopen() = runBlocking {
        val context = InstrumentationRegistry
            .getInstrumentation().targetContext

        val databaseName = "pnr_persistence_test.db"

        context.deleteDatabase(databaseName)

        fun openDatabase(): PnrDatabase =
            Room.databaseBuilder(
                context,
                PnrDatabase::class.java,
                databaseName
            ).build()

        val original = PnrInfo(
            pnrNumber = "9876543210",
            trainNumber = "12345",
            trainName = "Persistence Test",
            journeyDate = LocalDate.of(2026, 12, 1),
            fromStation = "NDLS",
            toStation = "HWH",
            bookingStatus = "WL 5",
            currentStatus = "CNF",
            chartStatus = "Not Prepared",
            passengers = listOf(
                PassengerStatus(
                    index = 1,
                    bookingStatus = "WL 5",
                    currentStatus = "CNF",
                    coach = "B1",
                    berth = "22"
                )
            ),
            lastCheckedAtMillis = 1000L
        )

        try {
            val firstDatabase = openDatabase()

            try {
                LocalPnrRepository(
                    firstDatabase.pnrDao()
                ).save(original)
            } finally {
                firstDatabase.close()
            }

            val reopenedDatabase = openDatabase()

            try {
                val saved = LocalPnrRepository(
                    reopenedDatabase.pnrDao()
                ).getByPnr("9876543210")

                assertNotNull(saved)
                assertEquals(original, saved)
            } finally {
                reopenedDatabase.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
