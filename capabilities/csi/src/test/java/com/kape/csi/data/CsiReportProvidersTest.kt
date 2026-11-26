package com.kape.csi.data

import com.kape.localprefs.data.settings.AutomaticSettings
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.privateinternetaccess.csi.ProviderType
import com.privateinternetaccess.csi.ReportType
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CsiReportProvidersTest {
    private val csiPrefs = mockk<CsiPrefs>()
    private val settingsPrefs =
        mockk<SettingsPrefs> {
            coEvery { getSelectedProtocolNow() } returns VpnProtocols.WireGuard
            coEvery { getWireGuardSettingsNow() } returns WireGuardSettings()
            coEvery { getOpenVpnSettingsNow() } returns OpenVpnSettings()
            coEvery { getAutoSettingsNow() } returns AutomaticSettings()
            coEvery { isPortForwardingEnabledNow() } returns false
            coEvery { isMaceEnabledNow() } returns false
            coEvery { isAllowLocalTrafficEnabledNow() } returns false
            coEvery { isConnectOnLaunchEnabledNow() } returns false
            coEvery { isLaunchOnBootEnabledNow() } returns false
            coEvery { isConnectOnAppUpdateEnabledNow() } returns false
        }
    private val providers = CsiReportProviders(csiPrefs, settingsPrefs, "pia-agent/4.2.0")

    private fun protocolInformation() = providers.protocolInformationProvider.value!!.lines()

    @Test
    fun `lists every provider with its file and report type`() {
        assertEquals(
            listOf(
                Triple("application_information", ProviderType.APPLICATION_INFORMATION, ReportType.DIAGNOSTIC),
                Triple("device_information", ProviderType.DEVICE_INFORMATION, ReportType.DIAGNOSTIC),
                Triple("last_known_exception", ProviderType.LAST_KNOWN_EXCEPTION, ReportType.CRASH),
                Triple("protocol_information", ProviderType.PROTOCOL_INFORMATION, ReportType.DIAGNOSTIC),
                Triple("regions_information", ProviderType.REGION_INFORMATION, ReportType.DIAGNOSTIC),
                Triple("user_settings", ProviderType.USER_SETTINGS, ReportType.DIAGNOSTIC),
                Triple("protocol_debug_logs", ProviderType.LOGGING_INFORMATION, ReportType.LOG),
            ),
            providers.all.map { Triple(it.filename, it.providerType, it.reportType) },
        )
    }

    @Test
    fun `only the exception, regions and user settings are persisted`() {
        assertEquals(
            listOf("last_known_exception", "regions_information", "user_settings"),
            providers.all.filter { it.isPersistedData }.map { it.filename },
        )
    }

    @Test
    fun `describes the wireguard settings`() {
        coEvery { settingsPrefs.getWireGuardSettingsNow() } returns
            WireGuardSettings(port = "51820", useSmallPackets = true, useAmnezia = true)

        val lines = protocolInformation()

        assertTrue("Protocol: WireGuard" in lines)
        assertTrue("Connection Type: UDP" in lines)
        assertTrue("Remote Port: 51820" in lines)
        assertTrue("Wireguard Use Amnezia: true" in lines)
        assertTrue("MTU: 1280" in lines)
        assertTrue("Data Encryption: ChaCha20" in lines)
        assertTrue("Data Authentication: " in lines)
        assertTrue("Wireguard Handshake: NOISE_IK" in lines)
    }

    @Test
    fun `describes the openvpn settings, with gcm ciphers as the authentication`() {
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.OpenVPN
        coEvery { settingsPrefs.getOpenVpnSettingsNow() } returns
            OpenVpnSettings(transport = Transport.TCP, dataEncryption = DataEncryption.AES_256_GCM, useSmallPackets = true)

        val lines = protocolInformation()

        assertTrue("Protocol: OpenVPN" in lines)
        assertTrue("Connection Type: TCP" in lines)
        assertTrue("MTU: 1350" in lines)
        assertTrue("Data Encryption: AES-256-GCM" in lines)
        assertTrue("Data Authentication: AES-256-GCM" in lines)
        assertTrue("OpenVPN Handshake: RSA4096" in lines)
    }

    @Test
    fun `automatic lists the mtu of both protocols`() {
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.Automatic

        assertTrue("MTU: WireGuard 1420, OpenVPN 1420" in protocolInformation())

        coEvery { settingsPrefs.getAutoSettingsNow() } returns AutomaticSettings(useSmallPackets = true)

        assertTrue("MTU: WireGuard 1280, OpenVPN 1350" in protocolInformation())
    }

    @Test
    fun `reports each protocol's own small packets and handshake, whichever is active`() {
        coEvery { settingsPrefs.getSelectedProtocolNow() } returns VpnProtocols.OpenVPN
        coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings(useSmallPackets = true)
        coEvery { settingsPrefs.getOpenVpnSettingsNow() } returns OpenVpnSettings(useSmallPackets = false)

        val lines = protocolInformation()

        assertTrue("OVPN Use Small Packets: false" in lines)
        assertTrue("Wireguard Use Small Packets: true" in lines)
        assertTrue("OpenVPN Handshake: RSA4096" in lines)
        assertTrue("Wireguard Handshake: NOISE_IK" in lines)
    }

    @Test
    fun `includes the blocking and app settings`() {
        coEvery { settingsPrefs.isPortForwardingEnabledNow() } returns true
        coEvery { settingsPrefs.isMaceEnabledNow() } returns true
        coEvery { settingsPrefs.isConnectOnLaunchEnabledNow() } returns true
        coEvery { settingsPrefs.isConnectOnAppUpdateEnabledNow() } returns true

        val lines = protocolInformation()

        assertTrue("Port Forwarding: true" in lines)
        assertTrue("MACE: true" in lines)
        assertTrue("Allow Local Network: false" in lines)
        assertTrue("1 click connect: true" in lines)
        assertTrue("Connect on Boot: false" in lines)
        assertTrue("Connect on App Updated: true" in lines)
    }

    @Test
    fun `redacts ip addresses from the protocol information`() {
        coEvery { settingsPrefs.getWireGuardSettingsNow() } returns WireGuardSettings(port = "10.0.0.1")

        assertTrue("Remote Port: REDACTED" in protocolInformation())
    }

    @Test
    fun `redacts ip addresses from the debug logs`() {
        coEvery { csiPrefs.getProtocolDebugLogsNow() } returns "Connecting to 192.168.1.20:1337 via 10.0.0.243\nversion 1.2.3"

        assertEquals("Connecting to REDACTED:1337 via REDACTED\nversion 1.2.3", providers.protocolDebugLogsProvider.value)
    }

    @Test
    fun `reports the stored exception and the user agent`() {
        coEvery { csiPrefs.getLastKnownExceptionNow() } returns "java.lang.IllegalStateException: boom"

        assertEquals("java.lang.IllegalStateException: boom", providers.lastKnownExceptionProvider.value)
        assertEquals("pia-agent/4.2.0", providers.applicationInformationProvider.value)
    }

    @Test
    fun `regions and user settings aren't reported yet`() {
        assertEquals("", providers.regionInformationProvider.value)
        assertEquals("", providers.userSettingsProvider.value)
        assertFalse(providers.protocolInformationProvider.value.isNullOrEmpty())
    }
}