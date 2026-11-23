
package com.first.pnrenq.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PnrNetworkConfigTest {

    @Test
    fun baseUrlUsesHttps() {

        assertTrue(
            PnrNetworkConfig.BASE_URL.startsWith("https://")
        )
    }

    @Test
    fun baseUrlHasTrailingSlash() {

        assertTrue(
            PnrNetworkConfig.BASE_URL.endsWith("/")
        )
    }

    @Test
    fun baseUrlHasNoQueryParameters() {

        assertFalse(
            PnrNetworkConfig.BASE_URL.contains("?")
        )
    }

    @Test
    fun connectionTimeoutIsValid() {

        assertTrue(
            PnrNetworkConfig.CONNECT_TIMEOUT_SECONDS in 1L..30L
        )
    }

    @Test
    fun overallTimeoutIsBounded() {

        assertTrue(
            PnrNetworkConfig.CALL_TIMEOUT_SECONDS in 1L..60L
        )
    }

    @Test
    fun retrofitHealthServiceCanBeCreated() {

        val service = PnrNetworkClient.healthService

        assertNotNull(service)
    }
}
