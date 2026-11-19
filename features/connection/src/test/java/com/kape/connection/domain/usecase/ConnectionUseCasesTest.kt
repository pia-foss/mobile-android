package com.kape.connection.domain.usecase

import com.kape.connection.vpnServer
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.SnoozeStatus
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ConnectionUseCasesTest {
    private val de = vpnServer("de")
    private val connectionManager = mockk<ConnectionManager>(relaxed = true) { every { isConnectionInProgress() } returns false }
    private val snooze = mockk<SnoozeStatus>(relaxed = true)
    private val settingsPrefs = mockk<SettingsStore>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionStore>(relaxed = true)

    @Test
    fun `disconnecting with automation on remembers that the user disconnected`() =
        runTest {
            coEvery { settingsPrefs.isAutomationEnabledNow() } returns true

            DisconnectUseCase(connectionManager, connectionPrefs, settingsPrefs, backgroundScope)().join()

            coVerifyOrder {
                connectionPrefs.setDisconnectedByUser(true)
                connectionManager.disconnect()
            }
        }

    @Test
    fun `disconnecting with automation off only disconnects`() =
        runTest {
            coEvery { settingsPrefs.isAutomationEnabledNow() } returns false

            DisconnectUseCase(connectionManager, connectionPrefs, settingsPrefs, backgroundScope)().join()

            coVerify(exactly = 0) { connectionPrefs.setDisconnectedByUser(any()) }
            coVerify { connectionManager.disconnect() }
        }

    @Test
    fun `quick connect selects the server, ends the snooze and reconnects, disconnecting when the connection stops`() =
        runTest {
            val vpnRegionPrefs = mockk<VpnRegionStore>(relaxed = true)
            val stopCallback = slot<() -> Unit>()
            coEvery { connectionManager.reconnect(de, capture(stopCallback)) } returns Unit

            QuickConnectUseCase(connectionManager, vpnRegionPrefs, snooze, backgroundScope)(de).join()
            coVerifyOrder {
                vpnRegionPrefs.selectVpnServer(de)
                snooze.cancelSnooze()
                connectionManager.reconnect(de, any())
            }
            coVerify(exactly = 0) { connectionManager.disconnect() }

            stopCallback.captured()
            testScheduler.runCurrent()

            coVerify { connectionManager.disconnect() }
        }

    private fun connectOnLaunch(
        enabled: Boolean = true,
        snoozeEnd: Long = 0L,
        connected: Boolean = false,
        scope: CoroutineScope,
    ): ConnectOnLaunchUseCase {
        coEvery { settingsPrefs.isConnectOnLaunchEnabledNow() } returns enabled
        coEvery { connectionPrefs.getLastSnoozeEndTimeNow() } returns snoozeEnd
        coEvery { connectionPrefs.getSelectedVpnServerNow() } returns de
        val connectionInfo = mockk<ConnectionInfoProvider> { every { isConnected() } returns connected }
        val getOptimal = mockk<GetOptimalVpnServerUseCase>()
        val connect = ConnectUseCase(connectionManager, snooze, getOptimal, scope)
        return ConnectOnLaunchUseCase(settingsPrefs, connectionPrefs, connectionInfo, connect)
    }

    @Test
    fun `on launch connects automatically to the selected server, only the first time`() =
        runTest {
            val useCase = connectOnLaunch(scope = backgroundScope)

            useCase {}
            useCase {}

            coVerify(exactly = 1) { connectionManager.connect(de, false, any(), any()) }
        }

    @Test
    fun `on launch does not connect when the setting is off, a snooze is running, or already connected`() =
        runTest {
            connectOnLaunch(enabled = false, scope = backgroundScope).invoke {}
            connectOnLaunch(snoozeEnd = System.currentTimeMillis() + 60_000, scope = backgroundScope).invoke {}
            connectOnLaunch(connected = true, scope = backgroundScope).invoke {}

            coVerify(exactly = 0) { connectionManager.connect(any(), any(), any(), any()) }
        }

    @Test
    fun `on launch an expired snooze does not prevent connecting`() =
        runTest {
            connectOnLaunch(snoozeEnd = System.currentTimeMillis() - 60_000, scope = backgroundScope).invoke {}

            coVerify(exactly = 1) { connectionManager.connect(de, false, any(), any()) }
        }
}