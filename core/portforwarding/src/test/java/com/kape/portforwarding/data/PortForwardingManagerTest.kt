package com.kape.portforwarding.data

import com.kape.connection.model.DecodedPayload
import com.kape.connection.model.PortBindInformation
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.network.pinning.EndpointIdentity
import com.kape.portforwarding.domain.PortForwardingApi
import com.kape.settings.data.VpnProtocols
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

internal class PortForwardingManagerTest {
    private val api = FakeApi()
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val useCase = PortForwardingManager(api, connectionPrefs, settingsPrefs)

    private val server =
        VpnServer(
            name = "Germany",
            iso = "DE",
            dns = "de.example",
            latency = null,
            endpoints =
                mapOf(
                    ServerGroup.WIREGUARD to listOf(ServerEndpointDetails("10.0.0.1:1337", "wg-cn")),
                    ServerGroup.OPENVPN_UDP to listOf(ServerEndpointDetails("10.0.0.2", "ovpn-cn")),
                ),
            key = "de",
            latitude = null,
            longitude = null,
            isGeo = false,
            isOffline = false,
            allowsPortForwarding = true,
            autoRegion = true,
            dipToken = null,
            dedicatedIp = null,
        )

    @BeforeEach
    fun setUp() {
        coEvery { connectionPrefs.getGatewayNow() } returns "10.1.0.1"
        coEvery { connectionPrefs.getSelectedVpnServerNow() } returns server
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
    }

    @Test
    fun `requests and binds a new port, checking the gateway against the protocol's common names`() =
        runTest {
            coEvery { connectionPrefs.getPortBindingInfoNow() } returns null
            api.assignment = binding(port = 47000, expiresInDays = 60)

            useCase.bindPort("vpn-token")

            assertEquals(PortForwardingStatus.Success, useCase.portForwardingStatus.value)
            assertEquals("47000", useCase.port.value)
            assertEquals(listOf(EndpointIdentity("10.1.0.1", "wg-cn")), api.identitiesSeen.first())
            coVerify { connectionPrefs.setPortBindingInformation(api.assignment) }
        }

    @Test
    fun `re-binds a stored port with more than a week left, keeping the same port`() =
        runTest {
            coEvery { connectionPrefs.getPortBindingInfoNow() } returns binding(port = 46000, expiresInDays = 30)

            useCase.bindPort("vpn-token")

            assertEquals("46000", useCase.port.value)
            assertEquals(0, api.assignmentRequests)
        }

    @Test
    fun `replaces a stored port that expires within a week`() =
        runTest {
            coEvery { connectionPrefs.getPortBindingInfoNow() } returns binding(port = 46000, expiresInDays = 3)
            api.assignment = binding(port = 47000, expiresInDays = 60)

            useCase.bindPort("vpn-token")

            assertEquals("47000", useCase.port.value)
            assertEquals(1, api.assignmentRequests)
        }

    @Test
    fun `fails without a gateway`() =
        runTest {
            coEvery { connectionPrefs.getGatewayNow() } returns ""

            useCase.bindPort("vpn-token")

            assertEquals(PortForwardingStatus.Error, useCase.portForwardingStatus.value)
        }

    @Test
    fun `accepts any of the server's common names with Automatic`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic
            coEvery { connectionPrefs.getPortBindingInfoNow() } returns null
            api.assignment = binding(port = 47000, expiresInDays = 60)

            useCase.bindPort("vpn-token")

            assertEquals(
                setOf("wg-cn", "ovpn-cn"),
                api.identitiesSeen
                    .first()
                    .map { it.commonName }
                    .toSet(),
            )
        }

    private fun binding(
        port: Int,
        expiresInDays: Long,
    ): PortBindInformation {
        val expires = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expiresInDays))
        val decoded = DecodedPayload(port, "token-$port", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(expires))
        return mockk {
            every { status } returns "OK"
            every { payload } returns "payload-$port"
            every { signature } returns "signature"
            every { decodedPayload } returns decoded
        }
    }
}

private class FakeApi : PortForwardingApi {
    var assignment: PortBindInformation? = null
    var assignmentRequests = 0
    val identitiesSeen = mutableListOf<List<EndpointIdentity>>()

    override suspend fun getPayloadAndSignature(
        vpnToken: String,
        gatewayIdentities: List<EndpointIdentity>,
    ): PortBindInformation? {
        assignmentRequests++
        identitiesSeen += gatewayIdentities
        return assignment
    }

    override suspend fun bindPort(
        token: String,
        payload: String,
        signature: String,
        gatewayIdentities: List<EndpointIdentity>,
    ): Boolean {
        identitiesSeen += gatewayIdentities
        return true
    }
}