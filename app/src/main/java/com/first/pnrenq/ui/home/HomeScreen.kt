
package com.first.pnrenq.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.first.pnrenq.R
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PnrInfo

import com.first.pnrenq.ui.common.StatusChip
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.formatClockTime
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.common.parseClockTime

import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.PnrAccentSurface
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.pnrAccentBrush
import com.first.pnrenq.ui.theme.ThemeText as Text
import com.first.pnrenq.ui.common.PnrNumberBubble

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/* =========================================================
   DATE AND TIME SORTING
========================================================= */

private val shortDateFormat =
    DateTimeFormatter.ofPattern(
        "d MMM",
        Locale.getDefault()
    )

private fun isPastJourney(
    pnr: PnrInfo,
    now: LocalDateTime
): Boolean {
    val date = pnr.journeyDate

    val departure = parseClockTime(
        pnr.details.departureTime
    )

    return when {
        date.isBefore(now.toLocalDate()) -> true
        date.isAfter(now.toLocalDate()) -> false
        departure == null -> false
        else -> departure.isBefore(now.toLocalTime())
    }
}

private val upcomingOrder =
    compareBy<PnrInfo> {
        it.journeyDate
    }.thenBy {
        parseClockTime(
            it.details.departureTime
        ) ?: LocalTime.MAX
    }.thenBy {
        it.pnrNumber
    }

private val pastOrder =
    compareByDescending<PnrInfo> {
        it.journeyDate
    }.thenByDescending {
        parseClockTime(
            it.details.departureTime
        ) ?: LocalTime.MIN
    }.thenBy {
        it.pnrNumber
    }

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
    homeMessage: String? = null,
    onHomeMessageShown: () -> Unit = {}
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

    LaunchedEffect(homeMessage) {
        if (homeMessage != null) {
            onHomeMessageShown()

            snackbarHostState.showSnackbar(
                homeMessage
            )
        }
    }

    val now = LocalDateTime.now()

    val upcoming = pnrs
        .filterNot {
            isPastJourney(it, now)
        }
        .sortedWith(upcomingOrder)

    val past = pnrs
        .filter {
            isPastJourney(it, now)
        }
        .sortedWith(pastOrder)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = scheme.onBackground,

            topBar = {
                HomeHeader()
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

                        contentAlignment =
                            Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                /* =====================================
                   ERROR STATE
                ===================================== */

                loadError != null && pnrs.isEmpty() -> {
                    HomeErrorState(
                        message = loadError,
                        onRetry = onRetry,

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
                            bottom = 132.dp
                        ),

                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        /* =============================
                           UPCOMING JOURNEYS
                        ============================= */

                        if (upcoming.isNotEmpty()) {

                            item(
                                key = "upcoming_title"
                            ) {
                                SectionHeading(
                                    title = "Upcoming journeys",

                                    count = upcoming.size,

                                    expanded = upcomingExpanded,

                                    onToggle = {
                                        upcomingExpanded =
                                            !upcomingExpanded
                                    }
                                )
                            }

                            if (upcomingExpanded) {

                                items(
                                    items = upcoming,

                                    key = {
                                        it.pnrNumber
                                    }
                                ) { pnr ->

                                    HomePnrCard(
                                        pnr = pnr,

                                        isDeleting =
                                            pnr.pnrNumber in deletingPnrs,

                                        onClick = {
                                            onPnrClick(
                                                pnr.pnrNumber
                                            )
                                        },

                                        onDeleteClick = {
                                            pnrToDelete =
                                                pnr.pnrNumber
                                        }
                                    )
                                }
                            }
                        }

                        /* =============================
                           PAST JOURNEYS
                        ============================= */

                        if (past.isNotEmpty()) {

                            item(
                                key = "past_title"
                            ) {

                                if (upcoming.isNotEmpty()) {
                                    Spacer(
                                        Modifier.height(8.dp)
                                    )
                                }

                                SectionHeading(
                                    title = "Past journeys",

                                    count = past.size,

                                    expanded = pastExpanded,

                                    onToggle = {
                                        pastExpanded =
                                            !pastExpanded
                                    }
                                )
                            }

                            if (pastExpanded) {

                                items(
                                    items = past,

                                    key = {
                                        it.pnrNumber
                                    }
                                ) { pnr ->

                                    HomePnrCard(
                                        pnr = pnr,

                                        isDeleting =
                                            pnr.pnrNumber in deletingPnrs,

                                        onClick = {
                                            onPnrClick(
                                                pnr.pnrNumber
                                            )
                                        },

                                        onDeleteClick = {
                                            pnrToDelete =
                                                pnr.pnrNumber
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        /* =============================================
           FIXED BOTTOM BUTTONS
        ============================================= */

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            HomeSettingsButton(
                onClick = onSettingsClick
            )

            Spacer(
                Modifier.weight(1f)
            )

            if (pnrs.isNotEmpty()) {

                HomeAddPnrButton(
                    onClick = onAddClick
                )
            }
        }
    }

    /* =================================================
       DELETE CONFIRMATION
    ================================================= */

    pnrToDelete?.let { selectedPnr ->

        val deleting =
            selectedPnr in deletingPnrs

        AlertDialog(
            onDismissRequest = {
                if (!deleting) {
                    pnrToDelete = null
                }
            },

            title = {
                Text(
                    text = "Remove saved PNR?"
                )
            },

            text = {
                Text(
                    text = "PNR $selectedPnr and its saved journey " +
                            "details will be removed from this device."
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !deleting,

                    onClick = {
                        pnrToDelete = null
                        onDeletePnr(selectedPnr)
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
                    enabled = !deleting,

                    onClick = {
                        pnrToDelete = null
                    }
                ) {
                    Text(
                        text = "Cancel"
                    )
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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),

            backgroundColor =
                scheme.primaryContainer,

            foregroundColor =
                scheme.onPrimaryContainer,

            shape = RoundedCornerShape(15.dp),

            strength = 1.2f,

            border = BorderStroke(
                1.dp,
                scheme.primary.copy(alpha = 0.20f)
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 4.dp
                    ),

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text = "PNR EXPERT",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Saved journeys",
                    style =
                        MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = scheme.onPrimaryContainer,
                    maxLines = 1
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

        backgroundColor =
            scheme.secondaryContainer,

        foregroundColor =
            scheme.onSecondaryContainer,

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

            // Preserve approximately the original FAB height.
            .height(56.dp)

            // Clip the ENTIRE button to one shape.
            .clip(shape)

            // Paint one continuous gradient.
            .background(backgroundBrush)

            // Make the whole shape clickable.
            .clickable(
                role = Role.Button,
                onClick = onClick
            )

            // Content padding stays inside the button.
            .padding(horizontal = 20.dp),

        horizontalArrangement =
            Arrangement.spacedBy(9.dp),

        verticalAlignment =
            Alignment.CenterVertically
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
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            /* =====================================
               SECTION TITLE
            ===================================== */

            Text(
                text = title,

                modifier = Modifier.weight(1f),

                style =
                    MaterialTheme.typography.titleMedium,

                fontWeight = FontWeight.Bold,

                color = scheme.onSurface
            )

            /* =====================================
               JOURNEY COUNT BUBBLE
            ===================================== */

            Surface(
                shape = RoundedCornerShape(50),

                color = scheme.primaryContainer,

                contentColor =
                    scheme.onPrimaryContainer,

                tonalElevation = 0.dp,

                shadowElevation = 0.dp
            ) {
                Text(
                    text = count.toString(),

                    modifier = Modifier.padding(
                        horizontal = 11.dp,
                        vertical = 5.dp
                    ),

                    style =
                        MaterialTheme.typography.labelMedium,

                    fontWeight = FontWeight.Bold,

                    color = scheme.onPrimaryContainer,

                    maxLines = 1
                )
            }

            /* =====================================
               EXPAND / COLLAPSE ARROW

               Same symbols as Settings dropdown.
            ===================================== */

            Text(
                text = if (expanded) {
                    "▴"
                } else {
                    "▾"
                },

                style =
                    MaterialTheme.typography.headlineSmall,

                color = scheme.primary,

                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
/* =========================================================
   SAVED PNR CARD — UNCHANGED SOLID BACKGROUND
========================================================= */
@Composable
private fun HomePnrCard(
    pnr: PnrInfo,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    val timeFormat =
        LocalPnrTimeFormat.current

    val departure = listOfNotNull(
        pnr.journeyDate.format(shortDateFormat),

        formatClockTime(
            pnr.details.departureTime,
            timeFormat
        )
    ).joinToString(" · ")

    val arrivalDate =
        availableText(
            pnr.details.arrivalDate
        )?.let {

            try {
                LocalDate.parse(it)
                    .format(shortDateFormat)
            } catch (_: Exception) {
                null
            }
        }

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
            containerColor =
                scheme.surfaceContainerLow,

            contentColor = scheme.onSurface
        ),

        border = BorderStroke(
            1.dp,
            scheme.outlineVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),

            verticalArrangement =
                Arrangement.spacedBy(9.dp)
        ) {

            /* =====================================
               TRAIN NAME AND ACTION BUTTONS
            ===================================== */

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = trainTitle,
                    modifier = Modifier.weight(1f),

                    style =
                        MaterialTheme.typography.labelLarge,

                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    },

                    enabled = !isDeleting,

                    modifier = Modifier.size(40.dp)
                ) {

                    Icon(
                        painter = painterResource(
                            R.drawable.ic_pnr_copy
                        ),

                        contentDescription = "Copy PNR",

                        tint = scheme.primary,

                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    enabled = !isDeleting,
                    modifier = Modifier.size(40.dp)
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

                            contentDescription = "Delete PNR",

                            tint = scheme.error,

                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            /* =====================================
               JOURNEY INFORMATION
            ===================================== */

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                StationLabel(
                    code = pnr.fromStation,
                    dateAndTime = departure,
                    alignEnd = false,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "→",

                    modifier = Modifier.padding(
                        horizontal = 9.dp
                    ),

                    style =
                        MaterialTheme.typography.titleLarge,

                    color = scheme.primary
                )

                StationLabel(
                    code = pnr.toStation,
                    dateAndTime = arrival,
                    alignEnd = true,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                color = scheme.outlineVariant
            )

            /* =====================================
               PNR BUBBLE AND CURRENT STATUS
            ===================================== */

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f),

                    horizontalAlignment = Alignment.Start,

                    verticalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {

                    // Theme-aware PNR bubble.
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

                        modifier = Modifier.padding(
                            start = 4.dp
                        ),

                        style =
                            MaterialTheme.typography.labelSmall,

                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                availableText(
                    pnr.currentStatus
                )?.let { status ->

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    StatusChip(
                        status = status
                    )
                }
            }
        }
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

        verticalArrangement =
            Arrangement.spacedBy(3.dp)
    ) {

        Text(
            text = code,
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (dateAndTime.isNotBlank()) {

            Text(
                text = dateAndTime,
                style =
                    MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,

                textAlign = if (alignEnd) {
                    TextAlign.End
                } else {
                    TextAlign.Start
                },

                maxLines = 2
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
        modifier = modifier.padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "Your journeys start here",
            style =
                MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(
            Modifier.height(10.dp)
        )

        Text(
            text = "Add a PNR to save your journey and " +
                    "check its reservation status.",
            style =
                MaterialTheme.typography.bodyMedium,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(
            Modifier.height(22.dp)
        )

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

            Spacer(
                Modifier.width(9.dp)
            )

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
    modifier: Modifier = Modifier
) {

    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "Couldn't load saved PNRs",
            style =
                MaterialTheme.typography.titleLarge,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(
            Modifier.height(10.dp)
        )

        Text(
            text = message,
            style =
                MaterialTheme.typography.bodyMedium,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(
            Modifier.height(20.dp)
        )

        Button(onClick = onRetry) {
            Text("Try again")
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
