package com.first.pnrenq.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.first.pnrenq.ui.common.PnrFeedbackEffect
import com.first.pnrenq.ui.state.PnrMessageKind
import com.first.pnrenq.ui.state.PnrOperationMessage
import com.first.pnrenq.ui.state.ScopedPnrFeedback
import java.util.concurrent.atomic.AtomicInteger
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.details.DetailsScreen
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.state.PnrRecordState
import com.first.pnrenq.ui.theme.PnrenqTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrScreenIntegrationTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val pnr = PnrInfo(
        pnrNumber = "1234567890",
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2099, 1, 1),
        fromStation = "ABC",
        toStation = "XYZ",
        bookingStatus = "WL 5",
        currentStatus = "CNF",
        chartStatus = null,
        passengers = emptyList(),
        lastCheckedAtMillis = 1000L
    )

    @Test
    fun homeKeepsReadableRecordAndOffersRetry() {
        var retries = 0

        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = listOf(pnr),
                    onAddClick = {},
                    onPnrClick = {},
                    loadError = "Unable to read saved PNRs",
                    onRetry = { retries++ }
                )
            }
        }

        rule.onNodeWithText(
            "Unable to read saved PNRs"
        ).assertExists()

        rule.onNodeWithText(
            "Try again"
        ).performClick()

        rule.runOnIdle {
            assertEquals(1, retries)
        }

        rule.onNode(hasScrollAction())
            .performScrollToNode(
                hasText("12345 · Test Express")
            )

        rule.onNodeWithText(
            "12345 · Test Express"
        ).assertExists()
    }

    @Test
    fun allUnreadableHomeRecordsOfferAddRecovery() {
        var adds = 0

        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = emptyList(),
                    onAddClick = { adds++ },
                    onPnrClick = {},
                    loadError = "Unreadable saved PNR",
                    unreadablePnrs = listOf(pnr.pnrNumber)
                )
            }
        }

        rule.onNodeWithText(
            "Couldn't load saved PNRs"
        ).assertExists()

        rule.onNodeWithText("Add PNR")
            .performScrollTo()
            .performClick()

        rule.runOnIdle {
            assertEquals(1, adds)
        }
    }

    @Test
    fun homeDeleteDisabledWhilePnrIsBusyAndRestoredWhenIdle() {
        val busy = mutableStateOf(
            setOf(pnr.pnrNumber)
        )

        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = listOf(pnr),
                    onAddClick = {},
                    onPnrClick = {},
                    busyPnrs = busy.value
                )
            }
        }

        rule.onNode(hasScrollAction())
            .performScrollToNode(
                hasContentDescription(
                    "Delete saved PNR ending in 7890"
                )
            )

        val delete = rule.onNodeWithContentDescription(
            "Delete saved PNR ending in 7890"
        )

        delete.assertIsNotEnabled()

        rule.runOnIdle {
            busy.value = emptySet()
        }

        delete.assertIsEnabled()
            .performClick()

        rule.onNodeWithText(
            "Remove saved PNR?"
        ).assertExists()

        rule.runOnIdle {
            busy.value = setOf(pnr.pnrNumber)
        }

        rule.onNodeWithText(
            "Delete",
            substring = false
        ).assertIsNotEnabled()

        rule.onNodeWithText(
            "Cancel",
            substring = false
        ).assertIsEnabled()
            .performClick()
    }

    @Test
    fun detailsDistinguishesUnavailableAndMissingAndAllowsRetry() {
        val state = mutableStateOf(
            PnrRecordState.UNAVAILABLE
        )

        var retries = 0

        rule.setContent {
            PnrenqTheme {
                DetailsScreen(
                    pnr = null,
                    isRefreshing = false,
                    onRefresh = {},
                    onDelete = {},
                    onBack = {},
                    recordState = state.value,
                    onRetry = { retries++ }
                )
            }
        }

        rule.onNodeWithText(
            "Unable to load saved PNRs. Please retry."
        ).assertExists()

        rule.onNodeWithText(
            "This PNR is no longer saved."
        ).assertDoesNotExist()

        rule.onNodeWithText(
            "Try again"
        ).performClick()

        rule.runOnIdle {
            assertEquals(1, retries)
            state.value = PnrRecordState.MISSING
        }

        rule.onNodeWithText(
            "This PNR is no longer saved."
        ).assertExists()

        rule.onNodeWithText(
            "Try again"
        ).assertDoesNotExist()
    }

    @Test
    fun unreadableDetailsOffersRepairButBlocksDuplicateRepair() {
        val busy = mutableStateOf(false)
        var repairs = 0

        rule.setContent {
            PnrenqTheme {
                DetailsScreen(
                    pnr = null,
                    isRefreshing = busy.value,
                    onRefresh = {
                        repairs++
                        busy.value = true
                    },
                    onDelete = {},
                    onBack = {},
                    recordState = PnrRecordState.UNREADABLE,
                    isBusy = busy.value
                )
            }
        }

        rule.onNodeWithText(
            "Saved PNR information is unreadable."
        ).assertExists()

        rule.onNodeWithText(
            "Refresh to repair"
        ).performClick()

        rule.onNodeWithText(
            "Repairing PNR..."
        ).assertIsNotEnabled()

        rule.runOnIdle {
            assertEquals(1, repairs)
        }
    }

    @Test
    fun detailsBlocksBothActionsDuringBusyOperation() {
        val busy = mutableStateOf(true)

        rule.setContent {
            PnrenqTheme {
                DetailsScreen(
                    pnr = pnr,
                    isRefreshing = busy.value,
                    onRefresh = {},
                    onDelete = {},
                    onBack = {},
                    isBusy = busy.value
                )
            }
        }

        rule.onNodeWithContentDescription(
            "Refresh PNR ending in 7890"
        ).assertIsNotEnabled()

        rule.onNodeWithContentDescription(
            "Delete saved PNR ending in 7890"
        ).assertIsNotEnabled()

        rule.runOnIdle {
            busy.value = false
        }

        rule.onNodeWithContentDescription(
            "Refresh PNR ending in 7890"
        ).assertIsEnabled()

        rule.onNodeWithContentDescription(
            "Delete saved PNR ending in 7890"
        ).assertIsEnabled()
    }

    @Test
    fun realSnackbarHostStateDoesNotAcknowledgeUntilDismissal() {
        val host = SnackbarHostState()

        val feedback = ScopedPnrFeedback(
            pnr.pnrNumber,
            PnrOperationMessage(
                41L,
                PnrMessageKind.REFRESH,
                "Updated"
            )
        )

        val pending = mutableStateOf<ScopedPnrFeedback?>(feedback)
        val acknowledged = AtomicInteger(0)

        rule.setContent {
            PnrenqTheme {
                // Deliberately omit SnackbarHost so no timeout can race explicit dismissal.
                PnrFeedbackEffect(
                    host,
                    pending.value,
                    onShown = { owner, id ->
                        assertEquals(pnr.pnrNumber, owner)
                        assertEquals(41L, id)
                        acknowledged.incrementAndGet()
                        pending.value = null
                    }
                )

                Text("Feedback test")
            }
        }

        rule.waitUntil(5_000L) {
            host.currentSnackbarData != null
        }

        rule.runOnIdle {
            assertEquals(0, acknowledged.get())
            pending.value = null
        }

        rule.waitUntil(5_000L) {
            host.currentSnackbarData == null
        }

        rule.runOnIdle {
            assertEquals(0, acknowledged.get())
            pending.value = feedback
        }

        rule.waitUntil(5_000L) {
            host.currentSnackbarData != null
        }

        rule.runOnIdle {
            host.currentSnackbarData!!.dismiss()
        }

        rule.waitUntil(5_000L) {
            acknowledged.get() == 1
        }

        rule.runOnIdle {
            assertEquals(1, acknowledged.get())
        }
    }
}