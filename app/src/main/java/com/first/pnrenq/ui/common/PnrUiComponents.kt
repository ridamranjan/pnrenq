package com.first.pnrenq.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.first.pnrenq.domain.model.StatusCategory
import com.first.pnrenq.domain.model.statusCategoryOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val journeyDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

private val lastCheckedFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.getDefault())

fun formatJourneyDate(date: LocalDate): String = journeyDateFormatter.format(date)

fun formatLastChecked(epochMillis: Long): String =
    lastCheckedFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

/** Small coloured label showing a railway status. The text always carries the meaning. */
@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (statusCategoryOf(status)) {
        StatusCategory.CONFIRMED -> scheme.primaryContainer to scheme.onPrimaryContainer
        StatusCategory.RAC -> scheme.secondaryContainer to scheme.onSecondaryContainer
        StatusCategory.WAITING -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        StatusCategory.CANCELLED -> scheme.errorContainer to scheme.onErrorContainer
        StatusCategory.UNKNOWN -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}