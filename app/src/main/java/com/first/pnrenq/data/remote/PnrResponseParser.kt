package com.first.pnrenq.data.remote

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.time.parseRailwayDate
import com.first.pnrenq.domain.validation.isValidPnr
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.StringReader
import java.time.Instant

/** Converts a bounded backend response into a validated PNR snapshot. */
internal object PnrResponseParser {

    private const val MAX_FUTURE_SKEW_SECONDS = 300L
    private val trainPattern = Regex("[0-9]{4,5}")
    private val jsonAdapter = Gson().getAdapter(JsonElement::class.java)

    fun parse(
        json: String,
        requestedPnr: String,
        nowMillis: Long = System.currentTimeMillis()
    ): PnrInfo {
        try {
            require(isValidPnr(requestedPnr))
            require(json.length <= PnrNetworkConfig.MAX_RESPONSE_CHARS)

            val root = readRoot(json)
            val success = root.get("success")
            require(
                success != null && success.isJsonPrimitive &&
                        success.asJsonPrimitive.isBoolean && success.asBoolean
            )

            val data = root.getAsJsonObject("data")
                ?: throw IllegalArgumentException()
            require(requiredString(data, "pnr") == requestedPnr)

            val trainNumber = requiredString(data, "trainNumber")
            require(trainPattern.matches(trainNumber))
            val trainName = requiredString(data, "trainName")

            // Use the same strict date grammar as the railway presentation layer.
            val journeyDate = parseRailwayDate(
                requiredString(data, "dateOfJourney")
            ) ?: throw IllegalArgumentException()

            val array = data.getAsJsonArray("passengers")
                ?: throw IllegalArgumentException()
            require(array.size() in 1..12)

            val passengers = array.map { element ->
                parsePassenger(element.asJsonObject)
            }.sortedBy { it.index }
            require(passengers.map { it.index }.distinct().size == passengers.size)

            return PnrInfo(
                pnrNumber = requestedPnr,
                trainNumber = trainNumber,
                trainName = trainName,
                journeyDate = journeyDate,
                fromStation = optionalString(data, "sourceStation") ?: "Unknown",
                toStation = optionalString(data, "destinationStation") ?: "Unknown",
                bookingStatus = passengers.map { it.bookingStatus }
                    .distinct().singleOrNull() ?: "Mixed",
                currentStatus = passengers.map { it.currentStatus }
                    .distinct().singleOrNull() ?: "Mixed",
                chartStatus = optionalString(data, "chartStatus"),
                passengers = passengers,
                lastCheckedAtMillis = parseCheckedAt(root, nowMillis),
                details = parseDetails(data)
            )
        } catch (_: Exception) {
            throw PnrCheckException(
                "Invalid PNR response. Saved information was not changed."
            )
        }
    }

    private fun readRoot(json: String): JsonObject =
        JsonReader(StringReader(json)).use { reader ->
            // Use the public adapter directly to avoid JsonParser's lenient mode.
            reader.isLenient = false
            val root = jsonAdapter.read(reader)
            require(reader.peek() == JsonToken.END_DOCUMENT)
            root.asJsonObject
        }

    private fun parsePassenger(passenger: JsonObject): PassengerStatus {
        val index = optionalInt(passenger, "serialNumber")
            ?: throw IllegalArgumentException()
        require(index in 1..12)

        val currentBerthNo = optionalInt(passenger, "currentBerthNo")
        val currentBerthCode = optionalString(passenger, "currentBerthCode")

        return PassengerStatus(
            index = index,
            bookingStatus = requiredString(passenger, "bookingStatus"),
            currentStatus = requiredString(passenger, "currentStatus"),
            coach = optionalString(passenger, "currentCoach"),
            berth = currentBerthNo?.toString() ?: currentBerthCode,
            bookingStatusCode = optionalString(passenger, "bookingStatusCode"),
            currentStatusCode = optionalString(passenger, "currentStatusCode"),
            bookingCoach = optionalString(passenger, "bookingCoach"),
            bookingBerthNo = optionalInt(passenger, "bookingBerthNo"),
            currentBerthNo = currentBerthNo,
            bookingBerthCode = optionalString(passenger, "bookingBerthCode"),
            currentBerthCode = currentBerthCode,
            coachPosition = optionalInt(passenger, "coachPosition")
        )
    }

    private fun parseDetails(data: JsonObject): PnrDetails = PnrDetails(
        departureTime = optionalString(data, "departureTime"),
        arrivalDate = optionalString(data, "arrivalDate"),
        arrivalTime = optionalString(data, "arrivalTime"),
        duration = optionalString(data, "duration"),
        sourceStationName = optionalString(data, "sourceStationName"),
        destinationStationName = optionalString(data, "destinationStationName"),
        boardingPoint = optionalString(data, "boardingPoint"),
        reservationUpto = optionalString(data, "reservationUpto"),
        journeyClass = optionalString(data, "journeyClass"),
        quota = optionalString(data, "quota"),
        distanceKm = optionalDouble(data, "distanceKm"),
        bookingFare = optionalDouble(data, "bookingFare"),
        ticketFare = optionalDouble(data, "ticketFare"),
        bookingDate = optionalString(data, "bookingDate"),
        vikalpStatus = optionalString(data, "vikalpStatus"),
        ticketType = optionalString(data, "ticketType"),
        provider = optionalString(data, "provider"),
        providerTimestamp = optionalString(data, "providerTimestamp")
    )

    private fun parseCheckedAt(root: JsonObject, nowMillis: Long): Long {
        val value = root.get("checkedAtUtc")
        // Retain the existing receipt-time fallback for absent timestamps.
        if (value == null || value.isJsonNull) return nowMillis

        val checkedAt = Instant.parse(requiredString(root, "checkedAtUtc"))
        val millis = checkedAt.toEpochMilli()
        require(millis > 0L)
        require(!checkedAt.isAfter(
            Instant.ofEpochMilli(nowMillis).plusSeconds(MAX_FUTURE_SKEW_SECONDS)
        ))
        return millis
    }

    private fun requiredString(obj: JsonObject, key: String): String {
        val value = obj.get(key)
        require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString)
        return value.asString.trim().takeIf {
            it.isNotBlank() && it.length <= 200
        } ?: throw IllegalArgumentException()
    }

    private fun optionalString(obj: JsonObject, key: String): String? {
        val value = obj.get(key) ?: return null
        if (!value.isJsonPrimitive) return null
        val primitive = value.asJsonPrimitive
        if (!primitive.isString && !primitive.isNumber) return null
        return primitive.asString.trim().takeIf {
            it.isNotBlank() && it.length <= 200
        }
    }

    private fun optionalInt(obj: JsonObject, key: String): Int? {
        val value = obj.get(key) ?: return null
        if (!value.isJsonPrimitive) return null
        val primitive = value.asJsonPrimitive

        return try {
            when {
                // Reject fractional or overflowing numbers rather than truncate.
                primitive.isNumber -> primitive.asBigDecimal.intValueExact()
                // Preserve the previously supported integer-string format.
                primitive.isString -> primitive.asString.toIntOrNull()
                else -> null
            }
        } catch (_: ArithmeticException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun optionalDouble(obj: JsonObject, key: String): Double? {
        val value = obj.get(key) ?: return null
        if (!value.isJsonPrimitive) return null
        return try {
            value.asDouble.takeIf { it.isFinite() && it >= 0.0 }
        } catch (_: NumberFormatException) {
            null
        }
    }
}