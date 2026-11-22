package com.first.pnrenq.domain.model

import java.time.LocalDate

/** One passenger's seat status inside a PNR. Passenger names are deliberately not stored. */
data class PassengerStatus(
    val index: Int,
    val bookingStatus: String,
    val currentStatus: String,
    val coach: String?,
    val berth: String?
)

data class PnrInfo(
    val pnrNumber: String,
    val trainNumber: String,
    val trainName: String,
    val journeyDate: LocalDate,
    val fromStation: String,
    val toStation: String,
    val bookingStatus: String,
    val currentStatus: String,
    val chartStatus: String?,
    val passengers: List<PassengerStatus>,
    val lastCheckedAtMillis: Long
)

enum class StatusCategory { CONFIRMED, RAC, WAITING, CANCELLED, UNKNOWN }

/** Groups a raw railway status text (CNF, RAC, WL 10, ...) for display purposes only. */
fun statusCategoryOf(status: String): StatusCategory {
    val s = status.trim().uppercase()
    return when {
        s.startsWith("CNF") -> StatusCategory.CONFIRMED
        s.startsWith("RAC") -> StatusCategory.RAC
        s.startsWith("CAN") -> StatusCategory.CANCELLED
        s.contains("WL") -> StatusCategory.WAITING
        else -> StatusCategory.UNKNOWN
    }
}

/** For example "B2 / 34", or null when no passenger has a coach yet. */
fun PnrInfo.coachBerthSummary(): String? {
    val first = passengers.firstOrNull { !it.coach.isNullOrBlank() } ?: return null
    val berth = first.berth
    return if (berth.isNullOrBlank()) first.coach else "${first.coach} / $berth"
}