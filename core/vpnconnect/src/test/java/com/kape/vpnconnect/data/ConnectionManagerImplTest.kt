package com.kape.vpnconnect.data

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.data.ConnectionStatus
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.domain.ObfuscatorProcessInformation
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVpnTunnelError
import com.kape.portforwarding.data.PortForwardingManager
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import com.kape.regions.vpn.domain.usecase.ObserveIsDefaultVpnServerListUseCase
import com.kape.regions.vpn.domain.usecase.RefreshVpnServersUseCase
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.ConnectionProblemDetector
import com.kape.vpnconnect.platformsdk.PiaService
import com.kape.vpnconnect.utils.NotificationHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConnectionManagerImplTest {
    private val context: Context = mockk(relaxed = true)
    private val connectionSource: ConnectionDataSource = mockk(relaxed = true)
    private val connectionInfo: ConnectionInfoProviderImpl = mockk(relaxed = true)
    private val problemDetector: ConnectionProblemDetector = mockk(relaxed = true)
    private val connectionPrefs: ConnectionPrefs = mockk(relaxed = true)
    private val settingsPrefs: SettingsPrefs = mockk(relaxed = true)
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs = mockk()
    private val startObfuscator: StartObfuscatorProcess = mockk()
    private val stopObfuscator: StopObfuscatorProcess = mockk()
    private val portForwarding: PortForwardingManager = mockk(relaxed = true)
    private val isUserLoggedIn = FakeIsUserLoggedInUseCase()
    private val isDefaultVpnServerList: ObserveIsDefaultVpnServerListUseCase = mockk()
    private val refreshVpnServers: RefreshVpnServersUseCase = mockk(relaxed = true)
    private val getOptimalVpnServer: GetOptimalVpnServerUseCase = mockk()

    private val service: PiaService = mockk(relaxed = true)
    private val serviceStatus = MutableStateFlow(KapeVPNConnectionStatus.Disconnected)
    private val serviceTunnelError = MutableStateFlow<KapeVpnTunnelError?>(null)
    private val binder: PiaService.LocalBinder = mockk { every { getService() } returns service }
    private val serviceConnection = slot<ServiceConnection>()

    private lateinit var statusTracker: VpnStatusTracker

    @BeforeEach
    fun setUp() {
        every { context.getString(any()) } returns ""
        every { context.bindService(any(), capture(serviceConnection), any<Int>()) } returns true
        every { service.connectionStatus } returns serviceStatus
        every { service.lastTunnelError } returns serviceTunnelError
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
        coEvery { settingsPrefs.getOpenVpnSettingsNow() } returns OpenVpnSettings(transport = Transport.UDP)
        coEvery { settingsPrefs.getVpnExcludedAppsNow() } returns EXCLUDED_APPS
        coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns false
        coEvery { stopObfuscator() } returns Result.success(Unit)
    }

    private fun TestScope.createManager(): ConnectionManagerImpl {
        statusTracker = VpnStatusTracker(context, mockk<NotificationHandler>(relaxed = true), backgroundScope)
        return ConnectionManagerImpl(
            context = context,
            connectionSource = connectionSource,
            connectionInfo = connectionInfo,
            statusTracker = statusTracker,
            problemDetector = problemDetector,
            connectionPrefs = connectionPrefs,
            settingsPrefs = settingsPrefs,
            shadowsocksRegionPrefs = shadowsocksRegionPrefs,
            startObfuscator = startObfuscator,
            stopObfuscator = stopObfuscator,
            portForwarding = portForwarding,
            isUserLoggedIn = isUserLoggedIn,
            isDefaultVpnServerList = isDefaultVpnServerList,
            refreshVpnServers = refreshVpnServers,
            getOptimalVpnServer = getOptimalVpnServer,
            appScope = backgroundScope,
            mainDispatcher = UnconfinedTestDispatcher(testScheduler),
        )
    }

    private fun bindService() = serviceConnection.captured.onServiceConnected(mockk<ComponentName>(), binder)

    @Test
    fun `connect - protocol has no endpoints on the server - shows the dialog and does not connect`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            var dialogShown = false

            manager.connect(server(groups = setOf(VpnServer.ServerGroup.OPENVPN_UDP)), isManual = true, stopCallback = {}, showDialog = {
                dialogShown = true
            })

            assertTrue(dialogShown)
            verify { problemDetector.onProtocolNotAvailable() }
            coVerify(exactly = 0) { connectionPrefs.setSelectedVpnServer(any()) }
            verify(exactly = 0) { context.bindService(any(), any(), any<Int>()) }
            assertFalse(manager.isConnectionInProgress())
        }

    @Test
    fun `connect - OpenVPN over TCP picks the TCP endpoints for the availability check`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.OpenVPN
            coEvery { settingsPrefs.getOpenVpnSettingsNow() } returns OpenVpnSettings(transport = Transport.TCP)
            val manager = createManager()
            var dialogShown = false

            manager.connect(server(groups = setOf(VpnServer.ServerGroup.OPENVPN_UDP)), isManual = true, stopCallback = {}, showDialog = {
                dialogShown = true
            })

            assertTrue(dialogShown)
        }

    @Test
    fun `connect - Automatic protocol skips the endpoint check`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic
            val manager = createManager()

            launch { manager.connect(server(groups = emptySet()), isManual = true, stopCallback = {}, showDialog = {}) }

            verify(exactly = 0) { problemDetector.onProtocolNotAvailable() }
            verify { context.bindService(any(), any(), any<Int>()) }
            bindService()
        }

    @Test
    fun `connect - binds the service, starts the vpn with excluded apps and saves the server`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            val server = server()

            val connecting = launch { manager.connect(server, isManual = true, stopCallback = {}, showDialog = {}) }

            assertTrue(manager.isConnectionInProgress())
            assertFalse(connecting.isCompleted)
            coVerify(exactly = 0) { service.startVpn(any()) }

            bindService()

            assertTrue(connecting.isCompleted)
            coVerify { service.startVpn(EXCLUDED_APPS) }
            verify { connectionInfo.updateInfo(SERVER_NAME, SERVER_ISO, true) }
            coVerify { connectionPrefs.setSelectedVpnServer(server) }
            coVerify { connectionPrefs.addToQuickConnect(SERVER_KEY, false) }
            verify(exactly = 1) { context.startService(any()) }
        }

    @Test
    fun `connect - service already bound - reuses it without binding again`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()

            manager.connect(server(), isManual = false, stopCallback = {}, showDialog = {})

            verify(exactly = 1) { context.bindService(any(), any(), any<Int>()) }
            coVerify(exactly = 2) { service.startVpn(EXCLUDED_APPS) }
        }

    @Test
    fun `bound service - forwards its status and tunnel errors to the tracker`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()

            serviceStatus.value = KapeVPNConnectionStatus.Connected
            serviceTunnelError.value = KapeVpnTunnelError.VpnPermissionRevoked

            assertTrue(manager.isConnected())
            assertEquals(KapeVpnTunnelError.VpnPermissionRevoked, statusTracker.lastTunnelError.value)
        }

    @Test
    fun `service disconnects unexpectedly - status goes back to disconnected and stops being collected`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()
            serviceStatus.value = KapeVPNConnectionStatus.Connected

            serviceConnection.captured.onServiceDisconnected(mockk())
            serviceStatus.value = KapeVPNConnectionStatus.Reconnecting

            assertEquals(ConnectionStatus.DISCONNECTED, statusTracker.status.value)
        }

    @Test
    fun `connect - shadowsocks enabled - starts the obfuscator with the selected server before the vpn`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns true
            coEvery { shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() } returns SHADOWSOCKS_SERVER
            coEvery { startObfuscator(any(), any()) } returns Result.success(Unit)
            val manager = createManager()

            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()

            coVerifyOrder {
                startObfuscator(ObfuscatorProcessInformation("5.6.7.8", "443", "secret", "aes-256-gcm"), any())
                service.startVpn(EXCLUDED_APPS)
            }
        }

    @Test
    fun `connect - shadowsocks enabled without a selected server - aborts`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns true
            coEvery { shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() } returns null
            val manager = createManager()

            manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {})

            assertFalse(manager.isConnectionInProgress())
            verify(exactly = 0) { context.bindService(any(), any(), any<Int>()) }
        }

    @Test
    fun `connect - obfuscator fails to start - aborts`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.isShadowsocksObfuscationEnabledNow() } returns true
            coEvery { shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() } returns SHADOWSOCKS_SERVER
            coEvery { startObfuscator(any(), any()) } returns Result.failure(IllegalStateException())
            val manager = createManager()

            manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {})

            assertFalse(manager.isConnectionInProgress())
            verify(exactly = 0) { context.bindService(any(), any(), any<Int>()) }
        }

    @Test
    fun `disconnect - after connecting - tears everything down in order`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()
            serviceStatus.value = KapeVPNConnectionStatus.Connected

            manager.disconnect()

            coVerifyOrder {
                problemDetector.onUserInitiatedDisconnect()
                service.stopSessionController(any())
                context.unbindService(serviceConnection.captured)
                context.stopService(any())
                stopObfuscator()
                connectionSource.stopPortForwarding()
                portForwarding.clearBindPort()
                connectionInfo.resetConnectionInfo()
            }
            assertEquals(ConnectionStatus.DISCONNECTED, statusTracker.status.value)
            assertFalse(manager.isConnectionInProgress())
        }

    @Test
    fun `disconnect - status updates from the old service are no longer collected`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()

            manager.disconnect()
            serviceStatus.value = KapeVPNConnectionStatus.Connected

            assertFalse(manager.isConnected())
        }

    @Test
    fun `disconnect - never connected - does not unbind`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()

            manager.disconnect()

            verify(exactly = 0) { context.unbindService(any()) }
            verify { context.stopService(any()) }
            verify { connectionSource.stopPortForwarding() }
        }

    @Test
    fun `disconnect - while waiting for the service to bind - cancels the pending start`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            val connecting = launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }

            manager.disconnect()

            assertTrue(connecting.isCompleted)
            coVerify(exactly = 0) { service.startVpn(any()) }
            verify { context.unbindService(serviceConnection.captured) }
            assertFalse(manager.isConnectionInProgress())
        }

    @Test
    fun `connect after disconnect - binds the service again`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()
            manager.disconnect()

            launch { manager.connect(server(), isManual = true, stopCallback = {}, showDialog = {}) }
            bindService()

            verify(exactly = 2) { context.bindService(any(), any(), any<Int>()) }
            coVerify(exactly = 2) { service.startVpn(EXCLUDED_APPS) }
        }

    @Test
    fun `reconnect - disconnects first, then connects manually to the new server`() =
        runTest(UnconfinedTestDispatcher()) {
            val manager = createManager()
            launch { manager.connect(server(), isManual = false, stopCallback = {}, showDialog = {}) }
            bindService()
            val newServer = server(key = "uk", name = "UK")

            launch { manager.reconnect(newServer, stopCallback = {}) }
            bindService()

            coVerifyOrder {
                connectionPrefs.addToQuickConnect("uk", false)
                service.stopSessionController(any())
                connectionInfo.resetConnectionInfo()
                connectionInfo.updateInfo("UK", SERVER_ISO, true)
                connectionPrefs.setSelectedVpnServer(newServer)
                service.startVpn(EXCLUDED_APPS)
            }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - logged out - does nothing`() =
        runTest(UnconfinedTestDispatcher()) {
            isUserLoggedIn.loggedIn = false
            val manager = createManager()

            manager.connectToLastKnownOrOptimalServer()

            coVerify(exactly = 0) { connectionPrefs.getSelectedVpnServerNow() }
            verify(exactly = 0) { context.bindService(any(), any(), any<Int>()) }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - automation after a user disconnect - only clears the flag`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.isAutomationEnabledNow() } returns true
            coEvery { connectionPrefs.isDisconnectedByUserNow() } returns true
            val manager = createManager()

            manager.connectToLastKnownOrOptimalServer()

            coVerify { connectionPrefs.setDisconnectedByUser(false) }
            verify(exactly = 0) { context.bindService(any(), any(), any<Int>()) }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - user disconnect without automation - still connects`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { settingsPrefs.isAutomationEnabledNow() } returns false
            coEvery { connectionPrefs.isDisconnectedByUserNow() } returns true
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns server()
            val manager = createManager()

            launch { manager.connectToLastKnownOrOptimalServer() }
            bindService()

            coVerify(exactly = 0) { connectionPrefs.setDisconnectedByUser(any()) }
            coVerify { service.startVpn(EXCLUDED_APPS) }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - last selected server - connects to it as non-manual`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns server()
            val manager = createManager()

            launch { manager.connectToLastKnownOrOptimalServer() }
            bindService()

            verify { connectionInfo.updateInfo(SERVER_NAME, SERVER_ISO, false) }
            coVerify(exactly = 0) { getOptimalVpnServer() }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - no server selected and default list - refreshes before picking the optimal one`() =
        runTest(UnconfinedTestDispatcher()) {
            val optimal = server(key = "optimal", name = "Optimal")
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns null
            every { isDefaultVpnServerList() } returns flowOf(true)
            coEvery { getOptimalVpnServer() } returns optimal
            val manager = createManager()

            launch { manager.connectToLastKnownOrOptimalServer() }
            bindService()

            coVerifyOrder {
                refreshVpnServers(any(), false, any())
                getOptimalVpnServer()
                connectionPrefs.setSelectedVpnServer(optimal)
            }
        }

    @Test
    fun `connectToLastKnownOrOptimalServer - no server selected and list already fetched - skips the refresh`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns null
            every { isDefaultVpnServerList() } returns flowOf(false)
            coEvery { getOptimalVpnServer() } returns server()
            val manager = createManager()

            launch { manager.connectToLastKnownOrOptimalServer() }
            bindService()

            coVerify(exactly = 0) { refreshVpnServers(any(), any(), any()) }
            coVerify { service.startVpn(EXCLUDED_APPS) }
        }

    private fun server(
        key: String = SERVER_KEY,
        name: String = SERVER_NAME,
        groups: Set<VpnServer.ServerGroup> = setOf(VpnServer.ServerGroup.WIREGUARD, VpnServer.ServerGroup.OPENVPN_UDP),
    ) = VpnServer(
        name = name,
        iso = SERVER_ISO,
        dns = "",
        latency = null,
        endpoints = groups.associateWith { listOf(VpnServer.ServerEndpointDetails(ip = "1.1.1.1", cn = "cn")) },
        key = key,
        latitude = null,
        longitude = null,
        isGeo = false,
        isOffline = false,
        allowsPortForwarding = false,
        autoRegion = false,
        dipToken = null,
        dedicatedIp = null,
    )

    private class FakeIsUserLoggedInUseCase : IsUserLoggedInUseCase {
        var loggedIn = true

        override suspend fun invoke(): Boolean = loggedIn
    }

    private companion object {
        const val SERVER_KEY = "de"
        const val SERVER_NAME = "Germany"
        const val SERVER_ISO = "DE"
        val EXCLUDED_APPS = listOf("com.example.excluded")
        val SHADOWSOCKS_SERVER = ShadowsocksServer(region = "NL", host = "5.6.7.8", port = 443, key = "secret", cipher = "aes-256-gcm")
    }
}