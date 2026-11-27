package com.first.pnrenq.ui.state

import org.junit.Assert.*
import org.junit.Test

class PnrPresentationConsistencyTest {

    @Test
    fun busySetMatchesIndividualBusyPredicateAcrossAllMembershipCombinations() {
        val numbers = listOf(
            "0123456789",
            "9876543210",
            "1111111111"
        )

        for (refreshMask in 0..7) {
            for (deleteMask in 0..7) {
                val refreshing = numbers.filterIndexed { index, _ ->
                    refreshMask and (1 shl index) != 0
                }.toSet()

                val deleting = numbers.filterIndexed { index, _ ->
                    deleteMask and (1 shl index) != 0
                }.toSet()

                for (checking in listOf(null) + numbers) {
                    val state = PnrUiState(
                        checkingPnr = checking,
                        refreshingPnrs = refreshing,
                        deletingPnrs = deleting
                    )

                    val expected =
                        refreshing +
                                deleting +
                                listOfNotNull(checking)

                    val actual = state.busyPnrs()

                    assertEquals(
                        expected.toList(),
                        actual.toList()
                    )

                    assertEquals(
                        numbers.filter(state::isBusy).toSet(),
                        actual
                    )

                    assertFalse(
                        "2222222222" in actual
                    )
                }
            }
        }
    }

    @Test
    fun busySetDoesNotDependOnUnrelatedPresentationFlags() {
        val state = PnrUiState(
            isChecking = true,
            isLoading = true,
            addError = "Example"
        )

        assertTrue(
            state.busyPnrs().isEmpty()
        )

        val checking = state.copy(
            isChecking = false,
            checkingPnr = "0123456789"
        )

        assertEquals(
            setOf("0123456789"),
            checking.busyPnrs()
        )

        assertTrue(
            checking.isBusy("0123456789")
        )
    }
}