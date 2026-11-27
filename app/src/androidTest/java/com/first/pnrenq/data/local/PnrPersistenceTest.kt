package com.first.pnrenq.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.model.PnrDetails
import com.google.gson.JsonParser
import java.util.UUID
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

        val databaseName =
            "pnr_persistence_test_${UUID.randomUUID()}.db"

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
                    berth = "22",
                    bookingStatusCode = "WL",
                    currentStatusCode = "CNF",
                    bookingCoach = "B2",
                    bookingBerthNo = 5,
                    currentBerthNo = 22,
                    bookingBerthCode = "LB",
                    currentBerthCode = "UB",
                    coachPosition = 3
                )
            ),
            lastCheckedAtMillis = 1000L,
            details = PnrDetails(
                departureTime = "20:35",
                arrivalDate = "2026-12-02",
                arrivalTime = "02:33",
                duration = "05:58",
                sourceStationName = "Example Origin",
                destinationStationName = "Example Destination",
                boardingPoint = "NDLS",
                reservationUpto = "HWH",
                journeyClass = "3A",
                quota = "GN",
                distanceKm = 350.0,
                bookingFare = 123.45,
                ticketFare = 125.75,
                bookingDate = "2026-11-01",
                vikalpStatus = "No",
                ticketType = "E-ticket",
                provider = "example",
                providerTimestamp = "2026-11-27T10:00:00Z"
            )
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

                val stored = reopenedDatabase.pnrDao()
                    .getByPnr(original.pnrNumber)!!

                val json = JsonParser.parseString(
                    stored.pnr.detailsJson!!
                ).asJsonObject

                assertEquals(
                    setOf(
                        "details",
                        "passengers"
                    ),
                    json.keySet()
                )

                assertEquals(
                    setOf(
                        "departureTime",
                        "arrivalDate",
                        "arrivalTime",
                        "duration",
                        "sourceStationName",
                        "destinationStationName",
                        "boardingPoint",
                        "reservationUpto",
                        "journeyClass",
                        "quota",
                        "distanceKm",
                        "bookingFare",
                        "ticketFare",
                        "bookingDate",
                        "vikalpStatus",
                        "ticketType",
                        "provider",
                        "providerTimestamp"
                    ),
                    json.getAsJsonObject("details")
                        .keySet()
                )

                assertEquals(
                    setOf(
                        "index",
                        "bookingStatus",
                        "currentStatus",
                        "coach",
                        "berth",
                        "bookingStatusCode",
                        "currentStatusCode",
                        "bookingCoach",
                        "bookingBerthNo",
                        "currentBerthNo",
                        "bookingBerthCode",
                        "currentBerthCode",
                        "coachPosition"
                    ),
                    json.getAsJsonArray("passengers")[0]
                        .asJsonObject
                        .keySet()
                )
            } finally {
                reopenedDatabase.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun legacySnapshotRestoresExtrasAndRelationalFieldsRemainAuthoritative() {
        val record = fixture(
            """
            {
                "details": {
                    "departureTime": "20:35",
                    "ticketFare": 123.45
                },
                "passengers": [
                    {
                        "index": 1,
                        "bookingStatus": "OLD",
                        "currentStatus": "OLD",
                        "coach": "OLD",
                        "berth": "OLD",
                        "currentStatusCode": "CNF",
                        "currentBerthNo": 22,
                        "coachPosition": 3
                    }
                ]
            }
            """.trimIndent()
        )

        val restored = record.toDomain()
        val passenger = restored.passengers.single()

        assertEquals("WL 5", passenger.bookingStatus)
        assertEquals("CNF", passenger.currentStatus)
        assertEquals("B1", passenger.coach)
        assertEquals("22", passenger.berth)
        assertEquals("CNF", passenger.currentStatusCode)
        assertEquals(22, passenger.currentBerthNo)
        assertEquals(3, passenger.coachPosition)

        assertEquals(
            "20:35",
            restored.details.departureTime
        )

        assertEquals(
            123.45,
            restored.details.ticketFare!!,
            0.0
        )
    }

    @Test
    fun missingOrMalformedSnapshotPreservesCoreInformation() {
        for (
        json in listOf(
            null,
            "",
            "{",
            "null",
            "{}",
            "{\"details\":null,\"passengers\":null}"
        )
        ) {
            val restored = fixture(json).toDomain()

            assertEquals(
                PnrDetails(),
                restored.details
            )

            assertEquals(
                PassengerStatus(
                    index = 1,
                    bookingStatus = "WL 5",
                    currentStatus = "CNF",
                    coach = "B1",
                    berth = "22"
                ),
                restored.passengers.single()
            )

            assertEquals(
                LocalDate.of(2026, 12, 1),
                restored.journeyDate
            )
        }
    }

    private fun fixture(
        json: String?
    ): PnrWithPassengers = PnrWithPassengers(
        pnr = SavedPnrEntity(
            pnrNumber = "9876543210",
            trainNumber = "12345",
            trainName = "Persistence Test",
            journeyDate = "2026-12-01",
            fromStation = "NDLS",
            toStation = "HWH",
            bookingStatus = "WL 5",
            currentStatus = "CNF",
            chartStatus = "Not Prepared",
            lastCheckedAtMillis = 1000L,
            createdAtMillis = 123L,
            detailsJson = json
        ),
        passengers = listOf(
            PassengerEntity(
                pnrNumber = "9876543210",
                passengerIndex = 1,
                bookingStatus = "WL 5",
                currentStatus = "CNF",
                coach = "B1",
                berth = "22"
            )
        )
    )
}