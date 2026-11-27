package com.first.pnrenq.domain.time

import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class RailwayJourneyTrackerTest {

    private val date =
        LocalDate.of(2026, 12, 25)

    private val departure =
        Instant.parse("2026-12-25T15:05:00Z")

    private val midnight =
        Instant.parse("2026-12-25T18:30:00Z")

    @Test
    fun repeatedForegroundChecksReuseOneClassification() {
        val schedule = RailwayJourneySchedule(
            listOf(
                sample(1, "20:35"),
                sample(2, null)
            )
        )

        val tracker = schedule.newTracker()

        val first = tracker.update(
            departure.minusSeconds(3600)
        ).sections

        for (second in 3599 downTo 1) {
            val update = tracker.update(
                departure.minusSeconds(
                    second.toLong()
                )
            )

            assertTrue(
                first === update.sections
            )
        }

        assertEquals(
            1000L,
            tracker.update(
                departure.minusSeconds(1)
            ).delayMillis
        )

        assertEquals(
            1L,
            tracker.update(
                departure.minusNanos(1)
            ).delayMillis
        )
    }

    @Test
    fun equalCutoffsMoveTogetherAndMidnightRetainsUnknownTimeOrdering() {
        val first = sample(1, "20:35")
        val second = sample(2, "8:35 PM")
        val unknown = sample(3, null)

        val tracker = RailwayJourneySchedule(
            listOf(
                unknown,
                second,
                first
            )
        ).newTracker()

        val before = tracker.update(
            departure.minusNanos(1)
        )

        val crossed = tracker.update(
            departure
        )

        assertEquals(
            listOf(first, second, unknown),
            before.sections.upcoming
        )

        assertEquals(
            listOf(first, second),
            crossed.sections.past
        )

        assertEquals(
            listOf(unknown),
            crossed.sections.upcoming
        )

        assertTrue(
            before.sections !== crossed.sections
        )

        assertEquals(
            listOf(first, second, unknown),
            tracker.update(midnight).sections.past
        )
    }

    @Test
    fun clockMovingBackwardReclassifiesAndNewScheduleNeverReusesOldRecords() {
        val original = sample(1, "20:35")

        val tracker = RailwayJourneySchedule(
            listOf(original)
        ).newTracker()

        assertEquals(
            listOf(original),
            tracker.update(departure).sections.past
        )

        assertEquals(
            listOf(original),
            tracker.update(
                departure.minusSeconds(1)
            ).sections.upcoming
        )

        val refreshed = original.copy(
            trainName = "Updated train",
            lastCheckedAtMillis = 2000L
        )

        val replacement = RailwayJourneySchedule(
            listOf(refreshed)
        ).newTracker()

        assertEquals(
            listOf(refreshed),
            replacement.update(
                departure.minusSeconds(1)
            ).sections.upcoming
        )
    }

    @Test
    fun trackerMatchesDirectClassificationAndDelayInBothDirections() {
        val schedule = RailwayJourneySchedule(
            listOf(
                sample(1, "20:35"),
                sample(2, null),
                sample(3, "00:00"),
                sample(4, "24:00"),

                sample(5, null).copy(
                    journeyDate = LocalDate.MAX
                )
            )
        )

        val tracker = schedule.newTracker()

        val instants = listOf(
            Instant.MIN,
            departure.minusNanos(1),
            departure,
            midnight.minusNanos(1),
            midnight,
            Instant.MAX
        )

        for (now in instants + instants.reversed()) {
            val actual = tracker.update(now)

            assertEquals(
                schedule.sectionsAt(now),
                actual.sections
            )

            assertEquals(
                schedule.nextUpdateDelayMillis(now),
                actual.delayMillis
            )
        }

        val empty = RailwayJourneySchedule(
            emptyList()
        ).newTracker()

        assertEquals(
            JourneySections(
                emptyList(),
                emptyList()
            ),

            empty.update(departure).sections
        )

        assertEquals(
            60_000L,
            empty.update(departure).delayMillis
        )
    }

    private fun sample(
        id: Int,
        time: String?
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
            departureTime = time
        )
    )
}