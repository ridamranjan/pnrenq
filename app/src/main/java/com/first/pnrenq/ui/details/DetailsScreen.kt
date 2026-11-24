
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
import androidx.compose.material3.Text as SolidText
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
import com.first.pnrenq.ui.common.PnrBackButton
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.PnrNumberBubble
import com.first.pnrenq.ui.common.formatArrivalDate
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.PnrAccentSurface
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.ThemeText as Text

import java.util.Locale

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
    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

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
        contentColor = scheme.onBackground,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ticket details",
                        fontWeight = FontWeight.Bold,
                        color = scheme.onBackground
                    )
                },
                navigationIcon = {
                    PnrBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = scheme.onBackground,
                    navigationIconContentColor = scheme.onBackground
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
                    Text(
                        text = "This PNR is no longer saved.",
                        color = scheme.onBackground
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    TrainSummaryCard(
                        pnr = pnr,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        onDelete = {
                            showDeleteDialog = true
                        }
                    )


                    if (pnr.passengers.isNotEmpty()) {
                        PassengerListCard(pnr.passengers)
                    }

                    // One shared explanation card for the entire PNR.
                    // Each distinct current status appears only once.

                    JourneyInformationCard(pnr)
                    ReservationInformationCard(pnr = pnr)
                    TicketInformationCard(pnr)
                    LastUpdateCard(pnr.lastCheckedAtMillis)


                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showDeleteDialog && pnr != null && !isRefreshing) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Remove saved PNR?")
            },
            text = {
                Text(
                    "PNR ${pnr.pnrNumber} and its saved details " +
                            "will be removed from this device."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = scheme.error
                    )
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
   TRAIN SUMMARY — GRADIENT ACCENT SURFACE
========================================================= */
@Composable
private fun TrainSummaryCard(
    pnr: PnrInfo,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    PnrAccentSurface(
        modifier = Modifier.fillMaxWidth(),

        backgroundColor = scheme.primaryContainer,
        foregroundColor = scheme.onPrimaryContainer,

        shape = RoundedCornerShape(25.dp),

        strength = 1.15f,

        border = BorderStroke(
            1.dp,
            scheme.primary.copy(alpha = 0.24f)
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            /* =====================================
               TRAIN NUMBER AND ACTION BUTTONS
            ===================================== */

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    shape = RoundedCornerShape(10.dp),

                    color = scheme.onPrimaryContainer.copy(
                        alpha = 0.10f
                    )
                ) {

                    Text(
                        text = "TRAIN ${pnr.trainNumber}",

                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),

                        style =
                            MaterialTheme.typography.labelMedium,

                        fontWeight = FontWeight.Bold,

                        color = scheme.onPrimaryContainer
                    )
                }

                Spacer(
                    Modifier.weight(1f)
                )

                TrainActionIcon(
                    icon = R.drawable.ic_pnr_refresh,
                    description = "Refresh PNR",
                    enabled = !isRefreshing,
                    loading = isRefreshing,
                    onClick = onRefresh
                )

                Spacer(
                    Modifier.width(5.dp)
                )

                TrainActionIcon(
                    icon = R.drawable.ic_pnr_delete,
                    description = "Delete PNR",
                    enabled = !isRefreshing,
                    destructive = true,
                    onClick = onDelete
                )
            }

            /* =====================================
               TRAIN NAME
            ===================================== */

            Text(
                text = pnr.trainName,

                style =
                    MaterialTheme.typography.headlineSmall,

                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                maxLines = 2
            )

            /* =====================================
               PNR NUMBER BUBBLE AND COPY BUTTON
            ===================================== */

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier.weight(1f),

                    contentAlignment = Alignment.CenterStart
                ) {

                    PnrNumberBubble(
                        pnrNumber = pnr.pnrNumber,
                        emphasized = true
                    )
                }

                Spacer(
                    Modifier.width(8.dp)
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
                                pnr.pnrNumber
                            )
                        )
                    }
                ) {

                    Icon(
                        painter = painterResource(
                            R.drawable.ic_pnr_copy
                        ),

                        contentDescription = "Copy PNR",

                        tint = scheme.onPrimaryContainer,

                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            HorizontalDivider(
                color = scheme.onPrimaryContainer.copy(
                    alpha = 0.20f
                )
            )

            /* =====================================
               JOURNEY TIMELINE
            ===================================== */

            JourneyTimeline(
                fromStation = pnr.fromStation,
                toStation = pnr.toStation,
                journeyDate = pnr.journeyDate,
                departureTime = pnr.details.departureTime,
                arrivalDate = pnr.details.arrivalDate,
                arrivalTime = pnr.details.arrivalTime,
                duration = pnr.details.duration
            )

            /* =====================================
               REFRESH PROGRESS
            ===================================== */

            if (isRefreshing) {

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TrainActionIcon(
    icon: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    loading: Boolean = false,
    destructive: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme

    // Preserve the original circular button background.
    Surface(
        shape = CircleShape,
        color = scheme.onPrimaryContainer.copy(
            alpha = 0.10f
        )
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(43.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    strokeWidth = 2.dp,
                    color = scheme.onPrimaryContainer
                )
            } else {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = description,
                    tint = if (destructive) {
                        scheme.error
                    } else {
                        scheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

/* =========================================================
   PASSENGER STATUS LOGIC — UNCHANGED
========================================================= */

private fun statusTokens(
    value: String
): List<String> {
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

private fun isConfirmed(
    status: String?
): Boolean {
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

    if (!hasEmbeddedSeat(raw)) {
        if (
            coachText != null &&
            !containsTokenSequence(result, coachText)
        ) {
            result = listOf(
                result,
                coachText
            ).filter {
                it.isNotBlank()
            }.joinToString(" ")
        }

        if (
            berthText != null &&
            coachText != null &&
            !containsTokenSequence(
                result,
                "$coachText $berthText"
            )
        ) {
            result = listOf(
                result,
                berthText
            ).filter {
                it.isNotBlank()
            }.joinToString(" ")
        }
    }

    if (
        typeText != null &&
        !containsTokenSequence(result, typeText)
    ) {
        result = listOf(
            result,
            typeText
        ).filter {
            it.isNotBlank()
        }.joinToString(" ")
    }

    return result
        .trim()
        .replace(Regex("\\s+"), " ")
        .takeIf { it.isNotBlank() }
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
   BOOKING RED / CURRENT GREEN — UNCHANGED
========================================================= */

@Composable
private fun bookingStatusColor(): Color {
    val lightBackground =
        MaterialTheme.colorScheme.background.luminance() >
                0.5f

    return if (lightBackground) {
        Color(0xFFB42318)
    } else {
        Color(0xFFFFA9A9)
    }
}

@Composable
private fun currentStatusColor(): Color {
    val lightBackground =
        MaterialTheme.colorScheme.background.luminance() >
                0.5f

    return if (lightBackground) {
        Color(0xFF15803D)
    } else {
        Color(0xFF86EFAC)
    }
}
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
            containerColor = scheme.surfaceContainerLow,
            contentColor = scheme.onSurface
        ),
        border = BorderStroke(
            1.dp,
            scheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Passengers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )

                Text(
                    text = "${passengers.size} total",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp,
                        Alignment.End
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBookingLegend) {
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

            HorizontalDivider(
                color = scheme.outlineVariant
            )

            passengers.forEachIndexed { index, passenger ->
                PassengerRow(passenger)

                if (index != passengers.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusLegend(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color
        ) {}

        SolidText(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PassengerRow(
    passenger: PassengerStatus
) {
    val scheme = MaterialTheme.colorScheme

    val booking = if (
        hasConfirmedAllocation(passenger)
    ) {
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

    val current = buildStatusText(
        status = passenger.currentStatus,
        coach = passenger.coach,
        berthNo = passenger.currentBerthNo,
        berthFallback = passenger.berth,
        berthType = passenger.currentBerthCode
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = scheme.secondaryContainer,
            contentColor = scheme.onSecondaryContainer
        ) {
            Box(
                modifier = Modifier.size(
                    width = 39.dp,
                    height = 35.dp
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
            if (booking != null) {
                PassengerBadge(
                    text = booking,
                    current = false,
                    modifier = if (current != null) {
                        Modifier.weight(1f, fill = false)
                    } else {
                        Modifier
                    }
                )
            }

            if (booking != null && current != null) {
                SolidText(
                    text = "/",
                    color = scheme.onSurfaceVariant
                )
            }

            if (current != null) {
                PassengerBadge(
                    text = current,
                    current = true,
                    modifier = if (booking != null) {
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
private fun PassengerBadge(
    text: String,
    current: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (current) {
        currentStatusColor()
    } else {
        bookingStatusColor()
    }

    Surface(
        modifier = modifier,
        color = statusColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            statusColor.copy(alpha = 0.30f)
        )
    ) {
        SolidText(
            text = text,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* =========================================================
   JOURNEY CARD — ORIGINAL SOLID BACKGROUND
========================================================= */

@Composable
private fun JourneyInformationCard(
    pnr: PnrInfo
) {
    val scheme = MaterialTheme.colorScheme
    val details = pnr.details

    val fields = listOf(
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

    if (fields.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow,
            contentColor = scheme.onSurface
        ),
        border = BorderStroke(
            1.dp,
            scheme.secondary.copy(alpha = 0.24f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(13.dp)
        ) {
            InformationHeader(
                icon = R.drawable.ic_pnr_route,
                title = "Journey information",
                subtitle = "Travel and reservation details",
                container = scheme.secondaryContainer,
                foreground = scheme.onSecondaryContainer
            )

            HorizontalDivider()

            fields.forEachIndexed { index, field ->
                DetailRow(
                    label = field.first,
                    value = field.second
                )

                if (index != fields.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant.copy(
                            alpha = 0.5f
                        )
                    )
                }
            }
        }
    }
}

/* =========================================================
   TICKET CARD — RESTORED ORIGINAL DESIGN
========================================================= */

@Composable
private fun TicketInformationCard(
    pnr: PnrInfo
) {
    val scheme = MaterialTheme.colorScheme
    val details = pnr.details

    val fare = details.bookingFare
        ?: details.ticketFare

    val fields = listOf(
        "Ticket fare" to details.ticketFare
            ?.takeIf { it != fare }
            ?.let(::formatFare),

        "Booking date" to
                formatArrivalDate(details.bookingDate),

        "VIKALP" to details.vikalpStatus,
        "Ticket type" to details.ticketType
    ).filter {
        availableText(it.second) != null
    }

    if (fare == null && fields.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface
        ),
        border = BorderStroke(
            1.dp,
            scheme.primary.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(49.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SolidText(
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
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Ticket information",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )

                    Text(
                        text = "Fare and booking details",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            if (fare != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = scheme.surfaceContainerLow,
                    contentColor = scheme.onSurface,
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
                            text = if (
                                details.bookingFare != null
                            ) {
                                "Booking fare"
                            } else {
                                "Ticket fare"
                            },
                            color = scheme.onSurfaceVariant
                        )

                        Text(
                            text = formatFare(fare),
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                    }
                }
            }

            if (fields.isNotEmpty()) {
                HorizontalDivider()

                fields.forEach { field ->
                    DetailRow(
                        label = field.first,
                        value = field.second
                    )
                }
            }
        }
    }
}

/* =========================================================
   LAST UPDATE — GRADIENT ACCENT SURFACE
========================================================= */

@Composable
private fun LastUpdateCard(
    lastCheckedAtMillis: Long
) {
    val scheme = MaterialTheme.colorScheme
    val timeFormat = LocalPnrTimeFormat.current

    PnrAccentSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = scheme.tertiaryContainer,
        foregroundColor = scheme.onTertiaryContainer,
        shape = RoundedCornerShape(24.dp),
        strength = 1.10f
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = scheme.onTertiaryContainer.copy(
                    alpha = 0.09f
                )
            ) {
                Box(
                    modifier = Modifier.size(49.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_pnr_clock
                        ),
                        contentDescription = null,
                        tint = scheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(13.dp))

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Last update",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onTertiaryContainer
                )

                Text(
                    text = formatLastChecked(
                        lastCheckedAtMillis,
                        timeFormat
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onTertiaryContainer
                )

                Text(
                    text = "Last successful PNR check",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onTertiaryContainer
                )
            }
        }
    }
}

/* =========================================================
   SHARED DETAILS COMPONENTS
========================================================= */

@Composable
private fun InformationHeader(
    icon: Int,
    title: String,
    subtitle: String,
    container: Color,
    foreground: Color
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = container,
            contentColor = foreground,
            shape = RoundedCornerShape(13.dp)
        ) {
            Box(
                modifier = Modifier.size(49.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = foreground,
                    modifier = Modifier.size(23.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?
) {
    val valid = availableText(value) ?: return
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = valid,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            color = scheme.onSurface
        )
    }
}

private fun formatNumber(
    value: Double
): String = if (value % 1.0 == 0.0) {
    String.format(Locale.US, "%,.0f", value)
} else {
    String.format(Locale.US, "%,.1f", value)
}

private fun formatFare(
    value: Double
): String = "₹${formatNumber(value)}"

@Preview(showBackground = true)
@Composable
private fun DetailsPreview() {
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
