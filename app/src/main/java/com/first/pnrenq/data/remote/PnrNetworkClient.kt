
package com.first.pnrenq.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Shared networking client for the PNR enq app.
 *
 * No automatic polling.
 * No provider API keys.
 * No HTTP body logging.
 */
internal object PnrNetworkClient {

    private val httpClient: OkHttpClient by lazy {

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

            .build()
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

    val healthService: PnrHealthService by lazy {

        retrofit.create(
            PnrHealthService::class.java
        )
    }
}
