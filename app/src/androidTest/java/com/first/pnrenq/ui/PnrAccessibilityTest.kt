package com.first.pnrenq.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.add.AddPnrScreen
import com.first.pnrenq.ui.common.PnrLoadWarning
import com.first.pnrenq.ui.details.DetailsScreen
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.settings.SettingsScreen
import com.first.pnrenq.ui.theme.AppTimeFormat
import com.first.pnrenq.ui.theme.PnrenqTheme
import com.first.pnrenq.ui.theme.ThemeSettings

import java.time.LocalDate

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrAccessibilityTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val record = PnrInfo(
        pnrNumber = "1234567890",
        trainNumber = "12345",
        trainName = "Test Express",
        journeyDate = LocalDate.of(2099, 1, 1),
        fromStation = "ABC",
        toStation = "XYZ",
        bookingStatus = "WL 10",
        currentStatus = "RAC 7",
        chartStatus = null,
        passengers = listOf(
            PassengerStatus(
                index = 1,
                bookingStatus = "WL 10",
                currentStatus = "RAC 7",
                coach = null,
                berth = null
            )
        ),
        lastCheckedAtMillis = 1000L
    )

    @Test
    fun homeActionsHaveMaskedDistinctLabelsAndMinimumSize() {
        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = listOf(
                        record,
                        record.copy(
                            pnrNumber = "9876543210"
                        )
                    ),
                    onAddClick = {},
                    onPnrClick = {}
                )
            }
        }

        for (suffix in listOf("7890", "3210")) {
            for (
            label in listOf(
                "Copy PNR ending in $suffix",
                "Delete saved PNR ending in $suffix"
            )
            ) {
                rule.onNode(hasScrollAction())
                    .performScrollToNode(
                        hasContentDescription(label)
                    )

                rule.onNodeWithContentDescription(label)
                    .assertIsEnabled()
                    .assertHasClickAction()
                    .assertWidthIsAtLeast(48.dp)
                    .assertHeightIsAtLeast(48.dp)
            }
        }

        rule.onNodeWithContentDescription("Settings")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun journeySectionAnnouncesExpansionAndRemainsOperable() {
        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = listOf(record),
                    onAddClick = {},
                    onPnrClick = {}
                )
            }
        }

        val section = rule.onNodeWithText(
            "Upcoming journeys"
        )

        section.assert(
            SemanticsMatcher.keyIsDefined(
                SemanticsProperties.Heading
            )
        ).assert(
            state("Expanded")
        ).performClick()

        section.assert(
            state("Collapsed")
        )

        rule.onNodeWithContentDescription(
            "Copy PNR ending in 7890"
        ).assertDoesNotExist()

        section.performClick()
            .assert(state("Expanded"))

        rule.onNodeWithContentDescription(
            "Copy PNR ending in 7890"
        ).assertExists()
    }

    @Test
    fun deletionRetainsActionIdentityWhileShowingProgress() {
        val deleting = mutableStateOf(
            setOf(record.pnrNumber)
        )

        rule.setContent {
            PnrenqTheme {
                HomeScreen(
                    pnrs = listOf(record),
                    onAddClick = {},
                    onPnrClick = {},
                    deletingPnrs = deleting.value
                )
            }
        }

        val delete = rule.onNodeWithContentDescription(
            "Delete saved PNR ending in 7890"
        )

        delete.assertIsNotEnabled()
            .assert(
                state("Removing saved PNR")
            )

        rule.runOnIdle {
            deleting.value = emptySet()
        }

        delete.assertIsEnabled()

        delete.assert(
            !SemanticsMatcher.keyIsDefined(
                SemanticsProperties.StateDescription
            )
        )
    }

    @Test
    fun detailsActionsAndPassengerRolesAreAccessible() {
        val busy = mutableStateOf(false)

        rule.setContent {
            PnrenqTheme {
                DetailsScreen(
                    pnr = record,
                    isRefreshing = busy.value,
                    onRefresh = {},
                    onDelete = {},
                    onBack = {},
                    isBusy = busy.value
                )
            }
        }

        for (
        label in listOf(
            "Copy PNR ending in 7890",
            "Refresh PNR ending in 7890",
            "Delete saved PNR ending in 7890",
            "Navigate back"
        )
        ) {
            rule.onNodeWithContentDescription(label)
                .assertIsEnabled()
                .assertWidthIsAtLeast(48.dp)
                .assertHeightIsAtLeast(48.dp)
        }

        rule.onNodeWithContentDescription(
            "Passenger 1"
        ).assertExists()

        rule.onNodeWithContentDescription(
            "Booking status: WL 10"
        ).assertTextEquals("WL 10")

        rule.onNodeWithContentDescription(
            "Current status: RAC 7"
        ).assertTextEquals("RAC 7")

        rule.runOnIdle {
            busy.value = true
        }

        rule.onNodeWithContentDescription(
            "Refresh PNR ending in 7890"
        ).assertIsNotEnabled()
            .assert(
                state("PNR operation in progress")
            )

        rule.onNodeWithContentDescription(
            "Delete saved PNR ending in 7890"
        ).assertIsNotEnabled()
            .assert(
                state("PNR operation in progress")
            )
    }

    @Test
    fun addInputExposesValidationAndCheckingState() {
        val checking = mutableStateOf(false)
        var submitted = ""

        rule.setContent {
            PnrenqTheme {
                AddPnrScreen(
                    isChecking = checking.value,
                    onCheck = {
                        submitted = it
                        checking.value = true
                    },
                    onBack = {}
                )
            }
        }

        val input = rule.onNode(
            hasSetTextAction()
        )

        input.assertContentDescriptionContains(
            "PNR number, 10 digits"
        )

        input.performTextReplacement("123")

        rule.onNodeWithText(
            "Check PNR status"
        ).performScrollTo()
            .performClick()

        input.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.Error,
                "Enter exactly 10 digits"
            )
        )

        rule.runOnIdle {
            assertEquals("", submitted)
        }

        input.performScrollTo()
            .performTextReplacement(
                record.pnrNumber
            )

        input.assert(
            !SemanticsMatcher.keyIsDefined(
                SemanticsProperties.Error
            )
        )

        rule.onNodeWithText(
            "Check PNR status →"
        ).performScrollTo()
            .performClick()

        rule.onNodeWithText(
            "Checking PNR..."
        ).assertIsNotEnabled()
            .assert(
                state("Checking PNR")
            )

        rule.runOnIdle {
            assertEquals(
                record.pnrNumber,
                submitted
            )
        }
    }

    @Test
    fun settingsExposeExpansionAndSingleSelectableOption() {
        val settings = mutableStateOf(
            ThemeSettings()
        )

        var selections = 0

        rule.setContent {
            PnrenqTheme {
                SettingsScreen(
                    settings = settings.value,
                    onModeSelected = {},
                    onPaletteSelected = {},
                    onStyleSelected = {},
                    onTimeFormatSelected = {
                        selections++

                        settings.value = settings.value.copy(
                            timeFormat = it
                        )
                    },
                    onReset = {},
                    onBack = {},
                    onPoliciesClick = {},
                    onReportBug = {},
                    onViewSource = {}
                )
            }
        }

        val appearance = rule.onNodeWithText(
            "Appearance"
        )

        appearance.performScrollTo()
            .assert(state("Collapsed"))
            .performClick()

        appearance.assert(state("Expanded"))
            .performClick()

        appearance.assert(state("Collapsed"))

        val radio = SemanticsMatcher.expectValue(
            SemanticsProperties.Role,
            Role.RadioButton
        )

        val option =
            hasText("12-hour") and radio and hasClickAction()

        rule.onNode(option)
            .performScrollTo()
            .assertIsNotSelected()
            .performClick()
            .assertIsSelected()

        rule.onAllNodes(option)
            .assertCountEquals(1)

        rule.onNode(
            hasText("24-hour") and radio and hasClickAction()
        ).assertIsNotSelected()

        rule.runOnIdle {
            assertEquals(1, selections)

            assertEquals(
                AppTimeFormat.HOUR_12,
                settings.value.timeFormat
            )
        }
    }

    @Test
    fun persistentWarningAnnouncesMessageAndRetainsRetryState() {
        val loading = mutableStateOf(false)
        var retries = 0

        rule.setContent {
            PnrenqTheme {
                PnrLoadWarning(
                    message = "Unable to read saved PNRs",
                    isLoading = loading.value,
                    onRetry = {
                        retries++
                        loading.value = true
                    }
                )
            }
        }

        rule.onNodeWithText(
            "Unable to read saved PNRs"
        ).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.LiveRegion,
                LiveRegionMode.Polite
            )
        )

        val retry = rule.onNodeWithText(
            "Try again"
        )

        retry.assertIsEnabled()
            .performClick()

        retry.assertIsNotEnabled()
            .assert(
                state("Loading saved PNRs")
            )

        rule.runOnIdle {
            assertEquals(1, retries)
        }
    }

    private fun state(value: String) =
        SemanticsMatcher.expectValue(
            SemanticsProperties.StateDescription,
            value
        )
}