package com.kape.vpnconnect.domain

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import androidx.compose.runtime.mutableStateOf
import app.cash.turbine.test
import com.kape.connection.ConnectionPrefs
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.di.vpnConnectModule
import com.kape.vpnconnect.utils.ConnectionManager
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

internal class ConnectionUseCaseTest {

    private val connectionDataSource: ConnectionDataSource = mockk<ConnectionDataSource>().apply {
        every { stopPortForwarding() } returns Unit
    }
    private val clientStateDataSource: ClientStateDataSource = mockk<ClientStateDataSource>().apply {
        every { getClientStatus() } returns flow {
            emit(true)
        }
        every { resetVpnIp() } just Runs
    }
    private val server: VpnServer = mockk<VpnServer>(relaxed = true).apply {
        every { endpoints } returns emptyMap()
        every { latency } returns "0"
        every { name } returns "name"
    }
    private val certificate: String = "mockCertificate"
    private val connectionManager: ConnectionManager = mockk<ConnectionManager>().apply {
        every { isManualConnection } returns true
        every { isManualConnection = any() } returns Unit
    }
    private val settingsPrefs: SettingsPrefs = mockk<SettingsPrefs>().apply {
        every { getSelectedProtocol() } returns VpnProtocols.WireGuard
        every { getSelectedDnsOption() } returns DnsOptions.PIA
        every { getVpnExcludedApps() } returns emptyList()
        every { isAllowLocalTrafficEnabled() } returns false
        every { getOpenVpnSettings() } returns OpenVpnSettings()
        every { getWireGuardSettings() } returns WireGuardSettings()
        every { isMaceEnabled() } returns true
        every { isPortForwardingEnabled() } returns false
        every { isShadowsocksObfuscationEnabled() } returns false
        every { isExternalProxyAppEnabled() } returns false
    }
    private val connectionPrefs: ConnectionPrefs = mockk<ConnectionPrefs>().apply {
        every { setSelectedVpnServer(any()) } returns Unit
        every { getClientIp() } returns "clientIp"
        every { getVpnIp() } returns "vpnIp"
    }
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs = mockk<ShadowsocksRegionPrefs>()
    private val intent: PendingIntent = mockk()
    private val context: android.content.Context = mockk()
    private val notificationBuilder: Notification.Builder = mockk<Notification.Builder>().apply {
        every { setContentTitle(any()) } returns Notification.Builder(context, "")
        every { setContentText(any()) } returns Notification.Builder(context, "")
        every { setContentIntent(any()) } returns Notification.Builder(context, "")
        every { build() } returns Notification()
    }
    private val portForwardingUseCase: PortForwardingUseCase =
        mockk<PortForwardingUseCase>().apply {
            every { portForwardingStatus } returns mutableStateOf(PortForwardingStatus.NoPortForwarding)
            every { port } returns mutableStateOf("")
            every { clearBindPort() } returns Unit
        }
    private val alarmManager: AlarmManager = mockk(relaxed = true)
    private val portForwardingIntent: PendingIntent = mockk()
    private lateinit var useCase: ConnectionUseCase
    private val getActiveInterfaceDnsUseCase = mockk<GetActiveInterfaceDnsUseCase>()
    private val startObfuscatorProcess: StartObfuscatorProcess = mockk(relaxed = true)
    private val stopObfuscatorProcess: StopObfuscatorProcess = mockk(relaxed = true)

    private val appModule = module {
        single { "certificate" }
    }

    @BeforeEach
    internal fun setUp() {
        stopKoin()
        startKoin {
            modules(appModule, vpnConnectModule(appModule))
        }
        useCase = ConnectionUseCase(
            connectionDataSource,
            clientStateDataSource,
            certificate,
            connectionManager,
            settingsPrefs,
            connectionPrefs,
            shadowsocksRegionPrefs,
            intent,
            notificationBuilder,
            getActiveInterfaceDnsUseCase,
            startObfuscatorProcess,
            stopObfuscatorProcess,
            portForwardingUseCase,
        )
        every { connectionManager.setConnectedServerName(any(), any()) } returns Unit
    }

    @Test
    fun `startConnection - success`() = runTest {
        val expected = true
        every { connectionDataSource.startConnection(any(), any()) } returns flow {
            emit(expected)
        }
        every { connectionDataSource.getVpnToken() } returns "username:password"

        useCase.startConnection(server, true).test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `startConnection - failure`() = runTest {
        val expected = false
        every { connectionDataSource.startConnection(any(), any()) } returns flow {
            emit(expected)
        }
        every { connectionDataSource.getVpnToken() } returns "username:password"

        useCase.startConnection(server, true).test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `stopConnection - success`() = runTest {
        val expected = true
        every { connectionDataSource.stopConnection() } returns flow {
            emit(expected)
        }
        every { connectionDataSource.getVpnToken() } returns "username:password"

        useCase.stopConnection().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `stopConnection - failure`() = runTest {
        val expected = false
        every { connectionDataSource.stopConnection() } returns flow {
            emit(expected)
        }
        every { connectionDataSource.getVpnToken() } returns "username:password"

        useCase.stopConnection().test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `startConnection - failure - reset vpn IP`() = runTest {
        val expected = false
        every { connectionDataSource.startConnection(any(), any()) } returns flow {
            emit(expected)
        }
        every { connectionDataSource.getVpnToken() } returns "username:password"

        useCase.startConnection(server, true).test {
            awaitItem()
            awaitComplete()
            verify(exactly = 1) { clientStateDataSource.resetVpnIp() }
        }
    }
}