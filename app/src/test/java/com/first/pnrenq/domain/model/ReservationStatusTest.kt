package com.first.pnrenq.domain.model

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ReservationStatusTest {

    @Test
    fun recognizedStatusTokensAndAliasesAreBounded() {
        for (
        text in listOf(
            "CNF",
            "Confirmed",
            "confirm",
            "CNF/B2/35/LB/GN",
            "CNF-B2-35"
        )
        ) {
            assertEquals(
                StatusCategory.CONFIRMED,
                statusCategoryOf(text)
            )
        }

        for (
        text in listOf(
            "RAC7",
            "RAC/7",
            "R1 35",
            "RAC 7 B2 35",
            "RAC/B2/35"
        )
        ) {
            assertEquals(
                StatusCategory.RAC,
                statusCategoryOf(text)
            )
        }

        for (
        text in listOf(
            "CANCELLED",
            "CAN",
            "WEBCAN",
            "WEBCANRF"
        )
        ) {
            assertEquals(
                StatusCategory.CANCELLED,
                statusCategoryOf(text)
            )
        }
    }

    @Test
    fun allExistingWaitingListCodesRemainRecognized() {
        for (
        code in listOf(
            "WL",
            "GNWL",
            "RLWL",
            "PQWL",
            "TQWL",
            "RQWL",
            "DPWL",
            "RSWL",
            "RLGN",
            "CKWL"
        )
        ) {
            assertEquals(
                StatusCategory.WAITING,
                statusCategoryOf("$code/12")
            )
        }

        assertEquals(
            StatusCategory.WAITING,
            statusCategoryOf("waiting list 5")
        )
    }

    @Test
    fun misleadingPrefixesSubstringsAndCombinedOutcomesStayUnknown() {
        for (
        text in listOf(
            "CONFIRMATION PENDING",
            "CNFISH",
            "CANNOT",
            "RACING",
            "AWL",
            "NOT CNF",
            "CNF/WL/3",
            "WL 10/CNF",
            "R1 0",
            "CNF/B2/-35",
            "CNF/WL5/35",
            "CNF/RAC7/35",
            "CNF/B0/35"
        )
        ) {
            assertEquals(
                StatusCategory.UNKNOWN,
                statusCategoryOf(text)
            )
        }

        assertEquals(
            StatusCategory.UNKNOWN,
            statusCategoryOf("Mixed")
        )
    }

    @Test
    fun specialCodesDoNotBecomeWaitingOrConfirmed() {
        assertEquals(
            ReservationCode.REGRET,
            parseReservation("REGRET/WL").code
        )

        assertEquals(
            ReservationCode.REL,
            parseReservation("Released").code
        )

        for (
        code in listOf(
            "REGRET/WL",
            "RELEASED",
            "MOD",
            "NT",
            "TDR",
            "NOSB"
        )
        ) {
            assertEquals(
                StatusCategory.UNKNOWN,
                statusCategoryOf(code)
            )
        }
    }

    @Test
    fun structuredCodesFillMissingOrCompatibleStatusAndAllocation() {
        assertEquals(
            ReservationCode.GNWL,
            resolveReservation("WL 5", "GNWL").code
        )

        assertEquals(
            ReservationCode.RLWL,
            resolveReservation("RLWL 3", "WL").code
        )

        assertEquals(
            ReservationCode.RAC,
            resolveReservation("N/A", "RAC").code
        )

        val seat = resolveReservation(
            "B2 35 LB",
            "CNF"
        )

        assertEquals(
            ReservationCode.CNF,
            seat.code
        )

        assertEquals(
            SeatAllocation("B2", 35, "LB"),
            seat.allocation
        )
    }

    @Test
    fun contradictoryOrOpaqueCurrentTextIsNotSilentlyOverridden() {
        val conflict = resolveReservation(
            "CNF",
            "WL"
        )

        assertEquals(
            ReservationCode.UNKNOWN,
            conflict.code
        )

        assertEquals(
            "CNF (code WL)",
            conflict.text
        )

        assertEquals(
            ReservationCode.UNKNOWN,
            resolveReservation("X9", "CNF").code
        )

        assertEquals(
            ReservationCode.UNKNOWN,
            resolveReservation("GNWL 5", "RLWL").code
        )
    }

    @Test
    fun glossaryNeverSubstitutesBookingAndKeepsDistinctCurrentOutcomes() {
        val pnr = record(
            listOf(
                passenger(1, "CNF", "X9"),
                passenger(2, "CNF", "N/A", "RAC"),
                passenger(3, "WL", "R1 35"),
                passenger(4, "WL", "CNF")
            )
        )

        assertEquals(
            listOf(
                ReservationCode.UNKNOWN,
                ReservationCode.RAC,
                ReservationCode.CNF
            ),
            currentReservationCodes(pnr)
        )
    }

    @Test
    fun glossaryUsesAggregateOnlyWhenNoPassengersExist() {
        assertEquals(
            listOf(ReservationCode.GNWL),
            currentReservationCodes(
                record(emptyList()).copy(
                    currentStatus = "GNWL 5"
                )
            )
        )

        assertEquals(
            listOf(ReservationCode.MIXED),
            currentReservationCodes(
                record(emptyList())
            )
        )
    }

    @Test
    fun homeDoesNotMislabelDifferentConfirmedSeatsOrWaitlistPositionsAsMixed() {
        assertEquals(
            "CNF",
            homeReservationText(
                record(
                    listOf(
                        passenger(1, "WL", "CNF/B2/35"),
                        passenger(2, "WL", "CNF/B2/36")
                    )
                )
            )
        )

        assertEquals(
            "RAC",
            homeReservationText(
                record(
                    listOf(
                        passenger(1, "WL", "RAC 4"),
                        passenger(2, "WL", "RAC 5")
                    )
                )
            )
        )

        assertEquals(
            "GNWL",
            homeReservationText(
                record(
                    listOf(
                        passenger(1, "WL", "WL 3", "GNWL")
                    )
                )
            )
        )
    }

    @Test
    fun homeDistinguishesMixedAndUnknownWithoutTrustingOldAggregate() {
        assertEquals(
            "Mixed",
            homeReservationText(
                record(
                    listOf(
                        passenger(1, "WL", "CNF"),
                        passenger(2, "WL", "RAC 5")
                    )
                )
            )
        )

        assertEquals(
            "X9",
            homeReservationText(
                record(
                    listOf(
                        passenger(1, "CNF", "X9")
                    )
                ).copy(
                    currentStatus = "CNF"
                )
            )
        )

        assertEquals(
            "Unknown",
            homeReservationText(
                record(
                    listOf(
                        passenger(1, "CNF", "N/A")
                    )
                )
            )
        )

        assertEquals(
            "CNF (code WL)",
            homeReservationText(
                record(
                    listOf(
                        passenger(1, "WL", "CNF", "WL")
                    )
                )
            )
        )

        val opaqueCode = homeReservationText(
            record(
                listOf(
                    passenger(1, "WL", "N/A", "WL5")
                )
            )
        )

        assertEquals(
            "Unknown (WL5)",
            opaqueCode
        )

        assertEquals(
            StatusCategory.UNKNOWN,
            statusCategoryOf(opaqueCode!!)
        )
    }

    private fun passenger(
        index: Int,
        booking: String,
        current: String,
        code: String? = null
    ) = PassengerStatus(
        index,
        booking,
        current,
        null,
        null,
        currentStatusCode = code
    )

    private fun record(
        passengers: List<PassengerStatus>
    ) = PnrInfo(
        "1234567890",
        "12345",
        "Test Express",
        LocalDate.of(2028, 1, 1),
        "ABC",
        "XYZ",
        "WL",
        "Mixed",
        null,
        passengers,
        1000L
    )
}