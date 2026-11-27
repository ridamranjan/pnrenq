package com.first.pnrenq.data.remote

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.Proxy
import java.net.ServerSocket
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class PnrNetworkClientTest {

    private val limit = PnrNetworkConfig.MAX_RESPONSE_BYTES.toInt()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val testRequest = Request.Builder()
        .url("https://example.invalid/health")
        .build()

    @Test
    fun clientPolicyMatchesConfiguration() {
        withClient(PnrNetworkClient.newHttpClientBuilder().build()) { client ->
            assertEquals(10_000, client.connectTimeoutMillis)
            assertEquals(15_000, client.readTimeoutMillis)
            assertEquals(15_000, client.writeTimeoutMillis)
            assertEquals(25_000, client.callTimeoutMillis)
            assertFalse(client.retryOnConnectionFailure)
            assertFalse(client.followRedirects)
            assertFalse(client.followSslRedirects)
            assertEquals(65_536, PnrNetworkConfig.MAX_RESPONSE_CHARS)
            assertEquals(262_148, limit)
            assertTrue(client.interceptors.any { it is PnrResponseLimitInterceptor })
        }
    }

    @Test
    fun normalSuccessPreservesBytesAndContentType() {
        val payload = "{\"message\":\"नमस्ते\"}".toByteArray(Charsets.UTF_8)
        val body = TrackingBody(payload, payload.size.toLong())

        withClient(fakeClient(200, body)) { client ->
            client.newCall(testRequest).execute().use { response ->
                assertEquals(200, response.code)
                assertEquals(jsonType, response.body!!.contentType())
                assertArrayEquals(payload, response.body!!.bytes())
            }
        }
        assertTrue(body.closed)
    }

    @Test
    fun exactByteLimitIsAccepted() {
        val payload = ByteArray(limit) { 65 }
        val body = TrackingBody(payload, -1L)

        withClient(fakeClient(200, body)) { client ->
            client.newCall(testRequest).execute().use { response ->
                assertArrayEquals(payload, response.body!!.bytes())
            }
        }
        assertTrue(body.closed)
    }

    @Test
    fun knownOversizedSuccessIsRejectedBeforeReading() {
        val body = TrackingBody(ByteArray(0), limit.toLong() + 1L)

        withClient(fakeClient(200, body)) { client ->
            assertThrows(PnrResponseTooLargeException::class.java) {
                runBlocking { healthService(client).health() }
            }
        }
        assertEquals(0L, body.bytesRead)
        assertTrue(body.closed)
    }

    @Test
    fun unknownOversizedSuccessStopsEarly() {
        val payload = ByteArray(limit * 4) { 65 }
        val body = TrackingBody(payload, -1L)

        withClient(fakeClient(200, body)) { client ->
            assertThrows(PnrResponseTooLargeException::class.java) {
                client.newCall(testRequest).execute().close()
            }
        }
        assertTrue(body.bytesRead > limit)
        assertTrue(body.bytesRead < payload.size)
        assertTrue(body.closed)
    }

    @Test
    fun oneByteAboveLimitIsRejectedWithoutContentLength() {
        val body = TrackingBody(ByteArray(limit + 1), -1L)

        withClient(fakeClient(200, body)) { client ->
            assertThrows(PnrResponseTooLargeException::class.java) {
                client.newCall(testRequest).execute().close()
            }
        }
        assertTrue(body.closed)
    }

    @Test
    fun normalErrorBodyAndStatusArePreservedThroughRetrofit() {
        val text = "{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\"}}"
        val payload = text.toByteArray(Charsets.UTF_8)
        val body = TrackingBody(payload, payload.size.toLong())

        withClient(fakeClient(429, body)) { client ->
            val response = runBlocking { healthService(client).health() }
            assertEquals(429, response.code())
            assertEquals(text, response.errorBody()!!.use { it.string() })
        }
        assertTrue(body.closed)
    }

    @Test
    fun oversizedKnownErrorPreservesStatusThroughRetrofit() {
        val body = TrackingBody(ByteArray(0), limit.toLong() + 1L)

        withClient(fakeClient(429, body)) { client ->
            val response = runBlocking { healthService(client).health() }
            assertEquals(429, response.code())
            assertEquals("", response.errorBody()!!.use { it.string() })
        }
        assertEquals(0L, body.bytesRead)
        assertTrue(body.closed)
    }

    @Test
    fun oversizedUnknownErrorStopsEarlyAndPreservesStatus() {
        val payload = ByteArray(limit * 4) { 65 }
        val body = TrackingBody(payload, -1L)

        withClient(fakeClient(503, body)) { client ->
            client.newCall(testRequest).execute().use { response ->
                assertEquals(503, response.code)
                assertEquals("", response.body!!.string())
            }
        }
        assertTrue(body.bytesRead > limit)
        assertTrue(body.bytesRead < payload.size)
        assertTrue(body.closed)
    }

    @Test
    fun emptySuccessBodyIsPreserved() {
        val body = TrackingBody(ByteArray(0), 0L)

        withClient(fakeClient(200, body)) { client ->
            client.newCall(testRequest).execute().use { response ->
                assertEquals("", response.body!!.string())
            }
        }
        assertTrue(body.closed)
    }

    @Test
    fun readFailureClosesOriginalBody() {
        val body = TrackingBody(ByteArray(1), -1L, failOnRead = true)

        withClient(fakeClient(200, body)) { client ->
            assertThrows(IOException::class.java) {
                client.newCall(testRequest).execute().close()
            }
        }
        assertTrue(body.closed)
    }

    @Test
    fun ordinaryGzipResponseIsDecoded() {
        val text = "{\"success\":true}"
        withLoopbackResponse("200 OK", gzip(text.toByteArray()), "Content-Encoding: gzip\r\n") { url ->
            withClient(loopbackClient()) { client ->
                client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    assertEquals(text, response.body!!.string())
                }
            }
        }
    }

    @Test
    fun gzipExpansionBeyondLimitIsRejected() {
        val compressed = gzip(ByteArray(limit + 1) { 65 })
        assertTrue(compressed.size < limit)

        withLoopbackResponse("200 OK", compressed, "Content-Encoding: gzip\r\n") { url ->
            withClient(loopbackClient()) { client ->
                assertThrows(PnrResponseTooLargeException::class.java) {
                    client.newCall(Request.Builder().url(url).build()).execute().close()
                }
            }
        }
    }

    @Test
    fun oversizedGzipErrorPreservesHttpStatus() {
        val compressed = gzip(ByteArray(limit + 1) { 65 })

        withLoopbackResponse("429 Too Many Requests", compressed, "Content-Encoding: gzip\r\n") { url ->
            withClient(loopbackClient()) { client ->
                client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                    assertEquals(429, response.code)
                    assertEquals("", response.body!!.string())
                }
            }
        }
    }

    @Test
    fun postRedirectIsReturnedInsteadOfFollowed() {
        withLoopbackResponse("307 Temporary Redirect", ByteArray(0), "Location: /unexpected\r\n") { url ->
            withClient(loopbackClient()) { client ->
                val request = Request.Builder().url(url)
                    .post("{\"pnr\":\"1234567890\"}".toRequestBody(jsonType))
                    .build()
                client.newCall(request).execute().use { response ->
                    assertEquals(307, response.code)
                    assertEquals(url, response.request.url.toString())
                    assertEquals("/unexpected", response.header("Location"))
                }
            }
        }
    }

    private fun fakeClient(code: Int, body: ResponseBody): OkHttpClient =
        PnrNetworkClient.newHttpClientBuilder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("Test response")
                    .body(body)
                    .build()
            }
            .build()

    private fun healthService(client: OkHttpClient): PnrHealthService =
        Retrofit.Builder()
            .baseUrl("https://example.invalid/")
            .client(client)
            .build()
            .create(PnrHealthService::class.java)

    private fun loopbackClient(): OkHttpClient =
        PnrNetworkClient.newHttpClientBuilder()
            .proxy(Proxy.NO_PROXY)
            .build()

    private fun withClient(client: OkHttpClient, block: (OkHttpClient) -> Unit) {
        try {
            block(client)
        } finally {
            client.dispatcher.cancelAll()
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        }
    }

    private inner class TrackingBody(
        payload: ByteArray,
        private val declaredLength: Long,
        private val failOnRead: Boolean = false
    ) : ResponseBody() {
        var bytesRead = 0L
            private set
        var closed = false
            private set

        private val trackedSource = object : ForwardingSource(Buffer().write(payload)) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                if (failOnRead) throw IOException("Synthetic read failure")
                val count = super.read(sink, byteCount)
                if (count > 0L) bytesRead += count
                return count
            }

            override fun close() {
                closed = true
                super.close()
            }
        }.buffer()

        override fun contentType(): MediaType = jsonType
        override fun contentLength(): Long = declaredLength
        override fun source(): BufferedSource = trackedSource
    }

    private fun gzip(payload: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(payload) }
        return output.toByteArray()
    }

    // JVM-only, single-request HTTP fixture. Never contacts the real backend.
    private fun withLoopbackResponse(
        status: String,
        payload: ByteArray,
        extraHeaders: String,
        block: (String) -> Unit
    ) {
        ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).use { server ->
            server.soTimeout = 5_000
            val task = FutureTask<Unit> {
                server.accept().use { socket ->
                    socket.soTimeout = 5_000
                    val reader = socket.getInputStream().bufferedReader(Charsets.US_ASCII)
                    check(!reader.readLine().isNullOrBlank())
                    var contentLength = 0
                    while (true) {
                        val line = reader.readLine() ?: error("Incomplete request")
                        if (line.isEmpty()) break
                        if (line.startsWith("Content-Length:", ignoreCase = true)) {
                            contentLength = line.substringAfter(':').trim().toInt()
                        }
                    }
                    // The fixture sends only ASCII request bodies with known length.
                    repeat(contentLength) { check(reader.read() != -1) }
                    val headers = "HTTP/1.1 $status\r\n" +
                            "Connection: close\r\n" +
                            "Content-Length: ${payload.size}\r\n" +
                            extraHeaders + "\r\n"
                    val output = socket.getOutputStream()
                    output.write(headers.toByteArray(Charsets.US_ASCII))
                    output.write(payload)
                    output.flush()
                }
            }
            val thread = Thread(task, "pnr-loopback-test").apply {
                isDaemon = true
                start()
            }
            try {
                block("http://127.0.0.1:${server.localPort}/health")
                task.get(5, TimeUnit.SECONDS)
            } finally {
                server.close()
                thread.join(5_000)
            }
        }
    }
}