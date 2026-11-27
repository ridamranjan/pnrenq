package com.first.pnrenq.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.add.AddPnrScreen
import com.first.pnrenq.ui.common.pnrControlColor
import com.first.pnrenq.ui.details.DetailsScreen
import com.first.pnrenq.ui.home.HomeScreen
import com.first.pnrenq.ui.settings.SettingsScreen
import com.first.pnrenq.ui.theme.*

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrScreenReadabilityTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val appearance = ThemeSettings(
        mode = AppThemeMode.LIGHT,
        palette = AppThemePalette.AMBER,
        style = AppThemeStyle.GRADIENT,
        timeFormat = AppTimeFormat.HOUR_24
    )

    private val record = PnrInfo(
        pnrNumber = "0123456789",
        trainNumber = "12345",
        trainName = "Long Intercity Test Express",
        journeyDate = LocalDate.of(
            2099,
            1,
            1
        ),
        fromStation = "LONG BOARDING STATION",
        toStation = "LONG DESTINATION STATION",
        bookingStatus = "WL 10",
        currentStatus = "CNF",
        chartStatus = null,
        passengers = listOf(
            PassengerStatus(
                index = 12,
                bookingStatus = "WL 10",
                currentStatus = "CNF/B2/35/LB",
                coach = "B3",
                berth = "36",
                currentBerthCode = "UB"
            )
        ),
        lastCheckedAtMillis = Instant.parse(
            "2026-12-24T18:30:00Z"
        ).toEpochMilli(),
        details = PnrDetails(
            departureTime = "20:35",
            arrivalDate = "2099-01-02",
            arrivalTime = "02:33",
            duration = "05:58",
            sourceStationName = "Long source railway station",
            bookingFare = 125000.5,
            ticketFare = 1200.25,
            distanceKm = 1234.56,
            ticketType = "E-Ticket"
        )
    )

    @Test
    fun passengerAndEnabledControlContrastAcrossThemeMatrix() {
        var cases = 0

        for (mode in AppThemeMode.entries) {
            val systemChoices = if (
                mode == AppThemeMode.SYSTEM
            ) {
                listOf(false, true)
            } else {
                listOf(false)
            }

            for (systemDark in systemChoices) {
                for (palette in AppThemePalette.entries) {
                    for (style in AppThemeStyle.entries) {
                        val settings = ThemeSettings(
                            mode = mode,
                            palette = palette,
                            style = style
                        )

                        val dark = isDarkAppearance(
                            mode,
                            systemDark
                        )

                        val c = pnrColorScheme(
                            settings,
                            systemDark
                        )

                        val label =
                            "$mode/$systemDark/$palette/$style"

                        val surface = c.surfaceContainerLow

                        // Actual passenger badge backgrounds are opaque
                        // composited tints of the real card surface.
                        for (current in listOf(false, true)) {
                            val colors = passengerBadgeColors(
                                surface.toArgb(),
                                dark,
                                current
                            )

                            val ratio = pnrContrastRatio(
                                colors.foreground,
                                colors.background
                            )

                            assertTrue(
                                "Passenger contrast $ratio: " +
                                        "$label/current=$current",
                                ratio >= MIN_TEXT_CONTRAST
                            )
                        }

                        // Home action icons, Add PNR borders/cursors,
                        // and Settings radio indicators.
                        for (
                        preferred in listOf(
                            c.primary,
                            c.error,
                            c.outline,
                            c.onSurfaceVariant
                        )
                        ) {
                            assertControlContrast(
                                preferred,
                                listOf(listOf(surface)),
                                label
                            )
                        }

                        // Use the actual train-summary accent strength.
                        val accent = pnrAccentColors(
                            c.primaryContainer,
                            c.onPrimaryContainer,
                            settings,
                            dark,
                            1.15f
                        )

                        val tintedAccent = accent.map {
                            c.onPrimaryContainer
                                .copy(alpha = 0.10f)
                                .compositeOver(it)
                        }

                        // Details delete and refresh controls.
                        assertControlContrast(
                            c.error,
                            listOf(tintedAccent),
                            "$label/Details delete"
                        )

                        assertControlContrast(
                            c.onPrimaryContainer,
                            listOf(tintedAccent),
                            "$label/Details refresh"
                        )

                        // Details copy control.
                        assertControlContrast(
                            c.onPrimaryContainer,
                            listOf(accent),
                            "$label/Details copy"
                        )

                        // Home Settings icon on the accent surface.
                        val settingsAccent = pnrAccentColors(
                            c.secondaryContainer,
                            c.onSecondaryContainer,
                            settings,
                            dark,
                            1f
                        )

                        assertTrue(
                            "$label/Home Settings",
                            minimumPnrGradientContrast(
                                listOf(
                                    c.onSecondaryContainer.toArgb()
                                ),
                                settingsAccent.map {
                                    it.toArgb()
                                }
                            ) >= MIN_CONTROL_CONTRAST
                        )

                        // Settings reset outline on the screen background.
                        val screenBackgrounds =
                            pnrTextBackgroundGroups(
                                c.onBackground,
                                c,
                                settings,
                                dark
                            )

                        assertControlContrast(
                            c.outline,
                            screenBackgrounds,
                            "$label/Reset outline"
                        )

                        cases++
                    }
                }
            }
        }

        assertEquals(
            144,
            cases
        )
    }

    @Test
    fun homeHeaderFooterAndJourneyContentRemainReadableAtLargeFont() {
        var adds = 0

        val homeRecord = record.copy(
            currentStatus = "RAC 7",
            passengers = emptyList()
        )

        rule.setContent {
            NarrowScreen {
                HomeScreen(
                    pnrs = listOf(homeRecord),
                    onAddClick = {
                        adds++
                    },
                    onPnrClick = {},
                    clock = Clock.fixed(
                        Instant.parse(
                            "2026-12-25T00:00:00Z"
                        ),
                        ZoneOffset.UTC
                    )
                )
            }
        }

        assertNoOverflow(
            text = "PNR EXPERT",
            scroll = false
        )

        assertNoOverflow(
            text = "Saved journeys",
            scroll = false
        )

        assertNoOverflow(
            text = "Add PNR",
            scroll = false
        )

        val add = rule.onNodeWithText(
            "Add PNR"
        )

        add.assertHasClickAction()
            .assertIsEnabled()
            .assertHeightIsAtLeast(56.dp)
            .assertIsDisplayed()

        rule.onNodeWithContentDescription(
            "Settings"
        ).assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        val list = rule.onNode(
            hasScrollToIndexAction()
        )

        // The scrolling content must end above the fixed footer.
        val listBounds =
            list.fetchSemanticsNode().boundsInRoot

        val buttonBounds =
            add.fetchSemanticsNode().boundsInRoot

        assertTrue(
            "The Add PNR control overlaps the journey viewport",
            listBounds.bottom <= buttonBounds.top + 1f
        )

        // Index zero is the upcoming heading; index one is its saved card.
        list.performScrollToIndex(1)

        for (
        text in listOf(
            "12345 · Long Intercity Test Express",
            "LONG BOARDING STATION",
            "LONG DESTINATION STATION",
            homeRecord.pnrNumber,
            "RAC 7",
            "Checked 25 Dec, 00:00 IST"
        )
        ) {
            assertNoOverflow(
                text = text
            )
        }

        rule.onNodeWithContentDescription(
            "Copy PNR ending in 6789"
        ).assertIsEnabled()

        rule.onNodeWithContentDescription(
            "Delete saved PNR ending in 6789"
        ).assertIsEnabled()

        add.performClick()

        rule.runOnIdle {
            assertEquals(
                1,
                adds
            )
        }
    }

    @Test
    fun detailsRetainsPassengerConflictAndCompleteFareAtLargeFont() {
        rule.setContent {
            NarrowScreen {
                DetailsScreen(
                    pnr = record,
                    isRefreshing = false,
                    onRefresh = {},
                    onDelete = {},
                    onBack = {}
                )
            }
        }

        assertNoOverflow(
            text = "Ticket details",
            scroll = false
        )

        rule.onNodeWithContentDescription(
            "Navigate back"
        ).assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        for (
        description in listOf(
            "Copy PNR ending in 6789",
            "Refresh PNR ending in 6789",
            "Delete saved PNR ending in 6789"
        )
        ) {
            rule.onNodeWithContentDescription(
                description
            ).performScrollTo()
                .assertIsEnabled()
                .assertWidthIsAtLeast(48.dp)
                .assertHeightIsAtLeast(48.dp)
        }

        val current =
            "CNF/B2/35/LB (allocation differs)"

        for (
        text in listOf(
            record.pnrNumber,
            "Long Intercity Test Express",
            "P12",
            "WL 10",
            current,
            "Booking fare",
            "₹1,25,000.50",
            "Ticket fare",
            "₹1,200.25",
            "1,234.6 km",
            "Long source railway station",
            "25 Dec, 00:00 IST"
        )
        ) {
            assertNoOverflow(
                text = text
            )
        }

        rule.onNodeWithContentDescription(
            "Passenger 12"
        ).assertExists()

        rule.onNodeWithContentDescription(
            "Booking status: WL 10"
        ).assertTextEquals("WL 10")

        rule.onNodeWithContentDescription(
            "Current status: $current"
        ).assertTextEquals(current)
    }

    @Test
    fun addPnrValidationAndSubmissionRemainReadableAndFunctional() {
        val checking = mutableStateOf(false)

        var submissions = 0
        var submitted = ""

        rule.setContent {
            NarrowScreen {
                AddPnrScreen(
                    isChecking = checking.value,
                    onCheck = {
                        submitted = it
                        submissions++
                        checking.value = true
                    },
                    onBack = {}
                )
            }
        }

        assertNoOverflow(
            text = "Add PNR",
            scroll = false
        )

        assertNoOverflow(
            text = "10"
        )

        assertNoOverflow(
            text = "Track a new journey"
        )

        assertNoOverflow(
            text = "PNR NUMBER"
        )

        val input = rule.onNode(
            hasSetTextAction()
        )

        input.performScrollTo()
            .assertContentDescriptionContains(
                "PNR number, 10 digits"
            )
            .performTextReplacement("123")

        assertNoOverflow(
            text = "Check PNR status"
        )

        rule.onNodeWithText(
            "Check PNR status"
        ).performScrollTo()
            .assertHeightIsAtLeast(54.dp)
            .performClick()

        input.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.Error,
                "Enter exactly 10 digits"
            )
        )

        assertNoOverflow(
            text = "Enter exactly 10 digits"
        )

        assertNoOverflow(
            text = "3/10"
        )

        rule.runOnIdle {
            assertEquals(
                0,
                submissions
            )
        }

        input.performScrollTo()
            .performTextReplacement(
                record.pnrNumber
            )

        assertNoOverflow(
            text = "10/10"
        )

        assertNoOverflow(
            text = "Check PNR status →"
        )

        rule.onNodeWithText(
            "Check PNR status →"
        ).performScrollTo()
            .performClick()

        assertNoOverflow(
            text = "Checking PNR..."
        )

        rule.onNodeWithText(
            "Checking PNR..."
        ).assertIsNotEnabled()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "Checking PNR"
                )
            )

        rule.onNodeWithContentDescription(
            "Navigate back"
        ).assertIsNotEnabled()

        rule.runOnIdle {
            assertEquals(
                1,
                submissions
            )

            assertEquals(
                record.pnrNumber,
                submitted
            )
        }
    }

    @Test
    fun settingsPreviewAndSelectionRemainReadableAtLargeFont() {
        val settings = mutableStateOf(appearance)

        var selections = 0

        rule.setContent {
            NarrowScreen(
                settings = settings.value
            ) {
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

        assertNoOverflow(
            text = "Settings",
            scroll = false
        )

        assertNoOverflow(
            text = "LIVE PREVIEW"
        )

        assertNoOverflow(
            text = "PNR EXPERT"
        )

        assertNoOverflow(
            text = "${appearance.palette.title} · " +
                    appearance.style.title
        )

        assertNoOverflow(
            text = "Preview of the current screen background."
        )

        val appearanceHeader = rule.onNodeWithText(
            "Appearance"
        )

        appearanceHeader.performScrollTo()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "Collapsed"
                )
            )
            .performClick()

        appearanceHeader.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                "Expanded"
            )
        )

        assertNoOverflow(
            text = AppThemeMode.HIGH_CONTRAST_LIGHT.title
        )

        appearanceHeader.performScrollTo()
            .performClick()

        val radio = SemanticsMatcher.expectValue(
            SemanticsProperties.Role,
            Role.RadioButton
        )

        val option =
            hasText(AppTimeFormat.HOUR_12.title) and
                    radio and
                    hasClickAction()

        assertNoOverflow(
            text = AppTimeFormat.HOUR_12.title
        )

        rule.onNode(option)
            .performScrollTo()
            .assertIsNotSelected()
            .performClick()
            .assertIsSelected()

        rule.onAllNodes(option)
            .assertCountEquals(1)

        rule.onNode(
            hasText(AppTimeFormat.HOUR_24.title) and
                    radio and
                    hasClickAction()
        ).assertIsNotSelected()

        rule.runOnIdle {
            assertEquals(
                1,
                selections
            )

            assertEquals(
                AppTimeFormat.HOUR_12,
                settings.value.timeFormat
            )
        }

        assertNoOverflow(
            text = "Reset to Default"
        )
    }

    private fun assertControlContrast(
        preferred: Color,
        backgrounds: List<List<Color>>,
        label: String
    ) {
        val corrected = pnrControlColor(
            preferred,
            backgrounds
        )

        val ratio = minimumPnrTextContrast(
            listOf(
                corrected.toArgb()
            ),
            backgrounds.map { range ->
                range.map {
                    it.toArgb()
                }
            }
        )

        assertTrue(
            "Control contrast $ratio: $label/$preferred",
            ratio >= MIN_CONTROL_CONTRAST
        )
    }

    @Composable
    private fun NarrowScreen(
        settings: ThemeSettings = appearance,
        content: @Composable () -> Unit
    ) {
        val density = LocalDensity.current.density

        CompositionLocalProvider(
            LocalDensity provides Density(
                density,
                2f
            )
        ) {
            PnrenqTheme(settings) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                    ) {
                        CompositionLocalProvider(
                            LocalPnrTimeFormat provides
                                    settings.timeFormat
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }

    /**
     * Inspect the measured text rather than accepting the presence of
     * a semantics string as proof of visual readability.
     *
     * The accepted full-layout callback behavior must remain in place.
     * All values in these assertions are synthetic.
     */
    private fun assertNoOverflow(
        text: String,
        scroll: Boolean = true
    ) {
        val node = rule.onNodeWithText(
            text,
            useUnmergedTree = true
        )

        node.assertExists()

        if (scroll) {
            node.performScrollTo()
        }

        rule.waitForIdle()

        node.assertIsDisplayed()

        val results = mutableListOf<TextLayoutResult>()

        node.performSemanticsAction(
            SemanticsActions.GetTextLayoutResult
        ) { action ->
            assertTrue(
                "Unable to obtain text layout: $text",
                action(results)
            )
        }

        assertTrue(
            "No measured layout: $text",
            results.isNotEmpty()
        )

        for (result in results) {
            val diagnostics =
                "Text: $text\n" +
                        "Font scale: ${result.layoutInput.density.fontScale}\n" +
                        "Constraints: ${result.layoutInput.constraints}\n" +
                        "Measured size: ${result.size}\n" +
                        "Paragraph width: ${result.multiParagraph.width}\n" +
                        "Paragraph height: ${result.multiParagraph.height}\n" +
                        "Horizontal overflow: ${result.didOverflowWidth}\n" +
                        "Vertical overflow: ${result.didOverflowHeight}\n" +
                        "Line count: ${result.lineCount}"

            assertFalse(
                diagnostics,
                result.hasVisualOverflow
            )

            assertEquals(
                diagnostics,
                text,
                result.layoutInput.text.text
            )

            assertEquals(
                diagnostics,
                2f,
                result.layoutInput.density.fontScale,
                0.001f
            )

            assertTrue(
                diagnostics,
                result.lineCount > 0
            )

            for (line in 0 until result.lineCount) {
                assertFalse(
                    "Ellipsized line $line\n$diagnostics",
                    result.isLineEllipsized(line)
                )
            }

            assertEquals(
                "Incomplete text coverage\n$diagnostics",
                text.length,
                result.getLineEnd(
                    result.lineCount - 1,
                    visibleEnd = false
                )
            )
        }
    }
}