package com.first.pnrenq.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.first.pnrenq.R
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.model.homeReservationText
import com.first.pnrenq.ui.common.copyPnrToClipboard
import com.first.pnrenq.ui.common.PnrFeedbackEffect
import com.first.pnrenq.ui.common.PnrLoadWarning
import com.first.pnrenq.ui.state.ScopedPnrFeedback
import com.first.pnrenq.ui.common.StatusChip
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.formatClockTime
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.common.formatJourneyDate
import com.first.pnrenq.ui.common.formatArrivalDate
import com.first.pnrenq.ui.common.pnrControlColor
import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.PnrAccentSurface
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.pnrAccentBrush
import com.first.pnrenq.ui.theme.ThemeText as Text
import com.first.pnrenq.ui.common.PnrNumberBubble

import java.time.Clock

/* =========================================================
   HOME SCREEN
========================================================= */

@Composable
fun HomeScreen(
    pnrs: List<PnrInfo>,
    onAddClick: () -> Unit,
    onPnrClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    loadError: String? = null,
    onRetry: () -> Unit = {},
    onDeletePnr: (String) -> Unit = {},
    deletingPnrs: Set<String> = emptySet(),
    busyPnrs: Set<String> = deletingPnrs,
    unreadablePnrs: List<String> = emptyList(),
    feedback: ScopedPnrFeedback? = null,
    onFeedbackShown: (String, Long) -> Unit = { _, _ -> },
    clock: Clock = Clock.systemUTC()
) {
    val scheme = MaterialTheme.colorScheme

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var pnrToDelete by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // Both journey sections are expanded by default.
    var upcomingExpanded by rememberSaveable {
        mutableStateOf(true)
    }

    var pastExpanded by rememberSaveable {
        mutableStateOf(true)
    }

    PnrFeedbackEffect(
        snackbarHostState,
        feedback,
        onFeedbackShown,
        includePnr = true
    )

    LaunchedEffect(pnrs, pnrToDelete) {
        if (
            pnrToDelete != null &&
            pnrs.none { it.pnrNumber == pnrToDelete }
        ) {
            pnrToDelete = null
        }
    }

    val (upcoming, past) = rememberJourneySections(
        pnrs,
        clock
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = scheme.onBackground,
        topBar = {
            HomeHeader()
        },
        bottomBar = {
            // Reserve the actual footer height rather than overlaying the list.
            HomeBottomBar(
                showAdd = pnrs.isNotEmpty(),
                onSettingsClick = onSettingsClick,
                onAddClick = onAddClick
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        when {

            /* =====================================
               INITIAL LOADING
            ===================================== */

            isLoading && pnrs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Loading saved journeys"
                        }
                    )
                }
            }

            /* =====================================
               ERROR STATE
            ===================================== */

            loadError != null && pnrs.isEmpty() -> {
                HomeErrorState(
                    message = loadError,
                    onRetry = onRetry,
                    onRecover = onAddClick.takeIf {
                        unreadablePnrs.isNotEmpty()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            /* =====================================
               EMPTY STATE
            ===================================== */

            pnrs.isEmpty() -> {
                HomeEmptyState(
                    onAddClick = onAddClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            /* =====================================
               SAVED JOURNEYS
            ===================================== */

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (loadError != null) {
                        item(key = "load_warning") {
                            PnrLoadWarning(
                                loadError,
                                isLoading,
                                onRetry
                            )
                        }
                    }

                    /* =============================
                       UPCOMING JOURNEYS
                    ============================= */

                    if (upcoming.isNotEmpty()) {
                        item(key = "upcoming_title") {
                            SectionHeading(
                                title = "Upcoming journeys",
                                count = upcoming.size,
                                expanded = upcomingExpanded,
                                onToggle = {
                                    upcomingExpanded = !upcomingExpanded
                                }
                            )
                        }

                        if (upcomingExpanded) {
                            items(
                                items = upcoming,
                                key = { it.pnrNumber }
                            ) { pnr ->
                                HomePnrCard(
                                    pnr = pnr,
                                    isDeleting = pnr.pnrNumber in deletingPnrs,
                                    isBusy = pnr.pnrNumber in busyPnrs,
                                    onClick = {
                                        onPnrClick(pnr.pnrNumber)
                                    },
                                    onDeleteClick = {
                                        pnrToDelete = pnr.pnrNumber
                                    }
                                )
                            }
                        }
                    }

                    /* =============================
                       PAST JOURNEYS
                    ============================= */

                    if (past.isNotEmpty()) {
                        item(key = "past_title") {
                            if (upcoming.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                            }

                            SectionHeading(
                                title = "Past journeys",
                                count = past.size,
                                expanded = pastExpanded,
                                onToggle = {
                                    pastExpanded = !pastExpanded
                                }
                            )
                        }

                        if (pastExpanded) {
                            items(
                                items = past,
                                key = { it.pnrNumber }
                            ) { pnr ->
                                HomePnrCard(
                                    pnr = pnr,
                                    isDeleting = pnr.pnrNumber in deletingPnrs,
                                    isBusy = pnr.pnrNumber in busyPnrs,
                                    onClick = {
                                        onPnrClick(pnr.pnrNumber)
                                    },
                                    onDeleteClick = {
                                        pnrToDelete = pnr.pnrNumber
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /* =================================================
       DELETE CONFIRMATION
    ================================================= */

    pnrToDelete?.let { selectedPnr ->
        val busy =
            selectedPnr in busyPnrs || selectedPnr in deletingPnrs

        AlertDialog(
            onDismissRequest = {
                pnrToDelete = null
            },
            title = {
                Text("Remove saved PNR?")
            },
            text = {
                Text(
                    text = "PNR $selectedPnr and its saved journey " +
                            "details will be removed from this device."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        pnrToDelete = null

                        if (!busy) {
                            onDeletePnr(selectedPnr)
                        }
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = if (busy) {
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
                        pnrToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* =========================================================
   HOME HEADER — THEME-MATCHING ACCENT
========================================================= */

@Composable
private fun HomeHeader() {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PnrAccentSurface(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = scheme.primaryContainer,
            foregroundColor = scheme.onPrimaryContainer,
            shape = RoundedCornerShape(15.dp),
            strength = 1.2f,
            border = BorderStroke(
                1.dp,
                scheme.primary.copy(alpha = 0.20f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 4.dp
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PNR EXPERT",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer
                )

                Text(
                    text = "Saved journeys",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = scheme.onPrimaryContainer
                )
            }
        }
    }
}

/* =========================================================
   BOTTOM BUTTONS

   The Scaffold reserves this row's actual measured height.
========================================================= */

@Composable
private fun HomeBottomBar(
    showAdd: Boolean,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeSettingsButton(
            onClick = onSettingsClick
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (showAdd) {
                HomeAddPnrButton(
                    onClick = onAddClick
                )
            }
        }
    }
}

/* =========================================================
   BOTTOM-LEFT SETTINGS BUTTON
========================================================= */

@Composable
private fun HomeSettingsButton(
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    PnrAccentSurface(
        modifier = Modifier.size(48.dp),
        backgroundColor = scheme.secondaryContainer,
        foregroundColor = scheme.onSecondaryContainer,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(
            1.dp,
            scheme.secondary.copy(alpha = 0.20f)
        )
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(
                    R.drawable.ic_pnr_settings
                ),
                contentDescription = "Settings",
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

/* =========================================================
   FIXED ADD PNR BUTTON
   One rounded surface.
   One gradient layer.
   One clickable area.
   No nested ExtendedFloatingActionButton.
   No rectangular gradient inside another button.
========================================================= */

@Composable
private fun HomeAddPnrButton(
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    val backgroundBrush = pnrAccentBrush(
        baseColor = scheme.primaryContainer,
        foregroundColor = scheme.onPrimaryContainer,
        strength = 1.15f
    )

    Row(
        modifier = Modifier
            // Preserve the original minimum FAB height.
            .heightIn(min = 56.dp)
            // Clip the entire button to one shape.
            .clip(shape)
            .background(backgroundBrush)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            // Allow the button to grow with enlarged text.
            .padding(
                horizontal = 20.dp,
                vertical = 8.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                R.drawable.ic_pnr_add
            ),
            contentDescription = null,
            tint = scheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = "Add PNR",
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/* =========================================================
   SECTION HEADING
========================================================= */

@Composable
private fun SectionHeading(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow,
            contentColor = scheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (expanded) {
                scheme.primary.copy(alpha = 0.45f)
            } else {
                scheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) {
                        "Collapse $title"
                    } else {
                        "Expand $title"
                    },
                    onClick = onToggle
                )
                .semantics {
                    heading()
                    stateDescription = if (expanded) {
                        "Expanded"
                    } else {
                        "Collapsed"
                    }
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* ---------- Section title ---------- */

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )

            /* ---------- Journey count bubble ---------- */

            Surface(
                shape = RoundedCornerShape(50),
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(
                        horizontal = 11.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer
                )
            }

            /* ---------- Expand / collapse arrow ---------- */

            Text(
                modifier = Modifier.clearAndSetSemantics {},
                text = if (expanded) "▴" else "▾",
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/* =========================================================
   SAVED PNR CARD — SOLID BACKGROUND
========================================================= */

@Composable
private fun HomePnrCard(
    pnr: PnrInfo,
    isDeleting: Boolean,
    isBusy: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val timeFormat = LocalPnrTimeFormat.current

    val departure = listOfNotNull(
        formatJourneyDate(
            pnr.journeyDate,
            compact = true
        ),
        formatClockTime(
            pnr.details.departureTime,
            timeFormat
        )
    ).joinToString(" · ")

    val arrivalDate = formatArrivalDate(
        pnr.details.arrivalDate,
        compact = true
    )

    val arrival = listOfNotNull(
        arrivalDate,
        formatClockTime(
            pnr.details.arrivalTime,
            timeFormat
        )
    ).joinToString(" · ")

    val trainTitle = listOfNotNull(
        availableText(pnr.trainNumber),
        availableText(pnr.trainName)
    ).joinToString(" · ")

    Card(
        onClick = onClick,
        enabled = !isDeleting,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                )
        ) {
            val largeFont =
                LocalDensity.current.fontScale >= 1.3f

            val stacked =
                maxWidth < 260.dp || largeFont

            val stackedSummary =
                maxWidth < 300.dp || largeFont

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                /* ---------- Train name and actions ---------- */

                if (stacked) {
                    HomeTrainTitle(trainTitle)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        HomeCardActions(
                            pnr = pnr,
                            isDeleting = isDeleting,
                            isBusy = isBusy,
                            onDeleteClick = onDeleteClick
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeTrainTitle(
                            title = trainTitle,
                            modifier = Modifier.weight(1f)
                        )

                        HomeCardActions(
                            pnr = pnr,
                            isDeleting = isDeleting,
                            isBusy = isBusy,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }

                /* ---------- Journey information ---------- */

                if (stacked) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StationLabel(
                            code = pnr.fromStation,
                            dateAndTime = departure,
                            alignEnd = false
                        )

                        Text(
                            text = "↓",
                            modifier = Modifier.clearAndSetSemantics {},
                            style = MaterialTheme.typography.titleLarge,
                            color = scheme.primary
                        )

                        StationLabel(
                            code = pnr.toStation,
                            dateAndTime = arrival,
                            alignEnd = false
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StationLabel(
                            code = pnr.fromStation,
                            dateAndTime = departure,
                            alignEnd = false,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "→",
                            modifier = Modifier
                                .clearAndSetSemantics {}
                                .padding(horizontal = 9.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = scheme.primary
                        )

                        StationLabel(
                            code = pnr.toStation,
                            dateAndTime = arrival,
                            alignEnd = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(
                    color = scheme.outlineVariant
                )

                /* ---------- PNR and current status ---------- */

                val status = homeReservationText(pnr)

                if (stackedSummary) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HomePnrSummary(pnr)

                        status?.let {
                            StatusChip(status = it)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomePnrSummary(
                            pnr = pnr,
                            modifier = Modifier.weight(1f)
                        )

                        status?.let {
                            Spacer(Modifier.width(8.dp))
                            StatusChip(status = it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTrainTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/** Existing action behavior and masked accessibility labels. */
@Composable
private fun HomeCardActions(
    pnr: PnrInfo,
    isDeleting: Boolean,
    isBusy: Boolean,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val background = scheme.surfaceContainerLow

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                copyPnrToClipboard(
                    context,
                    pnr.pnrNumber
                )
            },
            enabled = !isDeleting,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(
                    R.drawable.ic_pnr_copy
                ),
                contentDescription =
                    "Copy PNR ending in ${pnr.pnrNumber.takeLast(4)}",
                tint = if (isDeleting) {
                    scheme.onSurface.copy(alpha = 0.38f)
                } else {
                    pnrControlColor(
                        scheme.primary,
                        background
                    )
                },
                modifier = Modifier.size(19.dp)
            )
        }

        IconButton(
            onClick = onDeleteClick,
            enabled = !isDeleting && !isBusy,
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    contentDescription =
                        "Delete saved PNR ending in ${pnr.pnrNumber.takeLast(4)}"

                    if (isDeleting) {
                        stateDescription = "Removing saved PNR"
                    } else if (isBusy) {
                        stateDescription = "PNR operation in progress"
                    }
                }
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(17.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(
                        R.drawable.ic_pnr_delete
                    ),
                    contentDescription = null,
                    tint = if (isBusy) {
                        scheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        pnrControlColor(
                            scheme.error,
                            background
                        )
                    },
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun HomePnrSummary(
    pnr: PnrInfo,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val timeFormat = LocalPnrTimeFormat.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Retain the verified responsive PNR bubble.
        PnrNumberBubble(
            pnrNumber = pnr.pnrNumber
        )

        Text(
            text = "Checked ${
                formatLastChecked(
                    pnr.lastCheckedAtMillis,
                    timeFormat
                )
            }",
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant
        )
    }
}

/* =========================================================
   STATION LABEL
========================================================= */

@Composable
private fun StationLabel(
    code: String,
    dateAndTime: String,
    alignEnd: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) {
            Alignment.End
        } else {
            Alignment.Start
        },
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            textAlign = if (alignEnd) {
                TextAlign.End
            } else {
                TextAlign.Start
            }
        )

        if (dateAndTime.isNotBlank()) {
            Text(
                text = dateAndTime,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = if (alignEnd) {
                    TextAlign.End
                } else {
                    TextAlign.Start
                }
            )
        }
    }
}

/* =========================================================
   EMPTY STATE
========================================================= */

@Composable
private fun HomeEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your journeys start here",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Add a PNR to save your journey and " +
                    "check its reservation status.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(
                    R.drawable.ic_pnr_add
                ),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(9.dp))

            Text("Add your first PNR")
        }
    }
}

/* =========================================================
   ERROR STATE
========================================================= */

@Composable
private fun HomeErrorState(
    message: String,
    onRetry: () -> Unit,
    onRecover: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Couldn't load saved PNRs",
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = message,
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            },
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = onRetry) {
            Text("Try again")
        }

        if (onRecover != null) {
            Spacer(Modifier.height(10.dp))

            Button(onClick = onRecover) {
                Text("Add PNR")
            }
        }
    }
}

/* =========================================================
   PREVIEW
========================================================= */

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PnrenqTheme {
        HomeScreen(
            pnrs = MockPnrData.sample(),
            onAddClick = {},
            onPnrClick = {}
        )
    }
}