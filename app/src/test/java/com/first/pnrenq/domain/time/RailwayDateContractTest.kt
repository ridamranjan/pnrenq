package com.first.pnrenq.domain.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class RailwayDateContractTest {

    private val dayFirst = DateTimeFormatter
        .ofPattern("dd-MM-uuuu", Locale.US)
        .withResolverStyle(ResolverStyle.STRICT)

    private val legacy = DateTimeFormatter
        .ofPattern("MMM d, uuuu h:mm:ss a", Locale.US)
        .withResolverStyle(ResolverStyle.STRICT)

    @Test
    fun sharedParserMatchesPreviousBackendGrammarForCalendarDates() {
        for (year in listOf(1900, 2000, 2026, 2028, 2100)) {
            for (month in 1..12) {
                for (day in 1..31) {
                    val date = String.format(
                        Locale.ROOT,
                        "%04d-%02d-%02d",
                        year,
                        month,
                        day
                    )

                    val reversed = String.format(
                        Locale.ROOT,
                        "%02d-%02d-%04d",
                        day,
                        month,
                        year
                    )

                    for (raw in listOf(date, reversed)) {
                        assertEquals(
                            previousBackendDate(raw),
                            parseRailwayDate(raw)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun legacyDateTimeAndMalformedInputsMatchPreviousBackendGrammar() {
        for (
        raw in listOf(
            "Feb 29, 2028 12:00:00 AM",
            "Feb 29, 2028 12:00:00 PM",
            "Feb 29, 2028 1:00:00 PM",
            "  Feb 29, 2028 1:00:00 PM  ",
            "Feb 29, 2028 13:00:00 AM",
            "Feb 29, 2028 12:60:00 AM",
            "Feb 30, 2028 1:00:00 PM",
            "2028-02-29T00:00:00Z",
            "2028-02-29 trailing",
            "2028-2-29",
            "29/02/2028",
            "",
            "null"
        )
        ) {
            assertEquals(
                previousBackendDate(raw),
                parseRailwayDate(raw)
            )
        }

        assertNull(
            parseRailwayDate(null)
        )
    }

    // The previous backend first trimmed required strings, then tried these formats.
    private fun previousBackendDate(value: String): LocalDate? {
        val raw = value.trim()

        return try {
            LocalDate.parse(raw)
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(
                    raw,
                    dayFirst
                )
            } catch (_: DateTimeParseException) {
                try {
                    LocalDateTime.parse(
                        raw,
                        legacy
                    ).toLocalDate()
                } catch (_: DateTimeParseException) {
                    null
                }
            }
        }
    }
}