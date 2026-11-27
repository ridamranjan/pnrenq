package com.first.pnrenq.ui.theme

import org.junit.Assert.*
import org.junit.Test

class PnrScreenContrastTest {

    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()

    @Test
    fun passengerBadgesMeetTextContrastOnLightAndDarkCards() {
        for (
        (surface, dark) in listOf(
            white to false,
            0xFFF0ECE6.toInt() to false,
            black to true,
            0xFF252525.toInt() to true
        )
        ) {
            for (current in listOf(false, true)) {
                val colors = passengerBadgeColors(
                    surface,
                    dark,
                    current
                )

                assertTrue(
                    pnrContrastRatio(
                        colors.foreground,
                        colors.background
                    ) >= MIN_TEXT_CONTRAST
                )
            }
        }
    }

    @Test
    fun badgePreservesOriginalTintAndDistinctRoles() {
        val booking = passengerBadgeColors(
            white,
            false,
            false
        )

        val current = passengerBadgeColors(
            white,
            false,
            true
        )

        assertEquals(
            blendPnrColor(
                white,
                0xFFB42318.toInt(),
                0.12
            ),
            booking.background
        )

        assertEquals(
            blendPnrColor(
                white,
                0xFF15803D.toInt(),
                0.12
            ),
            current.background
        )

        assertNotEquals(
            booking.foreground,
            current.foreground
        )

        assertNotEquals(
            booking.background,
            current.background
        )

        assertEquals(
            0xFFB42318.toInt(),
            booking.foreground
        )
    }

    @Test
    fun enabledControlPreservesReadableColorAndCorrectsWeakColor() {
        val backgrounds = listOf(
            listOf(white)
        )

        assertEquals(
            black,
            readablePnrControl(
                black,
                backgrounds
            )
        )

        val weak = 0xFFCCCCCC.toInt()

        val corrected = readablePnrControl(
            weak,
            backgrounds
        )

        assertNotEquals(
            weak,
            corrected
        )

        assertTrue(
            pnrContrastRatio(
                corrected,
                white
            ) >= MIN_CONTROL_CONTRAST
        )
    }

    @Test
    fun controlContrastCoversWholeGradientRange() {
        val background = listOf(
            0xFFBFCFE8.toInt(),
            white
        )

        val corrected = readablePnrControl(
            0xFFA0A0A0.toInt(),
            listOf(background)
        )

        assertTrue(
            minimumPnrGradientContrast(
                listOf(corrected),
                background
            ) >= MIN_CONTROL_CONTRAST
        )
    }

    @Test
    fun unsupportedAndTranslucentControlColorsArePreserved() {
        val disabled = 0x61000000

        assertEquals(
            disabled,
            readablePnrControl(
                disabled,
                listOf(listOf(white))
            )
        )

        val original = 0xFF888888.toInt()

        assertEquals(
            original,
            readablePnrControl(
                original,
                emptyList()
            )
        )

        assertEquals(
            original,
            readablePnrControl(
                original,
                listOf(listOf(black, white))
            )
        )
    }
}