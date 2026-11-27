package com.first.pnrenq.ui.details

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.theme.AppTimeFormat
import com.first.pnrenq.ui.theme.LocalPnrTimeFormat
import com.first.pnrenq.ui.theme.PnrenqTheme

import java.time.Instant
import java.time.LocalDate

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailsFareIntegrationTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val base = PnrInfo(
        pnrNumber = "1234567890",
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2026, 12, 25),
        fromStation = "ABC",
        toStation = "XYZ",
        bookingStatus = "WL 10",
        currentStatus = "CNF",
        chartStatus = null,
        passengers = listOf(
            PassengerStatus(
                index = 1,
                bookingStatus = "WL 10",
                currentStatus = "CNF",
                coach = "B2",
                berth = "35",
                currentBerthNo = 35,
                currentBerthCode = "LB"
            )
        ),
        lastCheckedAtMillis = Instant.parse(
            "2026-12-24T18:30:00Z"
        ).toEpochMilli()
    )

    @Test
    fun distinctFaresAndDistancePreserveTimeAndAllocationPresentation() {
        showDetails(
            mutableStateOf(
                base.copy(
                    details = PnrDetails(
                        departureTime = "20:35",
                        arrivalTime = "02:33",
                        arrivalDate = "26-12-2026",
                        distanceKm = 1234.56,
                        bookingFare = 1250.75,
                        ticketFare = 1200.5
                    )
                )
            )
        )

        assertVisible("20:35")
        assertVisible("CNF B2 35 LB")
        assertVisible("Confirmed")
        assertVisible("1,234.6 km")

        assertVisible("Booking fare")
        assertVisible("₹1,250.75")

        assertVisible("Ticket fare")
        assertVisible("₹1,200.50")

        assertVisible("25 Dec, 00:00 IST")

        rule.onNodeWithText(
            "WL 10"
        ).assertDoesNotExist()
    }

    @Test
    fun duplicateRoundedFaresAndInvalidBookingFallbackUpdateCorrectly() {
        val record = mutableStateOf(
            base.copy(
                details = PnrDetails(
                    bookingFare = 450.501,
                    ticketFare = 450.504
                )
            )
        )

        showDetails(record)

        assertVisible("Booking fare")
        assertVisible("₹450.50")

        rule.onAllNodesWithText(
            "₹450.50"
        ).assertCountEquals(1)

        rule.onNodeWithText(
            "Ticket fare"
        ).assertDoesNotExist()

        rule.runOnIdle {
            record.value = base.copy(
                details = PnrDetails(
                    bookingFare = Double.NaN,
                    ticketFare = 650.25
                )
            )
        }

        assertVisible("Ticket fare")
        assertVisible("₹650.25")

        rule.onNodeWithText(
            "Booking fare"
        ).assertDoesNotExist()

        rule.onNodeWithText(
            "₹450.50"
        ).assertDoesNotExist()
    }

    @Test
    fun invalidNumericValuesDoNotHideOtherTicketInformation() {
        val record = mutableStateOf(
            base.copy(
                details = PnrDetails(
                    distanceKm = Double.POSITIVE_INFINITY,
                    bookingFare = -5.0,
                    ticketFare = Double.NaN,
                    ticketType = "E-Ticket"
                )
            )
        )

        showDetails(record)

        assertVisible("Ticket information")
        assertVisible("Ticket type")
        assertVisible("E-Ticket")

        rule.onNodeWithText(
            "Distance"
        ).assertDoesNotExist()

        rule.onNodeWithText(
            "Booking fare"
        ).assertDoesNotExist()

        rule.onNodeWithText(
            "Ticket fare"
        ).assertDoesNotExist()

        rule.onNodeWithText(
            "NaN",
            substring = true
        ).assertDoesNotExist()

        rule.onNodeWithText(
            "Infinity",
            substring = true
        ).assertDoesNotExist()

        rule.runOnIdle {
            record.value = base
        }

        rule.onNodeWithText(
            "Ticket information"
        ).assertDoesNotExist()
    }

    private fun showDetails(
        record: androidx.compose.runtime.State<PnrInfo>
    ) {
        rule.setContent {
            PnrenqTheme {
                CompositionLocalProvider(
                    LocalPnrTimeFormat provides AppTimeFormat.HOUR_24
                ) {
                    DetailsScreen(
                        pnr = record.value,
                        isRefreshing = false,
                        onRefresh = {},
                        onDelete = {},
                        onBack = {}
                    )
                }
            }
        }
    }

    private fun assertVisible(text: String) {
        rule.onNodeWithText(text)
            .performScrollTo()
            .assertIsDisplayed()
    }
}