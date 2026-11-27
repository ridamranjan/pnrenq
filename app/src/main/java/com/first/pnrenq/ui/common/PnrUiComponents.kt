package com.first.pnrenq.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

import com.first.pnrenq.domain.model.StatusCategory
import com.first.pnrenq.domain.model.statusCategoryOf
import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.ThemeText as Text

import java.time.LocalDate

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
        modifier = modifier.widthIn(max = 160.dp),
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
            textAlign = TextAlign.Center
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

    val departureDate = formatJourneyDate(
        journeyDate,
        compact
    )

    val arrivalDateText = formatArrivalDate(
        arrivalDate,
        compact
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val stacked =
            maxWidth < 250.dp ||
                    LocalDensity.current.fontScale >= 1.3f

        if (stacked) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimelineStation(
                    departure,
                    from,
                    departureDate,
                    false,
                    compact,
                    Modifier.fillMaxWidth()
                )

                TimelineDirection(
                    durationText,
                    vertical = true
                )

                TimelineStation(
                    arrival,
                    to,
                    arrivalDateText,
                    false,
                    compact,
                    Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimelineStation(
                    departure,
                    from,
                    departureDate,
                    false,
                    compact,
                    Modifier.weight(1f)
                )

                TimelineDirection(
                    durationText,
                    vertical = false,
                    modifier = Modifier.weight(0.7f)
                )

                TimelineStation(
                    arrival,
                    to,
                    arrivalDateText,
                    true,
                    compact,
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimelineDirection(
    duration: String?,
    vertical: Boolean,
    modifier: Modifier = Modifier
) {
    val foreground = LocalContentColor.current

    Column(
        modifier = modifier,
        horizontalAlignment = if (vertical) {
            Alignment.Start
        } else {
            Alignment.CenterHorizontally
        },
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = if (vertical) "↓" else "→",
            modifier = Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = foreground
        )

        if (duration != null) {
            Text(
                text = duration,
                style = MaterialTheme.typography.labelSmall,
                textAlign = if (vertical) {
                    TextAlign.Start
                } else {
                    TextAlign.Center
                },
                color = foreground
            )
        }
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                textAlign = textAlignment
            )
        }

        if (station != null) {
            Text(
                text = station,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = foreground,
                textAlign = textAlignment
            )
        }

        if (date != null) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = foreground,
                textAlign = textAlignment
            )
        }
    }
}