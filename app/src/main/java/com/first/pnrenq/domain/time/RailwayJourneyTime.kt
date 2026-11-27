package com.first.pnrenq.domain.time

import com.first.pnrenq.domain.model.PnrInfo
import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

internal val RAILWAY_ZONE: ZoneId =
    ZoneId.of("Asia/Kolkata")

private val clockPattern = Regex(
    "([0-9]{1,2}):([0-9]{2})(?::([0-9]{2})(?:\\.([0-9]{1,9}))?)?(?:\\s*([AP]M))?"
)

private val dayFirstDate = DateTimeFormatter
    .ofPattern("dd-MM-uuuu", Locale.US)
    .withResolverStyle(ResolverStyle.STRICT)

private val legacyDateTime = DateTimeFormatter
    .ofPattern("MMM d, uuuu h:mm:ss a", Locale.US)
    .withResolverStyle(ResolverStyle.STRICT)

/** Parse a railway wall-clock time without silently normalizing invalid values. */
internal fun parseRailwayClock(value: String?): LocalTime? {
    val match = clockPattern.matchEntire(
        value?.trim()?.uppercase(Locale.ROOT)
            ?: return null
    ) ?: return null

    val (hours, minutes, seconds, fraction, period) =
        match.destructured

    val rawHour = hours.toInt()

    val hour = if (period.isEmpty()) {
        rawHour
    } else {
        if (rawHour !in 1..12) return null

        rawHour % 12 +
                if (period == "PM") 12 else 0
    }

    return try {
        LocalTime.of(
            hour,
            minutes.toInt(),
            seconds.toIntOrNull() ?: 0,
            fraction.padEnd(9, '0').toInt()
        )
    } catch (_: DateTimeException) {
        null
    }
}

/** The date formats already supported for the backend's required journey date. */
internal fun parseRailwayDate(
    value: String?
): LocalDate? {
    val raw = value?.trim() ?: return null

    return try {
        LocalDate.parse(raw)
    } catch (_: DateTimeParseException) {
        try {
            LocalDate.parse(
                raw,
                dayFirstDate
            )
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(
                    raw,
                    legacyDateTime
                ).toLocalDate()
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}

internal data class JourneySections(
    val upcoming: List<PnrInfo>,
    val past: List<PnrInfo>
)

/** Parse and sort once per saved-record snapshot; classify against a supplied instant. */
internal class RailwayJourneySchedule(
    pnrs: List<PnrInfo>
) {
    private data class Entry(
        val pnr: PnrInfo,
        val departure: LocalTime?,
        val cutoff: Instant?
    ) {
        fun isPast(now: Instant): Boolean =
            cutoff?.let {
                !now.isBefore(it)
            } ?: false
    }

    private val entries = pnrs.map { pnr ->
        val departure =
            parseRailwayClock(
                pnr.details.departureTime
            )

        val date = pnr.journeyDate

        val cutoff = when {
            departure != null ->
                date.atTime(departure)
                    .atZone(RAILWAY_ZONE)
                    .toInstant()

            date == LocalDate.MAX ->
                null

            else ->
                date.plusDays(1)
                    .atStartOfDay(RAILWAY_ZONE)
                    .toInstant()
        }

        Entry(
            pnr,
            departure,
            cutoff
        )
    }

    // Unknown times sort last within their date, in either section.
    private val upcomingOrder = entries.sortedWith(
        compareBy<Entry> { it.pnr.journeyDate }
            .thenBy { it.departure == null }
            .thenBy { it.departure }
            .thenBy { it.pnr.pnrNumber }
    )

    private val pastOrder = entries.sortedWith(
        compareByDescending<Entry> {
            it.pnr.journeyDate
        }
            .thenBy { it.departure == null }
            .thenByDescending { it.departure }
            .thenBy { it.pnr.pnrNumber }
    )

    // Equal cutoffs are one classification boundary, even for multiple journeys.
    private val boundaries = entries
        .mapNotNull { it.cutoff }
        .distinct()
        .sorted()

    private fun boundaryIndex(now: Instant): Int {
        // Upper bound: all instants at or before now have already been crossed.
        var low = 0
        var high = boundaries.size

        while (low < high) {
            val middle = low + (high - low) / 2

            if (boundaries[middle].isAfter(now)) {
                high = middle
            } else {
                low = middle + 1
            }
        }

        return low
    }

    fun sectionsAt(now: Instant): JourneySections =
        JourneySections(
            upcoming = buildList {
                for (entry in upcomingOrder) {
                    if (!entry.isPast(now)) {
                        add(entry.pnr)
                    }
                }
            },

            past = buildList {
                for (entry in pastOrder) {
                    if (entry.isPast(now)) {
                        add(entry.pnr)
                    }
                }
            }
        )

    /** Wake at the next classification boundary, with a bounded wall-clock recheck. */
    fun nextUpdateDelayMillis(now: Instant): Long =
        delayAt(
            now,
            boundaryIndex(now)
        )

    private fun delayAt(
        now: Instant,
        index: Int
    ): Long {
        val next = boundaries.getOrNull(index)
            ?: return MAX_RECHECK_MILLIS

        // Bound the duration before converting to milliseconds (very distant dates).
        return Duration.between(now, next)
            .coerceAtMost(
                Duration.ofMillis(MAX_RECHECK_MILLIS)
            )
            .toMillis()
            .coerceAtLeast(1L)
    }

    /** A tracker belongs to one UI consumer, never shared across threads. */
    fun newTracker(): Tracker = Tracker()

    inner class Tracker internal constructor() {
        private var lastIndex = -1
        private var lastSections: JourneySections? = null

        fun update(now: Instant): JourneyScheduleUpdate {
            val index = boundaryIndex(now)

            val sections = if (index == lastIndex) {
                checkNotNull(lastSections)
            } else {
                sectionsAt(now).also {
                    lastIndex = index
                    lastSections = it
                }
            }

            return JourneyScheduleUpdate(
                sections,
                delayAt(now, index)
            )
        }
    }

    private companion object {
        const val MAX_RECHECK_MILLIS = 60_000L
    }
}

internal data class JourneyScheduleUpdate(
    val sections: JourneySections,
    val delayMillis: Long
)