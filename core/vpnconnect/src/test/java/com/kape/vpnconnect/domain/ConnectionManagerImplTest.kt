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
import com.kape.vpnmanager.data.models.ClientConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

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

    private val server = VpnServer(
        name = "US East",
        iso = "us",
        dns = "us-east.example.com",
        latency = null,
        endpoints = emptyMap(),
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

    private val appModule = module {
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
    }

    private lateinit var connectionManager: ConnectionManagerImpl

    @BeforeEach
    fun setUp() {
        stopKoin()
        startKoin { modules(appModule) }
        connectionManager = ConnectionManagerImpl()
    }

    // region connect

    @Test
    fun `connect - shadowsocks disabled - VPN succeeds - updates info, sets server and quick connect`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled() } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = true) {}

            verify { connectionInfoProvider.updateInfo(server.name, server.iso, true) }
            verify { connectionPrefs.setSelectedVpnServer(server) }
            verify { connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp) }
        }

    @Test
    fun `connect - shadowsocks disabled - VPN succeeds - port forwarding disabled - does not start port forwarding`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled() } returns false
            every { settingsPrefs.isPortForwardingEnabled() } returns false
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = false) {}

            coVerify(exactly = 0) { portForwardingUseCase.bindPort(any()) }
            verify(exactly = 0) { connectionSource.startPortForwarding() }
        }

    @Test
    fun `connect - shadowsocks disabled - VPN succeeds - port forwarding enabled - starts port forwarding`() =
        runTest {
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled() } returns false
            every { settingsPrefs.isPortForwardingEnabled() } returns true
            every { connectionSource.getVpnToken() } returns "vpn_token"
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = false) {}

            coVerify { portForwardingUseCase.bindPort("vpn_token") }
            verify { connectionSource.startPortForwarding() }
        }

    @Test
    fun `connect - shadowsocks disabled - VPN fails - calls stopObfuscatorProcess`() = runTest {
        val clientConfig = mockk<ClientConfiguration>()
        every { settingsPrefs.isShadowsocksObfuscationEnabled() } returns false
        every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
        coEvery {
            connectionSource.startConnection(clientConfig, connectionStatusProvider)
        } returns Result.failure(RuntimeException("VPN failed"))

        connectionManager.connect(server, isManual = false) {}

        coVerify { stopObfuscatorProcess() }
    }

    @Test
    fun `connect - shadowsocks enabled - no server selected - returns early without starting VPN`() =
        runTest {
            every { settingsPrefs.isShadowsocksObfuscationEnabled() } returns true
            every { shadowsocksRegionPrefs.getSelectedShadowsocksServer() } returns null

            connectionManager.connect(server, isManual = false) {}

            coVerify(exactly = 0) { connectionSource.startConnection(any(), any()) }
        }

    @Test
    fun `connect - shadowsocks enabled - server found - obfuscator starts successfully - proceeds to VPN`() =
        runTest {
            val shadowsocksServer = ShadowsocksServer(
                region = "US East",
                host = "127.0.0.1",
                port = 1080,
                key = "test-key",
                cipher = "aes-256-gcm",
            )
            val clientConfig = mockk<ClientConfiguration>()
            every { settingsPrefs.isShadowsocksObfuscationEnabled() } returns true
            every { shadowsocksRegionPrefs.getSelectedShadowsocksServer() } returns shadowsocksServer
            coEvery { startObfuscatorProcess(any(), any()) } returns Result.success(Unit)
            every { connectionConfigurationUseCase.generateConnectionConfiguration(server) } returns clientConfig
            coEvery { connectionSource.startConnection(clientConfig, connectionStatusProvider) } returns Result.success(Unit)

            connectionManager.connect(server, isManual = false) {}

            coVerify { connectionSource.startConnection(clientConfig, connectionStatusProvider) }
        }

    @Test
    fun `connect - shadowsocks enabled - obfuscator fails - returns early without starting VPN`() =
        runTest {
            val shadowsocksServer = ShadowsocksServer(
                region = "US East",
                host = "127.0.0.1",
                port = 1080,
                key = "test-key",
                cipher = "aes-256-gcm",
            )
            every { settingsPrefs.isShadowsocksObfuscationEnabled() } returns true
            every { shadowsocksRegionPrefs.getSelectedShadowsocksServer() } returns shadowsocksServer
            coEvery { startObfuscatorProcess(any(), any()) } returns Result.failure(RuntimeException("obfuscator failed"))

            connectionManager.connect(server, isManual = false) {}

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
    fun `disconnect - stopConnection fails - still returns success and continues cleanup`() = runTest {
        coEvery { connectionSource.stopConnection() } returns Result.failure(RuntimeException("stop failed"))
        coEvery { stopObfuscatorProcess() } returns Result.success(Unit)

        val result = connectionManager.disconnect()

        assertTrue(result.isSuccess)
        coVerify { stopObfuscatorProcess() }
        verify { connectionSource.stopPortForwarding() }
        verify { portForwardingUseCase.clearBindPort() }
    }

    // endregion
}