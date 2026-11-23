
package com.first.pnrenq.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.first.pnrenq.domain.model.StatusCategory
import com.first.pnrenq.domain.model.statusCategoryOf
import com.first.pnrenq.ui.theme.AppTimeFormat
import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.ThemeText as Text

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

private val longDateFormatter = DateTimeFormatter.ofPattern(
    "d MMM yyyy",
    Locale.getDefault()
)

private val shortDateFormatter = DateTimeFormatter.ofPattern(
    "d MMM",
    Locale.getDefault()
)

private val amPmParser = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("h:mm a")
    .toFormatter(Locale.ENGLISH)

private val amPmSecondsParser = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("h:mm:ss a")
    .toFormatter(Locale.ENGLISH)

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
    date.format(longDateFormatter)

fun formatArrivalDate(value: String?): String? {
    val valid = availableText(value) ?: return null

    return try {
        LocalDate.parse(valid).format(longDateFormatter)
    } catch (_: Exception) {
        valid
    }
}

fun parseClockTime(value: String?): LocalTime? {
    val raw = availableText(value) ?: return null

    val normalized = raw
        .uppercase(Locale.ENGLISH)
        .replace(
            Regex("(?<=\\d)(AM|PM)$"),
            " $1"
        )
        .replace(Regex("\\s+"), " ")

    return try {
        LocalTime.parse(normalized)
    } catch (_: Exception) {
        try {
            LocalTime.parse(normalized, amPmParser)
        } catch (_: Exception) {
            try {
                LocalTime.parse(
                    normalized,
                    amPmSecondsParser
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

fun formatClockTime(
    value: String?,
    format: AppTimeFormat
): String? {
    val valid = availableText(value) ?: return null

    val time = parseClockTime(valid) ?: return valid

    val pattern =
        if (format == AppTimeFormat.HOUR_12) {
            "h:mm a"
        } else {
            "HH:mm"
        }

    return time.format(
        DateTimeFormatter.ofPattern(
            pattern,
            Locale.ENGLISH
        )
    )
}

fun formatLastChecked(
    epochMillis: Long,
    format: AppTimeFormat = AppTimeFormat.HOUR_24
): String {
    val pattern =
        if (format == AppTimeFormat.HOUR_12) {
            "d MMM, h:mm a"
        } else {
            "d MMM, HH:mm"
        }

    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(
            DateTimeFormatter.ofPattern(
                pattern,
                Locale.ENGLISH
            )
        )
}

fun formatJourneyDuration(value: String?): String? {
    val valid = availableText(value) ?: return null
    val parts = valid.split(":")

    if (parts.size != 2) return valid

    val hours = parts[0].toIntOrNull() ?: return valid
    val minutes = parts[1].toIntOrNull() ?: return valid

    if (hours < 0 || minutes !in 0..59) return valid

    return "${hours}h ${minutes}m"
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val colors = when (statusCategoryOf(status)) {
        StatusCategory.CONFIRMED ->
            scheme.primaryContainer to
                    scheme.onPrimaryContainer

        StatusCategory.RAC ->
            scheme.tertiaryContainer to
                    scheme.onTertiaryContainer

        StatusCategory.WAITING ->
            scheme.secondaryContainer to
                    scheme.onSecondaryContainer

        StatusCategory.CANCELLED ->
            scheme.errorContainer to
                    scheme.onErrorContainer

        StatusCategory.UNKNOWN ->
            scheme.surfaceVariant to
                    scheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        color = colors.first,
        contentColor = colors.second,
        shape = CircleShape
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(
                horizontal = 11.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.second,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun JourneyTimeline(
    fromStation: String?,
    toStation: String?,
    journeyDate: LocalDate,
    departureTime: String?,
    arrivalDate: String?,
    arrivalTime: String?,
    duration: String?,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val format = LocalPnrTimeFormat.current
    val foreground = LocalContentColor.current

    val departure = formatClockTime(
        departureTime,
        format
    )

    val arrival = formatClockTime(
        arrivalTime,
        format
    )

    val from = availableText(fromStation)
    val to = availableText(toStation)

    val durationText = formatJourneyDuration(duration)

    val departureDate =
        if (compact) {
            journeyDate.format(shortDateFormatter)
        } else {
            journeyDate.format(longDateFormatter)
        }

    val arrivalDateText =
        availableText(arrivalDate)?.let {
            try {
                LocalDate.parse(it).format(
                    if (compact) shortDateFormatter
                    else longDateFormatter
                )
            } catch (_: Exception) {
                it
            }
        }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimelineStation(
            time = departure,
            station = from,
            date = departureDate,
            alignEnd = false,
            compact = compact,
            modifier = Modifier.weight(1f)
        )

        Column(
            modifier = Modifier.weight(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = foreground
            )

            if (durationText != null) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = foreground.copy(alpha = 0.82f)
                )
            }
        }

        TimelineStation(
            time = arrival,
            station = to,
            date = arrivalDateText,
            alignEnd = true,
            compact = compact,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TimelineStation(
    time: String?,
    station: String?,
    date: String?,
    alignEnd: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val foreground = LocalContentColor.current

    val alignment =
        if (alignEnd) Alignment.End else Alignment.Start

    val textAlignment =
        if (alignEnd) TextAlign.End else TextAlign.Start

    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {
        if (time != null) {
            Text(
                text = time,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.Bold,
                color = foreground,
                textAlign = textAlignment,
                maxLines = 2
            )
        }

        if (station != null) {
            Text(
                text = station,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = foreground,
                textAlign = textAlignment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (date != null) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = foreground.copy(alpha = 0.82f),
                textAlign = textAlignment,
                maxLines = 1
            )
        }
    }
}
