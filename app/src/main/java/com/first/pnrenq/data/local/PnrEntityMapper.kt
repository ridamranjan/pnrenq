
package com.first.pnrenq.data.local

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrInfo
import java.time.LocalDate

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
    createdAtMillis = createdAtMillis
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

fun PnrWithPassengers.toDomain(): PnrInfo = PnrInfo(
    pnrNumber = pnr.pnrNumber,
    trainNumber = pnr.trainNumber,
    trainName = pnr.trainName,
    journeyDate = LocalDate.parse(pnr.journeyDate),
    fromStation = pnr.fromStation,
    toStation = pnr.toStation,
    bookingStatus = pnr.bookingStatus,
    currentStatus = pnr.currentStatus,
    chartStatus = pnr.chartStatus,
    passengers = passengers
        .sortedBy { it.passengerIndex }
        .map { passenger ->
            PassengerStatus(
                index = passenger.passengerIndex,
                bookingStatus = passenger.bookingStatus,
                currentStatus = passenger.currentStatus,
                coach = passenger.coach,
                berth = passenger.berth
            )
        },
    lastCheckedAtMillis = pnr.lastCheckedAtMillis
)
