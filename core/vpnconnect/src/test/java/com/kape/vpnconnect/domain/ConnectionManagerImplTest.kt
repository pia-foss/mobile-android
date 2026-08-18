package com.kape.vpnconnect.domain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.core.content.ContextCompat
import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.DI
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.platformsdk.PiaService
import com.kape.vpnregions.utils.RegionListProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * PiaService is the real VpnService that ConnectionManagerImpl binds to and drives — it's mocked
 * here (not the [ConnectionDataSource]) since that's now where connect/disconnect actually lands.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionManagerImplTest {
    private val connectionSource = mockk<ConnectionDataSource>(relaxed = true)
    private val connectionInfoProvider = mockk<ConnectionInfoProvider>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val shadowsocksRegionPrefs = mockk<ShadowsocksRegionPrefs>(relaxed = true)
    private val startObfuscatorProcess = mockk<StartObfuscatorProcess>(relaxed = true)
    private val stopObfuscatorProcess = mockk<StopObfuscatorProcess>(relaxed = true)
    private val portForwardingUseCase = mockk<PortForwardingUseCase>(relaxed = true)
    private val connectionStatusProvider = mockk<ConnectionStatusProvider>(relaxed = true)
    private val regionListProvider = mockk<RegionListProvider>(relaxed = true)
    private val authenticationDataSource = mockk<AuthenticationDataSource>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    private val piaService = mockk<PiaService>(relaxed = true)
    private val localBinder = mockk<PiaService.LocalBinder>()

    private val appModule =
        module {
            single { connectionSource }
            single { connectionInfoProvider }
            single { connectionPrefs }
            single { settingsPrefs }
            single { shadowsocksRegionPrefs }
            single { startObfuscatorProcess }
            single { stopObfuscatorProcess }
            single { portForwardingUseCase }
            single { connectionStatusProvider }
            single { regionListProvider }
            single { authenticationDataSource }
            single { context }
            single<CoroutineScope>(named(DI.IO_SCOPE)) { CoroutineScope(Dispatchers.Unconfined) }
        }

    // Default server with a WireGuard endpoint (matches @BeforeEach selectedProtocol stub).
    private val server =
        VpnServer(
            name = "US East",
            iso = "us",
            dns = "us-east.example.com",
            latency = null,
            endpoints =
                mapOf(
                    VpnServer.ServerGroup.WIREGUARD to
                        listOf(VpnServer.ServerEndpointDetails("1.2.3.4", "us-east.example.com")),
                ),
            key = "us-east",
            latitude = null,
            longitude = null,
            isGeo = false,
            isOffline = false,
            allowsPortForwarding = false,
            autoRegion = false,
            dipToken = null,
            dedicatedIp = null,
        )

    private lateinit var connectionManager: ConnectionManagerImpl

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()
        startKoin { modules(appModule) }

        every { settingsPrefs.selectedProtocol.value } returns VpnProtocols.WireGuard
        coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns false
        coEvery { settingsPrefs.getSelectedDnsOptionNow() } returns DnsOptions.PIA
        coEvery { settingsPrefs.getVpnExcludedAppsNow() } returns emptyList()
        coEvery { settingsPrefs.isAutomationEnabledNow() } returns false
        every { authenticationDataSource.isUserLoggedIn() } returns true
        every { connectionPrefs.isDisconnectedByUser.value } returns false
        coEvery { connectionPrefs.getSelectedVpnServerNow() } returns null

        every { localBinder.getService() } returns piaService
        every { piaService.connectionStatus } returns MutableStateFlow(KapeVPNConnectionStatus.Disconnected)
        every {
            context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>())
        } answers {
            secondArg<ServiceConnection>().onServiceConnected(mockk<ComponentName>(), localBinder)
            true
        }

        mockkStatic(ContextCompat::class)
        every { ContextCompat.startForegroundService(any(), any()) } just Runs

        connectionManager = ConnectionManagerImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
        stopKoin()
        Dispatchers.resetMain()
    }

    // region connect

    @Test
    fun `connect - no endpoints for selected protocol - calls showDialog and never binds the service`() =
        runTest {
            val serverNoEndpoints = server.copy(endpoints = emptyMap())
            var dialogShown = false

            connectionManager.connect(serverNoEndpoints, isManual = true, {}) { dialogShown = true }

            assertTrue(dialogShown)
            verify(exactly = 0) { context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>()) }
        }

    @Test
    fun `connect - OpenVPN UDP endpoints - resolves OPENVPN_UDP server group and starts the VPN`() =
        runTest {
            val openvpnUdpServer =
                server.copy(
                    endpoints =
                        mapOf(
                            VpnServer.ServerGroup.OPENVPN_UDP to
                                listOf(VpnServer.ServerEndpointDetails("1.2.3.4", "us-east.example.com")),
                        ),
                )
            every { settingsPrefs.selectedProtocol.value } returns VpnProtocols.OpenVPN
            every { settingsPrefs.openVpnSettings.value } returns OpenVpnSettings(transport = Transport.UDP)

            connectionManager.connect(openvpnUdpServer, isManual = false, {}) {}

            coVerify { piaService.startVpn(DnsOptions.PIA, emptyList()) }
        }

    @Test
    fun `connect - OpenVPN TCP endpoints - resolves OPENVPN_TCP server group and starts the VPN`() =
        runTest {
            val openvpnTcpServer =
                server.copy(
                    endpoints =
                        mapOf(
                            VpnServer.ServerGroup.OPENVPN_TCP to
                                listOf(VpnServer.ServerEndpointDetails("1.2.3.4", "us-east.example.com")),
                        ),
                )
            every { settingsPrefs.selectedProtocol.value } returns VpnProtocols.OpenVPN
            every { settingsPrefs.openVpnSettings.value } returns OpenVpnSettings(transport = Transport.TCP)

            connectionManager.connect(openvpnTcpServer, isManual = false, {}) {}

            coVerify { piaService.startVpn(DnsOptions.PIA, emptyList()) }
        }

    @Test
    fun `connect - valid endpoints - updates connection info and persists selected server before starting`() =
        runTest {
            connectionManager.connect(server, isManual = true, {}) {}

            verify { connectionInfoProvider.updateInfo(server.name, server.iso, true) }
            coVerify { connectionPrefs.setSelectedVpnServer(server) }
            coVerify { connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp) }
        }

    @Test
    fun `connect - valid endpoints - binds PiaService and starts the VPN with resolved DNS and excluded apps`() =
        runTest {
            coEvery { settingsPrefs.getSelectedDnsOptionNow() } returns DnsOptions.CUSTOM
            coEvery { settingsPrefs.getVpnExcludedAppsNow() } returns listOf("com.example.app")

            connectionManager.connect(server, isManual = false, {}) {}

            verify { ContextCompat.startForegroundService(context, any()) }
            coVerify { piaService.startVpn(DnsOptions.CUSTOM, listOf("com.example.app")) }
        }

    @Test
    fun `connect - shadowsocks enabled - no server selected - returns early without starting the service`() =
        runTest {
            coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns true
            coEvery { shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() } returns null

            connectionManager.connect(server, isManual = false, {}) {}

            verify(exactly = 0) { context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>()) }
            assertFalse(connectionManager.isConnectionInProgress())
        }

    @Test
    fun `connect - shadowsocks enabled - server found and obfuscator succeeds - proceeds to start the service`() =
        runTest {
            val shadowsocksServer =
                ShadowsocksServer(
                    region = "US East",
                    host = "127.0.0.1",
                    port = 1080,
                    key = "test-key",
                    cipher = "aes-256-gcm",
                )
            coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns true
            coEvery { shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() } returns shadowsocksServer
            coEvery { startObfuscatorProcess(any(), any()) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = false, {}) {}

            coVerify { piaService.startVpn(any(), any()) }
        }

    @Test
    fun `connect - shadowsocks enabled - obfuscator fails - returns early without starting the service`() =
        runTest {
            val shadowsocksServer =
                ShadowsocksServer(
                    region = "US East",
                    host = "127.0.0.1",
                    port = 1080,
                    key = "test-key",
                    cipher = "aes-256-gcm",
                )
            coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns true
            coEvery { shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() } returns shadowsocksServer
            coEvery { startObfuscatorProcess(any(), any()) } returns Result.failure(RuntimeException("obfuscator failed"))

            connectionManager.connect(server, isManual = false, {}) {}

            verify(exactly = 0) { context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>()) }
            assertFalse(connectionManager.isConnectionInProgress())
        }

    // endregion

    // region connectToLastKnownOrOptimalServer

    @Test
    fun `connectToLastKnownOrOptimalServer - not logged in - does not connect`() =
        runTest {
            every { authenticationDataSource.isUserLoggedIn() } returns false

            connectionManager.connectToLastKnownOrOptimalServer()

            verify(exactly = 0) { context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>()) }
            coVerify(exactly = 0) { regionListProvider.getOptimalServer() }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - automation enabled and user disconnected - resets flag without connecting`() =
        runTest {
            coEvery { settingsPrefs.isAutomationEnabledNow() } returns true
            every { connectionPrefs.isDisconnectedByUser.value } returns true

            connectionManager.connectToLastKnownOrOptimalServer()

            coVerify { connectionPrefs.setDisconnectedByUser(false) }
            verify(exactly = 0) { context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>()) }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - no selected server and stale list - refreshes latencies then connects to optimal server`() =
        runTest {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns null
            every { regionListProvider.isDefaultList } returns MutableStateFlow(true)
            coEvery {
                regionListProvider.updateServerLatencies(isConnected = false, isUserInitiated = false)
            } returns listOf(server)
            coEvery { regionListProvider.getOptimalServer() } returns server

            connectionManager.connectToLastKnownOrOptimalServer()

            coVerify { regionListProvider.updateServerLatencies(isConnected = false, isUserInitiated = false) }
            coVerify { connectionPrefs.setSelectedVpnServer(server) }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - no selected server and fresh list - connects to optimal server`() =
        runTest {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns null
            every { regionListProvider.isDefaultList } returns MutableStateFlow(false)
            coEvery { regionListProvider.getOptimalServer() } returns server

            connectionManager.connectToLastKnownOrOptimalServer()

            coVerify(exactly = 0) { regionListProvider.updateServerLatencies(any(), any()) }
            coVerify { connectionPrefs.setSelectedVpnServer(server) }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - server already selected - connects directly without consulting region list`() =
        runTest {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns server

            connectionManager.connectToLastKnownOrOptimalServer()

            coVerify(exactly = 0) { regionListProvider.getOptimalServer() }
            coVerify { connectionPrefs.setSelectedVpnServer(server) }
        }

    // endregion

    // region disconnect

    @Test
    fun `disconnect - previously connected - stops session, unbinds, resets state, cancels port forwarding`() =
        runTest {
            connectionManager.connect(server, isManual = false, {}) {}

            connectionManager.disconnect()

            coVerify { piaService.stopSessionController() }
            verify { context.unbindService(any()) }
            verify { connectionInfoProvider.resetConnectionInfo() }
            coVerify { stopObfuscatorProcess() }
            verify { connectionSource.stopPortForwarding() }
            verify { portForwardingUseCase.clearBindPort() }
            assertFalse(connectionManager.isConnectionInProgress())
        }

    @Test
    fun `disconnect - never connected - resets info and cancels port forwarding without unbinding`() =
        runTest {
            connectionManager.disconnect()

            verify(exactly = 0) { context.unbindService(any()) }
            verify { connectionInfoProvider.resetConnectionInfo() }
            verify { connectionSource.stopPortForwarding() }
        }

    // endregion

    // region isConnectionInProgress

    @Test
    fun `isConnectionInProgress - initially false`() {
        assertFalse(connectionManager.isConnectionInProgress())
    }

    // endregion

    // region reconnect

    @Test
    fun `reconnect - records server in quick connect history immediately`() =
        runTest {
            connectionManager.reconnect(server) {}

            coVerify { connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp) }
        }

    @Test
    fun `reconnect - disconnects the existing connection then connects to the requested server`() =
        runTest {
            connectionManager.connect(server, isManual = false, {}) {}

            connectionManager.reconnect(server) {}

            coVerify { piaService.stopSessionController() }
            coVerify(atLeast = 1) { piaService.startVpn(any(), any()) }
        }

    @Test
    fun `reconnect - previous attempt still awaiting service bind - drops it and connects to the new server`() =
        runTest {
            val server2 = server.copy(name = "EU West", iso = "eu", key = "eu-west")

            // Bind never completes for this attempt, so connect() is left suspended awaiting it.
            every {
                context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>())
            } returns true

            val firstConnect =
                launch(Dispatchers.Unconfined) {
                    connectionManager.connect(server, isManual = true, {}) {}
                }

            assertFalse(firstConnect.isCompleted)
            coVerify(exactly = 0) { piaService.startVpn(any(), any()) }

            // Restore normal bind behavior so the reconnect's own attempt can proceed.
            every {
                context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>())
            } answers {
                secondArg<ServiceConnection>().onServiceConnected(mockk<ComponentName>(), localBinder)
                true
            }

            connectionManager.reconnect(server2) {}

            assertTrue(firstConnect.isCompleted)
            coVerify { connectionPrefs.setSelectedVpnServer(server2) }
            coVerify { piaService.startVpn(any(), any()) }
        }

    @Test
    fun `reconnect - when server is updated after the first request - both are recorded in quick connect history`() =
        runTest {
            val server2 = server.copy(name = "EU West", iso = "eu", key = "eu-west")

            connectionManager.reconnect(server) {}
            connectionManager.reconnect(server2) {}

            coVerify { connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp) }
            coVerify { connectionPrefs.addToQuickConnect(server2.key, server2.isDedicatedIp) }
        }

    // endregion
}