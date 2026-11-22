package com.first.pnrenq.data.mock

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import java.time.LocalDate

/** FAKE data for UI prototyping only. These are not real PNRs or real trains. */
object MockPnrData {
    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE

    fun sample(now: Long = System.currentTimeMillis()): List<PnrInfo> = listOf(
        PnrInfo(
            pnrNumber = "1234567890",
            trainNumber = "12345",
            trainName = "Example Express",
            journeyDate = LocalDate.of(2026, 12, 1),
            fromStation = "NDLS",
            toStation = "HWH",
            bookingStatus = "WL 10",
            currentStatus = "CNF",
            chartStatus = "Not Prepared",
            passengers = listOf(
                PassengerStatus(1, "WL 10", "CNF", "B2", "34"),
                PassengerStatus(2, "WL 11", "CNF", "B2", "35")
            ),
            lastCheckedAtMillis = now - 12 * MINUTE
        ),
        PnrInfo(
            pnrNumber = "2345678901",
            trainNumber = "22222",
            trainName = "Sample Superfast",
            journeyDate = LocalDate.of(2026, 12, 15),
            fromStation = "BBS",
            toStation = "MAS",
            bookingStatus = "WL 25",
            currentStatus = "WL 8",
            chartStatus = "Not Prepared",
            passengers = listOf(
                PassengerStatus(1, "WL 25", "WL 8", null, null)
            ),
            lastCheckedAtMillis = now - 3 * HOUR
        ),
        PnrInfo(
            pnrNumber = "3456789012",
            trainNumber = "33333",
            trainName = "Demo Mail",
            journeyDate = LocalDate.of(2026, 11, 28),
            fromStation = "PNBE",
            toStation = "NDLS",
            bookingStatus = "RAC 5",
            currentStatus = "RAC 2",
            chartStatus = "Prepared",
            passengers = listOf(
                PassengerStatus(1, "RAC 5", "RAC 2", "S4", "12"),
                PassengerStatus(2, "RAC 6", "CNF", "S4", "13"),
                PassengerStatus(3, "CAN", "CAN", null, null)
            ),
            lastCheckedAtMillis = now - 26 * HOUR
        )
    )
}