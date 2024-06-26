package com.kape.vpnconnect.data

import android.app.AlarmManager
import android.app.PendingIntent
import app.cash.turbine.test
import com.kape.connection.ConnectionPrefs
import com.kape.csi.CsiPrefs
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.shareevents.domain.KpiDataSource
import com.kape.vpnconnect.di.vpnConnectModule
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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

internal class ConnectionDataSourceImplTest {

    private val connectionApi: VPNManagerAPI = mockk(relaxed = true)
    private val authenticationApi: AndroidAccountAPI = mockk(relaxed = true)
    private val connectionPrefs: ConnectionPrefs = mockk<ConnectionPrefs>().apply {
        every { clearGateway() } returns Unit
        every { clearPortBindingInfo() } returns Unit
        every { setGateway(any()) } returns Unit
    }
    private val alarmManager: AlarmManager = mockk<AlarmManager>().apply {
        every { cancel(any<PendingIntent>()) } returns Unit
        every { setRepeating(any(), any(), any(), any()) } returns Unit
    }
    private val portForwardingIntent: PendingIntent = mockk()
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

    private val appModule = module {
        single { connectionApi }
        single { authenticationApi }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, vpnConnectModule(appModule))
        }
        source = ConnectionDataSourceImpl(
            connectionApi,
            authenticationApi,
            connectionPrefs,
            alarmManager,
            settingsPrefs,
            kpiDataSource,
            usageProvider,
            portForwardingIntent,
            csiPrefs,
        )
    }

    @Test
    fun `startConnection - kpiEnabled - success`() = runTest {
        coEvery { connectionApi.addConnectionListener(any(), any()) } returns Unit
        coEvery { connectionApi.startConnection(any(), any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(
                Result.success(
                    ServerPeerInformation("", "gateway"),
                ),
            )
        }
        every { settingsPrefs.isHelpImprovePiaEnabled() } returns true
        source.startConnection(clientConfiguration, connectionListener).test {
            verify { kpiDataSource.start() }
            verify { connectionPrefs.setGateway("gateway") }
            val actual = awaitItem()
            assertEquals(true, actual)
        }
    }

    @Test
    fun `startConnection - kpiDisabled - success`() = runTest {
        coEvery { connectionApi.addConnectionListener(any(), any()) } returns Unit
        coEvery { connectionApi.startConnection(any(), any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.success(null))
        }
        every { settingsPrefs.isHelpImprovePiaEnabled() } returns false
        source.startConnection(clientConfiguration, connectionListener).test {
            verify { kpiDataSource.stop() }
            val actual = awaitItem()
            assertEquals(true, actual)
        }
    }

    @Test
    fun `startConnection - failure`() = runTest {
        coEvery { connectionApi.startConnection(any(), any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.failure(Exception()))
        }
        source.startConnection(clientConfiguration, connectionListener).test {
            val actual = awaitItem()
            assertEquals(false, actual)
        }
    }

    @Test
    fun `stopConnection - success`() = runTest {
        coEvery { connectionApi.stopConnection(any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.success(null))
        }
        source.stopConnection().test {
            val actual = awaitItem()
            assertEquals(true, actual)
        }
    }

    @Test
    fun `stopConnection - failure`() = runTest {
        coEvery { connectionApi.stopConnection(any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.failure(Exception()))
        }
        source.stopConnection().test {
            val actual = awaitItem()
            assertEquals(false, actual)
        }
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
        source.getDebugLogs().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
            assertEquals(2, actual.size)
        }
    }

    @Test
    fun `getDebugLogs - failure`() = runTest {
        val expected = emptyList<String>()
        coEvery { connectionApi.getVpnProtocolLogs(any(), any()) } answers {
            lastArg<(Result<List<String>>) -> Unit>().invoke(Result.failure(Exception()))
        }
        source.getDebugLogs().test {
            val actual = awaitItem()
            assertEquals(expected, actual)
            assertEquals(0, actual.size)
        }
    }

    @Test
    fun `startPortForwarding() sets an alarm`() = runTest {
        source.startPortForwarding()
        verify(exactly = 1) { alarmManager.setRepeating(any(), any(), any(), any()) }
    }
}