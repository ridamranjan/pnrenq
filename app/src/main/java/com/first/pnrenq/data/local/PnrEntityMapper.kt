
package com.first.pnrenq.data.local

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import com.google.gson.Gson
import java.time.LocalDate

private val gson = Gson()

private data class ExtraSnapshot(
    val details: PnrDetails,
    val passengers: List<PassengerStatus>
)

fun PnrInfo.toSavedEntity(
    createdAtMillis: Long = System.currentTimeMillis()
): SavedPnrEntity = SavedPnrEntity(
    pnrNumber = pnrNumber,
    trainNumber = trainNumber,
    trainName = trainName,
    journeyDate = journeyDate.toString(),
    fromStation = fromStation,
    toStation = toStation,
    bookingStatus = bookingStatus,
    currentStatus = currentStatus,
    chartStatus = chartStatus,
    lastCheckedAtMillis = lastCheckedAtMillis,
    createdAtMillis = createdAtMillis,
    detailsJson = gson.toJson(
        ExtraSnapshot(
            details = details,
            passengers = passengers
        )
    )
)

fun PnrInfo.toPassengerEntities(): List<PassengerEntity> =
    passengers.map { passenger ->
        PassengerEntity(
            pnrNumber = pnrNumber,
            passengerIndex = passenger.index,
            bookingStatus = passenger.bookingStatus,
            currentStatus = passenger.currentStatus,
            coach = passenger.coach,
            berth = passenger.berth
        )
    }

fun PnrWithPassengers.toDomain(): PnrInfo {
    val snapshot = try {
        pnr.detailsJson?.let {
            gson.fromJson(it, ExtraSnapshot::class.java)
        }
    } catch (_: Exception) {
        null
    }

    val extraPassengers = try {
        snapshot?.passengers
            ?.associateBy { it.index }
            ?: emptyMap()
    } catch (_: Exception) {
        emptyMap()
    }

    val restoredPassengers = passengers
        .sortedBy { it.passengerIndex }
        .map { passenger ->
            val extra = extraPassengers[passenger.passengerIndex]

            PassengerStatus(
                index = passenger.passengerIndex,
                bookingStatus = passenger.bookingStatus,
                currentStatus = passenger.currentStatus,
                coach = passenger.coach,
                berth = passenger.berth,
                bookingStatusCode = extra?.bookingStatusCode,
                currentStatusCode = extra?.currentStatusCode,
                bookingCoach = extra?.bookingCoach,
                bookingBerthNo = extra?.bookingBerthNo,
                currentBerthNo = extra?.currentBerthNo,
                bookingBerthCode = extra?.bookingBerthCode,
                currentBerthCode = extra?.currentBerthCode,
                coachPosition = extra?.coachPosition
            )
        }

    return PnrInfo(
        pnrNumber = pnr.pnrNumber,
        trainNumber = pnr.trainNumber,
        trainName = pnr.trainName,
        journeyDate = LocalDate.parse(pnr.journeyDate),
        fromStation = pnr.fromStation,
        toStation = pnr.toStation,
        bookingStatus = pnr.bookingStatus,
        currentStatus = pnr.currentStatus,
        chartStatus = pnr.chartStatus,
        passengers = restoredPassengers,
        lastCheckedAtMillis = pnr.lastCheckedAtMillis,
        details = snapshot?.details ?: PnrDetails()
    )
}
