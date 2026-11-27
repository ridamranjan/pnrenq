package com.first.pnrenq.ui.theme

import org.junit.Assert.*
import org.junit.Test

class PnrContrastTest {

    private val black = 0xFF000000.toInt()
    private val white = 0xFFFFFFFF.toInt()
    private val blue = 0xFF2759AD.toInt()

    @Test
    fun blackAndWhiteContrastIsTwentyOne() {
        assertEquals(
            21.0,
            pnrContrastRatio(black, white),
            0.000001
        )

        assertEquals(
            1.0,
            pnrContrastRatio(white, white),
            0.000001
        )
    }

    @Test
    fun alphaIsCompositedBeforeContrastCalculation() {
        assertEquals(
            1.0,
            pnrContrastRatio(
                0x00000000,
                white
            ),
            0.000001
        )

        assertTrue(
            pnrContrastRatio(
                0x80000000.toInt(),
                white
            ) < 4.5
        )

        assertTrue(
            pnrContrastRatio(
                0xDD000000.toInt(),
                white
            ) > 4.5
        )
    }

    @Test
    fun gradientBoundRejectsOverlappingLuminanceRanges() {
        assertEquals(
            1.0,
            minimumPnrGradientContrast(
                listOf(black, white),
                listOf(0xFF777777.toInt())
            ),
            0.000001
        )

        assertEquals(
            1.0,
            minimumPnrGradientContrast(
                listOf(0x80000000.toInt()),
                listOf(white)
            ),
            0.000001
        )
    }

    @Test
    fun channelBoundsAreConservativeForMixedHueStops() {
        val foreground = listOf(
            0xFF001070.toInt(),
            0xFF400000.toInt()
        )

        val background = listOf(
            0xFFFFEEDD.toInt(),
            0xFFDDFFFF.toInt()
        )

        val bound = minimumPnrGradientContrast(
            foreground,
            background
        )

        for (i in 0..20) {
            for (j in 0..20) {
                val f = blendPnrColor(
                    foreground.first(),
                    foreground.last(),
                    i / 20.0
                )

                val b = blendPnrColor(
                    background.first(),
                    background.last(),
                    j / 20.0
                )

                assertTrue(
                    pnrContrastRatio(f, b) + 0.000001 >= bound
                )
            }
        }

        assertTrue(bound >= 4.5)
    }

    @Test
    fun textAccentIsReducedUntilWholeGradientIsReadable() {
        val end = safePnrGradientStop(
            black,
            white,
            listOf(listOf(white)),
            0.95,
            true
        )

        assertNotEquals(white, end)
        assertNotEquals(black, end)

        assertTrue(
            minimumPnrGradientContrast(
                listOf(black, end),
                listOf(white)
            ) >= 4.5
        )
    }

    @Test
    fun backgroundAccentIsReducedAndBadBaseIsNotHidden() {
        val end = safePnrGradientStop(
            white,
            black,
            listOf(listOf(black)),
            0.95,
            false
        )

        assertNotEquals(black, end)
        assertNotEquals(white, end)

        assertTrue(
            minimumPnrGradientContrast(
                listOf(black),
                listOf(white, end)
            ) >= 4.5
        )

        assertEquals(
            white,
            safePnrGradientStop(
                white,
                blue,
                listOf(listOf(white)),
                0.5,
                false
            )
        )
    }

    @Test
    fun maximumRequestedStrengthBoundsAllSmallerStrengths() {
        val end = safePnrGradientStop(
            white,
            black,
            listOf(listOf(black)),
            0.9,
            false
        )

        for (i in 0..90) {
            val smaller = safePnrGradientStop(
                white,
                black,
                listOf(listOf(black)),
                i / 100.0,
                false
            )

            assertTrue(
                (smaller and 255) >= (end and 255)
            )
        }
    }

    @Test
    fun accentReservesContrastForForegroundTintedOverlay() {
        val end = safePnrGradientStop(
            black,
            white,
            listOf(listOf(white)),
            0.9,
            textGradient = false,
            backgroundOverlayFraction = 0.10
        )

        val backgrounds = listOf(
            black,
            end
        ).flatMap {
            listOf(
                it,
                blendPnrColor(
                    it,
                    white,
                    0.10
                )
            )
        }

        assertTrue(
            minimumPnrGradientContrast(
                listOf(white),
                backgrounds
            ) >= 4.5
        )
    }

    @Test
    fun unrelatedSolidSurfacesAreNotTreatedAsOneGradient() {
        val green = 0xFF23754C.toInt()
        val red = 0xFFB42318.toInt()

        assertTrue(
            minimumPnrGradientContrast(
                listOf(white),
                listOf(green, red)
            ) < 4.5
        )

        assertTrue(
            minimumPnrTextContrast(
                listOf(white),
                listOf(
                    listOf(green),
                    listOf(red)
                )
            ) >= 4.5
        )
    }

    @Test
    fun readableForegroundKeepsGoodColorsAndRepairsWeakKnownRoles() {
        assertEquals(
            black,
            readablePnrForeground(
                black,
                listOf(listOf(white))
            )
        )

        val backgrounds = listOf(
            white,
            0xFFE8D5C0.toInt()
        )

        val original = 0xFF986108.toInt()

        val corrected = readablePnrForeground(
            original,
            listOf(backgrounds)
        )

        assertTrue(
            minimumPnrGradientContrast(
                listOf(corrected),
                backgrounds
            ) >= 4.5
        )

        assertEquals(
            original,
            readablePnrForeground(
                original,
                listOf(listOf(black, white))
            )
        )

        assertEquals(
            0x80000000.toInt(),
            readablePnrForeground(
                0x80000000.toInt(),
                listOf(backgrounds)
            )
        )
    }
}