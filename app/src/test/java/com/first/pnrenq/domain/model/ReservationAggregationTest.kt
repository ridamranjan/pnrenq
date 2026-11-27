package com.first.pnrenq.domain.model

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ReservationAggregationTest {

    @Test
    fun normalizationPatternsPreserveRecognizedAndUnknownGrammar() {
        assertEquals(
            ReservationCode.WL,
            parseReservation("waiting list 12").code
        )

        assertEquals(
            "12",
            parseReservation("WAIT LIST/12").position
        )

        assertEquals(
            ReservationCode.GNWL,
            parseReservation("GNWL-8").code
        )

        assertEquals(
            ReservationCode.REGRET,
            parseReservation("REGRET/WL").code
        )

        assertEquals(
            ReservationCode.UNKNOWN,
            parseReservation("REGRET/RAC").code
        )

        assertEquals(
            ReservationCode.UNKNOWN,
            parseReservation("NOT CNF").code
        )
    }

    @Test
    fun singlePassAggregationMatchesPreviousAlgorithmAcrossStatusCombinations() {
        val examples = listOf(
            "CNF" to null,
            "CONFIRMED" to null,
            "CNF/B2/35/LB" to null,
            "CNF/B3/36/UB" to null,
            "RAC 7" to null,
            "R1 35" to null,
            "WL 5" to "GNWL",
            "GNWL 8" to "WL",
            "RLWL 8" to null,
            "CAN" to null,
            "REGRET/WL" to null,
            "UNKNOWN" to null,
            "" to "CNF",
            "RAC 7" to "CNF",
            "unfamiliar" to null,
            "unfamiliar" to "WL",
            "" to null
        )

        for ((first, firstCode) in examples) {
            for ((second, secondCode) in examples) {
                val pnr = sample(
                    listOf(
                        passenger(
                            1,
                            first,
                            firstCode
                        ),

                        passenger(
                            2,
                            second,
                            secondCode
                        )
                    )
                )

                assertEquals(
                    previousHomeText(pnr),
                    homeReservationText(pnr)
                )

                assertEquals(
                    pnr.passengers.map {
                        it.currentReservation().code
                    }.distinct(),

                    currentReservationCodes(pnr)
                )
            }
        }

        val empty = sample(emptyList())

        assertEquals(
            previousHomeText(empty),
            homeReservationText(empty)
        )

        assertEquals(
            listOf(ReservationCode.CNF),
            currentReservationCodes(empty)
        )
    }

    @Test
    fun mixedAggregationStopsAfterFirstDifferentCanonicalCode() {
        val values = listOf(
            passenger(1, "CNF", null),
            passenger(2, "RAC 7", null),
            passenger(3, "WL 5", null),
            passenger(4, "CAN", null)
        )

        var reads = 0

        val counted =
            object : AbstractList<PassengerStatus>() {

                override val size: Int
                    get() = values.size

                override fun get(
                    index: Int
                ): PassengerStatus {
                    reads++
                    return values[index]
                }
            }

        assertEquals(
            "Mixed",
            homeReservationText(sample(counted))
        )

        assertEquals(2, reads)

        assertEquals(
            listOf(
                ReservationCode.CNF,
                ReservationCode.RAC,
                ReservationCode.WL,
                ReservationCode.CAN
            ),
            currentReservationCodes(sample(values))
        )
    }

    // Independent fixture of the accepted aggregation algorithm.
    private fun previousHomeText(
        pnr: PnrInfo
    ): String? {
        if (pnr.passengers.isEmpty()) {
            return reservationText(pnr.currentStatus)
        }

        val resolved = pnr.passengers.map {
            it.currentReservation()
        }

        val code = resolved.map {
            it.code
        }.distinct().singleOrNull()
            ?: return "Mixed"

        val texts = resolved.map {
            it.text
        }.distinct()

        val same = texts.singleOrNull()

        if (code == ReservationCode.UNKNOWN) {
            return if (texts.size == 1) {
                same?.let {
                    if (
                        parseReservation(it).code ==
                        ReservationCode.UNKNOWN
                    ) {
                        it
                    } else {
                        "Unknown ($it)"
                    }
                } ?: "Unknown"
            } else {
                "Mixed"
            }
        }

        return same?.takeIf {
            parseReservation(it).code == code
        } ?: code.name
    }

    private fun passenger(
        index: Int,
        text: String,
        code: String?
    ) = PassengerStatus(
        index,
        "WL 5",
        text,
        null,
        null,
        currentStatusCode = code
    )

    private fun sample(
        passengers: List<PassengerStatus>
    ) = PnrInfo(
        "0123456789",
        "12345",
        "Test Express",
        LocalDate.of(2026, 12, 25),
        "ABC",
        "XYZ",
        "WL 5",
        "CNF",
        null,
        passengers,
        1000L
    )
}