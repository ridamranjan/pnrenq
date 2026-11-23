
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.first.pnrenq.R
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo

import com.first.pnrenq.ui.common.JourneyTimeline
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.formatArrivalDate
import com.first.pnrenq.ui.common.formatLastChecked

import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.PnrenqTheme

import java.util.Locale

/* =========================================================
   DETAILS SCREEN
========================================================= */

@Composable
fun DetailsScreen(
    pnr: PnrInfo?,
    isRefreshing: Boolean,
    message: String?,
    onMessageShown: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(message) {
        if (message != null) {
            onMessageShown()
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,

        topBar = {
            TopAppBar(
                title = {
                    Text("Ticket details")
                },

                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back")
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },

        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->

        when {
            isLoading && pnr == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            pnr == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("This PNR is no longer saved.")
                }
            }

            else -> {
                DetailsContent(
                    pnr = pnr,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,

                    onDeleteClick = {
                        if (!isRefreshing) {
                            showDeleteDialog = true
                        }
                    },

                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (
        showDeleteDialog &&
        pnr != null &&
        !isRefreshing
    ) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Remove saved PNR?")
            },

            text = {
                Text(
                    "PNR ${pnr.pnrNumber} and its saved " +
                            "details will be removed from this device."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },

                    colors = ButtonDefaults.textButtonColors(
                        contentColor =
                            MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* =========================================================
   MAIN DETAILS CONTENT
========================================================= */

@Composable
private fun DetailsContent(
    pnr: PnrInfo,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val details = pnr.details

    val journeyFields = listOf(
        "Travel class" to details.journeyClass,
        "Quota" to details.quota,
        "Boarding station" to details.boardingPoint,
        "Reservation up to" to details.reservationUpto,
        "Source name" to details.sourceStationName,
        "Destination name" to details.destinationStationName,

        "Distance" to details.distanceKm?.let {
            "${formatNumber(it)} km"
        },

        "Chart status" to pnr.chartStatus
    ).filter {
        availableText(it.second) != null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TrainSummaryCard(
            pnr = pnr,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            onDeleteClick = onDeleteClick
        )

        if (pnr.passengers.isNotEmpty()) {
            PassengerListCard(pnr.passengers)
        }

        if (journeyFields.isNotEmpty()) {
            JourneyInformationCard(journeyFields)
        }

        TicketInformationCard(pnr)

        LastUpdateCard(pnr.lastCheckedAtMillis)

        Spacer(Modifier.height(12.dp))
    }
}

/* =========================================================
   TRAIN SUMMARY
========================================================= */

@Composable
private fun TrainSummaryCard(
    pnr: PnrInfo,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer
        ),

        border = BorderStroke(
            1.dp,
            scheme.primary.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = scheme.primary.copy(alpha = 0.13f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "TRAIN ${pnr.trainNumber}",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer
                    )
                }

                Spacer(Modifier.weight(1f))

                TrainActionButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    iconRes = R.drawable.ic_pnr_refresh,
                    contentDescription = "Refresh PNR status",
                    loading = isRefreshing
                )

                Spacer(Modifier.width(5.dp))

                TrainActionButton(
                    onClick = onDeleteClick,
                    enabled = !isRefreshing,
                    iconRes = R.drawable.ic_pnr_delete,
                    contentDescription = "Delete saved PNR",
                    isDestructive = true
                )
            }

            Text(
                text = pnr.trainName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            CopyablePnr(
                pnrNumber = pnr.pnrNumber
            )

            HorizontalDivider(
                color = scheme.onPrimaryContainer.copy(alpha = 0.18f)
            )

            JourneyTimeline(
                fromStation = pnr.fromStation,
                toStation = pnr.toStation,
                journeyDate = pnr.journeyDate,
                departureTime = pnr.details.departureTime,
                arrivalDate = pnr.details.arrivalDate,
                arrivalTime = pnr.details.arrivalTime,
                duration = pnr.details.duration
            )

            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = scheme.primary,
                    trackColor = scheme.onPrimaryContainer.copy(
                        alpha = 0.12f
                    )
                )
            }
        }
    }
}

/* =========================================================
   TRAIN ACTION BUTTON
========================================================= */

@Composable
private fun TrainActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    iconRes: Int,
    contentDescription: String,
    loading: Boolean = false,
    isDestructive: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = scheme.onPrimaryContainer.copy(alpha = 0.09f)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    strokeWidth = 2.dp,
                    color = scheme.onPrimaryContainer
                )
            } else {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = contentDescription,
                    tint = if (isDestructive) {
                        scheme.error
                    } else {
                        scheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/* =========================================================
   PNR COPY ICON
========================================================= */

@Composable
private fun CopyablePnr(
    pnrNumber: String
) {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "PNR $pnrNumber",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )

        IconButton(
            onClick = {
                val clipboard =
                    context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager

                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "PNR",
                        pnrNumber
                    )
                )
            }
        ) {
            Icon(
                painter = painterResource(
                    R.drawable.ic_pnr_copy
                ),
                contentDescription = "Copy PNR",
                tint = color,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

/* =========================================================
   PASSENGER STATUS FORMAT
========================================================= */

private fun statusTokens(value: String): List<String> {
    return Regex("[A-Za-z0-9]+")
        .findAll(value.uppercase(Locale.ROOT))
        .map { it.value }
        .toList()
}

private fun containsTokenSequence(
    source: String,
    candidate: String
): Boolean {
    val left = statusTokens(source)
    val right = statusTokens(candidate)

    if (right.isEmpty()) return true
    if (right.size > left.size) return false

    return left.windowed(right.size).any {
        it == right
    }
}

private val embeddedSeatPattern = Regex(
    "\\b[A-Z]{1,3}\\d{1,3}\\s+\\d{1,3}\\b"
)

private fun hasEmbeddedSeat(
    status: String?
): Boolean {
    val raw = availableText(status) ?: return false

    val normalized = raw
        .uppercase(Locale.ROOT)
        .replace('/', ' ')
        .replace('-', ' ')
        .replace(Regex("\\s+"), " ")

    return embeddedSeatPattern.containsMatchIn(normalized)
}

private fun isConfirmed(status: String?): Boolean {
    val raw = availableText(status) ?: return false

    return Regex(
        "^(CNF|CONFIRMED)\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(raw)
}

private fun buildStatusText(
    status: String?,
    coach: String?,
    berthNo: Int?,
    berthFallback: String?,
    berthType: String?
): String? {
    val raw = availableText(status).orEmpty()
    val coachText = availableText(coach)

    val berthText = berthNo
        ?.takeIf { it > 0 }
        ?.toString()
        ?: availableText(berthFallback)
            ?.takeIf { it != "0" }

    val typeText = availableText(berthType)

    var result = raw

    // A status that already includes coach and berth
    // takes precedence over separate metadata.
    if (!hasEmbeddedSeat(raw)) {

        if (
            coachText != null &&
            !containsTokenSequence(result, coachText)
        ) {
            result = listOf(result, coachText)
                .filter { it.isNotBlank() }
                .joinToString(" ")
        }

        if (berthText != null) {
            val seatPresent =
                coachText != null &&
                        containsTokenSequence(
                            result,
                            "$coachText $berthText"
                        )

            if (
                !seatPresent &&
                (
                        coachText != null ||
                                hasEmbeddedSeat(result)
                        )
            ) {
                result = listOf(result, berthText)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
            }
        }
    }

    if (
        typeText != null &&
        !containsTokenSequence(result, typeText)
    ) {
        result = listOf(result, typeText)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    return result
        .trim()
        .replace(Regex("\\s+"), " ")
        .takeIf { it.isNotEmpty() }
}

private fun hasConfirmedAllocation(
    passenger: PassengerStatus
): Boolean {
    if (!isConfirmed(passenger.currentStatus)) {
        return false
    }

    if (hasEmbeddedSeat(passenger.currentStatus)) {
        return true
    }

    val coach = availableText(passenger.coach)

    val berth = passenger.currentBerthNo
        ?.takeIf { it > 0 }
        ?: availableText(passenger.berth)
            ?.toIntOrNull()
            ?.takeIf { it > 0 }

    return coach != null && berth != null
}

/* =========================================================
   PASSENGER LIST CARD
========================================================= */

@Composable
private fun PassengerListCard(
    passengers: List<PassengerStatus>
) {
    val scheme = MaterialTheme.colorScheme

    val showBookingLegend = passengers.any {
        !hasConfirmedAllocation(it) &&
                availableText(it.bookingStatus) != null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow
        ),

        border = BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Passengers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = scheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${passengers.size} total",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBookingLegend) {
                    StatusLegend(
                        color = bookingColor(),
                        text = "Booking"
                    )
                }

                StatusLegend(
                    color = currentColor(),
                    text = "Current"
                )
            }

            HorizontalDivider()

            passengers.forEachIndexed { index, passenger ->
                PassengerRow(passenger)

                if (index < passengers.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant.copy(
                            alpha = 0.55f
                        )
                    )
                }
            }
        }
    }
}

/* =========================================================
   PASSENGER ROW
========================================================= */

@Composable
private fun PassengerRow(
    passenger: PassengerStatus
) {
    val scheme = MaterialTheme.colorScheme

    val bookingText =
        if (hasConfirmedAllocation(passenger)) {
            null
        } else {
            buildStatusText(
                status = passenger.bookingStatus,
                coach = passenger.bookingCoach,
                berthNo = passenger.bookingBerthNo,
                berthFallback = null,
                berthType = passenger.bookingBerthCode
            )
        }

    val currentText = buildStatusText(
        status = passenger.currentStatus,
        coach = passenger.coach,
        berthNo = passenger.currentBerthNo,
        berthFallback = passenger.berth,
        berthType = passenger.currentBerthCode
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = scheme.secondaryContainer,

            border = BorderStroke(
                1.dp,
                scheme.secondary.copy(alpha = 0.15f)
            )
        ) {
            Box(
                modifier = Modifier.size(
                    width = 38.dp,
                    height = 34.dp
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P${passenger.index}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSecondaryContainer
                )
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(
                5.dp,
                Alignment.End
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (bookingText != null) {
                PassengerStatusBadge(
                    text = bookingText,
                    isCurrent = false,

                    modifier = if (currentText != null) {
                        Modifier.weight(1f, fill = false)
                    } else {
                        Modifier
                    }
                )
            }

            if (
                bookingText != null &&
                currentText != null
            ) {
                Text(
                    text = "/",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant
                )
            }

            if (currentText != null) {
                PassengerStatusBadge(
                    text = currentText,
                    isCurrent = true,

                    modifier = if (bookingText != null) {
                        Modifier.weight(1f, fill = false)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}

@Composable
private fun PassengerStatusBadge(
    text: String,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isCurrent) {
        currentColor()
    } else {
        bookingColor()
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),

        border = BorderStroke(
            1.dp,
            color.copy(alpha = 0.30f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusLegend(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            color = color,
            shape = CircleShape,
            modifier = Modifier.size(7.dp)
        ) {}

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun bookingColor(): Color {
    val isLight =
        MaterialTheme.colorScheme.background.luminance() > 0.5f

    return if (isLight) {
        Color(0xFF9A6700)
    } else {
        Color(0xFFFFCA68)
    }
}

@Composable
private fun currentColor(): Color {
    val isLight =
        MaterialTheme.colorScheme.background.luminance() > 0.5f

    return if (isLight) {
        Color(0xFF15803D)
    } else {
        Color(0xFF86EFAC)
    }
}

/* =========================================================
   JOURNEY INFORMATION
========================================================= */

@Composable
private fun JourneyInformationCard(
    fields: List<Pair<String, String?>>
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow
        ),

        border = BorderStroke(
            1.dp,
            scheme.secondary.copy(alpha = 0.24f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = scheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_pnr_route
                            ),
                            contentDescription = null,
                            tint = scheme.onSecondaryContainer,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Journey information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Travel and reservation details",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = scheme.secondary.copy(alpha = 0.18f)
            )

            fields.forEachIndexed { index, field ->
                DetailRow(
                    label = field.first,
                    value = field.second
                )

                if (index < fields.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant.copy(
                            alpha = 0.45f
                        )
                    )
                }
            }
        }
    }
}

/* =========================================================
   TICKET INFORMATION
========================================================= */

@Composable
private fun TicketInformationCard(
    pnr: PnrInfo
) {
    val details = pnr.details
    val scheme = MaterialTheme.colorScheme

    val mainFare =
        details.bookingFare ?: details.ticketFare

    val extraFields = listOf(
        "Ticket fare" to details.ticketFare
            ?.takeIf { it != mainFare }
            ?.let(::formatFare),

        "Booking date" to formatArrivalDate(
            details.bookingDate
        ),

        "VIKALP" to details.vikalpStatus,
        "Ticket type" to details.ticketType
    ).filter {
        availableText(it.second) != null
    }

    if (
        mainFare == null &&
        extraFields.isEmpty()
    ) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerHigh
        ),

        border = BorderStroke(
            1.dp,
            scheme.primary.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = scheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Ticket information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Fare and booking details",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            if (mainFare != null) {
                Surface(
                    color = scheme.surfaceContainerLow,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (details.bookingFare != null) {
                                "Booking fare"
                            } else {
                                "Ticket fare"
                            },
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant
                        )

                        Text(
                            text = formatFare(mainFare),
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                    }
                }
            }

            if (extraFields.isNotEmpty()) {
                HorizontalDivider()

                extraFields.forEachIndexed { index, field ->
                    DetailRow(
                        label = field.first,
                        value = field.second
                    )

                    if (index < extraFields.lastIndex) {
                        HorizontalDivider(
                            color = scheme.outlineVariant.copy(
                                alpha = 0.45f
                            )
                        )
                    }
                }
            }
        }
    }
}

/* =========================================================
   LAST UPDATE
========================================================= */

@Composable
private fun LastUpdateCard(
    lastCheckedAtMillis: Long
) {
    val scheme = MaterialTheme.colorScheme
    val timeFormat = LocalPnrTimeFormat.current

    val formattedTime = formatLastChecked(
        lastCheckedAtMillis,
        timeFormat
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.tertiaryContainer.copy(
                alpha = 0.65f
            ),
            contentColor = scheme.onTertiaryContainer
        ),

        border = BorderStroke(
            1.dp,
            scheme.tertiary.copy(alpha = 0.20f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = scheme.onTertiaryContainer.copy(
                    alpha = 0.10f
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_pnr_clock
                        ),
                        contentDescription = null,
                        tint = scheme.onTertiaryContainer,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Last update",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onTertiaryContainer
                )

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onTertiaryContainer
                )

                Text(
                    text = "Last successful PNR check",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onTertiaryContainer.copy(
                        alpha = 0.78f
                    )
                )
            }
        }
    }
}

/* =========================================================
   INFORMATION ROW
========================================================= */

@Composable
private fun DetailRow(
    label: String,
    value: String?
) {
    val text = availableText(value) ?: return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}

/* =========================================================
   FORMATTING
========================================================= */

private fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", value)
    } else {
        String.format(Locale.US, "%,.1f", value)
    }
}

private fun formatFare(value: Double): String =
    "₹${formatNumber(value)}"

@Preview(showBackground = true)
@Composable
private fun ModernDetailsPreview() {
    PnrenqTheme {
        DetailsScreen(
            pnr = MockPnrData.sample().last(),
            isRefreshing = false,
            message = null,
            onMessageShown = {},
            onRefresh = {},
            onDelete = {},
            onBack = {}
        )
    }
}
