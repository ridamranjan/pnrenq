package com.first.pnrenq.ui.common

import com.first.pnrenq.domain.model.PnrDetails
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class PnrNumericFormattingTest {

    @Test
    fun wholeAndFractionalFaresPreservePaise() {
        assertEquals(
            "₹450",
            formatFare(450.0)
        )

        assertEquals(
            "₹450.50",
            formatFare(450.5)
        )

        assertEquals(
            "₹450.75",
            formatFare(450.75)
        )

        assertEquals(
            "₹1,250.25",
            formatFare(1250.25)
        )

        assertEquals(
            "₹0.01",
            formatFare(0.01)
        )
    }

    @Test
    fun groupingUsesIndianLakhAndCroreSeparators() {
        assertEquals(
            "₹999",
            formatFare(999.0)
        )

        assertEquals(
            "₹1,000",
            formatFare(1000.0)
        )

        assertEquals(
            "₹99,999.50",
            formatFare(99999.5)
        )

        assertEquals(
            "₹1,00,000",
            formatFare(100000.0)
        )

        assertEquals(
            "₹1,25,000.50",
            formatFare(125000.5)
        )

        assertEquals(
            "₹1,23,45,678.90",
            formatFare(12345678.9)
        )
    }

    @Test
    fun halfUpRoundingHandlesPaiseAndGroupingCarry() {
        assertEquals(
            "₹1.01",
            formatFare(1.005)
        )

        assertEquals(
            "₹1",
            formatFare(1.004)
        )

        assertEquals(
            "₹1,00,000",
            formatFare(99999.995)
        )

        assertEquals(
            "₹0.30",
            formatFare(0.1 + 0.2)
        )

        assertEquals(
            "₹0",
            formatFare(0.004)
        )
    }

    @Test
    fun invalidAndMissingValuesAreNotDisplayed() {
        for (
        value in listOf(
            null,
            -1.0,
            -0.001,
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        )
        ) {
            assertNull(
                formatFare(value)
            )

            assertNull(
                formatDistanceKm(value)
            )
        }
    }

    @Test
    fun zeroAndNegativeZeroAreValidWithoutNegativeSign() {
        assertEquals(
            "₹0",
            formatFare(0.0)
        )

        assertEquals(
            "₹0",
            formatFare(-0.0)
        )

        assertEquals(
            "0 km",
            formatDistanceKm(-0.0)
        )

        assertEquals(
            FarePresentation(
                "Booking fare",
                "₹0",
                "₹10"
            ),
            farePresentation(
                PnrDetails(
                    bookingFare = 0.0,
                    ticketFare = 10.0
                )
            )
        )
    }

    @Test
    fun equalFaresAreShownOnceAtPaisePrecision() {
        val expected = FarePresentation(
            "Booking fare",
            "₹450.50"
        )

        assertEquals(
            expected,
            farePresentation(
                PnrDetails(
                    bookingFare = 450.5,
                    ticketFare = 450.5
                )
            )
        )

        assertEquals(
            expected,
            farePresentation(
                PnrDetails(
                    bookingFare = 450.501,
                    ticketFare = 450.504
                )
            )
        )
    }

    @Test
    fun differingFaresAreRetainedWithoutCalculatingDifference() {
        assertEquals(
            FarePresentation(
                "Booking fare",
                "₹450.50",
                "₹450.51"
            ),
            farePresentation(
                PnrDetails(
                    bookingFare = 450.5,
                    ticketFare = 450.51
                )
            )
        )

        assertEquals(
            FarePresentation(
                "Booking fare",
                "₹500",
                "₹400"
            ),
            farePresentation(
                PnrDetails(
                    bookingFare = 500.0,
                    ticketFare = 400.0
                )
            )
        )
    }

    @Test
    fun missingOrInvalidBookingFareFallsBackToCorrectlyLabelledTicketFare() {
        for (
        booking in listOf(
            null,
            -1.0,
            Double.NaN,
            Double.POSITIVE_INFINITY
        )
        ) {
            assertEquals(
                FarePresentation(
                    "Ticket fare",
                    "₹650.25"
                ),
                farePresentation(
                    PnrDetails(
                        bookingFare = booking,
                        ticketFare = 650.25
                    )
                )
            )
        }
    }

    @Test
    fun missingOrInvalidTicketFareDoesNotHideBookingFare() {
        for (
        ticket in listOf(
            null,
            -1.0,
            Double.NaN,
            Double.NEGATIVE_INFINITY
        )
        ) {
            assertEquals(
                FarePresentation(
                    "Booking fare",
                    "₹600"
                ),
                farePresentation(
                    PnrDetails(
                        bookingFare = 600.0,
                        ticketFare = ticket
                    )
                )
            )
        }
    }

    @Test
    fun bothMissingOrInvalidFaresProduceNoFarePresentation() {
        assertNull(
            farePresentation(
                PnrDetails()
            )
        )

        assertNull(
            farePresentation(
                PnrDetails(
                    bookingFare = -5.0,
                    ticketFare = Double.NaN
                )
            )
        )

        assertNull(
            farePresentation(
                PnrDetails(
                    bookingDate = "2026-12-25",
                    ticketType = "E-Ticket"
                )
            )
        )
    }

    @Test
    fun distanceRetainsOneDecimalPrecisionWithoutCurrencyRules() {
        assertEquals(
            "850 km",
            formatDistanceKm(850.0)
        )

        assertEquals(
            "1,234.6 km",
            formatDistanceKm(1234.56)
        )

        assertEquals(
            "100 km",
            formatDistanceKm(99.95)
        )

        assertEquals(
            "1,00,000.1 km",
            formatDistanceKm(100000.05)
        )

        assertEquals(
            "0 km",
            formatDistanceKm(0.04)
        )
    }

    @Test
    fun extremeFiniteValuesDoNotOverflowOrUseScientificNotation() {
        val fare = formatFare(
            Double.MAX_VALUE
        )!!

        assertFalse(
            fare.contains('E')
        )

        assertEquals(
            309,
            fare.count {
                it in '0'..'9'
            }
        )

        assertTrue(
            fare.removePrefix("₹").matches(
                Regex(
                    "[0-9]{1,2}(,[0-9]{2})*,[0-9]{3}"
                )
            )
        )

        assertEquals(
            BigDecimal.valueOf(
                Double.MAX_VALUE
            ).toPlainString(),
            fare.removePrefix("₹")
                .replace(",", "")
        )

        assertEquals(
            "₹0",
            formatFare(Double.MIN_VALUE)
        )

        assertEquals(
            "0 km",
            formatDistanceKm(Double.MIN_VALUE)
        )
    }
}