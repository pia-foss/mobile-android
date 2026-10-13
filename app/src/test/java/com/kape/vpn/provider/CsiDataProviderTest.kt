package com.kape.vpn.provider

import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * The protocol information report must reflect what is stored, not the cached StateFlows, which only
 * hold fresh values while something collects them and otherwise report their defaults.
 */
class CsiDataProviderTest {
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val csiDataProvider = CsiDataProvider(mockk<CsiPrefs>(relaxed = true), settingsPrefs, "user-agent")

    @BeforeEach
    fun setUp() {
        // Stale defaults, as seen when nothing is collecting the flows.
        every { settingsPrefs.selectedProtocol } returns MutableStateFlow(VpnProtocols.Automatic)
        every { settingsPrefs.wireGuardSettings } returns MutableStateFlow(WireGuardSettings())
        every { settingsPrefs.isMaceEnabled } returns MutableStateFlow(false)
        every { settingsPrefs.isConnectOnLaunchEnabled } returns MutableStateFlow(false)

        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
        coEvery { settingsPrefs.getWireGuardSettingsNow() } returns
            WireGuardSettings(port = "53", useSmallPackets = true, useAmnezia = true)
        coEvery { settingsPrefs.isPortForwardingEnabledNow() } returns true
        coEvery { settingsPrefs.isMaceEnabledNow() } returns true
        coEvery { settingsPrefs.isAllowLocalTrafficEnabledNow() } returns true
        coEvery { settingsPrefs.isConnectOnLaunchEnabledNow() } returns true
        coEvery { settingsPrefs.isLaunchOnBootEnabledNow() } returns true
        coEvery { settingsPrefs.isConnectOnAppUpdateEnabledNow() } returns true
    }

    @Test
    fun `protocol information reports the stored wireguard settings`() {
        val report = csiDataProvider.protocolInformationProvider.value!!

        assertTrue(report.contains("Protocol: WireGuard\n"), report)
        assertTrue(report.contains("Remote Port: 53\n"), report)
        assertTrue(report.contains("Wireguard Use Small Packets: true\n"), report)
        assertTrue(report.contains("Wireguard Use Amnezia: true\n"), report)
        assertTrue(report.contains("MTU: 1280\n"), report)
    }

    @Test
    fun `protocol information reports the stored app settings`() {
        val report = csiDataProvider.protocolInformationProvider.value!!

        assertTrue(report.contains("Port Forwarding: true\n"), report)
        assertTrue(report.contains("MACE: true\n"), report)
        assertTrue(report.contains("Allow Local Network: true\n"), report)
        assertTrue(report.contains("1 click connect: true\n"), report)
        assertTrue(report.contains("Connect on Boot: true\n"), report)
        assertTrue(report.contains("Connect on App Updated: true\n"), report)
    }
}