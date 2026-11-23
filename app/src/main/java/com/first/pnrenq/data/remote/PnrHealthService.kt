
package com.first.pnrenq.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/**
 * Cloudflare Worker health-check endpoint.
 *
 * This does not check any PNR.
 * It does not establish provider availability.
 */
internal interface PnrHealthService {

    @GET("health")
    suspend fun health(): Response<ResponseBody>
}
