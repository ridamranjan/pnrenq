
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
                    Text(
                        text = "Ticket details",
                        fontWeight = FontWeight.Bold
                    )
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

                    JourneyInformationCard(pnr)

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
                        "Delete",
                        color = MaterialTheme.colorScheme.error
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
   TRAIN CARD
========================================================= */

@Composable
private fun TrainSummaryCard(
    pnr: PnrInfo,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer
        ),

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = scheme.onPrimaryContainer.copy(
                        alpha = 0.10f
                    ),
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

                TrainActionIcon(
                    icon = R.drawable.ic_pnr_refresh,
                    description = "Refresh PNR",
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    loading = isRefreshing
                )

                Spacer(Modifier.width(4.dp))

                TrainActionIcon(
                    icon = R.drawable.ic_pnr_delete,
                    description = "Delete PNR",
                    onClick = onDelete,
                    enabled = !isRefreshing,
                    destructive = true
                )
            }

            Text(
                text = pnr.trainName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
                maxLines = 2
            )

            CopyablePnr(pnr.pnrNumber)

            HorizontalDivider(
                color = scheme.onPrimaryContainer.copy(
                    alpha = 0.20f
                )
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
    onClick: () -> Unit,
    enabled: Boolean,
    loading: Boolean = false,
    destructive: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme

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

@Composable
private fun CopyablePnr(
    pnrNumber: String
) {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.onPrimaryContainer

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PNR $pnrNumber",
            modifier = Modifier.weight(1f),
            color = color,
            fontWeight = FontWeight.SemiBold
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
   PASSENGER FORMATTING
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

    // Never append an already embedded coach/berth twice.
    if (!hasEmbeddedSeat(raw)) {

        if (
            coachText != null &&
            !containsTokenSequence(
                result,
                coachText
            )
        ) {
            result = listOf(
                result,
                coachText
            ).filter {
                it.isNotBlank()
            }.joinToString(" ")
        }

        if (berthText != null) {

            val alreadyPresent =
                coachText != null &&
                        containsTokenSequence(
                            result,
                            "$coachText $berthText"
                        )

            if (
                !alreadyPresent &&
                (
                        coachText != null ||
                                hasEmbeddedSeat(result)
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
    }

    if (
        typeText != null &&
        !containsTokenSequence(
            result,
            typeText
        )
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
   PASSENGERS
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
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBookingLegend) {
                    StatusLegend(
                        bookingColor(),
                        "Booking"
                    )
                }

                StatusLegend(
                    currentColor(),
                    "Current"
                )
            }

            HorizontalDivider()

            passengers.forEachIndexed { index, passenger ->

                PassengerRow(passenger)

                if (index < passengers.lastIndex) {
                    HorizontalDivider(
                        color = scheme.outlineVariant.copy(
                            alpha = 0.6f
                        )
                    )
                }
            }
        }
    }
}

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
        horizontalArrangement =
            Arrangement.spacedBy(9.dp)
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
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
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
                PassengerBadge(
                    text = bookingText,
                    current = false,
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
                    color = scheme.onSurfaceVariant
                )
            }

            if (currentText != null) {
                PassengerBadge(
                    text = currentText,
                    current = true,
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
private fun PassengerBadge(
    text: String,
    current: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (current) {
        currentColor()
    } else {
        bookingColor()
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            color.copy(alpha = 0.28f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 7.dp
            ),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusLegend(
    color: Color,
    text: String
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(7.dp),
            shape = CircleShape,
            color = color
        ) {}

        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun bookingColor(): Color {
    val light =
        MaterialTheme.colorScheme.background.luminance() >
                0.5f

    return if (light) {
        Color(0xFF9A6700)
    } else {
        Color(0xFFFFCA68)
    }
}

@Composable
private fun currentColor(): Color {
    val light =
        MaterialTheme.colorScheme.background.luminance() >
                0.5f

    return if (light) {
        Color(0xFF15803D)
    } else {
        Color(0xFF86EFAC)
    }
}

/* =========================================================
   JOURNEY INFORMATION CARD
========================================================= */

@Composable
private fun JourneyInformationCard(
    pnr: PnrInfo
) {
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

    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),

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
                DetailRow(field.first, field.second)

                if (index < fields.lastIndex) {
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
   TICKET INFORMATION CARD
========================================================= */

@Composable
private fun TicketInformationCard(
    pnr: PnrInfo
) {
    val details = pnr.details
    val scheme = MaterialTheme.colorScheme

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

    if (fare == null && fields.isEmpty()) {
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerHigh
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
                    color = scheme.primaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(49.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            color = scheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Ticket information",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Fare and booking details",
                        color = scheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (fare != null) {
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
                            color = scheme.onSurfaceVariant
                        )

                        Text(
                            text = formatFare(fare),
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold,
                            style =
                                MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            if (fields.isNotEmpty()) {
                HorizontalDivider()

                fields.forEach { field ->
                    DetailRow(field.first, field.second)
                }
            }
        }
    }
}

/* =========================================================
   LAST UPDATE CARD
========================================================= */

@Composable
private fun LastUpdateCard(
    lastCheckedAtMillis: Long
) {
    val scheme = MaterialTheme.colorScheme
    val format = LocalPnrTimeFormat.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.tertiaryContainer,
            contentColor = scheme.onTertiaryContainer
        )
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
                    fontWeight = FontWeight.Bold,
                    color = scheme.onTertiaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = formatLastChecked(
                        lastCheckedAtMillis,
                        format
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onTertiaryContainer
                )

                Text(
                    text = "Last successful PNR check",
                    color = scheme.onTertiaryContainer.copy(
                        alpha = 0.82f
                    ),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/* =========================================================
   REUSABLE INFORMATION COMPONENTS
========================================================= */

@Composable
private fun InformationHeader(
    icon: Int,
    title: String,
    subtitle: String,
    container: Color,
    foreground: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = container,
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
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = valid,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", value)
    } else {
        String.format(Locale.US, "%,.1f", value)
    }

private fun formatFare(value: Double): String =
    "₹${formatNumber(value)}"

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
