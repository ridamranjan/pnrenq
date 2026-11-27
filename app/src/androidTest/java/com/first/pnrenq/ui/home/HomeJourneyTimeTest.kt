package com.first.pnrenq.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.theme.PnrenqTheme

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeJourneyTimeTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val departure = Instant.parse(
        "2026-12-25T15:05:00Z"
    )

    private val records = listOf(
        PnrInfo(
            pnrNumber = "1234567890",
            trainNumber = "12345",
            trainName = "Test Express",
            journeyDate = LocalDate.of(2026, 12, 25),
            fromStation = "ABC",
            toStation = "XYZ",
            bookingStatus = "WL 5",
            currentStatus = "CNF",
            chartStatus = null,
            passengers = emptyList(),
            lastCheckedAtMillis = 1000L,
            details = PnrDetails(
                departureTime = "20:35"
            )
        )
    )

    @Test
    fun activeHomeMovesJourneyWithoutChangingRecords() {
        val time = AtomicReference(
            departure.minusMillis(100)
        )

        showHome(time)

        rule.onNodeWithText(
            "Upcoming journeys"
        ).assertExists()

        // Advance only the injected wall clock, not the saved data or UI state.
        time.set(departure)

        assertPastJourney()
    }

    @Test
    fun resumedHomeReclassifiesUsingCurrentRailwayTime() {
        val time = AtomicReference(
            departure.minusSeconds(3600)
        )

        showHome(time)

        rule.onNodeWithText(
            "Upcoming journeys"
        ).assertExists()

        val scenario = rule.activityRule.scenario

        scenario.moveToState(
            Lifecycle.State.CREATED
        )

        try {
            time.set(departure)
        } finally {
            scenario.moveToState(
                Lifecycle.State.RESUMED
            )
        }

        assertPastJourney()
    }

    private fun showHome(
        time: AtomicReference<Instant>
    ) {
        val clock = MutableClock(time)

        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = records,
                    onAddClick = {},
                    onPnrClick = {},
                    clock = clock
                )
            }
        }
    }

    private fun assertPastJourney() {
        rule.waitUntil(
            timeoutMillis = 5_000L
        ) {
            rule.onAllNodesWithText(
                "Past journeys"
            ).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText(
            "Upcoming journeys"
        ).assertDoesNotExist()

        rule.onNodeWithText(
            "Past journeys"
        ).assertExists()
    }

    private class MutableClock(
        private val time: AtomicReference<Instant>,
        private val clockZone: ZoneId = ZoneOffset.UTC
    ) : Clock() {

        override fun instant(): Instant =
            time.get()

        override fun getZone(): ZoneId =
            clockZone

        override fun withZone(zone: ZoneId): Clock =
            MutableClock(time, zone)
    }
}