package com.kape.vpn.system

import com.kape.contracts.AutomationServiceLauncher
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpn.automation.StartAutomationIfEnabledUseCase
import com.kape.vpnlauncher.VpnLauncher
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SystemEventUseCasesTest {
    private var automationEnabled = false
    private var automationStarts = 0
    private val startAutomation = StartAutomationIfEnabledUseCase({ automationEnabled }, AutomationServiceLauncher { automationStarts++ })
    private val launcher = mockk<VpnLauncher>(relaxed = true)
    private var snoozeEnd = 0L
    private val connectionPrefs = mockk<ConnectionPrefs> { coEvery { getLastSnoozeEndTimeNow() } answers { snoozeEnd } }
    private var launchOnBoot = true
    private var connectOnUpdate = true
    private val settingsPrefs =
        mockk<SettingsPrefs> {
            coEvery { isLaunchOnBootEnabledNow() } answers { launchOnBoot }
            coEvery { isConnectOnAppUpdateEnabledNow() } answers { connectOnUpdate }
        }

    @Test
    fun `on boot, automation restarts and the VPN connects when asked to`() =
        runTest {
            automationEnabled = true

            OnBootUseCase(settingsPrefs, connectionPrefs, startAutomation, launcher)()

            assertEquals(1, automationStarts)
            verify { launcher.launchVpn() }
        }

    @Test
    fun `on boot, a snooze still running is respected`() =
        runTest {
            snoozeEnd = Long.MAX_VALUE

            OnBootUseCase(settingsPrefs, connectionPrefs, startAutomation, launcher)()

            verify(exactly = 0) { launcher.launchVpn() }
        }

    @Test
    fun `on boot, nothing connects unless the user asked`() =
        runTest {
            launchOnBoot = false

            OnBootUseCase(settingsPrefs, connectionPrefs, startAutomation, launcher)()

            verify(exactly = 0) { launcher.launchVpn() }
            assertEquals(0, automationStarts)
        }

    @Test
    fun `after an update, the VPN reconnects only when asked to`() =
        runTest {
            OnAppUpdatedUseCase(settingsPrefs, startAutomation, launcher)()
            verify(exactly = 1) { launcher.launchVpn() }

            connectOnUpdate = false
            OnAppUpdatedUseCase(settingsPrefs, startAutomation, launcher)()
            verify(exactly = 1) { launcher.launchVpn() }
        }

    private var connected = false
    private var loggedIn = true
    private val connectionInfo =
        mockk<ConnectionInfoProvider> {
            every { isConnected() } answers { connected }
            every { name } returns "Germany"
        }
    private val isUserLoggedIn =
        object : IsUserLoggedInUseCase {
            override suspend fun invoke() = loggedIn
        }

    @Test
    fun `the tile shows the connection, or that the user must log in`() =
        runTest {
            val getTile = GetVpnTileUseCase(connectionInfo, isUserLoggedIn)
            assertEquals(VpnTile.Disconnected, getTile())

            connected = true
            assertEquals(VpnTile.Connected("Germany"), getTile())

            connected = false
            loggedIn = false
            assertEquals(VpnTile.LoggedOut, getTile())
        }

    @Test
    fun `tapping the tile toggles the VPN, and does nothing when logged out`() =
        runTest {
            val toggle = ToggleVpnFromTileUseCase(connectionInfo, isUserLoggedIn, launcher)

            toggle()
            verify { launcher.launchVpn() }

            connected = true
            toggle()
            verify { launcher.stopVpn() }

            loggedIn = false
            toggle()
            verify(exactly = 1) { launcher.stopVpn() }
            verify(exactly = 1) { launcher.launchVpn() }
        }
}