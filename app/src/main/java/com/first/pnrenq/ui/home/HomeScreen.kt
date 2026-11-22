@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.first.pnrenq.data.mock.MockPnrData
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.model.coachBerthSummary
import com.first.pnrenq.ui.common.StatusChip
import com.first.pnrenq.ui.common.formatJourneyDate
import com.first.pnrenq.ui.common.formatLastChecked
import com.first.pnrenq.ui.theme.PnrenqTheme

@Composable
fun HomeScreen(
    pnrs: List<PnrInfo>,
    onAddClick: () -> Unit,
    onPnrClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("PNR enq") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddClick) { Text("Add PNR") }
        }
    ) { innerPadding ->
        if (pnrs.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pnrs, key = { it.pnrNumber }) { pnr ->
                    PnrCard(pnr = pnr, onClick = { onPnrClick(pnr.pnrNumber) })
                }
            }
        }
    }
}

@Composable
private fun PnrCard(pnr: PnrInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pnr.trainName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pnr.trainNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                StatusChip(status = pnr.currentStatus)
            }
            Text(
                text = "${pnr.fromStation} \u2192 ${pnr.toStation}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = formatJourneyDate(pnr.journeyDate),
                style = MaterialTheme.typography.bodyMedium
            )
            pnr.coachBerthSummary()?.let { summary ->
                Text(
                    text = "Coach / berth: $summary",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Last checked: ${formatLastChecked(pnr.lastCheckedAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No saved PNRs yet",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap Add PNR to check your first one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PnrenqTheme {
        HomeScreen(pnrs = MockPnrData.sample(), onAddClick = {}, onPnrClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    PnrenqTheme {
        HomeScreen(pnrs = emptyList(), onAddClick = {}, onPnrClick = {})
    }
}