package com.first.pnrenq.data.remote

import com.first.pnrenq.domain.model.PnrInfo
import com.first.pnrenq.domain.validation.isValidPnr
import com.google.gson.JsonParser
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


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

    private val service: LivePnrService by lazy {
        PnrNetworkClient.createService(LivePnrService::class.java)
    }

    suspend fun check(pnr: String): PnrInfo =
        withContext(Dispatchers.IO) {

            if (!isValidPnr(pnr)) {
                throw PnrCheckException(
                    "Enter a valid 10-digit PNR."
                )
            }

            val response = try {
                service.check(LivePnrRequest(pnr))
            } catch (e: CancellationException) {
                throw e
            } catch (_: PnrResponseTooLargeException) {
                throw PnrCheckException(
                    "Invalid response from the PNR service."
                )
            } catch (_: IOException) {
                throw PnrCheckException(
                    "Unable to connect. Check your internet connection."
                )
            }

            if (!response.isSuccessful) {
                val errorText = try {
                    response.errorBody()?.use {
                        it.string().take(PnrNetworkConfig.MAX_RESPONSE_CHARS)
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

                    if (result.length > PnrNetworkConfig.MAX_RESPONSE_CHARS) {
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

            PnrResponseParser.parse(responseText, pnr)
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
}