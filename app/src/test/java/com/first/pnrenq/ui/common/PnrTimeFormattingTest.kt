package com.first.pnrenq.ui.common

import com.first.pnrenq.ui.theme.AppTimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class PnrTimeFormattingTest {

    @Test
    fun clockPreferenceConvertsBothDirectionsIncludingNoonAndMidnight() {
        assertEquals(
            "8:35 PM",
            formatClockTime(
                "20:35",
                AppTimeFormat.HOUR_12
            )
        )

        assertEquals(
            "20:35",
            formatClockTime(
                "8:35pm",
                AppTimeFormat.HOUR_24
            )
        )

        assertEquals(
            "12:00 AM",
            formatClockTime(
                "00:00",
                AppTimeFormat.HOUR_12
            )
        )

        assertEquals(
            "12:00",
            formatClockTime(
                "12:00 PM",
                AppTimeFormat.HOUR_24
            )
        )
    }

    @Test
    fun invalidProviderTextIsNotPresentedAsAValidClockTime() {
        assertEquals(
            "24:00",
            formatClockTime(
                "24:00",
                AppTimeFormat.HOUR_12
            )
        )

        assertEquals(
            "13:00 PM",
            formatClockTime(
                "13:00 PM",
                AppTimeFormat.HOUR_24
            )
        )

        assertNull(
            formatClockTime(
                "N/A",
                AppTimeFormat.HOUR_24
            )
        )
    }

    @Test
    fun compactAndFullDatesUseTheSameStrictParsing() {
        val date = LocalDate.of(2026, 12, 25)
        val locale = Locale.getDefault()

        val full = date.format(
            DateTimeFormatter.ofPattern(
                "d MMM yyyy",
                locale
            )
        )

        val compact = date.format(
            DateTimeFormatter.ofPattern(
                "d MMM",
                locale
            )
        )

        for (
        raw in listOf(
            "2026-12-25",
            "25-12-2026",
            "Dec 25, 2026 2:33:00 AM"
        )
        ) {
            assertEquals(
                full,
                formatArrivalDate(raw)
            )

            assertEquals(
                compact,
                formatArrivalDate(
                    raw,
                    compact = true
                )
            )
        }

        assertEquals(
            full,
            formatJourneyDate(date)
        )

        assertEquals(
            compact,
            formatJourneyDate(
                date,
                compact = true
            )
        )

        assertEquals(
            "31-02-2026",
            formatArrivalDate("31-02-2026")
        )

        assertNull(
            formatArrivalDate("unknown")
        )
    }

    @Test
    fun lastCheckedUsesIstAcrossUtcDateBoundaryAndBothPreferences() {
        val millis = Instant.parse(
            "2026-12-24T18:30:00Z"
        ).toEpochMilli()

        assertEquals(
            "25 Dec, 00:00 IST",
            formatLastChecked(millis)
        )

        assertEquals(
            "25 Dec, 12:00 AM IST",
            formatLastChecked(
                millis,
                AppTimeFormat.HOUR_12
            )
        )
    }

    @Test
    fun durationsOver24HoursDoNotWrapAndInvalidTextIsPreserved() {
        assertEquals(
            "29h 58m",
            formatJourneyDuration("29:58")
        )

        assertEquals(
            "0h 5m",
            formatJourneyDuration("00:05")
        )

        assertEquals(
            "29:60",
            formatJourneyDuration("29:60")
        )

        assertEquals(
            "-1:20",
            formatJourneyDuration("-1:20")
        )

        assertNull(
            formatJourneyDuration("not available")
        )
    }
}