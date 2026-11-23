
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.common.JourneyTimeline
import com.first.pnrenq.ui.common.StatusChip
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
                        "Ticket details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
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
                    "PNR ${pnr.pnrNumber} and its saved details will be removed from this device."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
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

        "Booking status" to pnr.bookingStatus,
        "Current status" to pnr.currentStatus,
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),

            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerLow
            ),

            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "TRAIN ${pnr.trainNumber}",
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 7.dp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (
                        availableText(pnr.currentStatus) != null
                    ) {
                        Spacer(Modifier.width(8.dp))

                        StatusChip(
                            status = pnr.currentStatus
                        )
                    }
                }

                Text(
                    text = pnr.trainName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "PNR ${pnr.pnrNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                JourneyTimeline(
                    fromStation = pnr.fromStation,
                    toStation = pnr.toStation,
                    journeyDate = pnr.journeyDate,
                    departureTime = details.departureTime,
                    arrivalDate = details.arrivalDate,
                    arrivalTime = details.arrivalTime,
                    duration = details.duration
                )
            }
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

        /* ---------- Passengers ---------- */

        if (pnr.passengers.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Passengers",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "${pnr.passengers.size} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            pnr.passengers.forEach { passenger ->
                ModernPassengerCard(passenger)
            }
        }

        /* ---------- Last checked ---------- */

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


            DetailRow(
                label = "Provider time",
                value = details.providerTimestamp
            )
        }

        /* ---------- Actions ---------- */

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
                    "↻  Refresh status",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

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
   REUSABLE DETAILS SECTION
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
   MODERN PASSENGER CARD
========================================================= */

@Composable
private fun ModernPassengerCard(
    passenger: PassengerStatus
) {
    val bookingBerth = passenger.bookingBerthNo
        ?.takeIf { it > 0 }
        ?.toString()

    val currentBerth = passenger.currentBerthNo
        ?.takeIf { it > 0 }
        ?.toString()
        ?: availableText(passenger.berth)
            ?.takeIf { it != "0" }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Passenger ${passenger.index}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (
                    availableText(passenger.currentStatus) != null
                ) {
                    StatusChip(passenger.currentStatus)
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PassengerStatusPanel(
                    title = "BOOKING",
                    status = passenger.bookingStatus,
                    coach = passenger.bookingCoach,
                    berth = bookingBerth,
                    berthType = passenger.bookingBerthCode,
                    modifier = Modifier.weight(1f)
                )

                PassengerStatusPanel(
                    title = "CURRENT",
                    status = passenger.currentStatus,
                    coach = passenger.coach,
                    berth = currentBerth,
                    berthType = passenger.currentBerthCode,
                    modifier = Modifier.weight(1f)
                )
            }

            passenger.coachPosition
                ?.takeIf { it > 0 }
                ?.let { position ->
                    Text(
                        text = "Coach position: $position",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
        }
    }
}

/* =========================================================
   PASSENGER STATUS PANEL
========================================================= */

@Composable
private fun PassengerStatusPanel(
    title: String,
    status: String?,
    coach: String?,
    berth: String?,
    berthType: String?,
    modifier: Modifier = Modifier
) {
    val fields = listOf(
        "Status" to status,
        "Coach" to coach,
        "Berth" to berth,
        "Type" to berthType
    ).filter {
        availableText(it.second) != null
    }

    if (fields.isEmpty()) return

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            fields.forEach { (label, value) ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = value.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
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

private fun formatFare(value: Double?): String? {
    return value?.let {
        "₹${formatNumber(it)}"
    }
}

/* =========================================================
   PREVIEW
========================================================= */

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true
)
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
