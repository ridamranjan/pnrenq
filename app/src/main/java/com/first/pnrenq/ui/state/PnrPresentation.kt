package com.first.pnrenq.ui.state

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Identifies both the owner and the revision of a pending operation message. */
data class ScopedPnrFeedback(val pnr: String, val message: PnrOperationMessage) {
    fun homeText(): String = "PNR …${pnr.takeLast(4)}: ${message.text}"
}

enum class PnrRecordState { READABLE, LOADING, UNREADABLE, UNAVAILABLE, MISSING }

internal fun PnrUiState.homeFeedback(): ScopedPnrFeedback? =
    operationMessages.minByOrNull { it.value.id }?.let {
        ScopedPnrFeedback(it.key, it.value)
    }

internal fun PnrUiState.detailsFeedback(pnr: String): ScopedPnrFeedback? =
    operationMessages[pnr]?.let { ScopedPnrFeedback(pnr, it) }

internal fun PnrUiState.busyPnrs(): Set<String> =
    buildSet {
        addAll(refreshingPnrs)
        addAll(deletingPnrs)
        checkingPnr?.let { add(it) }
    }

// A successful partial read reports unreadable IDs; an observation failure does not.
internal fun PnrUiState.databaseReadFailed(): Boolean =
    loadError != null && unreadablePnrs.isEmpty()

internal fun PnrUiState.recordState(pnr: String): PnrRecordState = when {
    pnrs.any { it.pnrNumber == pnr } -> PnrRecordState.READABLE
    isLoading -> PnrRecordState.LOADING
    pnr in unreadablePnrs -> PnrRecordState.UNREADABLE
    databaseReadFailed() -> PnrRecordState.UNAVAILABLE
    else -> PnrRecordState.MISSING
}

/** Never acknowledge a presentation that was interrupted or failed. */
internal suspend fun presentPnrFeedback(
    feedback: ScopedPnrFeedback,
    present: suspend () -> Unit,
    acknowledge: (String, Long) -> Unit
) {
    present()
    currentCoroutineContext().ensureActive()
    acknowledge(feedback.pnr, feedback.message.id)
}