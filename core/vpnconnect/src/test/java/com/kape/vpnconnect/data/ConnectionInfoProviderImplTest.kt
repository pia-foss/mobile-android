package com.kape.vpnconnect.data

import android.content.Context
import com.kape.contracts.ConnectionEvents
import com.kape.contracts.NetworkStatus
import com.kape.data.NO_IP
import com.kape.data.kpi.KpiConnectionStatus
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.portforwarding.data.PortForwardingManager
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.utils.NotificationHandler
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConnectionInfoProviderImplTest {
    private val context: Context = mockk { every { getString(any()) } returns "" }
    private val connectionPrefs: ConnectionPrefs =
        mockk {
            every { clientIp } returns MutableStateFlow(NO_IP)
            every { vpnIp } returns MutableStateFlow(NO_IP)
        }
    private val portForwarding: PortForwardingManager =
        mockk {
            every { portForwardingStatus } returns MutableStateFlow(PortForwardingStatus.NoPortForwarding)
            every { port } returns MutableStateFlow("")
        }
    private val networkStatus = FakeNetworkStatus()
    private val clientState = FakeClientStateDataSource()
    private val events = RecordingConnectionEvents()

    private lateinit var tracker: VpnStatusTracker

    private fun TestScope.createProvider(): ConnectionInfoProviderImpl {
        tracker = VpnStatusTracker(context, mockk<NotificationHandler>(relaxed = true), backgroundScope)
        return ConnectionInfoProviderImpl(tracker, clientState, connectionPrefs, portForwarding, networkStatus, events, backgroundScope)
    }

    @Test
    fun `starting disconnected - fetches the public ip`() =
        runTest(UnconfinedTestDispatcher()) {
            createProvider()

            assertEquals(1, clientState.publicIpRequests)
            assertEquals(0, clientState.vpnIpRequests)
        }

    @Test
    fun `connecting - cancels an in-flight public ip fetch`() =
        runTest(UnconfinedTestDispatcher()) {
            clientState.publicIpGate = CompletableDeferred()
            createProvider()

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connecting)

            assertEquals(1, clientState.cancelledPublicIpRequests)
        }

    @Test
    fun `connected - fetches the vpn ip`() =
        runTest(UnconfinedTestDispatcher()) {
            createProvider()

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connected)

            assertEquals(1, clientState.vpnIpRequests)
        }

    @Test
    fun `disconnected again after a connection - fetches the public ip again`() =
        runTest(UnconfinedTestDispatcher()) {
            createProvider()

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connected)
            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnected)

            assertEquals(2, clientState.publicIpRequests)
        }

    @Test
    fun `requestClientIp - while a fetch is in flight - does not start a second one`() =
        runTest(UnconfinedTestDispatcher()) {
            clientState.publicIpGate = CompletableDeferred()
            val provider = createProvider()

            provider.requestClientIp()
            provider.requestClientIp()

            assertEquals(1, clientState.publicIpRequests)
        }

    @Test
    fun `requestClientIp - after the previous fetch finished - starts a new one`() =
        runTest(UnconfinedTestDispatcher()) {
            val provider = createProvider()

            provider.requestClientIp()

            assertEquals(2, clientState.publicIpRequests)
        }

    @Test
    fun `back online while disconnected - refreshes the public ip`() =
        runTest(UnconfinedTestDispatcher()) {
            createProvider()

            networkStatus.isOnline.value = false
            networkStatus.isOnline.value = true

            assertEquals(2, clientState.publicIpRequests)
        }

    @Test
    fun `back online while connected - does not refresh the public ip`() =
        runTest(UnconfinedTestDispatcher()) {
            createProvider()
            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connected)

            networkStatus.isOnline.value = false
            networkStatus.isOnline.value = true

            assertEquals(1, clientState.publicIpRequests)
        }

    @Test
    fun `sdk status changes - are reported as kpi events with the current manual flag`() =
        runTest(UnconfinedTestDispatcher()) {
            val provider = createProvider()
            provider.updateInfo(name = "Germany", iso = "de", isManual = true)

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connecting)
            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connected)
            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Reconnecting)
            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Paused)
            provider.resetConnectionInfo()
            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnecting)
            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnected)

            assertEquals(
                listOf(
                    KpiConnectionStatus.Connecting to true,
                    KpiConnectionStatus.Connected to true,
                    KpiConnectionStatus.Reconnecting to true,
                    KpiConnectionStatus.Paused to true,
                    KpiConnectionStatus.NotConnected to false,
                    KpiConnectionStatus.NotConnected to false,
                ),
                events.reported,
            )
        }

    @Test
    fun `no sdk status yet - reports no kpi event`() =
        runTest(UnconfinedTestDispatcher()) {
            createProvider()

            assertTrue(events.reported.isEmpty())
        }

    @Test
    fun `connection state helpers - reflect the tracked status`() =
        runTest(UnconfinedTestDispatcher()) {
            val provider = createProvider()

            assertFalse(provider.isConnected())
            assertFalse(provider.isInConnectState())
            assertFalse(provider.shouldAllowDisconnect())

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connecting)
            assertFalse(provider.isConnected())
            assertTrue(provider.isInConnectState())
            assertTrue(provider.shouldAllowDisconnect())

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Connected)
            assertTrue(provider.isConnected())
            assertTrue(provider.isInConnectState())

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Reconnecting)
            assertTrue(provider.isInConnectState())

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Paused)
            assertFalse(provider.isInConnectState())
            assertTrue(provider.shouldAllowDisconnect())

            tracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnecting)
            assertFalse(provider.shouldAllowDisconnect())
        }

    @Test
    fun `updateInfo and resetConnectionInfo - set and clear the server details`() =
        runTest(UnconfinedTestDispatcher()) {
            val provider = createProvider()

            provider.updateInfo(name = "Germany", iso = "de", isManual = true)
            assertEquals("Germany", provider.name)
            assertEquals("de", provider.iso)
            assertTrue(provider.isManual)

            provider.resetConnectionInfo()
            assertEquals("", provider.name)
            assertEquals("", provider.iso)
            assertFalse(provider.isManual)
        }

    private class FakeNetworkStatus : NetworkStatus {
        override val isOnline = MutableStateFlow(true)
    }

    private class FakeClientStateDataSource : ClientStateDataSource {
        var publicIpGate: CompletableDeferred<Unit>? = null
        var publicIpRequests = 0
        var cancelledPublicIpRequests = 0
        var vpnIpRequests = 0

        override suspend fun getPublicIp(): String {
            publicIpRequests++
            try {
                publicIpGate?.await()
            } catch (e: CancellationException) {
                cancelledPublicIpRequests++
                throw e
            }
            return "1.2.3.4"
        }

        override suspend fun getVpnIp(): String {
            vpnIpRequests++
            return "10.0.0.1"
        }
    }

    private class RecordingConnectionEvents : ConnectionEvents {
        val reported = mutableListOf<Pair<KpiConnectionStatus, Boolean>>()

        override suspend fun connectionStatusChanged(
            status: KpiConnectionStatus,
            isManual: Boolean,
        ) {
            reported += status to isManual
        }
    }
}