package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.vpnmanager.data.models.ClientConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionManagerImplTest {
    private val connectionSource = mockk<ConnectionDataSource>(relaxed = true)
    private val connectionInfoProvider = mockk<ConnectionInfoProvider>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val connectionConfigurationUseCase = mockk<ConnectionConfigurationUseCase>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val shadowsocksRegionPrefs = mockk<ShadowsocksRegionPrefs>(relaxed = true)
    private val startObfuscatorProcess = mockk<StartObfuscatorProcess>(relaxed = true)
    private val stopObfuscatorProcess = mockk<StopObfuscatorProcess>(relaxed = true)
    private val portForwardingUseCase = mockk<PortForwardingUseCase>(relaxed = true)
    private val connectionStatusProvider = mockk<ConnectionStatusProvider>(relaxed = true)

    private val appModule =
        module {
            single { connectionSource }
            single { connectionInfoProvider }
            single { connectionPrefs }
            single { connectionConfigurationUseCase }
            single { settingsPrefs }
            single { shadowsocksRegionPrefs }
            single { startObfuscatorProcess }
            single { stopObfuscatorProcess }
            single { portForwardingUseCase }
            single { connectionStatusProvider }
            single<CoroutineScope> { CoroutineScope(Dispatchers.Unconfined) }
        }

    // Default server with WireGuard endpoint (matches @BeforeEach selectedProtocol stub).
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
        stopKoin()
        startKoin { modules(appModule) }
        every { settingsPrefs.selectedProtocol.value } returns VpnProtocols.WireGuard
        every { settingsPrefs.isPortForwardingEnabled.value } returns false
        connectionManager = ConnectionManagerImpl()
    }

    // region connect

    @Test
    fun `connect - no endpoints for selected protocol - calls showDialog and skips startConnection`() =
        runTest {
            val serverNoEndpoints = server.copy(endpoints = emptyMap())
            val showDialogCalled = mutableListOf<Boolean>()

            connectionManager.connect(serverNoEndpoints, isManual = true, {}) {
                showDialogCalled.add(true)
            }

            assertTrue(showDialogCalled.isNotEmpty())
            coVerify(exactly = 0) { connectionSource.startConnection(any(), any()) }
        }

    @Test
    fun `connect - OpenVPN UDP - resolves OPENVPN_UDP server group`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
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
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(openvpnUdpServer) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(openvpnUdpServer, isManual = false, {}) {}

            coVerify { connectionSource.startConnection(clientConfig, connectionStatusProvider) }
        }

    @Test
    fun `connect - OpenVPN TCP - resolves OPENVPN_TCP server group`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
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
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(openvpnTcpServer) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(openvpnTcpServer, isManual = false, {}) {}

            coVerify { connectionSource.startConnection(clientConfig, connectionStatusProvider) }
        }

    @Test
    fun `connect - shadowsocks disabled - VPN succeeds - updates info, sets server and quick connect`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = true, {}) {}

            verify { connectionInfoProvider.updateInfo(server.name, server.iso, true) }
            verify { connectionPrefs.setSelectedVpnServer(server) }
            verify { connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp) }
        }

    @Test
    fun `connect - shadowsocks disabled - VPN succeeds - port forwarding disabled - does not start port forwarding`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { settingsPrefs.isPortForwardingEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = false, {}) {}

            coVerify(exactly = 0) { portForwardingUseCase.bindPort(any()) }
            verify(exactly = 0) { connectionSource.startPortForwarding() }
        }

    @Test
    fun `connect - shadowsocks disabled - VPN succeeds - port forwarding enabled - binds port and starts port forwarding`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { settingsPrefs.isPortForwardingEnabled.value } returns true
            every { connectionSource.getVpnToken() } returns "vpn_token"
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = false, {}) {}

            coVerify { portForwardingUseCase.bindPort("vpn_token") }
            verify { connectionSource.startPortForwarding() }
        }

    @Test
    fun `connect - shadowsocks disabled - VPN fails - calls disconnect`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery {
                connectionSource.startConnection(clientConfig, connectionStatusProvider)
            } returns Result.failure(RuntimeException("VPN failed"))

            connectionManager.connect(server, isManual = false, {}) {}

            coVerify { connectionSource.stopConnection() }
            coVerify { stopObfuscatorProcess() }
        }

    @Test
    fun `connect - shadowsocks enabled - no server selected - returns early without starting VPN`() =
        runTest {
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns true
            every { shadowsocksRegionPrefs.selectedShadowsocksServer.value } returns null

            connectionManager.connect(server, isManual = false, {}) {}

            coVerify(exactly = 0) { connectionSource.startConnection(any(), any()) }
        }

    @Test
    fun `connect - shadowsocks enabled - server found - obfuscator succeeds - proceeds to VPN`() =
        runTest {
            val shadowsocksServer =
                ShadowsocksServer(
                    region = "US East",
                    host = "127.0.0.1",
                    port = 1080,
                    key = "test-key",
                    cipher = "aes-256-gcm",
                )
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns true
            every { shadowsocksRegionPrefs.selectedShadowsocksServer.value } returns shadowsocksServer
            coEvery { startObfuscatorProcess(any(), any()) } returns Result.success(Unit)
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = false, {}) {}

            coVerify { connectionSource.startConnection(clientConfig, connectionStatusProvider) }
        }

    @Test
    fun `connect - shadowsocks enabled - obfuscator fails - returns early without starting VPN`() =
        runTest {
            val shadowsocksServer =
                ShadowsocksServer(
                    region = "US East",
                    host = "127.0.0.1",
                    port = 1080,
                    key = "test-key",
                    cipher = "aes-256-gcm",
                )
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns true
            every { shadowsocksRegionPrefs.selectedShadowsocksServer.value } returns shadowsocksServer
            coEvery { startObfuscatorProcess(any(), any()) } returns Result.failure(RuntimeException("obfuscator failed"))

            connectionManager.connect(server, isManual = false, {}) {}

            coVerify(exactly = 0) { connectionSource.startConnection(any(), any()) }
        }

    // endregion

    // region disconnect

    @Test
    fun `disconnect - success - stops connection, resets info, stops obfuscator, clears port forwarding`() =
        runTest {
            coEvery { connectionSource.stopConnection() } returns Result.success(Unit)
            coEvery { stopObfuscatorProcess() } returns Result.success(Unit)

            val result = connectionManager.disconnect()

            assertTrue(result.isSuccess)
            coVerify { connectionSource.stopConnection() }
            verify { connectionInfoProvider.resetConnectionInfo() }
            coVerify { stopObfuscatorProcess() }
            verify { connectionSource.stopPortForwarding() }
            verify { portForwardingUseCase.clearBindPort() }
        }

    @Test
    fun `disconnect - stopConnection fails - still returns success and continues cleanup`() =
        runTest {
            coEvery { connectionSource.stopConnection() } returns Result.failure(RuntimeException("stop failed"))
            coEvery { stopObfuscatorProcess() } returns Result.success(Unit)

            val result = connectionManager.disconnect()

            assertTrue(result.isSuccess)
            coVerify { stopObfuscatorProcess() }
            verify { connectionSource.stopPortForwarding() }
            verify { portForwardingUseCase.clearBindPort() }
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
    fun `reconnect - records server in quick connect history immediately before channel processing`() =
        runTest {
            connectionManager.reconnect(server) {}

            verify { connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp) }
        }

    @Test
    fun `reconnect - disconnects then connects to the requested server`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.stopConnection() } returns Result.success(Unit)
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.reconnect(server) {}

            coVerify { connectionSource.stopConnection() }
            coVerify { connectionSource.startConnection(clientConfig, connectionStatusProvider) }
        }

    @Test
    fun `reconnect - records every selected server in quick connect history immediately on call`() =
        runTest {
            val server2 =
                server.copy(
                    name = "EU West",
                    iso = "eu",
                    key = "eu-west",
                )
            val disconnectSignal = CompletableDeferred<Unit>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            coEvery { connectionSource.stopConnection() } coAnswers {
                disconnectSignal.await()
                Result.success(Unit)
            }
            coEvery { connectionSource.startConnection(any(), any()) } returns Result.success(Unit)

            val job = launch { connectionManager.reconnect(server) {} }
            advanceUntilIdle() // processor blocks inside stopConnection
            connectionManager.reconnect(server2) {} // recorded immediately
            disconnectSignal.complete(Unit)
            job.join()

            verify { connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp) }
            verify { connectionPrefs.addToQuickConnect(server2.key, server2.isDedicatedIp) }
        }

    @Test
    fun `reconnect - when server is updated during disconnect - skips to latest without connecting to the first`() =
        runTest {
            val server2 =
                server.copy(
                    name = "EU West",
                    iso = "eu",
                    key = "eu-west",
                    endpoints =
                        mapOf(
                            VpnServer.ServerGroup.WIREGUARD to
                                listOf(VpnServer.ServerEndpointDetails("5.6.7.8", "eu-west.example.com")),
                        ),
                )
            val clientConfig2 = mockk<ClientConfiguration>()
            val disconnectSignal = CompletableDeferred<Unit>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server2) } returns clientConfig2
            coEvery { connectionSource.stopConnection() } coAnswers {
                disconnectSignal.await()
                Result.success(Unit)
            }
            coEvery { connectionSource.startConnection(any(), any()) } returns Result.success(Unit)

            val job = launch { connectionManager.reconnect(server) {} }
            advanceUntilIdle() // processor runs until blocked inside stopConnection(server)
            connectionManager.reconnect(server2) {} // arrives while disconnect is in progress
            disconnectSignal.complete(Unit) // let disconnect finish; server2 is pending, so server is skipped
            job.join()

            coVerify { connectionSource.startConnection(clientConfig2, connectionStatusProvider) }
            coVerify(exactly = 0) {
                connectionSource.startConnection(
                    connectionConfigurationUseCase.generateConnectionConfiguration(server),
                    connectionStatusProvider,
                )
            }
        }

    @Test
    fun `reconnect - when server is updated during connect - disconnects and switches to latest`() =
        runTest {
            val server2 =
                server.copy(
                    name = "EU West",
                    iso = "eu",
                    key = "eu-west",
                    endpoints =
                        mapOf(
                            VpnServer.ServerGroup.WIREGUARD to
                                listOf(VpnServer.ServerEndpointDetails("5.6.7.8", "eu-west.example.com")),
                        ),
                )
            val clientConfig = mockk<ClientConfiguration>()
            val clientConfig2 = mockk<ClientConfiguration>()
            val connectToServerSignal = CompletableDeferred<Unit>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled.value } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server2) } returns clientConfig2
            coEvery { connectionSource.stopConnection() } returns Result.success(Unit)
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } coAnswers {
                connectToServerSignal.await()
                Result.success(Unit)
            }
            coEvery { connectionSource.startConnection(clientConfig2, connectionStatusProvider) } returns Result.success(Unit)

            val job = launch { connectionManager.reconnect(server) {} }
            advanceUntilIdle()
            connectionManager.reconnect(server2) {}
            connectToServerSignal.complete(Unit)
            job.join()

            coVerify { connectionSource.startConnection(clientConfig, connectionStatusProvider) }
            coVerify { connectionSource.startConnection(clientConfig2, connectionStatusProvider) }
        }

    // endregion
}