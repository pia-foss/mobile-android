package com.kape.vpn.utils

import android.content.Context
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.ConnectionStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.NetworkManagementPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import com.kape.ui.R
import com.kape.vpnlauncher.VpnLauncher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkManagerImplTest {
    private val context = mockk<Context>()
    private val networkPrefs = mockk<NetworkManagementPrefs>()
    private val vpnLauncher = mockk<VpnLauncher>(relaxed = true)
    private val settingsPrefs = mockk<SettingsPrefs>()
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    private val connectionStatusProvider =
        mockk<ConnectionStatusProvider> { every { status } returns connectionStatus }
    private val ioScope = TestScope(UnconfinedTestDispatcher())

    private val networkManager =
        NetworkManagerImpl(
            context,
            networkPrefs,
            vpnLauncher,
            settingsPrefs,
            connectionPrefs,
            connectionStatusProvider,
            ioScope,
        )

    @BeforeEach
    fun setUp() {
        every { context.getString(R.string.nmt_open_wifi) } returns OPEN_WIFI
        every { context.getString(R.string.nmt_mobile_data) } returns MOBILE_DATA
        coEvery { settingsPrefs.isAutomationEnabledNow() } returns true
        coEvery { connectionPrefs.getLastSnoozeEndTimeNow() } returns 0L
        every { networkPrefs.getRuleForNetwork(any()) } returns flowOf(null)
    }

    @Test
    fun `automation disabled - no rule is applied`() {
        coEvery { settingsPrefs.isAutomationEnabledNow() } returns false
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verifyNoVpnAction()
        coVerify(exactly = 0) { connectionPrefs.setDisconnectedByUser(any()) }
    }

    @Test
    fun `snooze active - no rule is applied and manual disconnect flag is kept`() {
        coEvery { connectionPrefs.getLastSnoozeEndTimeNow() } returns Calendar.getInstance().timeInMillis + 60_000
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verifyNoVpnAction()
        coVerify(exactly = 0) { connectionPrefs.setDisconnectedByUser(any()) }
    }

    @Test
    fun `expired snooze - rule is applied`() {
        coEvery { connectionPrefs.getLastSnoozeEndTimeNow() } returns Calendar.getInstance().timeInMillis - 60_000
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verify { vpnLauncher.launchVpn() }
    }

    @Test
    fun `network change - clears manual disconnect flag before connecting`() {
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        coVerifyOrder {
            connectionPrefs.setDisconnectedByUser(false)
            vpnLauncher.launchVpn()
        }
    }

    @Test
    fun `same network redelivered - keeps manual disconnect flag but still evaluates the rule`() {
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = false)

        coVerify(exactly = 0) { connectionPrefs.setDisconnectedByUser(any()) }
        // The connect request goes through so ConnectionManager can honour (and reset) the manual disconnect.
        verify(exactly = 1) { vpnLauncher.launchVpn() }
    }

    @Test
    fun `always connect while disconnected - launches VPN`() {
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verify(exactly = 1) { vpnLauncher.launchVpn() }
    }

    @Test
    fun `always connect while disconnecting - launches VPN`() {
        connectionStatus.value = ConnectionStatus.DISCONNECTING
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verify(exactly = 1) { vpnLauncher.launchVpn() }
    }

    @Test
    fun `always connect while connected - does not relaunch VPN`() {
        connectionStatus.value = ConnectionStatus.CONNECTED
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verifyNoVpnAction()
    }

    @Test
    fun `always connect while connecting - does not relaunch VPN`() {
        connectionStatus.value = ConnectionStatus.CONNECTING
        givenRule(HOME_SSID, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verifyNoVpnAction()
    }

    @Test
    fun `always disconnect - stops VPN`() {
        connectionStatus.value = ConnectionStatus.CONNECTED
        givenRule(HOME_SSID, NetworkBehavior.AlwaysDisconnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verify(exactly = 1) { vpnLauncher.stopVpn() }
        verify(exactly = 0) { vpnLauncher.launchVpn() }
    }

    @Test
    fun `retain state - leaves VPN untouched`() {
        givenRule(HOME_SSID, NetworkBehavior.RetainState)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verifyNoVpnAction()
    }

    @Test
    fun `SSID rule exists - takes precedence over the generic Wi-Fi rule`() {
        givenRule(HOME_SSID, NetworkBehavior.AlwaysDisconnect)
        givenRule(OPEN_WIFI, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork(HOME_SSID, isWifi = true, isNetworkChange = true)

        verify(exactly = 1) { vpnLauncher.stopVpn() }
        verify(exactly = 0) { vpnLauncher.launchVpn() }
    }

    @Test
    fun `wifi without SSID rule - falls back to the generic Wi-Fi rule`() {
        givenRule(OPEN_WIFI, NetworkBehavior.AlwaysConnect)
        givenRule(MOBILE_DATA, NetworkBehavior.AlwaysDisconnect)

        networkManager.handleCurrentNetwork(CAFE_SSID, isWifi = true, isNetworkChange = true)

        verify(exactly = 1) { vpnLauncher.launchVpn() }
        verify(exactly = 0) { vpnLauncher.stopVpn() }
    }

    @Test
    fun `cellular - falls back to the mobile data rule`() {
        givenRule(OPEN_WIFI, NetworkBehavior.AlwaysDisconnect)
        givenRule(MOBILE_DATA, NetworkBehavior.AlwaysConnect)

        networkManager.handleCurrentNetwork("", isWifi = false, isNetworkChange = true)

        verify(exactly = 1) { vpnLauncher.launchVpn() }
        verify(exactly = 0) { vpnLauncher.stopVpn() }
    }

    @Test
    fun `no matching rule - leaves VPN untouched`() {
        networkManager.handleCurrentNetwork(CAFE_SSID, isWifi = true, isNetworkChange = true)

        verifyNoVpnAction()
    }

    private fun givenRule(
        networkName: String,
        behavior: NetworkBehavior,
    ) {
        every { networkPrefs.getRuleForNetwork(networkName) } returns
            flowOf(NetworkItem(networkName, NetworkType.WifiCustom, behavior))
    }

    private fun verifyNoVpnAction() {
        verify(exactly = 0) { vpnLauncher.launchVpn() }
        verify(exactly = 0) { vpnLauncher.stopVpn() }
    }

    private companion object {
        const val HOME_SSID = "\"Home\""
        const val CAFE_SSID = "\"Cafe\""
        const val OPEN_WIFI = "Wi-Fi"
        const val MOBILE_DATA = "Mobile data"
    }
}