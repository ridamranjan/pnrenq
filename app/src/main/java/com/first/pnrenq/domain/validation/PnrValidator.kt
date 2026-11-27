package com.first.pnrenq.domain.validation

const val PNR_LENGTH = 10

/** A PNR is exactly 10 ASCII digits. Other scripts' digits and symbols are rejected. */
fun isValidPnr(input: String): Boolean =
    input.length == PNR_LENGTH && input.all { it in '0'..'9' }

/** Keeps only ASCII digits and caps the length, for use while the user types or pastes. */
fun sanitizePnrInput(raw: String): String =
    buildString(PNR_LENGTH) {
        for (character in raw) {
            if (character in '0'..'9') {
                append(character)
                if (length == PNR_LENGTH) break
            }
        }
    }