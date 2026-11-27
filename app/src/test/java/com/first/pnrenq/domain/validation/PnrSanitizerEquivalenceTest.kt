package com.first.pnrenq.domain.validation

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class PnrSanitizerEquivalenceTest {

    @Test
    fun matchesPreviousSanitizerAcrossDeterministicInputs() {
        val random = Random(812)
        val alphabet =
            "0123456789 abc-+/\n\t\u0661\u0968\uFF13\uD83D\uDE82"

        repeat(1000) {
            val raw = buildString {
                repeat(random.nextInt(0, 128)) {
                    append(
                        alphabet[random.nextInt(alphabet.length)]
                    )
                }
            }

            assertEquals(
                raw.filter { it in '0'..'9' }.take(PNR_LENGTH),
                sanitizePnrInput(raw)
            )
        }
    }

    @Test
    fun longPastePreservesLeadingZerosAndFirstTenAsciiDigits() {
        val raw =
            "0a1b2c3d4e5f6g7h8i9" +
                    "9876543210".repeat(10_000)

        val sanitized = sanitizePnrInput(raw)

        assertEquals(
            "0123456789",
            sanitized
        )

        assertTrue(
            isValidPnr(sanitized)
        )
    }

    @Test
    fun sparseDigitsAndNonAsciiDigitsRemainDistinct() {
        val raw =
            "\u0661\u0968\uFF13-".repeat(1000) +
                    "0 5"

        assertEquals(
            "05",
            sanitizePnrInput(raw)
        )

        assertFalse(
            isValidPnr(sanitizePnrInput(raw))
        )

        assertEquals(
            "",
            sanitizePnrInput("\u0661\u0968\uFF13")
        )

        assertEquals(
            "",
            sanitizePnrInput("")
        )
    }
}