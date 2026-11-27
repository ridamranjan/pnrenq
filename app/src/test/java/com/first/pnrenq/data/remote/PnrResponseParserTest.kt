package com.first.pnrenq.data.remote

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PnrResponseParserTest {

    private val pnr = "1234567890"

    private val now =
        Instant.parse("2028-02-01T10:00:00Z").toEpochMilli()

    private val timestamp =
        "\"2028-02-01T09:59:00Z\""

    @Test
    fun completeResponsePreservesAllSupportedFields() {
        val result = parse(response(
            passengers = "[${passenger(extra = """
                ,"currentCoach":"B2","currentBerthNo":24,
                "bookingStatusCode":"RAC","currentStatusCode":"CNF",
                "bookingCoach":"B1","bookingBerthNo":"7",
                "bookingBerthCode":"LB","currentBerthCode":"UB","coachPosition":3
            """, current = "CNF")}]",
            extra = """
                ,"sourceStation":"ABC","destinationStation":"XYZ",
                "chartStatus":"Chart Prepared","departureTime":"20:35",
                "arrivalDate":"2028-03-01","arrivalTime":"02:33","duration":"05:58",
                "sourceStationName":"Example Origin","destinationStationName":"Example Destination",
                "boardingPoint":"ABC","reservationUpto":"XYZ","journeyClass":"3A",
                "quota":"GN","distanceKm":350,"bookingFare":"123.45","ticketFare":125.75,
                "bookingDate":"2028-01-01","vikalpStatus":"No","ticketType":"E-ticket",
                "provider":"example","providerTimestamp":"2028-02-01T09:58:00Z"
            """
        ))

        assertEquals(pnr, result.pnrNumber)
        assertEquals("12345", result.trainNumber)
        assertEquals("Example Train", result.trainName)

        assertEquals(
            LocalDate.of(2028, 2, 29),
            result.journeyDate
        )

        assertEquals("ABC", result.fromStation)
        assertEquals("XYZ", result.toStation)
        assertEquals("RAC 7", result.bookingStatus)
        assertEquals("CNF", result.currentStatus)
        assertEquals("Chart Prepared", result.chartStatus)

        assertEquals(
            Instant.parse("2028-02-01T09:59:00Z")
                .toEpochMilli(),
            result.lastCheckedAtMillis
        )

        assertEquals(
            listOf(
                PassengerStatus(
                    index = 1,
                    bookingStatus = "RAC 7",
                    currentStatus = "CNF",
                    coach = "B2",
                    berth = "24",
                    bookingStatusCode = "RAC",
                    currentStatusCode = "CNF",
                    bookingCoach = "B1",
                    bookingBerthNo = 7,
                    currentBerthNo = 24,
                    bookingBerthCode = "LB",
                    currentBerthCode = "UB",
                    coachPosition = 3
                )
            ),
            result.passengers
        )

        assertEquals(
            PnrDetails(
                departureTime = "20:35",
                arrivalDate = "2028-03-01",
                arrivalTime = "02:33",
                duration = "05:58",
                sourceStationName = "Example Origin",
                destinationStationName = "Example Destination",
                boardingPoint = "ABC",
                reservationUpto = "XYZ",
                journeyClass = "3A",
                quota = "GN",
                distanceKm = 350.0,
                bookingFare = 123.45,
                ticketFare = 125.75,
                bookingDate = "2028-01-01",
                vikalpStatus = "No",
                ticketType = "E-ticket",
                provider = "example",
                providerTimestamp = "2028-02-01T09:58:00Z"
            ),
            result.details
        )
    }

    @Test
    fun supportedJourneyDateFormatsRemainValid() {
        for (
        date in listOf(
            "2028-02-29",
            "29-02-2028",
            "Feb 29, 2028 12:00:00 AM"
        )
        ) {
            assertEquals(
                LocalDate.of(2028, 2, 29),
                parse(response(date = date)).journeyDate
            )
        }
    }

    @Test
    fun invalidCalendarDatesAreRejected() {
        for (
        date in listOf(
            "2026-02-29",
            "31-02-2028",
            "31-04-2028",
            "Feb 30, 2028 1:00:00 PM"
        )
        ) {
            reject(response(date = date))
        }
    }

    @Test
    fun invalidLegacyTimeIsRejected() {
        reject(
            response(
                date = "Feb 29, 2028 13:00:00 AM"
            )
        )
    }

    @Test
    fun onlyActualTrueBooleanIsAccepted() {
        parse(response(success = "true"))

        for (
        success in listOf(
            "false",
            "\"true\"",
            "1",
            "null",
            "[]",
            "{}"
        )
        ) {
            reject(response(success = success))
        }

        reject(
            response().replace(
                "\"success\":true,",
                ""
            )
        )
    }

    @Test
    fun malformedAndTrailingJsonAreRejected() {
        reject("")
        reject("[]")
        reject(response().dropLast(1))
        reject(response() + " {}")

        reject(
            response().replace(
                "\"success\":true",
                "success:true"
            )
        )

        reject(
            response().replace(
                "\"success\":true",
                "\"success\":/*comment*/true"
            )
        )
    }

    @Test
    fun invalidOrMismatchedPnrIsRejected() {
        reject(
            response(responsePnr = "9876543210")
        )

        reject(
            response(responsePnr = "123")
        )

        reject(
            response(),
            requestedPnr = "123"
        )

        reject(
            response().replace(
                "\"pnr\":\"$pnr\"",
                "\"pnr\":$pnr"
            )
        )
    }

    @Test
    fun malformedRequiredStringsAreRejected() {
        reject(
            response().replace(
                "\"trainNumber\":\"12345\"",
                "\"trainNumber\":12345"
            )
        )

        reject(
            response().replace(
                "\"12345\"",
                "\"123ABC\""
            )
        )

        reject(
            response().replace(
                "\"Example Train\"",
                "\"   \""
            )
        )

        reject(
            response().replace(
                "\"Example Train\"",
                "\"${"A".repeat(201)}\""
            )
        )

        reject(
            response().replace(
                "\"bookingStatus\":\"RAC 7\",",
                ""
            )
        )

        reject(
            response().replace(
                "\"RAC 3\"",
                "null"
            )
        )
    }

    @Test
    fun exactIntegerNumbersAndIntegerStringsAreAccepted() {
        for (
        serial in listOf(
            "1",
            "1.0",
            "1e0",
            "\"1\"",
            "\"01\""
        )
        ) {
            val result = parse(
                response(
                    passengers = "[${passenger(serial)}]"
                )
            )

            assertEquals(
                1,
                result.passengers.single().index
            )
        }
    }

    @Test
    fun invalidRequiredIntegersAreRejected() {
        for (
        serial in listOf(
            "1.5",
            "4294967297",
            "2147483648",
            "1e50",
            "0",
            "-1",
            "13",
            "true",
            "null",
            "\"1.5\"",
            "\"2147483648\""
        )
        ) {
            reject(
                response(
                    passengers = "[${passenger(serial)}]"
                )
            )
        }
    }

    @Test
    fun invalidOptionalIntegersBecomeUnavailableWithoutTruncation() {
        val result = parse(
            response(
                passengers = "[${passenger(extra = """
                    ,"currentBerthNo":24.8,"currentBerthCode":"UB",
                    "bookingBerthNo":4294967297,"coachPosition":true
                """)}]"
            )
        )

        val passenger = result.passengers.single()

        assertNull(passenger.currentBerthNo)
        assertNull(passenger.bookingBerthNo)
        assertNull(passenger.coachPosition)
        assertEquals("UB", passenger.berth)
    }

    @Test
    fun emptyAndOversizedPassengerListsAreRejected() {
        reject(response(passengers = "[]"))
        reject(response(passengers = "null"))
        reject(response(passengers = "[null]"))

        val tooMany = (1..13).joinToString(
            ",",
            "[",
            "]"
        ) {
            passenger(it.toString())
        }

        reject(
            response(passengers = tooMany)
        )
    }

    @Test
    fun duplicatePassengerIndicesAreRejected() {
        reject(
            response(
                passengers =
                    "[${passenger("1")},${passenger("\"1\"")}]"
            )
        )
    }

    @Test
    fun passengerSortingAndMixedStatusArePreserved() {
        val result = parse(
            response(
                passengers =
                    "[${passenger("2", "WL 4", "CNF")},${passenger("1")}]"
            )
        )

        assertEquals(
            listOf(1, 2),
            result.passengers.map { it.index }
        )

        assertEquals("Mixed", result.bookingStatus)
        assertEquals("Mixed", result.currentStatus)
    }

    @Test
    fun twelvePassengersAreAccepted() {
        val passengers = (1..12).joinToString(
            ",",
            "[",
            "]"
        ) {
            passenger(it.toString())
        }

        assertEquals(
            12,
            parse(
                response(passengers = passengers)
            ).passengers.size
        )
    }

    @Test
    fun optionalNumericValuesRetainExistingFiniteNonnegativePolicy() {
        val result = parse(
            response(
                extra =
                    ",\"distanceKm\":-1,\"bookingFare\":\"NaN\",\"ticketFare\":1e999"
            )
        )

        assertNull(result.details.distanceKm)
        assertNull(result.details.bookingFare)
        assertNull(result.details.ticketFare)
    }

    @Test
    fun missingOptionalFieldsRemainSupported() {
        val result = parse(response())

        assertEquals("Unknown", result.fromStation)
        assertEquals("Unknown", result.toStation)
        assertNull(result.chartStatus)
        assertNull(result.passengers.single().coach)

        assertEquals(
            PnrDetails(),
            result.details
        )
    }

    @Test
    fun unknownFieldsDoNotBreakCompatibleResponses() {
        assertEquals(
            pnr,
            parse(
                response(
                    extra =
                        ",\"futureField\":{\"x\":[1,true,null]}"
                )
            ).pnrNumber
        )
    }

    @Test
    fun missingAndNullTimestampsUseReceiptTime() {
        assertEquals(
            now,
            parse(
                response(checkedAt = null)
            ).lastCheckedAtMillis
        )

        assertEquals(
            now,
            parse(
                response(checkedAt = "null")
            ).lastCheckedAtMillis
        )
    }

    @Test
    fun malformedAndInvalidTimestampsAreRejected() {
        for (
        checkedAt in listOf(
            "\"not-a-date\"",
            "\"\"",
            "42",
            "true",
            "{}",
            "\"1970-01-01T00:00:00Z\"",
            "\"1969-12-31T23:59:59Z\"",
            "\"+999999999-12-31T23:59:59Z\""
        )
        ) {
            reject(
                response(checkedAt = checkedAt)
            )
        }
    }

    @Test
    fun futureTimestampToleranceIsBounded() {
        val boundary =
            Instant.ofEpochMilli(now).plusSeconds(300)

        assertEquals(
            boundary.toEpochMilli(),
            parse(
                response(
                    checkedAt = "\"$boundary\""
                )
            ).lastCheckedAtMillis
        )

        reject(
            response(
                checkedAt = "\"${boundary.plusMillis(1)}\""
            )
        )
    }

    @Test
    fun responseTextLimitIsEnforced() {
        val json = response()

        val limit =
            PnrNetworkConfig.MAX_RESPONSE_CHARS

        assertEquals(
            pnr,
            parse(
                json.padEnd(limit, ' ')
            ).pnrNumber
        )

        reject(
            json.padEnd(limit + 1, ' ')
        )
    }

    private fun parse(
        json: String
    ) = PnrResponseParser.parse(
        json,
        pnr,
        now
    )

    private fun reject(
        json: String,
        requestedPnr: String = pnr
    ) {
        val exception = assertThrows(
            PnrCheckException::class.java
        ) {
            PnrResponseParser.parse(
                json,
                requestedPnr,
                now
            )
        }

        assertEquals(
            "Invalid PNR response. Saved information was not changed.",
            exception.message
        )
    }

    private fun response(
        success: String = "true",
        date: String = "2028-02-29",
        responsePnr: String = pnr,
        passengers: String = "[${passenger()}]",
        checkedAt: String? = timestamp,
        extra: String = ""
    ): String = """
        {"success":$success,"data":{
            "pnr":"$responsePnr","trainNumber":"12345","trainName":"Example Train",
            "dateOfJourney":"$date","passengers":$passengers$extra
        }${checkedAt?.let { ",\"checkedAtUtc\":$it" } ?: ""}}
    """.trimIndent()

    private fun passenger(
        serial: String = "1",
        booking: String = "RAC 7",
        current: String = "RAC 3",
        extra: String = ""
    ): String =
        """{"serialNumber":$serial,"bookingStatus":"$booking","currentStatus":"$current"$extra}"""
}