package com.kape.vpnconnect.data

import android.app.AlarmManager
import android.app.PendingIntent
import app.cash.turbine.test
import com.kape.connection.ConnectionPrefs
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.shareevents.domain.KpiDataSource
import com.kape.vpnconnect.di.vpnConnectModule
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerPeerInformation
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
    }
    private val alarmManager: AlarmManager = mockk<AlarmManager>().apply {
        every { cancel(any<PendingIntent>()) } returns Unit
    }
    private val portForwardingIntent: PendingIntent = mockk()
    private val settingsPrefs: SettingsPrefs = mockk<SettingsPrefs>().apply {
        every { isHelpImprovePiaEnabled() } returns false
        every { getSelectedProtocol() } returns VpnProtocols.WireGuard
    }
    private val kpiDataSource: KpiDataSource = mockk<KpiDataSource>().apply {
        every { stop() } returns Unit
        every { start() } returns Unit
    }
    private val clientConfiguration: ClientConfiguration = mockk()
    private val connectionListener: VPNManagerConnectionListener = mockk()
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
            portForwardingIntent,
            settingsPrefs,
            kpiDataSource,
        )
    }

    @Test
    fun `startConnection - success`() = runTest {
        coEvery { connectionApi.addConnectionListener(any(), any()) } returns Unit
        coEvery { connectionApi.startConnection(any(), any()) } answers {
            lastArg<(Result<ServerPeerInformation?>) -> Unit>().invoke(Result.success(null))
        }
        source.startConnection(clientConfiguration, connectionListener).test {
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
}