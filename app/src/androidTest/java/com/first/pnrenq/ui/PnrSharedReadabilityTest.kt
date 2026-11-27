package com.first.pnrenq.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.ui.common.JourneyTimeline
import com.first.pnrenq.ui.common.PnrNumberBubble
import com.first.pnrenq.ui.common.StatusChip
import com.first.pnrenq.ui.details.ReservationInformationCard
import com.first.pnrenq.ui.details.reservationGlossary
import com.first.pnrenq.ui.theme.*

import java.time.LocalDate

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrSharedReadabilityTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun knownTextRolesAndAccentStopsMeetContrastAcrossThemeMatrix() {
        var cases = 0
        var gradients = 0

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

                        val roles = listOf(
                            c.onBackground,
                            c.onSurface,
                            c.onSurfaceVariant,
                            c.onPrimary,
                            c.onPrimaryContainer,
                            c.onSecondary,
                            c.onSecondaryContainer,
                            c.onTertiary,
                            c.onTertiaryContainer,
                            c.onError,
                            c.onErrorContainer,
                            c.inverseOnSurface,
                            c.primary,
                            c.error
                        ).distinct()

                        for (role in roles) {
                            val background =
                                pnrTextBackgroundGroups(
                                    role,
                                    c,
                                    settings,
                                    dark
                                )

                            assertTrue(
                                "Missing references: $label/$role",
                                background.isNotEmpty()
                            )

                            val text = pnrReadableTextColors(
                                role,
                                c,
                                settings,
                                dark
                            )

                            val ratio = minimumPnrTextContrast(
                                text.map { it.toArgb() },
                                background.map { range ->
                                    range.map { it.toArgb() }
                                }
                            )

                            assertTrue(
                                "Text contrast $ratio: $label/$role",
                                ratio >= MIN_TEXT_CONTRAST
                            )

                            if (
                                style == AppThemeStyle.SIMPLIFIED
                            ) {
                                assertEquals(
                                    1,
                                    text.size
                                )
                            }

                            if (text.size > 1) {
                                gradients++
                            }
                        }

                        for (
                        (background, foreground) in listOf(
                            c.primaryContainer to
                                    c.onPrimaryContainer,

                            c.secondaryContainer to
                                    c.onSecondaryContainer,

                            c.tertiaryContainer to
                                    c.onTertiaryContainer,

                            c.surfaceContainerLow to
                                    c.onSurface
                        )
                        ) {
                            val stops = pnrAccentColors(
                                background,
                                foreground,
                                settings,
                                dark,
                                1.5f
                            )

                            assertTrue(
                                "Accent contrast: $label",
                                minimumPnrGradientContrast(
                                    listOf(foreground.toArgb()),
                                    stops.map { it.toArgb() }
                                ) >= MIN_TEXT_CONTRAST
                            )

                            if (
                                style == AppThemeStyle.SIMPLIFIED
                            ) {
                                assertEquals(
                                    listOf(background),
                                    stops
                                )
                            }
                        }

                        cases++
                    }
                }
            }
        }

        assertEquals(
            144,
            cases
        )

        assertTrue(
            "Gradient text must remain available",
            gradients > 0
        )
    }

    @Test
    fun fixedGlossaryBadgeColorsRemainReadable() {
        assertEquals(
            23,
            reservationGlossary.size
        )

        for (meaning in reservationGlossary.values) {
            assertTrue(
                meaning.code,
                pnrContrastRatio(
                    meaning.badgeForeground.toArgb(),
                    meaning.badgeBackground.toArgb()
                ) >= MIN_TEXT_CONTRAST
            )
        }
    }

    @Test
    fun pnrAndStatusWrapWithoutDiscardingTextAtLargerScales() {
        val scale = mutableStateOf(1f)

        val pnr = "0123456789"

        val status =
            "CNF/B2/35/LB (allocation differs)"

        rule.setContent {
            NarrowContent(scale.value) {
                PnrNumberBubble(pnr)
                StatusChip(status)
            }
        }

        for (
        value in listOf(
            1f,
            1.3f,
            1.5f,
            2f
        )
        ) {
            rule.runOnIdle {
                scale.value = value
            }

            rule.waitForIdle()

            assertNoOverflow(
                text = pnr,
                expectedFontScale = value
            )

            assertNoOverflow(
                text = status,
                expectedFontScale = value
            )
        }
    }

    @Test
    fun railwayTimelineRetainsBothStationsAndTimesAtLargeFont() {
        rule.setContent {
            NarrowContent(2f) {
                JourneyTimeline(
                    fromStation = "LONG BOARDING STATION",
                    toStation = "LONG DESTINATION STATION",
                    journeyDate = LocalDate.of(
                        2026,
                        12,
                        25
                    ),
                    departureTime = "20:35",
                    arrivalDate = "26-12-2026",
                    arrivalTime = "02:33",
                    duration = "05:58"
                )
            }
        }

        for (
        text in listOf(
            "20:35",
            "02:33",
            "LONG BOARDING STATION",
            "LONG DESTINATION STATION",
            "5h 58m"
        )
        ) {
            assertNoOverflow(
                text = text,
                expectedFontScale = 2f
            )
        }
    }

    @Test
    fun longGlossaryCodeAndExplanationRemainReadableAtLargeFont() {
        val pnr = PnrInfo(
            pnrNumber = "1234567890",
            trainNumber = "12345",
            trainName = "Test Express",
            journeyDate = LocalDate.of(
                2026,
                12,
                25
            ),
            fromStation = "ABC",
            toStation = "XYZ",
            bookingStatus = "CNF",
            currentStatus = "WEBCANRF",
            chartStatus = null,
            passengers = emptyList(),
            lastCheckedAtMillis = 1000L
        )

        rule.setContent {
            NarrowContent(2f) {
                ReservationInformationCard(pnr)
            }
        }

        val meaning =
            reservationGlossary.getValue("WEBCANRF")

        assertNoOverflow(
            text = meaning.code,
            expectedFontScale = 2f
        )

        assertNoOverflow(
            text = meaning.title,
            expectedFontScale = 2f
        )

        assertNoOverflow(
            text = meaning.explanation,
            expectedFontScale = 2f
        )
    }

    @Composable
    private fun NarrowContent(
        scale: Float,
        content: @Composable () -> Unit
    ) {
        val density = LocalDensity.current.density

        CompositionLocalProvider(
            LocalDensity provides Density(
                density,
                scale
            )
        ) {
            PnrenqTheme(
                ThemeSettings(
                    style = AppThemeStyle.GRADIENT
                )
            ) {
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    CompositionLocalProvider(
                        LocalPnrTimeFormat provides
                                AppTimeFormat.HOUR_24
                    ) {
                        content()
                    }
                }
            }
        }
    }

    /**
     * Keep the strict overflow assertion and inspect the actual text layout.
     *
     * Include detailed dimensions in assertion failures so a remaining
     * genuine layout problem can be distinguished from an inaccurate
     * reconstructed semantics result.
     *
     * All text used by this test class is synthetic.
     */
    private fun assertNoOverflow(
        text: String,
        expectedFontScale: Float
    ) {
        rule.waitForIdle()

        val node = rule.onNodeWithText(
            text,
            useUnmergedTree = true
        )

        val results = mutableListOf<TextLayoutResult>()

        node.assertExists().performSemanticsAction(
            SemanticsActions.GetTextLayoutResult
        ) { action ->
            assertTrue(
                "Unable to obtain text layout: $text",
                action(results)
            )
        }

        assertTrue(
            "No text layout: $text",
            results.isNotEmpty()
        )

        val diagnostics = results
            .mapIndexed { index, result ->
                describeLayout(
                    index,
                    result
                )
            }
            .joinToString(
                separator = "\n"
            )

        val failureMessage =
            "Text overflow: $text\n" +
                    "Expected font scale: $expectedFontScale\n" +
                    diagnostics

        // Original strict assertion: do not suppress any overflow.
        assertTrue(
            failureMessage,
            results.all {
                !it.hasVisualOverflow
            }
        )

        for (result in results) {
            assertEquals(
                "Unexpected measured text:\n$diagnostics",
                text,
                result.layoutInput.text.text
            )

            assertEquals(
                "Unexpected font scale:\n$diagnostics",
                expectedFontScale,
                result.layoutInput.density.fontScale,
                0.001f
            )

            assertTrue(
                "No measured text lines:\n$diagnostics",
                result.lineCount > 0
            )

            for (line in 0 until result.lineCount) {
                assertFalse(
                    "Ellipsized text: $text, line $line\n$diagnostics",
                    result.isLineEllipsized(line)
                )
            }

            assertEquals(
                "Incomplete measured text:\n$diagnostics",
                text.length,
                result.getLineEnd(
                    result.lineCount - 1,
                    visibleEnd = false
                )
            )
        }
    }

    private fun describeLayout(
        index: Int,
        result: TextLayoutResult
    ): String = buildString {
        appendLine("Layout #${index + 1}")

        appendLine(
            "Font scale: " +
                    result.layoutInput.density.fontScale
        )

        appendLine(
            "Density: " +
                    result.layoutInput.density.density
        )

        appendLine(
            "Constraints: " +
                    result.layoutInput.constraints
        )

        appendLine(
            "Measured width: " +
                    result.size.width
        )

        appendLine(
            "Measured height: " +
                    result.size.height
        )

        appendLine(
            "Paragraph width: " +
                    result.multiParagraph.width
        )

        appendLine(
            "Paragraph height: " +
                    result.multiParagraph.height
        )

        appendLine(
            "Horizontal overflow: " +
                    result.didOverflowWidth
        )

        appendLine(
            "Vertical overflow: " +
                    result.didOverflowHeight
        )

        appendLine(
            "Visual overflow: " +
                    result.hasVisualOverflow
        )

        appendLine(
            "Line count: " +
                    result.lineCount
        )

        appendLine(
            "Maximum lines: " +
                    result.layoutInput.maxLines
        )

        appendLine(
            "Soft wrap: " +
                    result.layoutInput.softWrap
        )

        appendLine(
            "Overflow mode: " +
                    result.layoutInput.overflow
        )

        appendLine(
            "Text length: " +
                    result.layoutInput.text.length
        )

        for (line in 0 until result.lineCount) {
            appendLine(
                "Line $line: " +
                        "start=${result.getLineStart(line)}, " +
                        "end=${result.getLineEnd(line)}, " +
                        "left=${result.getLineLeft(line)}, " +
                        "right=${result.getLineRight(line)}, " +
                        "ellipsized=${result.isLineEllipsized(line)}"
            )
        }
    }
}