package com.first.pnrenq.domain.time

import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class RailwayJourneyTimeTest {

    private val date = LocalDate.of(2026, 12, 25)

    @Test
    fun valid24HourTimesIncludeMidnightAndSeconds() {
        assertEquals(
            LocalTime.MIDNIGHT,
            parseRailwayClock("00:00")
        )

        assertEquals(
            LocalTime.of(8, 5),
            parseRailwayClock("8:05")
        )

        assertEquals(
            LocalTime.of(23, 59, 59, 123_000_000),
            parseRailwayClock("23:59:59.123")
        )
    }

    @Test
    fun valid12HourTimesHandleNoonMidnightAndCase() {
        assertEquals(
            LocalTime.MIDNIGHT,
            parseRailwayClock("12:00 AM")
        )

        assertEquals(
            LocalTime.NOON,
            parseRailwayClock("12:00 pm")
        )

        assertEquals(
            LocalTime.of(20, 35),
            parseRailwayClock(" 8:35pm ")
        )

        assertEquals(
            LocalTime.of(1, 2, 3),
            parseRailwayClock("1:02:03 AM")
        )
    }

    @Test
    fun invalidAndAmbiguousTimesAreNotNormalized() {
        for (
        value in listOf(
            null,
            "",
            "unknown",
            "24:00",
            "00:15 AM",
            "13:00 PM",
            "23:60",
            "10:59:60",
            "9:5",
            "08:30Z",
            "08:30+05:30",
            "08:30 PM extra"
        )
        ) {
            assertNull(parseRailwayClock(value))
        }
    }

    @Test
    fun knownDateFormatsAreParsedStrictly() {
        assertEquals(
            date,
            parseRailwayDate("2026-12-25")
        )

        assertEquals(
            date,
            parseRailwayDate("25-12-2026")
        )

        assertEquals(
            date,
            parseRailwayDate("Dec 25, 2026 2:33:00 AM")
        )

        assertEquals(
            LocalDate.of(2028, 2, 29),
            parseRailwayDate("29-02-2028")
        )

        for (
        value in listOf(
            "2026-02-29",
            "31-04-2026",
            "Apr 31, 2026 2:33:00 AM",
            "12/25/2026",
            "2026-12-25Z"
        )
        ) {
            assertNull(parseRailwayDate(value))
        }
    }

    @Test
    fun exactDepartureInstantMovesJourneyToPast() {
        val pnr = sample(1, date, "20:35")

        val schedule = RailwayJourneySchedule(
            listOf(pnr)
        )

        val departure = Instant.parse(
            "2026-12-25T15:05:00Z"
        )

        assertEquals(
            listOf(pnr),
            schedule.sectionsAt(
                departure.minusNanos(1)
            ).upcoming
        )

        assertEquals(
            listOf(pnr),
            schedule.sectionsAt(departure).past
        )

        assertEquals(
            emptyList<PnrInfo>(),
            schedule.sectionsAt(departure).upcoming
        )
    }

    @Test
    fun railwayDateBoundaryDoesNotDependOnDeviceZone() {
        val midnight = sample(1, date, "00:00")

        val schedule = RailwayJourneySchedule(
            listOf(midnight)
        )

        val boundary = Instant.parse(
            "2026-12-24T18:30:00Z"
        )

        assertEquals(
            date,
            boundary.atZone(RAILWAY_ZONE).toLocalDate()
        )

        assertEquals(
            date.minusDays(1),
            boundary.atZone(
                ZoneId.of("America/Los_Angeles")
            ).toLocalDate()
        )

        assertEquals(
            listOf(midnight),
            schedule.sectionsAt(boundary).past
        )
    }

    @Test
    fun unknownOrInvalidDepartureRemainsUpcomingUntilNextRailwayDay() {
        val unknown = sample(1, date, null)
        val invalid = sample(2, date, "24:00")

        val schedule = RailwayJourneySchedule(
            listOf(invalid, unknown)
        )

        val nextDay = Instant.parse(
            "2026-12-25T18:30:00Z"
        )

        assertEquals(
            listOf(unknown, invalid),
            schedule.sectionsAt(
                nextDay.minusMillis(1)
            ).upcoming
        )

        assertEquals(
            listOf(unknown, invalid),
            schedule.sectionsAt(nextDay).past
        )
    }

    @Test
    fun upcomingSortsEarliestFirstWithUnknownTimesLastAndStableTies() {
        val early = sample(1, date, "00:00")
        val tie = sample(2, date, "12:00 AM")
        val late = sample(3, date, "23:59:59.999999999")
        val unknown = sample(4, date, null)

        val tomorrow = sample(
            5,
            date.plusDays(1),
            "01:00"
        )

        val schedule = RailwayJourneySchedule(
            listOf(tomorrow, tie, unknown, late, early)
        )

        assertEquals(
            listOf(early, tie, late, unknown, tomorrow),
            schedule.sectionsAt(
                Instant.parse("2026-12-24T00:00:00Z")
            ).upcoming
        )
    }

    @Test
    fun pastSortsNewestFirstWithoutPromotingUnknownTimes() {
        val early = sample(1, date, "00:00")
        val tie = sample(2, date, "00:00")
        val late = sample(3, date, "23:59")
        val unknown = sample(4, date, null)

        val yesterday = sample(
            5,
            date.minusDays(1),
            "23:59"
        )

        val schedule = RailwayJourneySchedule(
            listOf(yesterday, tie, unknown, early, late)
        )

        assertEquals(
            listOf(late, early, tie, unknown, yesterday),
            schedule.sectionsAt(
                Instant.parse("2026-12-26T00:00:00Z")
            ).past
        )
    }

    @Test
    fun nextUpdateTargetsDepartureOrUnknownTimeMidnightAndBoundsLongWaits() {
        val schedule = RailwayJourneySchedule(
            listOf(
                sample(1, date, "20:35"),
                sample(2, date, null)
            )
        )

        val departure = Instant.parse(
            "2026-12-25T15:05:00Z"
        )

        val midnight = Instant.parse(
            "2026-12-25T18:30:00Z"
        )

        assertEquals(
            250L,
            schedule.nextUpdateDelayMillis(
                departure.minusMillis(250)
            )
        )

        assertEquals(
            1L,
            schedule.nextUpdateDelayMillis(
                departure.minusNanos(1)
            )
        )

        assertEquals(
            60_000L,
            schedule.nextUpdateDelayMillis(departure)
        )

        assertEquals(
            1_000L,
            schedule.nextUpdateDelayMillis(
                midnight.minusSeconds(1)
            )
        )

        assertEquals(
            60_000L,
            schedule.nextUpdateDelayMillis(midnight)
        )
    }

    @Test
    fun clockCorrectionReclassifiesWithoutChangingSavedRecords() {
        val pnr = sample(1, date, "20:35")

        val schedule = RailwayJourneySchedule(
            listOf(pnr)
        )

        val departure = Instant.parse(
            "2026-12-25T15:05:00Z"
        )

        assertEquals(
            listOf(pnr),
            schedule.sectionsAt(
                departure.plusSeconds(1)
            ).past
        )

        assertEquals(
            listOf(pnr),
            schedule.sectionsAt(
                departure.minusSeconds(1)
            ).upcoming
        )
    }

    @Test
    fun emptyAndExtremeDateSchedulesDoNotOverflow() {
        val now = Instant.parse(
            "2026-12-25T00:00:00Z"
        )

        val empty = RailwayJourneySchedule(
            emptyList()
        )

        assertEquals(
            JourneySections(emptyList(), emptyList()),
            empty.sectionsAt(now)
        )

        val distant = sample(
            1,
            LocalDate.MAX,
            "23:59"
        )

        val unknown = sample(
            2,
            LocalDate.MAX,
            null
        )

        val schedule = RailwayJourneySchedule(
            listOf(distant, unknown)
        )

        assertEquals(
            listOf(distant, unknown),
            schedule.sectionsAt(now).upcoming
        )

        assertEquals(
            60_000L,
            schedule.nextUpdateDelayMillis(now)
        )
    }

    private fun sample(
        id: Int,
        date: LocalDate,
        departure: String?
    ) = PnrInfo(
        pnrNumber = id.toString().padStart(10, '0'),
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = date,
        fromStation = "ABC",
        toStation = "XYZ",
        bookingStatus = "WL 5",
        currentStatus = "CNF",
        chartStatus = null,
        passengers = emptyList(),
        lastCheckedAtMillis = 1000L,
        details = PnrDetails(
            departureTime = departure
        )
    )
}