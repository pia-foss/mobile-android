package com.kape.vpnconnect.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.kape.contracts.ConnectionStatusProvider
import com.kape.contracts.KpiDataSource
import com.kape.data.ConnectionStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.provider.UsageProviderImpl
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectionDataSourceImplTest {
    private val connectionApi = mockk<VPNManagerAPI>()
    private val accountApi = mockk<AndroidAccountAPI>()
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val kpiDataSource = mockk<KpiDataSource>(relaxed = true)
    private val usageProvider = mockk<UsageProviderImpl>(relaxed = true)
    private val csiPrefs = mockk<CsiPrefs>(relaxed = true)

    private val connectionStatusProvider =
        object : ConnectionStatusProvider, VPNManagerConnectionListener {
            override val state: StateFlow<VpnConnectionStatus> =
                MutableStateFlow(VpnConnectionStatus(ConnectionStatus.DISCONNECTED, ""))

            override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {}
        }

    private lateinit var dataSource: ConnectionDataSourceImpl

    // region startConnection

    @Test
    fun `startConnection - success - sets gateway and returns success`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val clientConfiguration = mockk<ClientConfiguration>()
            val serverPeerInfo = ServerPeerInformation(networkInterface = "wg0", gateway = "10.0.0.1")
            val slot = slot<(Result<ServerPeerInformation>) -> Unit>()

            every { settingsPrefs.isHelpImprovePiaEnabled.value } returns false
            every { connectionApi.addConnectionListener(any(), any()) } answers { }
            every { connectionApi.startConnection(clientConfiguration, capture(slot)) } answers {
                slot.captured.invoke(Result.success(serverPeerInfo))
            }

            val result = dataSource.startConnection(clientConfiguration, connectionStatusProvider)

            assertTrue(result.isSuccess)
            coVerify { connectionPrefs.setGateway("10.0.0.1") }
        }

    @Test
    fun `startConnection - failure - logs error and returns failure`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )

            val clientConfiguration = mockk<ClientConfiguration>()
            val slot = slot<(Result<ServerPeerInformation>) -> Unit>()

            every { settingsPrefs.isHelpImprovePiaEnabled.value } returns false
            every { settingsPrefs.isDebugLoggingEnabled.value } returns false
            every { connectionApi.addConnectionListener(any(), any()) } answers { }
            every { connectionApi.startConnection(clientConfiguration, capture(slot)) } answers {
                slot.captured.invoke(Result.failure(RuntimeException("connection failed")))
            }

            val result = dataSource.startConnection(clientConfiguration, connectionStatusProvider)

            assertTrue(result.isFailure)
            coVerify { csiPrefs.addCustomDebugLogs(any(), false) }
        }

    @Test
    fun `startConnection - help improve PIA enabled - starts KPI`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )

            val clientConfiguration = mockk<ClientConfiguration>()
            val slot = slot<(Result<ServerPeerInformation>) -> Unit>()

            every { settingsPrefs.isHelpImprovePiaEnabled.value } returns true
            every { connectionApi.addConnectionListener(any(), any()) } answers { }
            every { connectionApi.startConnection(clientConfiguration, capture(slot)) } answers {
                slot.captured.invoke(Result.success(mockk(relaxed = true)))
            }

            dataSource.startConnection(clientConfiguration, connectionStatusProvider)

            verify { kpiDataSource.start() }
        }

    @Test
    fun `startConnection - help improve PIA disabled - stops KPI`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val clientConfiguration = mockk<ClientConfiguration>()
            val slot = slot<(Result<ServerPeerInformation>) -> Unit>()

            every { settingsPrefs.isHelpImprovePiaEnabled.value } returns false
            every { connectionApi.addConnectionListener(any(), any()) } answers { }
            every { connectionApi.startConnection(clientConfiguration, capture(slot)) } answers {
                slot.captured.invoke(Result.success(mockk(relaxed = true)))
            }

            dataSource.startConnection(clientConfiguration, connectionStatusProvider)

            verify { kpiDataSource.stop() }
        }

    // endregion

    // region stopConnection

    @Test
    fun `stopConnection - success - resets usage, stops port forwarding, returns success`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val slot = slot<(Result<Unit>) -> Unit>()

            every { connectionApi.stopConnection(capture(slot)) } answers {
                slot.captured.invoke(Result.success(Unit))
            }

            val result = dataSource.stopConnection()

            assertTrue(result.isSuccess)
            verify { usageProvider.reset() }
            coVerify { connectionPrefs.clearGateway() }
            coVerify { connectionPrefs.clearPortBindingInfo() }
            verify { workManager.cancelUniqueWork(any()) }
        }

    @Test
    fun `stopConnection - failure - logs error and returns failure`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val slot = slot<(Result<Unit>) -> Unit>()

            every { settingsPrefs.isDebugLoggingEnabled.value } returns true
            every { connectionApi.stopConnection(capture(slot)) } answers {
                slot.captured.invoke(Result.failure(RuntimeException("stop failed")))
            }

            val result = dataSource.stopConnection()

            assertTrue(result.isFailure)
            coVerify { csiPrefs.addCustomDebugLogs(any(), true) }
        }

    // endregion

    // region getVpnToken

    @Test
    fun `getVpnToken - account API returns token - returns token`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            every { accountApi.vpnToken() } returns "vpn_token_123"

            val result = dataSource.getVpnToken()

            assertEquals("vpn_token_123", result)
        }

    @Test
    fun `getVpnToken - account API returns null - returns empty string`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            every { accountApi.vpnToken() } returns null

            val result = dataSource.getVpnToken()

            assertEquals("", result)
        }

    // endregion

    // region startPortForwarding

    @Test
    fun `startPortForwarding - enqueues unique periodic work with KEEP policy`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            dataSource.startPortForwarding()

            verify { workManager.enqueueUniquePeriodicWork(any(), ExistingPeriodicWorkPolicy.KEEP, any()) }
        }

    // endregion

    // region stopPortForwarding

    @Test
    fun `stopPortForwarding - clears gateway, port binding info, and cancels worker`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            dataSource.stopPortForwarding()

            coVerify { connectionPrefs.clearGateway() }
            coVerify { connectionPrefs.clearPortBindingInfo() }
            verify { workManager.cancelUniqueWork(any()) }
        }

    // endregion

    // region getDebugLogs

    @Test
    fun `getDebugLogs - WireGuard protocol - success - returns log list`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val logs = listOf("wg_log1", "wg_log2")
            val slot = slot<(Result<List<String>>) -> Unit>()

            every { settingsPrefs.selectedProtocol.value } returns VpnProtocols.WireGuard
            every { connectionApi.getVpnProtocolLogs(VPNManagerProtocolTarget.WIREGUARD, capture(slot)) } answers {
                slot.captured.invoke(Result.success(logs))
            }

            val result = dataSource.getDebugLogs()

            assertEquals(logs, result)
        }

    @Test
    fun `getDebugLogs - OpenVPN protocol - success - returns log list`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val logs = listOf("ovpn_log1")
            val slot = slot<(Result<List<String>>) -> Unit>()

            every { settingsPrefs.selectedProtocol.value } returns VpnProtocols.OpenVPN
            every { connectionApi.getVpnProtocolLogs(VPNManagerProtocolTarget.OPENVPN, capture(slot)) } answers {
                slot.captured.invoke(Result.success(logs))
            }

            val result = dataSource.getDebugLogs()

            assertEquals(logs, result)
        }

    @Test
    fun `getDebugLogs - failure - returns empty list`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val slot = slot<(Result<List<String>>) -> Unit>()

            every { settingsPrefs.selectedProtocol.value } returns VpnProtocols.WireGuard
            every { connectionApi.getVpnProtocolLogs(any(), capture(slot)) } answers {
                slot.captured.invoke(Result.failure(RuntimeException("logs unavailable")))
            }

            val result = dataSource.getDebugLogs()

            assertEquals(emptyList<String>(), result)
        }

    // endregion

    // region updateConfigurationServers

    @Test
    fun `updateConfigurationServers - success - returns true`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val servers = mockk<ServerList>()
            val slot = slot<(Result<Unit>) -> Unit>()

            every { connectionApi.updateConfigurationServers(servers, capture(slot)) } answers {
                slot.captured.invoke(Result.success(Unit))
            }

            val result = dataSource.updateConfigurationServers(servers)

            assertTrue(result)
        }

    @Test
    fun `updateConfigurationServers - failure - returns false`() =
        runTest {
            dataSource =
                ConnectionDataSourceImpl(
                    connectionApi = connectionApi,
                    accountApi = accountApi,
                    connectionPrefs = connectionPrefs,
                    workManager = workManager,
                    settingsPrefs = settingsPrefs,
                    kpiDataSource = kpiDataSource,
                    usageProvider = usageProvider,
                    csiPrefs = csiPrefs,
                    ioScope = this,
                )
            val servers = mockk<ServerList>()
            val slot = slot<(Result<Unit>) -> Unit>()

            every { connectionApi.updateConfigurationServers(servers, capture(slot)) } answers {
                slot.captured.invoke(Result.failure(RuntimeException("update failed")))
            }

            val result = dataSource.updateConfigurationServers(servers)

            assertFalse(result)
        }

    // endregion
}