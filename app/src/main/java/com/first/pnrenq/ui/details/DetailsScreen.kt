@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.first.pnrenq.R
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.model.passengerStatusPresentation
import com.first.pnrenq.ui.common.copyPnrToClipboard
import com.first.pnrenq.ui.common.PnrFeedbackEffect
import com.first.pnrenq.ui.common.PnrLoadWarning
import com.first.pnrenq.ui.state.ScopedPnrFeedback
import com.first.pnrenq.ui.state.PnrRecordState
import com.first.pnrenq.ui.common.JourneyTimeline
import com.first.pnrenq.ui.common.PnrAdaptiveTopBar
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.PnrNumberBubble
import com.first.pnrenq.ui.common.formatArrivalDate
import com.first.pnrenq.ui.common.formatDistanceKm
import com.first.pnrenq.ui.common.farePresentation
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.common.pnrControlColor
import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.LocalPnrVisualSettings
import com.first.pnrenq.ui.theme.PnrAccentSurface
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.PnrBadgeColors
import com.first.pnrenq.ui.theme.isDarkAppearance
import com.first.pnrenq.ui.theme.passengerBadgeColors
import com.first.pnrenq.ui.theme.pnrAccentColors
import com.first.pnrenq.ui.theme.ThemeText as Text

@Composable
fun DetailsScreen(
    pnr: PnrInfo?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    recordState: PnrRecordState = when {
        pnr != null -> PnrRecordState.READABLE
        isLoading -> PnrRecordState.LOADING
        else -> PnrRecordState.MISSING
    },
    loadError: String? = null,
    onRetry: () -> Unit = {},
    feedback: ScopedPnrFeedback? = null,
    onFeedbackShown: (String, Long) -> Unit = { _, _ -> },
    isBusy: Boolean = isRefreshing
) {
    val scheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    PnrFeedbackEffect(
        snackbarHostState,
        feedback,
        onFeedbackShown
    )

    LaunchedEffect(pnr?.pnrNumber) {
        if (pnr == null) {
            showDeleteDialog = false
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = scheme.onBackground,
        topBar = {
            PnrAdaptiveTopBar(
                title = "Ticket details",
                onBack = onBack
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        when {
            recordState == PnrRecordState.LOADING && pnr == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Loading ticket details"
                        }
                    )
                }
            }

            pnr == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (recordState) {
                        PnrRecordState.UNREADABLE -> {
                            Text(
                                text = "Saved PNR information is unreadable.",
                                modifier = Modifier.semantics {
                                    liveRegion = LiveRegionMode.Polite
                                },
                                textAlign = TextAlign.Center,
                                color = scheme.onBackground
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = onRefresh,
                                enabled = !isBusy && !isLoading
                            ) {
                                Text(
                                    if (isRefreshing) {
                                        "Repairing PNR..."
                                    } else {
                                        "Refresh to repair"
                                    }
                                )
                            }
                        }

                        PnrRecordState.UNAVAILABLE -> {
                            Text(
                                text = loadError
                                    ?: "Unable to load saved PNRs. Please retry.",
                                modifier = Modifier.semantics {
                                    liveRegion = LiveRegionMode.Polite
                                },
                                textAlign = TextAlign.Center,
                                color = scheme.onBackground
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = onRetry,
                                enabled = !isLoading
                            ) {
                                Text("Try again")
                            }
                        }

                        else -> {
                            Text(
                                text = "This PNR is no longer saved.",
                                modifier = Modifier.semantics {
                                    liveRegion = LiveRegionMode.Polite
                                },
                                color = scheme.onBackground
                            )
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (loadError != null) {
                        PnrLoadWarning(
                            loadError,
                            isLoading,
                            onRetry
                        )
                    }

                    TrainSummaryCard(
                        pnr = pnr,
                        isRefreshing = isRefreshing,
                        isBusy = isBusy,
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

    if (showDeleteDialog && pnr != null) {
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
                    enabled = !isBusy,
                    onClick = {
                        showDeleteDialog = false

                        if (!isBusy) {
                            onDelete()
                        }
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = if (isBusy) {
                            scheme.onSurface.copy(alpha = 0.38f)
                        } else {
                            scheme.error
                        }
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
    isBusy: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val settings = LocalPnrVisualSettings.current

    val dark = isDarkAppearance(
        settings.mode,
        isSystemInDarkTheme()
    )

    val accentStops = remember(
        scheme,
        settings,
        dark
    ) {
        pnrAccentColors(
            baseColor = scheme.primaryContainer,
            foregroundColor = scheme.onPrimaryContainer,
            settings = settings,
            dark = dark,
            strength = 1.15f
        )
    }

    // Account for the translucent circular action-button surface.
    val actionBackgrounds = remember(
        accentStops,
        scheme.onPrimaryContainer
    ) {
        listOf(
            accentStops.map {
                scheme.onPrimaryContainer
                    .copy(alpha = 0.10f)
                    .compositeOver(it)
            }
        )
    }

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            val stacked =
                maxWidth < 240.dp ||
                        LocalDensity.current.fontScale >= 1.3f

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                /* ---------- Train number and actions ---------- */

                if (stacked) {
                    TrainNumberTag(pnr.trainNumber)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TrainSummaryActions(
                            pnr = pnr,
                            isRefreshing = isRefreshing,
                            isBusy = isBusy,
                            backgrounds = actionBackgrounds,
                            onRefresh = onRefresh,
                            onDelete = onDelete
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            TrainNumberTag(pnr.trainNumber)
                        }

                        TrainSummaryActions(
                            pnr = pnr,
                            isRefreshing = isRefreshing,
                            isBusy = isBusy,
                            backgrounds = actionBackgrounds,
                            onRefresh = onRefresh,
                            onDelete = onDelete
                        )
                    }
                }

                /* ---------- Train name ---------- */

                Text(
                    text = pnr.trainName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer
                )

                /* ---------- PNR number and copy ---------- */

                if (stacked) {
                    PnrNumberBubble(
                        pnrNumber = pnr.pnrNumber,
                        emphasized = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        DetailsCopyButton(
                            pnr = pnr,
                            backgrounds = listOf(accentStops)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
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

                        Spacer(Modifier.width(8.dp))

                        DetailsCopyButton(
                            pnr = pnr,
                            backgrounds = listOf(accentStops)
                        )
                    }
                }

                HorizontalDivider(
                    color = scheme.onPrimaryContainer.copy(alpha = 0.20f)
                )

                /* ---------- Journey timeline ---------- */

                JourneyTimeline(
                    fromStation = pnr.fromStation,
                    toStation = pnr.toStation,
                    journeyDate = pnr.journeyDate,
                    departureTime = pnr.details.departureTime,
                    arrivalDate = pnr.details.arrivalDate,
                    arrivalTime = pnr.details.arrivalTime,
                    duration = pnr.details.duration
                )

                /* ---------- Refresh progress ---------- */

                if (isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "PNR operation in progress"
                                liveRegion = LiveRegionMode.Polite
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainNumberTag(
    trainNumber: String
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = scheme.onPrimaryContainer.copy(alpha = 0.10f)
    ) {
        Text(
            text = "TRAIN $trainNumber",
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TrainSummaryActions(
    pnr: PnrInfo,
    isRefreshing: Boolean,
    isBusy: Boolean,
    backgrounds: List<List<Color>>,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrainActionIcon(
            icon = R.drawable.ic_pnr_refresh,
            description =
                "Refresh PNR ending in ${pnr.pnrNumber.takeLast(4)}",
            enabled = !isBusy,
            loading = isRefreshing,
            backgrounds = backgrounds,
            onClick = onRefresh
        )

        TrainActionIcon(
            icon = R.drawable.ic_pnr_delete,
            description =
                "Delete saved PNR ending in ${pnr.pnrNumber.takeLast(4)}",
            enabled = !isBusy,
            destructive = true,
            backgrounds = backgrounds,
            onClick = onDelete
        )
    }
}

@Composable
private fun DetailsCopyButton(
    pnr: PnrInfo,
    backgrounds: List<List<Color>>
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    IconButton(
        onClick = {
            copyPnrToClipboard(
                context,
                pnr.pnrNumber
            )
        },
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(
                R.drawable.ic_pnr_copy
            ),
            contentDescription =
                "Copy PNR ending in ${pnr.pnrNumber.takeLast(4)}",
            tint = pnrControlColor(
                scheme.onPrimaryContainer,
                backgrounds
            ),
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun TrainActionIcon(
    icon: Int,
    description: String,
    enabled: Boolean,
    backgrounds: List<List<Color>>,
    onClick: () -> Unit,
    loading: Boolean = false,
    destructive: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme

    // Preserve the original circular button background.
    Surface(
        shape = CircleShape,
        color = scheme.onPrimaryContainer.copy(alpha = 0.10f)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    contentDescription = description

                    if (!enabled) {
                        stateDescription = "PNR operation in progress"
                    }
                }
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    strokeWidth = 2.dp,
                    color = pnrControlColor(
                        scheme.onPrimaryContainer,
                        backgrounds
                    )
                )
            } else {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (!enabled) {
                        scheme.onPrimaryContainer.copy(alpha = 0.38f)
                    } else {
                        pnrControlColor(
                            if (destructive) {
                                scheme.error
                            } else {
                                scheme.onPrimaryContainer
                            },
                            backgrounds
                        )
                    },
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

/* =========================================================
   BOOKING RED / CURRENT GREEN

   Retain the existing hues with contrast-corrected text.
========================================================= */

@Composable
private fun statusBadgeColors(
    current: Boolean
): PnrBadgeColors {
    val scheme = MaterialTheme.colorScheme
    val settings = LocalPnrVisualSettings.current

    val dark = isDarkAppearance(
        settings.mode,
        isSystemInDarkTheme()
    )

    return remember(
        scheme.surfaceContainerLow,
        dark,
        current
    ) {
        passengerBadgeColors(
            surface = scheme.surfaceContainerLow.toArgb(),
            dark = dark,
            current = current
        )
    }
}

@Composable
private fun PassengerListCard(
    passengers: List<PassengerStatus>
) {
    val scheme = MaterialTheme.colorScheme

    val showBookingLegend = passengers.any {
        passengerStatusPresentation(it).booking != null
    }

    val bookingColor = Color(
        statusBadgeColors(current = false).foreground
    )

    val currentColor = Color(
        statusBadgeColors(current = true).foreground
    )

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            val stacked =
                maxWidth < 250.dp ||
                        LocalDensity.current.fontScale >= 1.3f

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                if (stacked) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PassengerHeading()
                        PassengerCount(passengers.size)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showBookingLegend) {
                            StatusLegend(
                                color = bookingColor,
                                label = "Booking"
                            )
                        }

                        StatusLegend(
                            color = currentColor,
                            label = "Current"
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PassengerHeading()
                        PassengerCount(passengers.size)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showBookingLegend) {
                            StatusLegend(
                                color = bookingColor,
                                label = "Booking"
                            )
                        }

                        StatusLegend(
                            color = currentColor,
                            label = "Current"
                        )
                    }
                }

                HorizontalDivider(
                    color = scheme.outlineVariant
                )

                passengers.forEachIndexed { index, passenger ->
                    PassengerRow(
                        passenger = passenger,
                        stacked = stacked
                    )

                    if (index != passengers.lastIndex) {
                        HorizontalDivider(
                            color = scheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PassengerHeading() {
    Text(
        text = "Passengers",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun PassengerCount(
    count: Int
) {
    Text(
        text = "$count total",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Select the full Compose text-layout path for fixed-color text. */
private val detailsTextLayoutCallback: (TextLayoutResult) -> Unit = {
    // No text or layout data is retained.
}

@Composable
private fun StatusLegend(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            onTextLayout = detailsTextLayoutCallback
        )
    }
}

@Composable
private fun PassengerRow(
    passenger: PassengerStatus,
    stacked: Boolean
) {
    val scheme = MaterialTheme.colorScheme

    val (booking, current) =
        passengerStatusPresentation(passenger)

    if (stacked) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PassengerIdentifier(passenger.index)

            if (booking != null) {
                PassengerRoleLabel(
                    current = false
                )

                PassengerBadge(
                    text = booking,
                    current = false
                )
            }

            PassengerRoleLabel(
                current = true
            )

            PassengerBadge(
                text = current,
                current = true
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            PassengerIdentifier(passenger.index)

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
                        modifier = Modifier.weight(
                            1f,
                            fill = false
                        )
                    )

                    SolidText(
                        text = "/",
                        modifier = Modifier.clearAndSetSemantics {},
                        color = scheme.onSurfaceVariant
                    )
                }

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
private fun PassengerIdentifier(
    index: Int
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = scheme.secondaryContainer,
        contentColor = scheme.onSecondaryContainer
    ) {
        Box(
            modifier = Modifier
                .sizeIn(
                    minWidth = 39.dp,
                    minHeight = 35.dp
                )
                .padding(
                    horizontal = 6.dp,
                    vertical = 5.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "P$index",
                modifier = Modifier.semantics {
                    contentDescription = "Passenger $index"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun PassengerRoleLabel(
    current: Boolean
) {
    Text(
        text = if (current) {
            "Current status"
        } else {
            "Booking status"
        },
        // The associated badge already announces its role and full value.
        modifier = Modifier.clearAndSetSemantics {},
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PassengerBadge(
    text: String,
    current: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = statusBadgeColors(current)
    val foreground = Color(colors.foreground)
    val background = Color(colors.background)

    Surface(
        modifier = modifier,
        color = background,
        contentColor = foreground,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            foreground.copy(alpha = 0.30f)
        )
    ) {
        SolidText(
            text = text,
            modifier = Modifier
                .semantics {
                    contentDescription = if (current) {
                        "Current status: $text"
                    } else {
                        "Booking status: $text"
                    }
                }
                .padding(
                    horizontal = 9.dp,
                    vertical = 7.dp
                ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = foreground,
            textAlign = TextAlign.Center,
            softWrap = true,
            maxLines = Int.MAX_VALUE,
            onTextLayout = detailsTextLayoutCallback
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
        "Distance" to formatDistanceKm(details.distanceKm),
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
            verticalArrangement = Arrangement.spacedBy(13.dp)
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
   TICKET CARD — ORIGINAL DESIGN WITH RESPONSIVE FARE
========================================================= */

@Composable
private fun TicketInformationCard(
    pnr: PnrInfo
) {
    val scheme = MaterialTheme.colorScheme
    val details = pnr.details

    val fare = farePresentation(details)

    val fields = listOf(
        "Ticket fare" to fare?.secondaryTicketAmount,
        "Booking date" to formatArrivalDate(details.bookingDate),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        modifier = Modifier
                            .sizeIn(
                                minWidth = 49.dp,
                                minHeight = 49.dp
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SolidText(
                            text = "₹",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer,
                            onTextLayout = detailsTextLayoutCallback
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Ticket information",
                        style = MaterialTheme.typography.titleMedium,
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
                PrimaryFare(
                    label = fare.primaryLabel,
                    amount = fare.primaryAmount
                )
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

@Composable
private fun PrimaryFare(
    label: String,
    amount: String
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val stacked =
                maxWidth < 240.dp ||
                        LocalDensity.current.fontScale >= 1.3f

            if (stacked) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = label,
                        color = scheme.onSurfaceVariant
                    )

                    Text(
                        text = amount,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        color = scheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = amount,
                        modifier = Modifier.weight(
                            1.5f,
                            fill = false
                        ),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
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
                color = scheme.onTertiaryContainer.copy(alpha = 0.09f)
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val stacked =
            maxWidth < 220.dp ||
                    LocalDensity.current.fontScale >= 1.3f

        if (stacked) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )

                Text(
                    text = valid,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface
                )
            }
        } else {
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
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsPreview() {
    PnrenqTheme {
        DetailsScreen(
            pnr = MockPnrData.sample().last(),
            isRefreshing = false,
            onRefresh = {},
            onDelete = {},
            onBack = {}
        )
    }
}