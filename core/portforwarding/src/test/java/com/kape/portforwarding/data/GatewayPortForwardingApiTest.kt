package com.kape.portforwarding.data

import android.net.Uri
import android.util.Base64
import com.kape.network.pinning.EndpointIdentity
import com.kape.network.pinning.PinnedHttpClientFactory
import com.kape.network.pinning.SocketProtector
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GatewayPortForwardingApiTest {
    private val engine = FakeEngine()
    private val clients = FakeClientFactory(engine)
    private val api = GatewayPortForwardingApi(clients, userAgent = "pia-test")
    private val gateway = listOf(EndpointIdentity("10.0.0.1", "gateway-cn"), EndpointIdentity("10.0.0.2", "other-cn"))

    @BeforeEach
    fun setUp() {
        // android.net.Uri and android.util.Base64 are stubs on the JVM, so give them working behaviour.
        mockkStatic(Uri::class, Base64::class)
        every { Uri.parse(any()) } answers { fakeUri(firstArg()) }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64
                .getDecoder()
                .decode(firstArg<String>())
        }
    }

    @AfterEach
    fun tearDown() = unmockkAll()

    @Test
    fun `requests a signature from the first gateway with the VPN token, through a client pinned to all gateways`() =
        runTest {
            engine.respond(HttpStatusCode.OK, signatureBody(status = "OK", port = 47047))

            val information = api.getPayloadAndSignature("vpn-token", gateway)

            assertEquals(
                "https://10.0.0.1:19999/getSignature?token=vpn-token",
                engine.requests
                    .single()
                    .url
                    .toString(),
            )
            assertEquals("pia-test", engine.requests.single().headers[HttpHeaders.UserAgent])
            assertEquals(listOf(gateway), clients.requestedEndpoints)
            assertEquals(listOf(null), clients.socketProtectors)
            assertEquals("signature", information!!.signature)
            assertEquals(47047, information.decodedPayload.port)
        }

    @Test
    fun `returns no signature when the gateway answers with a status other than OK`() =
        runTest {
            engine.respond(HttpStatusCode.OK, signatureBody(status = "ERROR", port = 47047))

            assertNull(api.getPayloadAndSignature("vpn-token", gateway))
        }

    @Test
    fun `returns no signature on an HTTP error`() =
        runTest {
            engine.respond(HttpStatusCode.Unauthorized, signatureBody(status = "OK", port = 47047))

            assertNull(api.getPayloadAndSignature("vpn-token", gateway))
        }

    @Test
    fun `returns no signature when the response cannot be parsed`() =
        runTest {
            engine.respond(HttpStatusCode.OK, "<html>")

            assertNull(api.getPayloadAndSignature("vpn-token", gateway))
        }

    @Test
    fun `returns no signature when the request fails`() =
        runTest {
            engine.failure = IllegalStateException("unreachable")

            assertNull(api.getPayloadAndSignature("vpn-token", gateway))
        }

    @Test
    fun `makes no request without a gateway`() =
        runTest {
            assertNull(api.getPayloadAndSignature("vpn-token", emptyList()))
            assertFalse(api.bindPort("token", "payload", "signature", emptyList()))

            assertTrue(engine.requests.isEmpty())
            assertTrue(clients.requestedEndpoints.isEmpty())
        }

    @Test
    fun `binds the port with the payload and signature, authorised by the token`() =
        runTest {
            engine.respond(HttpStatusCode.OK, "{}")

            assertTrue(api.bindPort("auth-token", "the-payload", "the-signature", gateway))

            val request = engine.requests.single()
            assertEquals("https://10.0.0.1:19999/bindPort?payload=the-payload&signature=the-signature", request.url.toString())
            assertEquals("auth-token", request.headers[HttpHeaders.Authorization])
            assertEquals("pia-test", request.headers[HttpHeaders.UserAgent])
            assertEquals(listOf(gateway), clients.requestedEndpoints)
        }

    @Test
    fun `reports a failed bind on an HTTP error or a failed request`() =
        runTest {
            engine.respond(HttpStatusCode.InternalServerError, "")
            assertFalse(api.bindPort("auth-token", "payload", "signature", gateway))

            engine.failure = IllegalStateException("unreachable")
            assertFalse(api.bindPort("auth-token", "payload", "signature", gateway))
        }

    private fun signatureBody(
        status: String,
        port: Int,
    ): String {
        val payload =
            java.util.Base64
                .getEncoder()
                .encodeToString("""{"port":$port,"token":"pf-token","expires_at":"2030-01-01T00:00:00"}""".toByteArray())
        return """{"status":"$status","payload":"$payload","signature":"signature","extra":1}"""
    }

    private fun fakeUri(base: String): Uri {
        val query = mutableListOf<String>()
        val builder = mockk<Uri.Builder>()
        every { builder.appendQueryParameter(any(), any()) } answers {
            query += "${firstArg<String>()}=${secondArg<String>()}"
            builder
        }
        every { builder.build() } answers {
            val built = mockk<Uri>()
            every { built.toString() } returns base + query.joinToString("&", prefix = "?")
            built
        }
        return mockk { every { buildUpon() } returns builder }
    }
}

private class FakeClientFactory(
    private val engine: FakeEngine,
) : PinnedHttpClientFactory {
    val requestedEndpoints = mutableListOf<List<EndpointIdentity>>()
    val socketProtectors = mutableListOf<SocketProtector?>()

    override fun create(
        knownEndpoints: List<EndpointIdentity>,
        socketProtector: SocketProtector?,
    ): HttpClient {
        requestedEndpoints += knownEndpoints
        socketProtectors += socketProtector
        return HttpClient(engine)
    }
}

private class FakeEngine : HttpClientEngineBase("fake") {
    override val config = HttpClientEngineConfig()
    val requests = mutableListOf<HttpRequestData>()
    var failure: Throwable? = null
    private var status = HttpStatusCode.OK
    private var body = ""

    fun respond(
        status: HttpStatusCode,
        body: String,
    ) {
        this.status = status
        this.body = body
    }

    @OptIn(InternalAPI::class)
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        requests += data
        failure?.let { throw it }
        return HttpResponseData(status, GMTDate(), Headers.Empty, HttpProtocolVersion.HTTP_1_1, ByteReadChannel(body), callContext())
    }
}