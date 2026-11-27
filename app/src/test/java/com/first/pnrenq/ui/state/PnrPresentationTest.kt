package com.first.pnrenq.ui.state

import com.first.pnrenq.domain.model.PnrInfo
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PnrPresentationTest {

    private val a = "1234567890"
    private val b = "9876543210"

    private val first = PnrOperationMessage(
        1,
        PnrMessageKind.REFRESH,
        "Refreshed"
    )

    private val second = PnrOperationMessage(
        2,
        PnrMessageKind.DELETE,
        "Unable to delete"
    )

    @Test
    fun homeSelectsOldestPendingMessageAndMasksPnr() {
        val state = PnrUiState(
            operationMessages = linkedMapOf(
                b to second,
                a to first
            )
        )

        assertEquals(
            ScopedPnrFeedback(a, first),
            state.homeFeedback()
        )

        assertEquals(
            "PNR …7890: Refreshed",
            state.homeFeedback()?.homeText()
        )

        assertEquals(
            ScopedPnrFeedback(b, second),
            state.copy(
                operationMessages = mapOf(b to second)
            ).homeFeedback()
        )

        assertNull(PnrUiState().homeFeedback())
    }

    @Test
    fun detailsOnlySelectsItsOwnFeedback() {
        val state = PnrUiState(
            operationMessages = mapOf(
                a to first,
                b to second
            )
        )

        assertEquals(
            ScopedPnrFeedback(b, second),
            state.detailsFeedback(b)
        )

        assertNull(
            state.detailsFeedback("1111111111")
        )
    }

    @Test
    fun busySetIncludesCheckRefreshAndDeleteWithoutBlockingUnrelatedPnrs() {
        val c = "1111111111"

        val state = PnrUiState(
            checkingPnr = a,
            refreshingPnrs = setOf(b),
            deletingPnrs = setOf(c)
        )

        assertEquals(
            setOf(a, b, c),
            state.busyPnrs()
        )

        assertTrue(
            state.busyPnrs().all {
                state.isBusy(it)
            }
        )

        assertFalse(
            state.isBusy("2222222222")
        )
    }

    @Test
    fun recordAvailabilityDistinguishesLoadingCorruptionFailureAndMissing() {
        val state = PnrUiState(isLoading = false)

        assertEquals(
            PnrRecordState.MISSING,
            state.recordState(a)
        )

        assertEquals(
            PnrRecordState.LOADING,
            state.copy(
                isLoading = true
            ).recordState(a)
        )

        val partial = state.copy(
            loadError = "Unreadable",
            unreadablePnrs = listOf(a)
        )

        assertEquals(
            PnrRecordState.UNREADABLE,
            partial.recordState(a)
        )

        assertEquals(
            PnrRecordState.MISSING,
            partial.recordState(b)
        )

        assertFalse(
            partial.databaseReadFailed()
        )

        val failed = state.copy(
            loadError = "Database unavailable"
        )

        assertTrue(
            failed.databaseReadFailed()
        )

        assertEquals(
            PnrRecordState.UNAVAILABLE,
            failed.recordState(a)
        )

        assertEquals(
            PnrRecordState.LOADING,
            failed.copy(
                isLoading = true
            ).recordState(a)
        )

        val saved = PnrInfo(
            pnrNumber = a,
            trainNumber = "12345",
            trainName = "Example",
            journeyDate = LocalDate.of(2028, 1, 1),
            fromStation = "ABC",
            toStation = "XYZ",
            bookingStatus = "WL 5",
            currentStatus = "CNF",
            chartStatus = null,
            passengers = emptyList(),
            lastCheckedAtMillis = 1000L
        )

        assertEquals(
            PnrRecordState.READABLE,
            failed.copy(
                pnrs = listOf(saved)
            ).recordState(a)
        )
    }

    @Test
    fun feedbackIsAcknowledgedOnlyAfterPresentationReturns() = runBlocking {
        val gate = CompletableDeferred<Unit>()

        val acknowledged =
            mutableListOf<Pair<String, Long>>()

        val job = launch(
            start = CoroutineStart.UNDISPATCHED
        ) {
            presentPnrFeedback(
                ScopedPnrFeedback(a, first),
                { gate.await() }
            ) { pnr, id ->
                acknowledged.add(pnr to id)
            }
        }

        assertTrue(acknowledged.isEmpty())

        gate.complete(Unit)
        job.join()

        assertEquals(
            listOf(a to 1L),
            acknowledged
        )
    }

    @Test
    fun cancelledPresentationIsNotAcknowledgedAndCanBeRetried() = runBlocking {
        val acknowledged =
            mutableListOf<Pair<String, Long>>()

        val feedback =
            ScopedPnrFeedback(a, first)

        val job = launch(
            start = CoroutineStart.UNDISPATCHED
        ) {
            presentPnrFeedback(
                feedback,
                { awaitCancellation() }
            ) { pnr, id ->
                acknowledged.add(pnr to id)
            }
        }

        job.cancelAndJoin()

        assertTrue(acknowledged.isEmpty())

        presentPnrFeedback(
            feedback,
            {}
        ) { pnr, id ->
            acknowledged.add(pnr to id)
        }

        assertEquals(
            listOf(a to 1L),
            acknowledged
        )
    }
}