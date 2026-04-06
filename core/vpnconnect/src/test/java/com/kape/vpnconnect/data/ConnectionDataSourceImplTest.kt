package com.kape.vpnconnect.data

import androidx.work.WorkManager
import com.kape.contracts.KpiDataSource
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ConnectionDataSourceImplTest {

    private val connectionApi: VPNManagerAPI = mockk(relaxed = true)
    private val authenticationApi: AndroidAccountAPI = mockk(relaxed = true)
    private val connectionPrefs: ConnectionPrefs = mockk<ConnectionPrefs>().apply {
        every { clearGateway() } returns Unit
        every { clearPortBindingInfo() } returns Unit
        every { setGateway(any()) } returns Unit
    }
    private val workManager: WorkManager = mockk(relaxed = true)
    private val settingsPrefs: SettingsPrefs = mockk<SettingsPrefs>().apply {
        every { isHelpImprovePiaEnabled() } returns false
        every { getSelectedProtocol() } returns VpnProtocols.WireGuard
        every { isDebugLoggingEnabled() } returns false
    }
    private val kpiDataSource: KpiDataSource = mockk<KpiDataSource>().apply {
        every { stop() } returns Unit
        every { start() } returns Unit
    }
    private val clientConfiguration: ClientConfiguration = mockk()
    private val connectionListener: VPNManagerConnectionListener = mockk()
    private val usageProvider: UsageProvider = mockk<UsageProvider>().apply {
        every { reset() } returns Unit
    }
    private val csiPrefs: CsiPrefs = mockk<CsiPrefs>().apply {
        every { addCustomDebugLogs(any(), any()) } returns Unit
    }
    private lateinit var source: ConnectionDataSource

    @BeforeEach
    internal fun setUp() {
        source = ConnectionDataSourceImpl(
            connectionApi,
            authenticationApi,
            connectionPrefs,
            workManager,
            settingsPrefs,
            kpiDataSource,
            usageProvider,
            csiPrefs,
        )
    }

    @Test
    fun `startConnection - kpiEnabled - success`() = runTest {
        coEvery { connectionApi.addConnectionListener(any(), any()) } returns Unit
        coEvery { connectionApi.startConnection(any(), any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(
                Result.success(ServerPeerInformation("", "gateway")),
            )
        }
        every { settingsPrefs.isHelpImprovePiaEnabled() } returns true
        val actual = source.startConnection(clientConfiguration, connectionListener)
        verify { kpiDataSource.start() }
        verify { connectionPrefs.setGateway("gateway") }
        assertEquals(true, actual)
    }

    @Test
    fun `startConnection - kpiDisabled - success`() = runTest {
        coEvery { connectionApi.addConnectionListener(any(), any()) } returns Unit
        coEvery { connectionApi.startConnection(any(), any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.success(null))
        }
        every { settingsPrefs.isHelpImprovePiaEnabled() } returns false
        val actual = source.startConnection(clientConfiguration, connectionListener)
        verify { kpiDataSource.stop() }
        assertEquals(true, actual)
    }

    @Test
    fun `startConnection - failure`() = runTest {
        coEvery { connectionApi.startConnection(any(), any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.failure(Exception()))
        }
        val actual = source.startConnection(clientConfiguration, connectionListener)
        assertEquals(false, actual)
    }

    @Test
    fun `stopConnection - success`() = runTest {
        coEvery { connectionApi.stopConnection(any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.success(null))
        }
        val actual = source.stopConnection()
        assertEquals(true, actual)
    }

    @Test
    fun `stopConnection - failure`() = runTest {
        coEvery { connectionApi.stopConnection(any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.failure(Exception()))
        }
        val actual = source.stopConnection()
        assertEquals(false, actual)
    }

    @Test
    fun `getVpnToken - success`() = runTest {
        val expected = "user:pass"
        every { authenticationApi.vpnToken() } returns expected
        val actual = source.getVpnToken()
        assertEquals(expected, actual)
    }

    @Test
    fun `getVpnToken - failure`() = runTest {
        val expected = ""
        every { authenticationApi.vpnToken() } returns null
        val actual = source.getVpnToken()
        assertEquals(expected, actual)
    }

    @Test
    fun `getDebugLogs - success`() = runTest {
        val expected = listOf("log1", "log2")
        coEvery { connectionApi.getVpnProtocolLogs(any(), any()) } answers {
            lastArg<(Result<List<String>>) -> Unit>().invoke(Result.success(expected))
        }
        val actual = source.getDebugLogs()
        assertEquals(expected, actual)
        assertEquals(2, actual.size)
    }

    @Test
    fun `getDebugLogs - failure`() = runTest {
        val expected = emptyList<String>()
        coEvery { connectionApi.getVpnProtocolLogs(any(), any()) } answers {
            lastArg<(Result<List<String>>) -> Unit>().invoke(Result.failure(Exception()))
        }
        val actual = source.getDebugLogs()
        assertEquals(expected, actual)
        assertEquals(0, actual.size)
    }

    @Test
    fun `startPortForwarding() enqueues periodic work`() = runTest {
        source.startPortForwarding()
        verify(exactly = 1) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }
}