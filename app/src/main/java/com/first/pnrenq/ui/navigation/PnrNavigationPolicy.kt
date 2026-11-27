package com.first.pnrenq.ui.navigation

import com.first.pnrenq.ui.state.PnrNavigationEvent

/* =========================================================
   APPLICATION ROUTES
========================================================= */

object Routes {
    const val HOME = "home"
    const val ADD = "add"
    const val SETTINGS = "settings"
    const val POLICIES = "policies"

    const val DETAILS_ARG = "pnr"
    const val DETAILS = "details/{$DETAILS_ARG}"

    fun details(pnr: String): String {
        return "details/$pnr"
    }
}

internal const val ADD_REQUEST_KEY = "pnr_add_request_id"

/** Unscoped or stale events must never change the active destination. */
internal fun shouldHandleNavigationEvent(
    event: PnrNavigationEvent,
    route: String?,
    activeEntryId: String?,
    activePnr: String?,
    activeAddRequestId: String?
): Boolean = when (event) {
    is PnrNavigationEvent.OpenDetails ->
        route == Routes.ADD &&
                event.requestId != null &&
                event.requestId == activeAddRequestId

    is PnrNavigationEvent.ReturnAfterDelete ->
        route == Routes.DETAILS &&
                event.originEntryId != null &&
                event.originEntryId == activeEntryId &&
                event.pnr == activePnr
}