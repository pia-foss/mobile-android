package com.kape.settings.domain.usecase

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.localprefs.api.CsiStore
import com.kape.localprefs.api.SettingsStore
import com.kape.regions.vpn.data.openVpnApiKey
import com.kape.regions.vpn.domain.usecase.GetVpnServerInfoUseCase
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SettingsUseCasesTest {
    private val prefs = mockk<SettingsStore>(relaxed = true)

    @Test
    fun `choosing a protocol other than OpenVPN turns shadowsocks off`() =
        runTest {
            SelectProtocolUseCase(prefs)(VpnProtocols.WireGuard)

            coVerify { prefs.setSelectedProtocol(VpnProtocols.WireGuard) }
            coVerify { prefs.setShadowsocksObfuscationEnabled(false) }
        }

    @Test
    fun `choosing OpenVPN keeps shadowsocks`() =
        runTest {
            SelectProtocolUseCase(prefs)(VpnProtocols.OpenVPN)

            coVerify(exactly = 0) { prefs.setShadowsocksObfuscationEnabled(any()) }
        }

    @Test
    fun `switching transport uses that transport's first port and reports the change`() =
        runTest {
            coEvery { prefs.getOpenVpnSettingsNow() } returns OpenVpnSettings(transport = Transport.UDP, port = "8080")
            val serverInfo = mockk<GetVpnServerInfoUseCase> { coEvery { this@mockk() } returns VpnServerInfo(tcpPorts = listOf(443, 80)) }

            val changed = SetTransportUseCase(prefs, serverInfo)(Transport.TCP)

            assertTrue(changed)
            coVerify { prefs.setOpenVpnSettings(match { it.transport == Transport.TCP && it.port == "443" }) }
        }

    @Test
    fun `without a server list, a default port is used, and UDP turns shadowsocks off`() =
        runTest {
            coEvery { prefs.getOpenVpnSettingsNow() } returns OpenVpnSettings(transport = Transport.UDP, port = "8080")
            val serverInfo = mockk<GetVpnServerInfoUseCase> { coEvery { this@mockk() } returns VpnServerInfo() }

            val changed = SetTransportUseCase(prefs, serverInfo)(Transport.UDP)

            assertFalse(changed)
            coVerify { prefs.setOpenVpnSettings(match { it.port == "8080" }) }
            coVerify { prefs.setShadowsocksObfuscationEnabled(false) }
        }

    @Test
    fun `blocking the local network turns shadowsocks off`() =
        runTest {
            SetAllowLocalNetworkUseCase(prefs)(false)

            coVerify { prefs.setAllowLocalTrafficEnabled(false) }
            coVerify { prefs.setShadowsocksObfuscationEnabled(false) }
        }

    @Test
    fun `turning debug logging off deletes the stored logs`() =
        runTest {
            val csiPrefs = mockk<CsiStore>(relaxed = true)

            SetDebugLoggingUseCase(prefs, csiPrefs)(false)

            coVerify { csiPrefs.clearProtocolDebugLogs() }
        }

    @Test
    fun `excluding an app keeps the apps already excluded`() =
        runTest {
            coEvery { prefs.getVpnExcludedAppsNow() } returns listOf("com.bank")

            EditExcludedAppsUseCase(prefs).add("com.game")

            coVerify { prefs.setVpnExcludedApps(listOf("com.bank", "com.game")) }
        }

    @Test
    fun `changing the proxy app stops excluding the previous one`() =
        runTest {
            coEvery { prefs.getExternalProxyAppPackageNameNow() } returns "com.old.proxy"
            coEvery { prefs.getVpnExcludedAppsNow() } returns listOf("com.bank", "com.old.proxy")
            val excludedApps = mockk<EditExcludedAppsUseCase>(relaxed = true)

            SetExternalProxyAppUseCase(prefs, excludedApps)("com.new.proxy")

            coVerify { excludedApps.remove("com.old.proxy") }
            coVerify { excludedApps.add("com.new.proxy") }
            coVerify { prefs.setExternalProxyAppEnabled(true) }
        }

    @Test
    fun `clearing the proxy app turns it off`() =
        runTest {
            coEvery { prefs.getExternalProxyAppPackageNameNow() } returns "com.old.proxy"
            val excludedApps = mockk<EditExcludedAppsUseCase>(relaxed = true)

            SetExternalProxyAppUseCase(prefs, excludedApps)("")

            coVerify { excludedApps.remove("com.old.proxy") }
            coVerify(exactly = 0) { excludedApps.add(any()) }
            coVerify { prefs.setExternalProxyAppEnabled(false) }
        }

    @Test
    fun `dedicated IP ports follow the OpenVPN transport, without duplicates`() {
        val udp = VpnServer.ServerGroup.OPENVPN_UDP.openVpnApiKey()!!
        val tcp = VpnServer.ServerGroup.OPENVPN_TCP.openVpnApiKey()!!
        val ports =
            listOf(
                mapOf(udp to listOf(1194, 8080), tcp to listOf(443)),
                mapOf(udp to listOf(8080, 53)),
            )

        assertEquals(listOf(1194, 8080, 53), dedicatedIpOpenVpnPorts(ports, Transport.UDP))
        assertEquals(listOf(443), dedicatedIpOpenVpnPorts(ports, Transport.TCP))
        assertEquals(emptyList<Int>(), dedicatedIpOpenVpnPorts(ports, Transport.AUTO))
    }
}