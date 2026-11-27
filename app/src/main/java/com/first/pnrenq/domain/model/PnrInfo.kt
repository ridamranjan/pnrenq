package com.first.pnrenq.domain.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class PassengerStatus(
    @field:SerializedName("index") val index: Int,
    @field:SerializedName("bookingStatus") val bookingStatus: String,
    @field:SerializedName("currentStatus") val currentStatus: String,
    @field:SerializedName("coach") val coach: String?,
    @field:SerializedName("berth") val berth: String?,
    @field:SerializedName("bookingStatusCode") val bookingStatusCode: String? = null,
    @field:SerializedName("currentStatusCode") val currentStatusCode: String? = null,
    @field:SerializedName("bookingCoach") val bookingCoach: String? = null,
    @field:SerializedName("bookingBerthNo") val bookingBerthNo: Int? = null,
    @field:SerializedName("currentBerthNo") val currentBerthNo: Int? = null,
    @field:SerializedName("bookingBerthCode") val bookingBerthCode: String? = null,
    @field:SerializedName("currentBerthCode") val currentBerthCode: String? = null,
    @field:SerializedName("coachPosition") val coachPosition: Int? = null
)

data class PnrDetails(
    @field:SerializedName("departureTime") val departureTime: String? = null,
    @field:SerializedName("arrivalDate") val arrivalDate: String? = null,
    @field:SerializedName("arrivalTime") val arrivalTime: String? = null,
    @field:SerializedName("duration") val duration: String? = null,
    @field:SerializedName("sourceStationName") val sourceStationName: String? = null,
    @field:SerializedName("destinationStationName") val destinationStationName: String? = null,
    @field:SerializedName("boardingPoint") val boardingPoint: String? = null,
    @field:SerializedName("reservationUpto") val reservationUpto: String? = null,
    @field:SerializedName("journeyClass") val journeyClass: String? = null,
    @field:SerializedName("quota") val quota: String? = null,
    @field:SerializedName("distanceKm") val distanceKm: Double? = null,
    @field:SerializedName("bookingFare") val bookingFare: Double? = null,
    @field:SerializedName("ticketFare") val ticketFare: Double? = null,
    @field:SerializedName("bookingDate") val bookingDate: String? = null,
    @field:SerializedName("vikalpStatus") val vikalpStatus: String? = null,
    @field:SerializedName("ticketType") val ticketType: String? = null,
    @field:SerializedName("provider") val provider: String? = null,
    @field:SerializedName("providerTimestamp") val providerTimestamp: String? = null
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

fun statusCategoryOf(status: String): StatusCategory =
    parseReservation(status).code.category

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