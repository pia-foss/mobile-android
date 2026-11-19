package com.kape.automation.domain

import com.kape.contracts.ConnectionStatusSource
import com.kape.data.ConnectionStatus
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.NetworkManagementStore
import com.kape.localprefs.api.SettingsStore
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import com.kape.vpnlauncher.VpnLauncher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ApplyNetworkRuleUseCaseTest {
    private val home = NetworkItem("\"Home\"", NetworkType.WifiSecure, NetworkBehavior.AlwaysDisconnect)
    private val openWifi = NetworkItem("Open Wi-Fi", NetworkType.WifiOpen, NetworkBehavior.AlwaysConnect, isDefaultForOpen = true)
    private val mobileData = NetworkItem("Mobile data", NetworkType.MobileData, NetworkBehavior.RetainState, isDefaultForMobile = true)
    private val rules = listOf(home, openWifi, mobileData)

    private var automationEnabled = true
    private var snoozeEndTime = 0L
    private val status = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private val connectionPrefs = mockk<ConnectionStore>(relaxed = true) { coEvery { getLastSnoozeEndTimeNow() } answers { snoozeEndTime } }
    private val launcher = mockk<VpnLauncher>(relaxed = true)
    private val applyRule =
        ApplyNetworkRuleUseCase(
            settingsPrefs = mockk<SettingsStore> { coEvery { isAutomationEnabledNow() } answers { automationEnabled } },
            connectionPrefs = connectionPrefs,
            networkPrefs = mockk<NetworkManagementStore> { coEvery { getAllRulesNow() } returns rules },
            connectionStatus =
                mockk<ConnectionStatusSource> {
                    every { this@mockk.status } returns this@ApplyNetworkRuleUseCaseTest.status
                },
            vpnLauncher = launcher,
        )

    @Test
    fun `a network's own rule wins, then the open Wi-Fi or mobile data default`() {
        assertEquals(home, ruleFor(rules, "\"Home\"", isWifi = true))
        assertEquals(openWifi, ruleFor(rules, "\"Cafe\"", isWifi = true))
        assertEquals(mobileData, ruleFor(rules, "", isWifi = false))
        assertNull(ruleFor(emptyList(), "\"Cafe\"", isWifi = true))
    }

    @Test
    fun `always-connect only connects when disconnected`() {
        assertEquals(RuleAction.Connect, actionFor(NetworkBehavior.AlwaysConnect, ConnectionStatus.DISCONNECTED))
        assertEquals(RuleAction.Nothing, actionFor(NetworkBehavior.AlwaysConnect, ConnectionStatus.CONNECTED))
        assertEquals(RuleAction.Disconnect, actionFor(NetworkBehavior.AlwaysDisconnect, ConnectionStatus.CONNECTED))
        assertEquals(RuleAction.Nothing, actionFor(NetworkBehavior.RetainState, ConnectionStatus.DISCONNECTED))
    }

    @Test
    fun `joining an open Wi-Fi connects`() =
        runTest {
            applyRule("\"Cafe\"", isWifi = true, isNetworkChange = true)

            verify { launcher.launchVpn() }
            coVerify { connectionPrefs.setDisconnectedByUser(false) }
        }

    @Test
    fun `the same network delivered again keeps a manual disconnect`() =
        runTest {
            applyRule("\"Home\"", isWifi = true, isNetworkChange = false)

            verify { launcher.stopVpn() }
            coVerify(exactly = 0) { connectionPrefs.setDisconnectedByUser(any()) }
        }

    @Test
    fun `nothing happens while automation is off or the VPN is snoozed`() =
        runTest {
            automationEnabled = false
            applyRule("\"Cafe\"", isWifi = true, isNetworkChange = true)

            automationEnabled = true
            snoozeEndTime = Long.MAX_VALUE
            applyRule("\"Cafe\"", isWifi = true, isNetworkChange = true)

            verify(exactly = 0) { launcher.launchVpn() }
        }
}