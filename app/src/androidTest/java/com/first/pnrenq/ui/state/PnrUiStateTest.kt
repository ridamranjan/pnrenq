
package com.first.pnrenq.ui.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrUiStateTest {

    @Test
    fun initialStateStartsLoading() {
        val state = PnrUiState()

        assertTrue(state.isLoading)
        assertTrue(state.pnrs.isEmpty())
        assertNull(state.loadError)
        assertFalse(state.isChecking)
    }

    @Test
    fun databaseErrorIsSeparateFromAddError() {
        val state = PnrUiState(
            isLoading = false,
            loadError = "Database unavailable"
        )

        assertEquals("Database unavailable", state.loadError)
        assertNull(state.addError)
    }

    @Test
    fun busyStateCanTrackMultiplePnrs() {
        val state = PnrUiState(
            refreshingPnrs = setOf("1234567890"),
            deletingPnrs = setOf("9876543210")
        )

        assertTrue("1234567890" in state.refreshingPnrs)
        assertTrue("9876543210" in state.deletingPnrs)
        assertFalse("1234567890" in state.deletingPnrs)
    }
}
