
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val journeyFormatter =
    DateTimeFormatter.ofPattern(
        "d MMM yyyy",
        Locale.getDefault()
    )

private val shortDateFormatter =
    DateTimeFormatter.ofPattern(
        "d MMM",
        Locale.getDefault()
    )

private val checkedFormatter =
    DateTimeFormatter.ofPattern(
        "d MMM, h:mm a",
        Locale.getDefault()
    )

fun formatJourneyDate(date: LocalDate): String =
    date.format(journeyFormatter)

fun formatLastChecked(epochMillis: Long): String =
    checkedFormatter.format(
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
    )

fun availableText(value: String?): String? {
    val result = value?.trim() ?: return null

    return result.takeIf {
        it.isNotEmpty() &&
                !it.equals("null", true) &&
                !it.equals("unknown", true) &&
                !it.equals("not available", true) &&
                !it.equals("n/a", true)
    }
}

fun formatArrivalDate(value: String?): String? {
    val valid = availableText(value) ?: return null

    return try {
        LocalDate.parse(valid).format(journeyFormatter)
    } catch (_: Exception) {
        valid
    }
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

/* ---------- Theme-aware status badge ---------- */

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val (background, foreground) =
        when (statusCategoryOf(status)) {
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
        shape = CircleShape,
        color = background,
        contentColor = foreground
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ---------- Reusable departure / arrival timeline ---------- */

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
    val departure = availableText(departureTime)
    val arrival = availableText(arrivalTime)
    val from = availableText(fromStation)
    val to = availableText(toStation)

    val durationText = formatJourneyDuration(duration)

    val departureDateText =
        if (compact) {
            journeyDate.format(shortDateFormatter)
        } else {
            formatJourneyDate(journeyDate)
        }

    val arrivalDateText =
        formatArrivalDate(arrivalDate)?.let { value ->
            if (compact) {
                try {
                    LocalDate.parse(arrivalDate.orEmpty())
                        .format(shortDateFormatter)
                } catch (_: Exception) {
                    value
                }
            } else {
                value
            }
        }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimelineStation(
            time = departure,
            station = from,
            date = departureDateText,
            modifier = Modifier.weight(1f),
            alignEnd = false,
            compact = compact
        )

        Column(
            modifier = Modifier.weight(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (durationText != null) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        TimelineStation(
            time = arrival,
            station = to,
            date = arrivalDateText,
            modifier = Modifier.weight(1f),
            alignEnd = true,
            compact = compact
        )
    }
}

@Composable
private fun TimelineStation(
    time: String?,
    station: String?,
    date: String?,
    modifier: Modifier = Modifier,
    alignEnd: Boolean,
    compact: Boolean
) {
    val alignment =
        if (alignEnd) Alignment.End else Alignment.Start

    val textAlignment =
        if (alignEnd) TextAlign.End else TextAlign.Start

    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (time != null) {
            Text(
                text = time,
                style = if (compact) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        if (station != null) {
            Text(
                text = station,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = textAlignment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (date != null) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlignment,
                maxLines = 1
            )
        }
    }
}
