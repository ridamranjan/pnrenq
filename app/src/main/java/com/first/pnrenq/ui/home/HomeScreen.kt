@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.common.StatusChip
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.theme.PnrenqTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/* =========================================================
   DATE / TIME HELPERS
========================================================= */

private val cardDateFormat =
    DateTimeFormatter.ofPattern(
        "d MMM",
        Locale.getDefault()
    )

private fun departureTime(
    pnr: PnrInfo
): LocalTime? {
    val value = availableText(
        pnr.details.departureTime
    ) ?: return null

    return try {
        LocalTime.parse(value)
    } catch (_: Exception) {
        null
    }
}

private fun isPastJourney(
    pnr: PnrInfo,
    now: LocalDateTime
): Boolean {
    val date = pnr.journeyDate
    val time = departureTime(pnr)

    return when {
        date.isBefore(now.toLocalDate()) -> true
        date.isAfter(now.toLocalDate()) -> false
        time == null -> false
        else -> time.isBefore(now.toLocalTime())
    }
}

private val upcomingOrder =
    compareBy<PnrInfo> {
        it.journeyDate
    }.thenBy {
        departureTime(it) ?: LocalTime.MAX
    }.thenBy {
        it.pnrNumber
    }

private val pastOrder =
    compareByDescending<PnrInfo> {
        it.journeyDate
    }.thenByDescending {
        departureTime(it) ?: LocalTime.MIN
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
    var pnrToDelete by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(homeMessage) {
        if (homeMessage != null) {
            onHomeMessageShown()
            snackbarHostState.showSnackbar(homeMessage)
        }
    }

    val now = LocalDateTime.now()

    val upcoming = pnrs
        .filterNot { isPastJourney(it, now) }
        .sortedWith(upcomingOrder)

    val past = pnrs
        .filter { isPastJourney(it, now) }
        .sortedWith(pastOrder)

    Scaffold(
        modifier = modifier,
        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PNR",
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(
                        onClick = onSettingsClick
                    ) {
                        Text("Themes")
                    }
                }
            )
        },

        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },

        floatingActionButton = {
            if (pnrs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    containerColor =
                        MaterialTheme.colorScheme.primary,
                    contentColor =
                        MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        "+  Add PNR",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { paddingValues ->

        when {
            isLoading && pnrs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            loadError != null && pnrs.isEmpty() -> {
                ErrorState(
                    message = loadError,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            pnrs.isEmpty() -> {
                EmptyState(
                    onAddClick = onAddClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 110.dp
                    ),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    /* ---------- Upcoming ---------- */

                    if (upcoming.isNotEmpty()) {

                        item(key = "upcoming_heading") {
                            SectionHeading(
                                title = "Upcoming journeys",
                                count = upcoming.size
                            )
                        }

                        items(
                            items = upcoming,
                            key = { it.pnrNumber }
                        ) { pnr ->

                            CompactPnrCard(
                                pnr = pnr,

                                onClick = {
                                    if (
                                        pnr.pnrNumber !in deletingPnrs
                                    ) {
                                        onPnrClick(
                                            pnr.pnrNumber
                                        )
                                    }
                                },

                                onDeleteClick = {
                                    pnrToDelete =
                                        pnr.pnrNumber
                                },

                                isDeleting =
                                    pnr.pnrNumber in deletingPnrs,

                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(
                                        durationMillis = 220
                                    ),
                                    placementSpec = spring(
                                        dampingRatio =
                                            Spring.DampingRatioNoBouncy,
                                        stiffness =
                                            Spring.StiffnessMediumLow
                                    ),
                                    fadeOutSpec = tween(
                                        durationMillis = 180
                                    )
                                )
                            )
                        }
                    }

                    /* ---------- Past ---------- */

                    if (past.isNotEmpty()) {

                        item(key = "past_heading") {
                            Spacer(Modifier.height(8.dp))

                            SectionHeading(
                                title = "Past journeys",
                                count = past.size
                            )
                        }

                        items(
                            items = past,
                            key = { it.pnrNumber }
                        ) { pnr ->

                            CompactPnrCard(
                                pnr = pnr,

                                onClick = {
                                    if (
                                        pnr.pnrNumber !in deletingPnrs
                                    ) {
                                        onPnrClick(
                                            pnr.pnrNumber
                                        )
                                    }
                                },

                                onDeleteClick = {
                                    pnrToDelete =
                                        pnr.pnrNumber
                                },

                                isDeleting =
                                    pnr.pnrNumber in deletingPnrs,

                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(
                                        durationMillis = 220
                                    ),
                                    placementSpec = spring(
                                        dampingRatio =
                                            Spring.DampingRatioNoBouncy,
                                        stiffness =
                                            Spring.StiffnessMediumLow
                                    ),
                                    fadeOutSpec = tween(
                                        durationMillis = 180
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /* =====================================================
       DELETE CONFIRMATION
    ===================================================== */

    pnrToDelete?.let { selectedPnr ->

        val deletionInProgress =
            selectedPnr in deletingPnrs

        AlertDialog(
            onDismissRequest = {
                if (!deletionInProgress) {
                    pnrToDelete = null
                }
            },

            title = {
                Text("Remove saved PNR?")
            },

            text = {
                Text(
                    "PNR $selectedPnr and its saved " +
                            "journey details will be removed " +
                            "from this device."
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !deletionInProgress,

                    onClick = {
                        pnrToDelete = null
                        onDeletePnr(selectedPnr)
                    },

                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor =
                                MaterialTheme.colorScheme.error
                        )
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !deletionInProgress,

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
   SECTION HEADING
========================================================= */

@Composable
private fun SectionHeading(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 4.dp
            ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* =========================================================
   COMPACT PNR CARD
========================================================= */

@Composable
private fun CompactPnrCard(
    pnr: PnrInfo,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val details = pnr.details

    val departureDate =
        pnr.journeyDate.format(cardDateFormat)

    val departure = availableText(
        details.departureTime
    )

    val arrivalDate = availableText(
        details.arrivalDate
    )?.let { value ->
        try {
            LocalDate.parse(value)
                .format(cardDateFormat)
        } catch (_: Exception) {
            null
        }
    }

    val arrival = availableText(
        details.arrivalTime
    )

    val trainTitle = listOfNotNull(
        availableText(pnr.trainNumber),
        availableText(pnr.trainName)
    ).joinToString(" · ")

    Card(
        onClick = onClick,

        enabled = !isDeleting,

        modifier = modifier.fillMaxWidth(),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerLow
        ),

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
                .copy(alpha = 0.65f)
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

            /* =============================================
               TOP ROW:
               TRAIN + COPY + DELETE
            ============================================= */

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = trainTitle,
                    modifier = Modifier.weight(1f),
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.width(6.dp))

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(0.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    TextButton(
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

                        contentPadding = PaddingValues(
                            horizontal = 6.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Text(
                            text = "⧉ Copy",
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }

                    TextButton(
                        onClick = onDeleteClick,
                        enabled = !isDeleting,

                        contentPadding = PaddingValues(
                            horizontal = 6.dp,
                            vertical = 4.dp
                        ),

                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor =
                                    MaterialTheme.colorScheme.error
                            )
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Delete",
                                style =
                                    MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            /* =============================================
               ROUTE / JOURNEY DATE AND TIME
            ============================================= */

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = pnr.fromStation,
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = listOfNotNull(
                            departureDate,
                            departure
                        ).joinToString(" · "),

                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "→",
                    modifier = Modifier.padding(
                        horizontal = 10.dp
                    ),
                    style =
                        MaterialTheme.typography.titleLarge,
                    color =
                        MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier.weight(1f),

                    horizontalAlignment =
                        Alignment.End,

                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = pnr.toStation,
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val arrivalText =
                        listOfNotNull(
                            arrivalDate,
                            arrival
                        ).joinToString(" · ")

                    if (arrivalText.isNotEmpty()) {
                        Text(
                            text = arrivalText,
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme.outlineVariant
            )

            /* =============================================
               BOTTOM ROW:
               PNR / CHECKED + CURRENT STATUS
            ============================================= */

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {

                    Text(
                        text = "PNR ${pnr.pnrNumber}",
                        style =
                            MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Checked ${
                            formatLastChecked(
                                pnr.lastCheckedAtMillis
                            )
                        }",

                        style =
                            MaterialTheme.typography.labelSmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                availableText(pnr.currentStatus)?.let {
                    Spacer(Modifier.width(8.dp))

                    StatusChip(
                        status = it
                    )
                }
            }
        }
    }
}

/* =========================================================
   EMPTY STATE
========================================================= */

@Composable
private fun EmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "🚆",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Your journeys start here",
            style =
                MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text =
                "Add a PNR to view your train " +
                        "and reservation status.",

            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "+  Add your first PNR",
                modifier =
                    Modifier.padding(vertical = 5.dp)
            )
        }
    }
}

/* =========================================================
   ERROR STATE
========================================================= */

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Couldn't load saved PNRs",
            style =
                MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = message,
            style =
                MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onRetry
        ) {
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
            onPnrClick = {},
            onDeletePnr = {}
        )
    }
}
