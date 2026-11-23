
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.common.JourneyTimeline
import com.first.pnrenq.ui.common.StatusChip
import com.first.pnrenq.ui.common.availableText
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.theme.PnrenqTheme

@Composable
fun HomeScreen(
    pnrs: List<PnrInfo>,
    onAddClick: () -> Unit,
    onPnrClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    loadError: String? = null,
    onRetry: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PNR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = onSettingsClick) {
                        Text("Themes")
                    }
                }
            )
        },
        floatingActionButton = {
            if (pnrs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                        top = 14.dp,
                        bottom = 110.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Your tickets",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                bottom = 2.dp
                            )
                        )
                    }

                    items(
                        items = pnrs,
                        key = { it.pnrNumber }
                    ) { pnr ->
                        ModernPnrCard(
                            pnr = pnr,
                            onClick = {
                                onPnrClick(pnr.pnrNumber)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernPnrCard(
    pnr: PnrInfo,
    onClick: () -> Unit
) {
    val details = pnr.details

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.65f
            )
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
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "TRAIN ${pnr.trainNumber}",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (availableText(pnr.currentStatus) != null) {
                    Spacer(Modifier.width(8.dp))

                    StatusChip(
                        status = pnr.currentStatus
                    )
                }
            }

            Text(
                text = pnr.trainName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            availableText(details.journeyClass)?.let {
                Text(
                    text = "Class $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            JourneyTimeline(
                fromStation = pnr.fromStation,
                toStation = pnr.toStation,
                journeyDate = pnr.journeyDate,
                departureTime = details.departureTime,
                arrivalDate = details.arrivalDate,
                arrivalTime = details.arrivalTime,
                duration = details.duration,
                compact = true
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PNR NUMBER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = pnr.pnrNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "View details  ›",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Updated ${
                    formatLastChecked(pnr.lastCheckedAtMillis)
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚆",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Your journeys start here",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Add a PNR to view your train and reservation status.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                modifier = Modifier.padding(vertical = 5.dp)
            )
        }
    }
}

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
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(22.dp))

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

@Preview(showBackground = true)
@Composable
private fun EmptyHomePreview() {
    PnrenqTheme {
        HomeScreen(
            pnrs = emptyList(),
            onAddClick = {},
            onPnrClick = {}
        )
    }
}
