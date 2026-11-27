package com.first.pnrenq.ui.common

import com.first.pnrenq.domain.time.RAILWAY_ZONE
import com.first.pnrenq.domain.time.parseRailwayClock
import com.first.pnrenq.domain.time.parseRailwayDate
import com.first.pnrenq.ui.theme.AppTimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val longDateFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy")

private val shortDateFormatter =
    DateTimeFormatter.ofPattern("d MMM")

private val clock12Formatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

private val clock24Formatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

private val checked12Formatter =
    DateTimeFormatter.ofPattern("d MMM, h:mm a 'IST'", Locale.ENGLISH)

private val checked24Formatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm 'IST'", Locale.ENGLISH)

fun availableText(value: String?): String? {
    val result = value?.trim() ?: return null

    return result.takeIf {
        it.isNotBlank() &&
                !it.equals("null", true) &&
                !it.equals("unknown", true) &&
                !it.equals("not available", true) &&
                !it.equals("n/a", true)
    }
}

fun formatJourneyDate(date: LocalDate): String =
    formatJourneyDate(date, compact = false)

internal fun formatJourneyDate(
    date: LocalDate,
    compact: Boolean
): String =
    date.format(
        (if (compact) shortDateFormatter else longDateFormatter)
            .withLocale(Locale.getDefault())
    )

fun formatArrivalDate(value: String?): String? =
    formatArrivalDate(value, compact = false)

internal fun formatArrivalDate(
    value: String?,
    compact: Boolean
): String? {
    val valid = availableText(value) ?: return null

    // Preserve unfamiliar provider text, without inventing a date.
    return parseRailwayDate(valid)?.let {
        formatJourneyDate(it, compact)
    } ?: valid
}

fun parseClockTime(value: String?): LocalTime? =
    parseRailwayClock(value)

fun formatClockTime(
    value: String?,
    format: AppTimeFormat
): String? {
    val valid = availableText(value) ?: return null
    val time = parseClockTime(valid) ?: return valid

    return time.format(
        if (format == AppTimeFormat.HOUR_12) {
            clock12Formatter
        } else {
            clock24Formatter
        }
    )
}

fun formatLastChecked(
    epochMillis: Long,
    format: AppTimeFormat = AppTimeFormat.HOUR_24
): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(RAILWAY_ZONE)
        .format(
            if (format == AppTimeFormat.HOUR_12) {
                checked12Formatter
            } else {
                checked24Formatter
            }
        )

fun formatJourneyDuration(value: String?): String? {
    val valid = availableText(value) ?: return null
    val parts = valid.split(":")

    if (parts.size != 2) return valid

    val hours = parts[0].toIntOrNull() ?: return valid
    val minutes = parts[1].toIntOrNull() ?: return valid

    if (hours < 0 || minutes !in 0..59) return valid

    return "${hours}h ${minutes}m"
}