package com.first.pnrenq.ui.navigation

import com.first.pnrenq.ui.state.PnrNavigationEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrNavigationPolicyTest {
    private val pnr = "1234567890"
    private val open = PnrNavigationEvent.OpenDetails(pnr, "request-1")
    private val deleted = PnrNavigationEvent.ReturnAfterDelete(pnr, "details-1")

    @Test
    fun matchingAddRequestCanOpenDetails() {
        assertTrue(handles(open, Routes.ADD, request = "request-1"))
    }

    @Test
    fun oldOrClearedAddRequestCannotOpenDetails() {
        assertFalse(handles(open, Routes.ADD, request = "request-2"))
        assertFalse(handles(open, Routes.ADD))
    }

    @Test
    fun addEventCannotNavigateFromAnotherRoute() {
        for (route in listOf(null, Routes.HOME, Routes.DETAILS, Routes.SETTINGS, Routes.POLICIES)) {
            assertFalse(handles(open, route, request = "request-1"))
        }
    }

    @Test
    fun matchingDeleteEventCanReturnFromOriginatingDetails() {
        assertTrue(handles(deleted, Routes.DETAILS, "details-1", pnr))
    }

    @Test
    fun deleteEventCannotPopDifferentEntryPnrOrRoute() {
        assertFalse(handles(deleted, Routes.DETAILS, "details-2", pnr))
        assertFalse(handles(deleted, Routes.DETAILS, "details-1", "9876543210"))
        assertFalse(handles(deleted, Routes.HOME, "details-1", pnr))
        assertFalse(handles(deleted, Routes.DETAILS, null, pnr))
    }

    @Test
    fun unscopedEventsAreNotEligibleForUiNavigation() {
        assertFalse(handles(PnrNavigationEvent.OpenDetails(pnr), Routes.ADD))
        assertFalse(handles(PnrNavigationEvent.ReturnAfterDelete(pnr), Routes.DETAILS, activePnr = pnr))
    }

    private fun handles(
        event: PnrNavigationEvent,
        route: String?,
        entry: String? = null,
        activePnr: String? = null,
        request: String? = null
    ) = shouldHandleNavigationEvent(event, route, entry, activePnr, request)
}