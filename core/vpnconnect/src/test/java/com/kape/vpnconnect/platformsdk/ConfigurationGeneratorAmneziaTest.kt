package com.kape.vpnconnect.platformsdk

import android.content.Context
import com.kape.connection.model.AwgObfuscationSettings
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.platformsdk.vpn.wireguard.WireGuardObfuscation
import com.kape.platformsdk.vpn.wireguard.WireGuardVpnConfiguration
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.utils.CountryDetector
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * The "Use Amnezia" toggle switches the WireGuard protocol from plain WireGuard endpoints to
 * AmneziaWG endpoints.
 */
class ConfigurationGeneratorAmneziaTest {
    private val settingsPrefs = mockk<SettingsPrefs>(relaxed = true)
    private val connectionPrefs = mockk<ConnectionPrefs>(relaxed = true)

    private val server =
        VpnServer(
            name = "Netherlands",
            iso = "NL",
            dns = "nl.example",
            latency = null,
            endpoints =
                mapOf(
                    ServerGroup.WIREGUARD to listOf(ServerEndpointDetails(ip = "10.1.0.1:1337", cn = "wg")),
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
        coEvery { connectionPrefs.getAwgObfuscationNow() } returns null
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.WireGuard
        coEvery { settingsPrefs.isMaceEnabledNow() } returns false
        coEvery { settingsPrefs.getSelectedDnsOptionNow() } returns DnsOptions.PIA

        generator =
            ConfigurationGenerator(
                caCertificate = "",
                connectionSource = mockk<ConnectionDataSource>(relaxed = true),
                settingsPrefs = settingsPrefs,
                connectionPrefs = connectionPrefs,
                getActiveInterfaceDnsUseCase = mockk<GetActiveInterfaceDnsUseCase>(relaxed = true),
                context = mockk<Context>(relaxed = true),
                countryDetector = mockk<CountryDetector>(relaxed = true),
            )
    }

    @Test
    fun `wireguard uses plain wireguard endpoints when amnezia toggled off`() =
        runTest {
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings()

            val configurations = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>()

            assertEquals(1, configurations.size)
            assertEquals("10.1.0.1", configurations.single().host)
            assertEquals(WireGuardObfuscation.None, configurations.single().obfuscation)
        }

    @Test
    fun `wireguard uses amnezia endpoints when amnezia toggled on`() =
        runTest {
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings().copy(useAmnezia = true)

            val configurations = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>()

            assertEquals(1, configurations.size)
            val configuration = configurations.single()
            assertEquals("10.1.0.4", configuration.host)
            assertEquals(1338, configuration.port)
            assertTrue(configuration.obfuscation is WireGuardObfuscation.Amnezia)
        }

    @Test
    fun `amnezia falls back to wireguard when the server has no amnezia endpoints`() =
        runTest {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns
                server.copy(endpoints = server.endpoints - ServerGroup.AMNEZIA)
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns
                WireGuardSettings().copy(useAmnezia = true, useSmallPackets = true)

            val configuration = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>().single()

            assertEquals("10.1.0.1", configuration.host)
            assertEquals(1337, configuration.port)
            assertEquals(WireGuardObfuscation.None, configuration.obfuscation)
            assertEquals(1280, configuration.mtu)
        }

    @Test
    fun `amnezia falls back to wireguard when the server amnezia endpoint list is empty`() =
        runTest {
            coEvery { connectionPrefs.getSelectedVpnServerNow() } returns
                server.copy(endpoints = server.endpoints + (ServerGroup.AMNEZIA to emptyList()))
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings().copy(useAmnezia = true)

            val configuration = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>().single()

            assertEquals("10.1.0.1", configuration.host)
            assertEquals(WireGuardObfuscation.None, configuration.obfuscation)
        }

    @Test
    fun `amnezia keeps the wireguard small packets mtu`() =
        runTest {
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns
                WireGuardSettings().copy(useAmnezia = true, useSmallPackets = true)

            val configuration = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>().single()

            assertEquals(1280, configuration.mtu)
        }

    @Test
    fun `amnezia uses the stored obfuscation settings when available`() =
        runTest {
            val stored =
                AwgObfuscationSettings(
                    junkPacketCount = 7,
                    junkPacketMinSize = 30,
                    junkPacketMaxSize = 120,
                    initPacketJunkSize = 9,
                    responsePacketJunkSize = 4,
                    initPacketMagicHeader = 11,
                    responsePacketMagicHeader = 12,
                    underloadPacketMagicHeader = 13,
                    transportPacketMagicHeader = 14,
                )
            coEvery { connectionPrefs.getAwgObfuscationNow() } returns stored
            coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings().copy(useAmnezia = true)

            val configuration = generator.generateConfigurations().filterIsInstance<WireGuardVpnConfiguration>().single()

            assertEquals(stored.toAmnezia(), configuration.obfuscation)
        }
}