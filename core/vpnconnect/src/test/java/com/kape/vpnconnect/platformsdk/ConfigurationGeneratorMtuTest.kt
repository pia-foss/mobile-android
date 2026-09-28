package com.kape.vpnconnect.platformsdk

import android.content.Context
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.localprefs.data.settings.AutomaticSettings
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.platformsdk.vpn.openvpn.OpenVpnConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardVpnConfiguration
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.utils.CountryDetector
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Regression tests for the "Use Small Packets" toggle: the MTU handed to the VPN library must follow
 * the toggle for every protocol, including Automatic.
 */
class ConfigurationGeneratorMtuTest {
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)
    private val countryDetector = mockk<CountryDetector>()

    private val server =
        VpnServer(
            name = "Netherlands",
            iso = "NL",
            dns = "nl.example",
            latency = null,
            endpoints =
                mapOf(
                    ServerGroup.WIREGUARD to listOf(ServerEndpointDetails(ip = "10.1.0.1:1337", cn = "wg")),
                    ServerGroup.OPENVPN_UDP to listOf(ServerEndpointDetails(ip = "10.1.0.2", cn = "udp")),
                    ServerGroup.OPENVPN_TCP to listOf(ServerEndpointDetails(ip = "10.1.0.3", cn = "tcp")),
                    ServerGroup.AMNEZIA to listOf(ServerEndpointDetails(ip = "10.1.0.4", cn = "awg")),
                ),
            key = "nl",
            latitude = null,
            longitude = null,
            isGeo = false,
            isOffline = false,
            allowsPortForwarding = false,
            autoRegion = false,
            dipToken = null,
            dedicatedIp = null,
        )

    private lateinit var generator: ConfigurationGenerator

    @BeforeEach
    fun setUp() {
        coEvery { connectionPrefs.getSelectedVpnServerNow() } returns server
        every { connectionPrefs.awgObfuscation } returns MutableStateFlow(null)
        coEvery { settingsPrefs.isMaceEnabledNow() } returns false
        coEvery { settingsPrefs.getSelectedDnsOptionNow() } returns DnsOptions.PIA
        coEvery { countryDetector.detectCountry() } returns "NL"

        generator =
            ConfigurationGenerator(
                caCertificate = "",
                connectionSource = mockk<ConnectionDataSource>(relaxed = true),
                settingsPrefs = settingsPrefs,
                connectionPrefs = connectionPrefs,
                getActiveInterfaceDnsUseCase = mockk<GetActiveInterfaceDnsUseCase>(relaxed = true),
                context = mockk<Context>(relaxed = true),
                countryDetector = countryDetector,
            )
    }

    @Test
    fun `wireguard uses small mtu when small packets toggled on`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings().copy(useSmallPackets = true)

            val configurations = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>()

            assertTrue(configurations.isNotEmpty())
            configurations.forEach { assertEquals(1280, it.mtu) }
        }

    @Test
    fun `wireguard uses default mtu when small packets toggled off`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings()

            val configurations = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>()

            assertTrue(configurations.isNotEmpty())
            configurations.forEach { assertEquals(1420, it.mtu) }
        }

    @Test
    fun `openvpn uses small mtu and mssfix when small packets toggled on`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.OpenVPN
            coEvery { settingsPrefs.getOpenVpnSettingsNow() } returns OpenVpnSettings().copy(useSmallPackets = true)

            val configurations = generator.generateConfigurations().filterIsInstance<OpenVpnConfiguration>()

            assertTrue(configurations.isNotEmpty())
            configurations.forEach {
                assertEquals(1350, it.mtu)
                assertTrue(it.ovpnConfiguration.contains("mssfix 1350"))
            }
        }

    @Test
    fun `automatic follows its small packets toggle for every protocol`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic
            coEvery { settingsPrefs.getAutoSettingsNow() } returns AutomaticSettings().copy(useSmallPackets = true)

            val configurations = generator.generateConfigurations()
            val wireGuard = configurations.filterIsInstance<WireGuardVpnConfiguration>()
            val openVpn = configurations.filterIsInstance<OpenVpnConfiguration>()

            // WireGuard and AmneziaWG endpoints
            assertEquals(2, wireGuard.size)
            wireGuard.forEach { assertEquals(1280, it.mtu) }
            // OpenVPN UDP and TCP endpoints
            assertEquals(2, openVpn.size)
            openVpn.forEach { assertEquals(1350, it.mtu) }
        }

    @Test
    fun `automatic uses default mtu when small packets toggled off`() =
        runTest {
            coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic
            coEvery { settingsPrefs.getAutoSettingsNow() } returns AutomaticSettings()

            val configurations = generator.generateConfigurations()

            assertEquals(4, configurations.size)
            configurations.filterIsInstance<WireGuardVpnConfiguration>().forEach { assertEquals(1420, it.mtu) }
            configurations.filterIsInstance<OpenVpnConfiguration>().forEach { assertEquals(1420, it.mtu) }
        }
}