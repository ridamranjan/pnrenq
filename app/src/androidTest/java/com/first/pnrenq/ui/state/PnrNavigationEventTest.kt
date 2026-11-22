
package com.first.pnrenq.ui.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrNavigationEventTest {

    @Test
    fun openDetailsContainsCorrectPnr() {
        val event = PnrNavigationEvent.OpenDetails("1234567890")

        assertEquals("1234567890", event.pnr)
    }

    @Test
    fun returnAfterDeleteContainsCorrectPnr() {
        val event = PnrNavigationEvent.ReturnAfterDelete("9876543210")

        assertEquals("9876543210", event.pnr)
    }

    @Test
    fun differentEventTypesAreNotEqual() {
        val open = PnrNavigationEvent.OpenDetails("1234567890")
        val delete = PnrNavigationEvent.ReturnAfterDelete("1234567890")

        assertFalse(open == delete)
    }

    @Test
    fun identicalNavigationEventsAreEqual() {
        val first = PnrNavigationEvent.OpenDetails("1234567890")
        val second = PnrNavigationEvent.OpenDetails("1234567890")

        assertTrue(first == second)
    }
}
