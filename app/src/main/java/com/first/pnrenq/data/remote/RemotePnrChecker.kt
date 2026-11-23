
package com.first.pnrenq.data.remote

import com.first.pnrenq.domain.model.PassengerStatus
import com.first.pnrenq.domain.model.PnrDetails
import com.first.pnrenq.domain.model.PnrInfo
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


private val WORKER_BASE_URL: String =
    PnrNetworkConfig.BASE_URL


private const val MAX_RESPONSE_CHARS = 65536

private data class LivePnrRequest(
    val pnr: String
)

private interface LivePnrService {
    @POST("v1/pnr/check")
    suspend fun check(
        @Body request: LivePnrRequest
    ): Response<ResponseBody>
}

class PnrCheckException(
    override val message: String
) : Exception(message)

object RemotePnrChecker {

    private val pnrPattern = Regex("[0-9]{10}")
    private val trainPattern = Regex("[0-9]{4,5}")

    private val legacyDateFormat =
        DateTimeFormatter.ofPattern(
            "MMM d, uuuu h:mm:ss a",
            Locale.US
        )

    private val dayFirstFormat =
        DateTimeFormatter.ofPattern(
            "dd-MM-uuuu",
            Locale.US
        )

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    private val service: LivePnrService by lazy {
        Retrofit.Builder()
            .baseUrl(WORKER_BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(LivePnrService::class.java)
    }

    suspend fun check(pnr: String): PnrInfo =
        withContext(Dispatchers.IO) {

            if (!pnrPattern.matches(pnr)) {
                throw PnrCheckException(
                    "Enter a valid 10-digit PNR."
                )
            }

            val response = try {
                service.check(LivePnrRequest(pnr))
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                throw PnrCheckException(
                    "Unable to connect. Check your internet connection."
                )
            }

            if (!response.isSuccessful) {
                val errorText = try {
                    response.errorBody()?.use {
                        it.string().take(MAX_RESPONSE_CHARS)
                    }
                } catch (_: IOException) {
                    null
                }

                throw PnrCheckException(
                    errorMessage(response.code(), errorText)
                )
            }

            val responseText = try {
                response.body()?.use { body ->
                    val result = body.string()

                    if (result.length > MAX_RESPONSE_CHARS) {
                        throw PnrCheckException(
                            "Invalid response from the PNR service."
                        )
                    }

                    result
                }
            } catch (e: PnrCheckException) {
                throw e
            } catch (_: IOException) {
                throw PnrCheckException(
                    "Unable to read the PNR response."
                )
            }

            if (responseText.isNullOrBlank()) {
                throw PnrCheckException(
                    "The PNR service returned an empty response."
                )
            }

            parseSuccessfulResponse(
                responseText,
                pnr
            )
        }

    private fun errorMessage(
        status: Int,
        text: String?
    ): String {
        val code = try {
            if (text.isNullOrBlank()) {
                null
            } else {
                JsonParser.parseString(text)
                    .asJsonObject
                    .getAsJsonObject("error")
                    ?.get("code")
                    ?.asString
            }
        } catch (_: Exception) {
            null
        }

        return when (code) {
            "PROVIDERS_DISABLED",
            "PROVIDERS_NOT_CONFIGURED" ->
                "Live PNR checking is not available yet."

            "INVALID_PNR" ->
                "Enter a valid 10-digit PNR."

            "PNR_NOT_FOUND" ->
                "PNR information was not found."

            "RATE_LIMIT_EXCEEDED" ->
                "Too many requests. Please try again later."

            "PROVIDER_UNAVAILABLE",
            "QUOTA_PROTECTION_UNAVAILABLE",
            "QUOTA_DB_UNAVAILABLE",
            "PROVIDER_QUOTA_UNAVAILABLE",
            "ALL_PROVIDERS_UNAVAILABLE" ->
                "PNR service temporarily unavailable."

            else -> when (status) {
                400 -> "Invalid PNR request."
                401, 403 ->
                    "PNR service authorization failed."
                429 ->
                    "Too many requests. Please try again later."
                500, 502, 503, 504 ->
                    "PNR service temporarily unavailable."
                else ->
                    "Unable to check PNR. Please try again."
            }
        }
    }

    private fun parseSuccessfulResponse(
        json: String,
        requestedPnr: String
    ): PnrInfo {
        try {
            val root = JsonParser
                .parseString(json)
                .asJsonObject

            if (root.get("success")?.asBoolean != true) {
                throw IllegalArgumentException()
            }

            val data = root.getAsJsonObject("data")
                ?: throw IllegalArgumentException()

            if (
                requiredString(data, "pnr") != requestedPnr
            ) {
                throw IllegalArgumentException("PNR mismatch")
            }

            val trainNumber = requiredString(
                data, "trainNumber"
            )

            if (!trainPattern.matches(trainNumber)) {
                throw IllegalArgumentException()
            }

            val trainName = requiredString(
                data, "trainName"
            )

            val journeyDate = parseJourneyDate(
                requiredString(data, "dateOfJourney")
            )

            val source = optionalString(
                data, "sourceStation"
            ) ?: "Unknown"

            val destination = optionalString(
                data, "destinationStation"
            ) ?: "Unknown"

            val array = data.getAsJsonArray("passengers")
                ?: throw IllegalArgumentException()

            if (array.size() !in 1..12) {
                throw IllegalArgumentException()
            }

            val passengers = array.map { element ->
                val passenger = element.asJsonObject

                val index = requiredInt(
                    passenger, "serialNumber"
                )

                if (index !in 1..12) {
                    throw IllegalArgumentException()
                }

                val bookingStatus = requiredString(
                    passenger, "bookingStatus"
                )

                val currentStatus = requiredString(
                    passenger, "currentStatus"
                )

                val currentCoach = optionalString(
                    passenger, "currentCoach"
                )

                val currentBerthNo = optionalInt(
                    passenger, "currentBerthNo"
                )

                PassengerStatus(
                    index = index,
                    bookingStatus = bookingStatus,
                    currentStatus = currentStatus,
                    coach = currentCoach,
                    berth = currentBerthNo?.toString()
                        ?: optionalString(
                            passenger, "currentBerthCode"
                        ),
                    bookingStatusCode = optionalString(
                        passenger, "bookingStatusCode"
                    ),
                    currentStatusCode = optionalString(
                        passenger, "currentStatusCode"
                    ),
                    bookingCoach = optionalString(
                        passenger, "bookingCoach"
                    ),
                    bookingBerthNo = optionalInt(
                        passenger, "bookingBerthNo"
                    ),
                    currentBerthNo = currentBerthNo,
                    bookingBerthCode = optionalString(
                        passenger, "bookingBerthCode"
                    ),
                    currentBerthCode = optionalString(
                        passenger, "currentBerthCode"
                    ),
                    coachPosition = optionalInt(
                        passenger, "coachPosition"
                    )
                )
            }.sortedBy { it.index }

            if (
                passengers.map { it.index }.distinct().size !=
                passengers.size
            ) {
                throw IllegalArgumentException()
            }

            val bookingStatuses = passengers
                .map { it.bookingStatus }
                .distinct()

            val currentStatuses = passengers
                .map { it.currentStatus }
                .distinct()

            val details = PnrDetails(
                departureTime = optionalString(
                    data, "departureTime"
                ),
                arrivalDate = optionalString(
                    data, "arrivalDate"
                ),
                arrivalTime = optionalString(
                    data, "arrivalTime"
                ),
                duration = optionalString(
                    data, "duration"
                ),
                sourceStationName = optionalString(
                    data, "sourceStationName"
                ),
                destinationStationName = optionalString(
                    data, "destinationStationName"
                ),
                boardingPoint = optionalString(
                    data, "boardingPoint"
                ),
                reservationUpto = optionalString(
                    data, "reservationUpto"
                ),
                journeyClass = optionalString(
                    data, "journeyClass"
                ),
                quota = optionalString(
                    data, "quota"
                ),
                distanceKm = optionalDouble(
                    data, "distanceKm"
                ),
                bookingFare = optionalDouble(
                    data, "bookingFare"
                ),
                ticketFare = optionalDouble(
                    data, "ticketFare"
                ),
                bookingDate = optionalString(
                    data, "bookingDate"
                ),
                vikalpStatus = optionalString(
                    data, "vikalpStatus"
                ),
                ticketType = optionalString(
                    data, "ticketType"
                ),
                provider = optionalString(
                    data, "provider"
                ),
                providerTimestamp = optionalString(
                    data, "providerTimestamp"
                )
            )

            val checkedAtMillis = try {
                val timestamp = optionalString(
                    root, "checkedAtUtc"
                )

                if (timestamp == null) {
                    System.currentTimeMillis()
                } else {
                    Instant.parse(timestamp).toEpochMilli()
                }
            } catch (_: Exception) {
                System.currentTimeMillis()
            }

            return PnrInfo(
                pnrNumber = requestedPnr,
                trainNumber = trainNumber,
                trainName = trainName,
                journeyDate = journeyDate,
                fromStation = source,
                toStation = destination,
                bookingStatus =
                    bookingStatuses.singleOrNull() ?: "Mixed",
                currentStatus =
                    currentStatuses.singleOrNull() ?: "Mixed",
                chartStatus = optionalString(
                    data, "chartStatus"
                ),
                passengers = passengers,
                lastCheckedAtMillis = checkedAtMillis,
                details = details
            )
        } catch (_: Exception) {
            throw PnrCheckException(
                "Invalid PNR response. Saved information was not changed."
            )
        }
    }

    private fun parseJourneyDate(
        value: String
    ): LocalDate {
        return try {
            LocalDate.parse(value)
        } catch (_: Exception) {
            try {
                LocalDate.parse(value, dayFirstFormat)
            } catch (_: Exception) {
                LocalDateTime.parse(
                    value, legacyDateFormat
                ).toLocalDate()
            }
        }
    }

    private fun requiredString(
        obj: JsonObject,
        key: String
    ): String {
        val value = obj.get(key)

        if (
            value == null ||
            !value.isJsonPrimitive ||
            !value.asJsonPrimitive.isString
        ) {
            throw IllegalArgumentException()
        }

        return value.asString.trim().takeIf {
            it.isNotBlank() && it.length <= 200
        } ?: throw IllegalArgumentException()
    }

    private fun optionalString(
        obj: JsonObject,
        key: String
    ): String? {
        val value = obj.get(key)

        if (value == null || value.isJsonNull) {
            return null
        }

        if (!value.isJsonPrimitive) {
            return null
        }

        val primitive = value.asJsonPrimitive

        if (
            !primitive.isString &&
            !primitive.isNumber
        ) {
            return null
        }

        return value.asString.trim().takeIf {
            it.isNotBlank() && it.length <= 200
        }
    }

    private fun requiredInt(
        obj: JsonObject,
        key: String
    ): Int {
        return optionalInt(obj, key)
            ?: throw IllegalArgumentException()
    }

    private fun optionalInt(
        obj: JsonObject,
        key: String
    ): Int? {
        val value = obj.get(key)

        if (
            value == null ||
            value.isJsonNull ||
            !value.isJsonPrimitive
        ) {
            return null
        }

        return try {
            value.asInt
        } catch (_: Exception) {
            null
        }
    }

    private fun optionalDouble(
        obj: JsonObject,
        key: String
    ): Double? {
        val value = obj.get(key)

        if (
            value == null ||
            value.isJsonNull ||
            !value.isJsonPrimitive
        ) {
            return null
        }

        return try {
            value.asDouble.takeIf {
                it.isFinite() && it >= 0.0
            }
        } catch (_: Exception) {
            null
        }
    }
}
