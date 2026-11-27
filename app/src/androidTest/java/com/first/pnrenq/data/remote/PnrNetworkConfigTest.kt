package com.first.pnrenq.data.remote

import java.net.URI
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun baseUrlHasNoCredentialsOrFragment() {
        val uri = URI(PnrNetworkConfig.BASE_URL).parseServerAuthority()

        assertNull(uri.rawUserInfo)
        assertNull(uri.rawFragment)
    }

    @Test
    fun baseUrlHasValidHostAndPort() {
        val uri = URI(PnrNetworkConfig.BASE_URL).parseServerAuthority()

        assertTrue(uri.host?.isNotBlank() == true)
        assertTrue(uri.port == -1 || uri.port in 1..65535)
        assertFalse(uri.rawAuthority.endsWith(":"))
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