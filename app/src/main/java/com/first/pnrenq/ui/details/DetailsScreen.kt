
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo

import com.first.pnrenq.ui.common.JourneyTimeline
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.formatArrivalDate
import com.first.pnrenq.ui.common.formatLastChecked

import com.first.pnrenq.ui.theme.PnrenqTheme

import java.util.Locale

/* =========================================================
   MAIN DETAILS SCREEN
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
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ticket details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },

                navigationIcon = {
                    TextButton(
                        onClick = onBack
                    ) {
                        Text("← Back")
                    }
                }
            )
        },

        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->

        when {
            isLoading && pnr == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),

                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            pnr == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),

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

                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    /* ---------- Delete confirmation ---------- */

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
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
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
   DETAILS CONTENT
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
        "PNR" to pnr.pnrNumber,
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

    val bookingFare = formatFare(details.bookingFare)
    val ticketFare = formatFare(details.ticketFare)

    val ticketFields = listOf(
        "Booking fare" to bookingFare,

        "Ticket fare" to ticketFare?.takeIf {
            it != bookingFare
        },

        "Booking date" to formatArrivalDate(
            details.bookingDate
        ),

        "VIKALP" to details.vikalpStatus,
        "Ticket type" to details.ticketType

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

        /* ---------- Train summary ---------- */

        TrainSummaryCard(pnr)

        /* ---------- Passengers ---------- */

        if (pnr.passengers.isNotEmpty()) {
            PassengerListCard(
                passengers = pnr.passengers
            )
        }

        /* ---------- Journey information ---------- */

        if (journeyFields.isNotEmpty()) {
            DetailSection(
                title = "Journey information",
                subtitle = "Travel and reservation details"
            ) {
                journeyFields.forEach { (label, value) ->
                    DetailRow(
                        label = label,
                        value = value
                    )
                }
            }
        }

        /* ---------- Ticket information ---------- */

        if (ticketFields.isNotEmpty()) {
            DetailSection(
                title = "Ticket information",
                subtitle = "Fare and booking details"
            ) {
                ticketFields.forEach { (label, value) ->
                    DetailRow(
                        label = label,
                        value = value
                    )
                }
            }
        }

        /* ---------- Last update ---------- */

        DetailSection(
            title = "Last update",
            subtitle = null
        ) {
            DetailRow(
                label = "Checked",
                value = formatLastChecked(
                    pnr.lastCheckedAtMillis
                )
            )
        }

        /* ---------- Refresh ---------- */

        Button(
            onClick = onRefresh,
            enabled = !isRefreshing,

            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),

            shape = RoundedCornerShape(16.dp)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(Modifier.width(12.dp))

                Text("Updating...")
            } else {
                Text(
                    text = "↻  Refresh status",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        /* ---------- Delete ---------- */

        OutlinedButton(
            onClick = onDeleteClick,
            enabled = !isRefreshing,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape = RoundedCornerShape(16.dp),

            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Remove saved PNR")
        }

        Spacer(Modifier.height(12.dp))
    }
}

/* =========================================================
   TRAIN SUMMARY
========================================================= */

@Composable
private fun TrainSummaryCard(
    pnr: PnrInfo
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

            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            /* ---------- Train number ---------- */

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

            /* ---------- Train name ---------- */

            Text(
                text = pnr.trainName,

                style = MaterialTheme.typography.headlineSmall,

                fontWeight = FontWeight.Bold,

                color = scheme.onPrimaryContainer,

                maxLines = 2,

                overflow = TextOverflow.Ellipsis
            )

            /* ---------- PNR / Copy ---------- */

            CopyablePnr(
                pnrNumber = pnr.pnrNumber
            )

            HorizontalDivider(
                color = scheme.onPrimaryContainer.copy(
                    alpha = 0.18f
                )
            )

            /* ---------- Route ---------- */

            JourneyTimeline(
                fromStation = pnr.fromStation,
                toStation = pnr.toStation,
                journeyDate = pnr.journeyDate,
                departureTime = pnr.details.departureTime,
                arrivalDate = pnr.details.arrivalDate,
                arrivalTime = pnr.details.arrivalTime,
                duration = pnr.details.duration
            )
        }
    }
}

/* =========================================================
   COPY PNR
========================================================= */

@Composable
private fun CopyablePnr(
    pnrNumber: String
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "PNR $pnrNumber",

            style = MaterialTheme.typography.bodyMedium,

            fontWeight = FontWeight.SemiBold,

            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        TextButton(
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
            },

            colors = ButtonDefaults.textButtonColors(
                contentColor =
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = "⧉ Copy",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/* =========================================================
   PASSENGER STATUS FORMATTING

   Fixes:
   - Avoid duplicated coach/berth
   - Preserve RAC/WL status
   - Hide redundant booking badge for allocated CNF
========================================================= */

/*
 Converts a value into comparable tokens.

 Example:
 "CNF A1 15" -> ["CNF", "A1", "15"]
 "A1/15"     -> ["A1", "15"]
*/
private fun statusTokens(
    value: String
): List<String> {
    return Regex("[A-Za-z0-9]+")
        .findAll(value.uppercase(Locale.ROOT))
        .map { it.value }
        .toList()
}

/*
 Checks whether all tokens in a piece of metadata
 already appear consecutively in the status.

 This is important because the API may return:

 currentStatus = "CNF A1 15"
 coach = "A1"
 berth = 15

 We should not append A1 15 again.
*/
private fun containsTokenSequence(
    existing: String,
    candidate: String
): Boolean {
    val source = statusTokens(existing)
    val target = statusTokens(candidate)

    if (target.isEmpty()) {
        return true
    }

    if (target.size > source.size) {
        return false
    }

    return source.windowed(target.size).any {
        it == target
    }
}

/*
 Detects whether the status itself already includes
 a confirmed seat allocation.

 Examples:
 CNF A1 15
 CNF B2 34 LB
 CONFIRMED S3 42

 This prevents adding conflicting or repeated
 coach/berth metadata to an already complete status.
*/
private fun hasEmbeddedConfirmedBerth(
    status: String?
): Boolean {
    val value = availableText(status) ?: return false
    val tokens = statusTokens(value)

    val confirmedIndex = tokens.indexOfFirst {
        it == "CNF" || it == "CONFIRMED"
    }

    if (confirmedIndex == -1) {
        return false
    }

    val remaining = tokens.drop(confirmedIndex + 1)

    if (remaining.size < 2) {
        return false
    }

    val coach = remaining[0]
    val berth = remaining[1]

    val validCoach = coach.matches(
        Regex("[A-Z]{1,3}[0-9]{1,3}")
    )

    val validBerth = berth.matches(
        Regex("[0-9]{1,3}")
    ) && berth.toIntOrNull()?.let {
        it > 0
    } == true

    return validCoach && validBerth
}

/*
 Builds a clean status badge.

 Examples:

 "CNF A1 15" + A1 + 15 + LB
 -> "CNF A1 15 LB"

 "RAC 7" + null + null
 -> "RAC 7"

 "CNF" + B2 + 34 + UB
 -> "CNF B2 34 UB"

 A field already contained in the status is not
 appended again.
*/
private fun buildPassengerStatus(
    status: String?,
    coach: String?,
    berthNumber: Int?,
    berthFallback: String?,
    berthType: String?
): String? {
    val rawStatus = availableText(status)

    val coachText = availableText(coach)

    val berthText = berthNumber
        ?.takeIf { it > 0 }
        ?.toString()
        ?: availableText(berthFallback)
            ?.takeIf {
                it != "0"
            }

    val typeText = availableText(berthType)

    if (
        rawStatus == null &&
        coachText == null &&
        berthText == null &&
        typeText == null
    ) {
        return null
    }

    var result = rawStatus.orEmpty()

    val embeddedSeat = hasEmbeddedConfirmedBerth(
        rawStatus
    )

    /*
     If a complete confirmed seat is already inside
     the status, trust it and do not append coach or
     berth again. Berth type may still be missing.
    */
    val pieces = if (embeddedSeat) {
        listOf(typeText)
    } else {
        listOf(
            coachText,
            berthText,
            typeText
        )
    }

    pieces.forEach { piece ->
        if (
            piece != null &&
            !containsTokenSequence(result, piece)
        ) {
            result = if (result.isBlank()) {
                piece
            } else {
                "$result $piece"
            }
        }
    }

    return result
        .trim()
        .replace(Regex("\\s+"), " ")
        .takeIf { it.isNotBlank() }
}

/*
 A confirmed berth is considered allocated when:
 - current status is CNF or CONFIRMED
 - and a coach + berth can be identified either
   within the status or from separate fields.
*/
private fun hasConfirmedAllocation(
    passenger: PassengerStatus
): Boolean {
    val status = availableText(
        passenger.currentStatus
    ) ?: return false

    val isConfirmed = statusTokens(status).firstOrNull()
        ?.let {
            it == "CNF" || it == "CONFIRMED"
        } == true

    if (!isConfirmed) {
        return false
    }

    if (hasEmbeddedConfirmedBerth(status)) {
        return true
    }

    val coach = availableText(
        passenger.coach
    )

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

    val anyBookingVisible = passengers.any { passenger ->
        !hasConfirmedAllocation(passenger) &&
                availableText(passenger.bookingStatus) != null
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
                    shape = RoundedCornerShape(10.dp),
                    color = scheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "${passengers.size} total",

                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),

                        style = MaterialTheme.typography.labelMedium,

                        color = scheme.onSurfaceVariant
                    )
                }
            }

            /* ---------- Legend ---------- */

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {
                if (anyBookingVisible) {
                    StatusLegend(
                        color = bookingStatusColor(),
                        label = "Booking"
                    )
                }

                StatusLegend(
                    color = currentStatusColor(),
                    label = "Current"
                )
            }

            HorizontalDivider()

            /* ---------- Passenger list ---------- */

            passengers.forEachIndexed { index, passenger ->

                PassengerEntry(
                    passenger = passenger
                )

                if (index < passengers.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant.copy(
                            alpha = 0.65f
                        )
                    )
                }
            }
        }
    }
}

/* =========================================================
   STATUS LEGEND
========================================================= */

@Composable
private fun StatusLegend(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(50),
            modifier = Modifier.size(7.dp)
        ) {}

        Text(
            text = label,

            style = MaterialTheme.typography.labelSmall,

            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* =========================================================
   PASSENGER ENTRY
========================================================= */

@Composable
private fun PassengerEntry(
    passenger: PassengerStatus
) {
    val confirmedWithBerth = hasConfirmedAllocation(
        passenger
    )

    val bookingText = if (confirmedWithBerth) {
        null
    } else {
        buildPassengerStatus(
            status = passenger.bookingStatus,
            coach = passenger.bookingCoach,
            berthNumber = passenger.bookingBerthNo,
            berthFallback = null,
            berthType = passenger.bookingBerthCode
        )
    }

    val currentText = buildPassengerStatus(
        status = passenger.currentStatus,
        coach = passenger.coach,
        berthNumber = passenger.currentBerthNo,
        berthFallback = passenger.berth,
        berthType = passenger.currentBerthCode
    )

    Column(
        modifier = Modifier.fillMaxWidth(),

        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        /*
         One row with passenger label on the left
         and status badge(s) on the right.

         The badge area scrolls horizontally on
         narrow screens rather than being cut off.
        */
        Row(
            modifier = Modifier.fillMaxWidth(),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "Passenger ${passenger.index}",

                style = MaterialTheme.typography.labelLarge,

                fontWeight = FontWeight.SemiBold,

                maxLines = 1
            )

            if (
                bookingText != null ||
                currentText != null
            ) {

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),

                    horizontalArrangement = Arrangement.spacedBy(6.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (bookingText != null) {
                        PassengerStatusBadge(
                            text = bookingText,
                            isCurrent = false
                        )
                    }

                    if (
                        bookingText != null &&
                        currentText != null
                    ) {
                        Text(
                            text = "/",

                            style = MaterialTheme.typography.titleMedium,

                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentText != null) {
                        PassengerStatusBadge(
                            text = currentText,
                            isCurrent = true
                        )
                    }
                }
            }
        }

        /* ---------- Optional coach position ---------- */

        passenger.coachPosition
            ?.takeIf { it > 0 }
            ?.let { position ->

                Text(
                    text = "Coach position: $position",

                    style = MaterialTheme.typography.labelSmall,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
    }
}

/* =========================================================
   PASSENGER BADGE
========================================================= */

@Composable
private fun PassengerStatusBadge(
    text: String,
    isCurrent: Boolean
) {
    val color = if (isCurrent) {
        currentStatusColor()
    } else {
        bookingStatusColor()
    }

    Surface(
        color = color.copy(alpha = 0.12f),

        shape = RoundedCornerShape(10.dp),

        border = BorderStroke(
            1.dp,
            color.copy(alpha = 0.30f)
        )
    ) {
        Text(
            text = text,

            modifier = Modifier.padding(
                horizontal = 11.dp,
                vertical = 7.dp
            ),

            style = MaterialTheme.typography.labelLarge,

            fontWeight = FontWeight.Bold,

            color = color,

            maxLines = 1
        )
    }
}

/* =========================================================
   STATUS COLORS
========================================================= */

@Composable
private fun bookingStatusColor(): Color {
    val isLight =
        MaterialTheme.colorScheme.background.luminance() > 0.5f

    return if (isLight) {
        Color(0xFF9A6700)
    } else {
        Color(0xFFFFCA68)
    }
}

@Composable
private fun currentStatusColor(): Color {
    val isLight =
        MaterialTheme.colorScheme.background.luminance() > 0.5f

    return if (isLight) {
        Color(0xFF15803D)
    } else {
        Color(0xFF86EFAC)
    }
}

/* =========================================================
   REUSABLE DETAIL SECTION
========================================================= */

@Composable
private fun DetailSection(
    title: String,
    subtitle: String?,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,

                style = MaterialTheme.typography.titleMedium,

                fontWeight = FontWeight.SemiBold
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,

                    style = MaterialTheme.typography.bodySmall,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            content()
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

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,

            modifier = Modifier.weight(1f),

            style = MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,

            modifier = Modifier.weight(1f),

            textAlign = TextAlign.End,

            style = MaterialTheme.typography.bodyMedium,

            fontWeight = FontWeight.Medium
        )
    }
}

/* =========================================================
   FORMATTING
========================================================= */

private fun formatNumber(
    value: Double
): String {
    return if (value % 1.0 == 0.0) {
        String.format(
            Locale.US,
            "%,.0f",
            value
        )
    } else {
        String.format(
            Locale.US,
            "%,.1f",
            value
        )
    }
}

private fun formatFare(
    value: Double?
): String? {
    return value?.let {
        "₹${formatNumber(it)}"
    }
}

/* =========================================================
   PREVIEW
========================================================= */

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
