package com.first.pnrenq.data.mock

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import java.time.LocalDate

/** FAKE check used only while prototyping. It never contacts any server. */
object MockPnrChecker {
    fun simulatedCheck(pnr: String, now: Long = System.currentTimeMillis()): PnrInfo {
        val seed = pnr.last().digitToInt()
        val (booking, current) = when (seed % 3) {
            0 -> "WL 12" to "CNF"
            1 -> "WL 20" to "WL 6"
            else -> "RAC 4" to "RAC 1"
        }
        val hasSeat = current == "CNF" || current.startsWith("RAC")
        return PnrInfo(
            pnrNumber = pnr,
            trainNumber = "9${pnr.takeLast(4)}",
            trainName = "Mock Express",
            journeyDate = LocalDate.now().plusDays(10),
            fromStation = "ABC",
            toStation = "XYZ",
            bookingStatus = booking,
            currentStatus = current,
            chartStatus = "Not Prepared",
            passengers = listOf(
                PassengerStatus(1, booking, current, if (hasSeat) "S1" else null, if (hasSeat) "21" else null)
            ),
            lastCheckedAtMillis = now
        )
    }
}