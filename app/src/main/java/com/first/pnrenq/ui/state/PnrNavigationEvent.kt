package com.first.pnrenq.ui.state

sealed interface PnrNavigationEvent {
    data class OpenDetails(
        val pnr: String,
        val requestId: String? = null
    ) : PnrNavigationEvent

    data class ReturnAfterDelete(
        val pnr: String,
        val originEntryId: String? = null
    ) : PnrNavigationEvent
}