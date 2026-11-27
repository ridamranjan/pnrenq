package com.first.pnrenq.data.remote

import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** A successful response exceeded the configured byte limit. */
internal class PnrResponseTooLargeException :
    IOException("PNR service response exceeds the configured size limit.")

/**
 * Limit response bodies before Retrofit can buffer them.
 * This is an application interceptor, so OkHttp's transparent gzip
 * decoding has already been applied when applicable.
 */
internal class PnrResponseLimitInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response

        val contentType = body.contentType()
        val limit = PnrNetworkConfig.MAX_RESPONSE_BYTES

        val bytes = body.use { original ->
            if (original.contentLength() > limit) {
                null
            } else {
                val source = original.source()

                // Request only enough bytes to determine whether the
                // response exceeds the limit, even without Content-Length.
                if (source.request(limit + 1L)) {
                    null
                } else {
                    // EOF has been reached, with at most limit bytes.
                    source.readByteString()
                }
            }
        }

        if (bytes == null) {
            if (response.isSuccessful) {
                throw PnrResponseTooLargeException()
            }

            // Preserve HTTP status handling without exposing an oversized
            // error body to Retrofit or the JSON parser.
            return response.newBuilder()
                .removeHeader("Content-Encoding")
                .removeHeader("Transfer-Encoding")
                .header("Content-Length", "0")
                .body(ByteArray(0).toResponseBody(contentType))
                .build()
        }

        return response.newBuilder()
            .body(bytes.toResponseBody(contentType))
            .build()
    }
}

/**
 * Shared networking client for the PNR enq app.
 *
 * No automatic polling.
 * No provider API keys.
 * No HTTP body logging.
 */
internal object PnrNetworkClient {

    internal fun newHttpClientBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectTimeout(
                PnrNetworkConfig.CONNECT_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .readTimeout(
                PnrNetworkConfig.READ_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                PnrNetworkConfig.WRITE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .callTimeout(
                PnrNetworkConfig.CALL_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(PnrResponseLimitInterceptor())

    private val httpClient: OkHttpClient by lazy {
        newHttpClientBuilder().build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(PnrNetworkConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    fun <T : Any> createService(serviceClass: Class<T>): T =
        retrofit.create(serviceClass)

    val healthService: PnrHealthService by lazy {
        createService(PnrHealthService::class.java)
    }
}