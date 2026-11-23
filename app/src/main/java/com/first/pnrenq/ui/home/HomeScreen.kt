
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.first.pnrenq.R
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.common.StatusChip
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.formatClockTime
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.common.parseClockTime
import com.first.pnrenq.ui.theme.AppThemeStyle
import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.LocalPnrVisualSettings
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.ThemeText as Text

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDateFormat = DateTimeFormatter.ofPattern(
    "d MMM",
    Locale.getDefault()
)

private fun isPastJourney(
    pnr: PnrInfo,
    now: LocalDateTime
): Boolean {
    val date = pnr.journeyDate
    val time = parseClockTime(
        pnr.details.departureTime
    )

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
        parseClockTime(it.details.departureTime)
            ?: LocalTime.MAX
    }.thenBy {
        it.pnrNumber
    }

private val pastOrder =
    compareByDescending<PnrInfo> {
        it.journeyDate
    }.thenByDescending {
        parseClockTime(it.details.departureTime)
            ?: LocalTime.MIN
    }.thenBy {
        it.pnrNumber
    }

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
        containerColor = Color.Transparent,

        topBar = {
            HomeHeader(
                onSettingsClick = onSettingsClick
            )
        },

        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },

        floatingActionButton = {
            if (pnrs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(19.dp),
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_pnr_add
                        ),
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(Modifier.width(9.dp))

                    Text(
                        text = "Add PNR",
                        color = scheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->

        when {
            isLoading && pnrs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            loadError != null && pnrs.isEmpty() -> {
                HomeErrorState(
                    message = loadError,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            pnrs.isEmpty() -> {
                HomeEmptyState(
                    onAddClick = onAddClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),

                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    if (upcoming.isNotEmpty()) {
                        item(key = "upcoming_title") {
                            SectionHeading(
                                title = "Upcoming journeys",
                                count = upcoming.size
                            )
                        }

                        items(
                            items = upcoming,
                            key = { it.pnrNumber }
                        ) { pnr ->
                            HomePnrCard(
                                pnr = pnr,
                                isDeleting =
                                    pnr.pnrNumber in deletingPnrs,
                                onClick = {
                                    onPnrClick(pnr.pnrNumber)
                                },
                                onDeleteClick = {
                                    pnrToDelete = pnr.pnrNumber
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(220),
                                    placementSpec = spring(
                                        dampingRatio =
                                            Spring.DampingRatioNoBouncy,
                                        stiffness =
                                            Spring.StiffnessMediumLow
                                    ),
                                    fadeOutSpec = tween(180)
                                )
                            )
                        }
                    }

                    if (past.isNotEmpty()) {
                        item(key = "past_title") {
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
                            HomePnrCard(
                                pnr = pnr,
                                isDeleting =
                                    pnr.pnrNumber in deletingPnrs,
                                onClick = {
                                    onPnrClick(pnr.pnrNumber)
                                },
                                onDeleteClick = {
                                    pnrToDelete = pnr.pnrNumber
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(220),
                                    placementSpec = spring(
                                        dampingRatio =
                                            Spring.DampingRatioNoBouncy,
                                        stiffness =
                                            Spring.StiffnessMediumLow
                                    ),
                                    fadeOutSpec = tween(180)
                                )
                            )
                        }
                    }

                    item(key = "bottom_space") {
                        Spacer(Modifier.height(95.dp))
                    }
                }
            }
        }
    }

    pnrToDelete?.let { selectedPnr ->

        val deleting = selectedPnr in deletingPnrs

        AlertDialog(
            onDismissRequest = {
                if (!deleting) pnrToDelete = null
            },
            title = {
                Text("Remove saved PNR?")
            },
            text = {
                Text(
                    "PNR $selectedPnr and its saved journey " +
                            "will be removed from this device."
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
                        "Delete",
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
                    Text("Cancel")
                }
            }
        )
    }
}

/* =========================================================
   NEW HEADER — BRAND CARD + SETTINGS CARD
========================================================= */

@Composable
private fun HomeHeader(
    onSettingsClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val gradient =
        LocalPnrVisualSettings.current.style ==
                AppThemeStyle.GRADIENT

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),

        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(66.dp),

            shape = RoundedCornerShape(21.dp),

            color = scheme.primaryContainer.copy(
                alpha = if (gradient) 0.93f else 1f
            ),

            border = BorderStroke(
                1.dp,
                scheme.primary.copy(alpha = 0.22f)
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 9.dp
                ),

                verticalArrangement =
                    Arrangement.Center
            ) {
                Text(
                    text = "PNR EXPERT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer
                )

                Text(
                    text = "Your saved journeys",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onPrimaryContainer
                        .copy(alpha = 0.78f)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),

            color = scheme.secondaryContainer,

            border = BorderStroke(
                1.dp,
                scheme.secondary.copy(alpha = 0.22f)
            )
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(66.dp)
            ) {
                Icon(
                    painter = painterResource(
                        R.drawable.ic_pnr_settings
                    ),
                    contentDescription = "Settings",
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 5.dp,
                bottom = 3.dp
            ),

        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* =========================================================
   COMPACT PNR CARD
========================================================= */

@Composable
private fun HomePnrCard(
    pnr: PnrInfo,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val timeFormat = LocalPnrTimeFormat.current

    val gradient =
        LocalPnrVisualSettings.current.style ==
                AppThemeStyle.GRADIENT

    val details = pnr.details

    val departure = listOfNotNull(
        pnr.journeyDate.format(shortDateFormat),
        formatClockTime(
            details.departureTime,
            timeFormat
        )
    ).joinToString(" · ")

    val arrivalDate = availableText(
        details.arrivalDate
    )?.let {
        try {
            LocalDate.parse(it).format(shortDateFormat)
        } catch (_: Exception) {
            null
        }
    }

    val arrival = listOfNotNull(
        arrivalDate,
        formatClockTime(
            details.arrivalTime,
            timeFormat
        )
    ).joinToString(" · ")

    Card(
        onClick = onClick,
        enabled = !isDeleting,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                scheme.surfaceContainerLow.copy(
                    alpha = if (gradient) 0.96f else 1f
                )
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

            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            /* ---------- Train + actions ---------- */

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = listOfNotNull(
                        availableText(pnr.trainNumber),
                        availableText(pnr.trainName)
                    ).joinToString(" · "),

                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
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

            /* ---------- Stations ---------- */

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
                    modifier = Modifier.padding(
                        horizontal = 9.dp
                    ),
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

            HorizontalDivider(
                color = scheme.outlineVariant
            )

            /* ---------- PNR and status ---------- */

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "PNR ${pnr.pnrNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Checked ${
                            formatLastChecked(
                                pnr.lastCheckedAtMillis,
                                timeFormat
                            )
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                availableText(pnr.currentStatus)?.let {
                    Spacer(Modifier.width(8.dp))
                    StatusChip(status = it)
                }
            }
        }
    }
}

@Composable
private fun StationLabel(
    code: String,
    dateAndTime: String,
    alignEnd: Boolean,
    modifier: Modifier = Modifier
) {
    val alignment = if (alignEnd) {
        Alignment.End
    } else {
        Alignment.Start
    }

    val textAlignment = if (alignEnd) {
        TextAlign.End
    } else {
        TextAlign.Start
    }

    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (dateAndTime.isNotBlank()) {
            Text(
                text = dateAndTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlignment,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun HomeEmptyState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "🚆",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Your journeys start here",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Add a PNR to save your journey and " +
                    "check its reservation status.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun HomeErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Couldn't load saved PNRs",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = onRetry) {
            Text("Try again")
        }
    }
}

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
