package com.first.pnrenq.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrValidatorTest {
    @Test fun tenDigitsIsValid() = assertTrue(isValidPnr("1234567890"))
    @Test fun nineDigitsIsInvalid() = assertFalse(isValidPnr("123456789"))
    @Test fun elevenDigitsIsInvalid() = assertFalse(isValidPnr("12345678901"))
    @Test fun emptyIsInvalid() = assertFalse(isValidPnr(""))
    @Test fun lettersAreInvalid() = assertFalse(isValidPnr("12345abcde"))
    @Test fun symbolsAreInvalid() = assertFalse(isValidPnr("12345-7890"))
    @Test fun spacesAreInvalid() = assertFalse(isValidPnr("12345 7890"))
    @Test fun surroundingSpaceIsInvalid() = assertFalse(isValidPnr(" 1234567890"))
    @Test fun nonAsciiDigitsAreInvalid() = assertFalse(isValidPnr("\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669\u0660"))
    @Test fun sanitizeStripsNonDigits() = assertEquals("12345", sanitizePnrInput("12a-3 45"))
    @Test fun sanitizeCapsAtTenDigits() = assertEquals("1234567890", sanitizePnrInput("123456789012345"))
}