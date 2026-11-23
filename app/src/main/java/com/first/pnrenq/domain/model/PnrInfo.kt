
package com.first.pnrenq.domain.model

import java.time.LocalDate

data class PassengerStatus(
    val index: Int,
    val bookingStatus: String,
    val currentStatus: String,
    val coach: String?,
    val berth: String?,
    val bookingStatusCode: String? = null,
    val currentStatusCode: String? = null,
    val bookingCoach: String? = null,
    val bookingBerthNo: Int? = null,
    val currentBerthNo: Int? = null,
    val bookingBerthCode: String? = null,
    val currentBerthCode: String? = null,
    val coachPosition: Int? = null
)

data class PnrDetails(
    val departureTime: String? = null,
    val arrivalDate: String? = null,
    val arrivalTime: String? = null,
    val duration: String? = null,
    val sourceStationName: String? = null,
    val destinationStationName: String? = null,
    val boardingPoint: String? = null,
    val reservationUpto: String? = null,
    val journeyClass: String? = null,
    val quota: String? = null,
    val distanceKm: Double? = null,
    val bookingFare: Double? = null,
    val ticketFare: Double? = null,
    val bookingDate: String? = null,
    val vikalpStatus: String? = null,
    val ticketType: String? = null,
    val provider: String? = null,
    val providerTimestamp: String? = null
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
    val lastCheckedAtMillis: Long,
    val details: PnrDetails = PnrDetails()
)

enum class StatusCategory {
    CONFIRMED,
    RAC,
    WAITING,
    CANCELLED,
    UNKNOWN
}

fun statusCategoryOf(status: String): StatusCategory {
    val s = status.trim().uppercase()

    return when {
        s.startsWith("CNF") ||
                s.startsWith("CONFIRM") ->
            StatusCategory.CONFIRMED

        s.startsWith("RAC") ->
            StatusCategory.RAC

        s.startsWith("CAN") ->
            StatusCategory.CANCELLED

        s.contains("WL") ->
            StatusCategory.WAITING

        else ->
            StatusCategory.UNKNOWN
    }
}

fun PnrInfo.coachBerthSummary(): String? {
    val first = passengers.firstOrNull {
        !it.coach.isNullOrBlank()
    } ?: return null

    val berth = first.berth

    return if (berth.isNullOrBlank()) {
        first.coach
    } else {
        "${first.coach} / $berth"
    }
}
