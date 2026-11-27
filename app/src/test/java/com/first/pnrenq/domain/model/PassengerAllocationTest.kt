package com.first.pnrenq.domain.model

import org.junit.Assert.*
import org.junit.Test

class PassengerAllocationTest {

    private val base = PassengerStatus(
        1,
        "WL 10",
        "CNF",
        null,
        null
    )

    @Test
    fun confirmedStructuredAllocationHidesBookingOnlyWhenComplete() {
        assertEquals(
            PassengerStatusPresentation(
                null,
                "CNF B2 35 LB"
            ),
            passengerStatusPresentation(
                base.copy(
                    coach = "B2",
                    currentBerthNo = 35,
                    currentBerthCode = "LB"
                )
            )
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "CNF B2"
            ),
            passengerStatusPresentation(
                base.copy(
                    coach = "B2"
                )
            )
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "CNF"
            ),
            passengerStatusPresentation(
                base.copy(
                    currentBerthNo = 35
                )
            )
        )
    }

    @Test
    fun embeddedAllocationAndRepeatedStructuredFieldsAreNotDuplicated() {
        val p = base.copy(
            currentStatus = "CNF/B2/35/LB/GN",
            coach = "b2",
            berth = "35",
            currentBerthNo = 35,
            currentBerthCode = "Lower Berth"
        )

        assertEquals(
            PassengerStatusPresentation(
                null,
                "CNF B2 35 LB GN"
            ),
            passengerStatusPresentation(p)
        )
    }

    @Test
    fun legacyBerthNumberAndTypeFallbackRemainSupported() {
        assertEquals(
            "CNF B2 35",
            passengerStatusPresentation(
                base.copy(
                    coach = "B2",
                    berth = "35"
                )
            ).current
        )

        assertEquals(
            "CNF B2 35 UB",
            passengerStatusPresentation(
                base.copy(
                    coach = "B2",
                    currentBerthNo = 35,
                    berth = "UB"
                )
            ).current
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "CNF B2"
            ),
            passengerStatusPresentation(
                base.copy(
                    coach = "B2",
                    berth = "LB"
                )
            )
        )
    }

    @Test
    fun implicitRacSeatAndExplicitRacPreserveBookingHistory() {
        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "RAC R1 35"
            ),
            passengerStatusPresentation(
                base.copy(
                    currentStatus = "R1 35"
                )
            )
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "RAC 7 B2 35 LB"
            ),
            passengerStatusPresentation(
                base.copy(
                    currentStatus = "RAC 7",
                    coach = "B2",
                    currentBerthNo = 35,
                    currentBerthCode = "LB"
                )
            )
        )
    }

    @Test
    fun waitlistAndCancellationNeverAcquireStaleAllocation() {
        for (
        status in listOf(
            "WL 3",
            "CAN",
            "WEBCANRF",
            "X9"
        )
        ) {
            val display = passengerStatusPresentation(
                base.copy(
                    currentStatus = status,
                    coach = "B2",
                    berth = "35",
                    currentBerthCode = "LB"
                )
            )

            assertEquals(
                PassengerStatusPresentation(
                    "WL 10",
                    status
                ),
                display
            )
        }
    }

    @Test
    fun conflictingAllocationRetainsBookingAndSignalsUncertainty() {
        val p = base.copy(
            currentStatus = "CNF/B2/35/LB",
            coach = "B3",
            berth = "36",
            currentBerthCode = "UB"
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "CNF/B2/35/LB (allocation differs)"
            ),
            passengerStatusPresentation(p)
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "CNF (allocation differs)"
            ),
            passengerStatusPresentation(
                base.copy(
                    coach = "B2",
                    currentBerthNo = 35,
                    berth = "36"
                )
            )
        )
    }

    @Test
    fun invalidCoachAndNonpositiveBerthDoNotHideBooking() {
        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "CNF"
            ),
            passengerStatusPresentation(
                base.copy(
                    coach = "N/A",
                    currentBerthNo = 35
                )
            )
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "CNF B2"
            ),
            passengerStatusPresentation(
                base.copy(
                    coach = "B2",
                    currentBerthNo = 0,
                    berth = "-1"
                )
            )
        )

        for (
        coach in listOf(
            "WL5",
            "RAC7",
            "B0"
        )
        ) {
            assertEquals(
                PassengerStatusPresentation(
                    "WL 10",
                    "CNF"
                ),
                passengerStatusPresentation(
                    base.copy(
                        coach = coach,
                        currentBerthNo = 35
                    )
                )
            )
        }
    }

    @Test
    fun currentUnknownIsExplicitAndBookingAllocationUsesOnlyBookingFields() {
        val p = base.copy(
            bookingStatus = "RAC 7",
            bookingCoach = "B1",
            bookingBerthNo = 7,
            bookingBerthCode = "LB",
            currentStatus = "N/A",
            coach = "B2",
            currentBerthNo = 35
        )

        assertEquals(
            PassengerStatusPresentation(
                "RAC 7 B1 7 LB",
                "Unknown"
            ),
            passengerStatusPresentation(p)
        )
    }

    @Test
    fun structuredStatusWithAllocationOnlyAndMissingRawTextAreDisplayed() {
        assertEquals(
            PassengerStatusPresentation(
                null,
                "CNF B2 35 LB"
            ),
            passengerStatusPresentation(
                base.copy(
                    currentStatus = "B2 35 LB",
                    currentStatusCode = "CNF"
                )
            )
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "RAC"
            ),
            passengerStatusPresentation(
                base.copy(
                    currentStatus = "N/A",
                    currentStatusCode = "RAC"
                )
            )
        )

        assertEquals(
            PassengerStatusPresentation(
                "WL 10",
                "WL 5 (GNWL)"
            ),
            passengerStatusPresentation(
                base.copy(
                    currentStatus = "WL 5",
                    currentStatusCode = "GNWL"
                )
            )
        )
    }
}