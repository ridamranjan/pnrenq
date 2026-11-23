
package com.first.pnrenq.data.remote

import com.first.pnrenq.BuildConfig

internal object PnrNetworkConfig {

    val BASE_URL: String =
        BuildConfig.PNR_API_BASE_URL

    const val CONNECT_TIMEOUT_SECONDS = 10L

    const val READ_TIMEOUT_SECONDS = 15L

    const val WRITE_TIMEOUT_SECONDS = 15L

    const val CALL_TIMEOUT_SECONDS = 25L
}
