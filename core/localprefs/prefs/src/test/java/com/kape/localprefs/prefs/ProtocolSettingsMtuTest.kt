package com.kape.localprefs.prefs

import com.kape.localprefs.data.settings.AutomaticSettings
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.WireGuardSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Regression tests for the "Use Small Packets" toggle. The MTU used to be a stored constructor
 * property whose default depended on useSmallPackets, so `copy(useSmallPackets = true)` kept the
 * old 1420 value and the toggle had no effect on the tunnel MTU.
 */
class ProtocolSettingsMtuTest {
    @Test
    fun `wireguard small packets toggle changes mtu`() {
        val settings = WireGuardSettings()
        assertEquals(1420, settings.mtu)

        val enabled = settings.copy(useSmallPackets = true)
        assertEquals(1280, enabled.mtu)
        assertEquals(1420, enabled.copy(useSmallPackets = false).mtu)
    }

    @Test
    fun `openvpn small packets toggle changes mtu`() {
        val settings = OpenVpnSettings()
        assertEquals(1420, settings.mtu)

        val enabled = settings.copy(useSmallPackets = true)
        assertEquals(1350, enabled.mtu)
        assertEquals(1420, enabled.copy(useSmallPackets = false).mtu)
    }

    @Test
    fun `automatic small packets toggle changes mtu`() {
        val settings = AutomaticSettings()
        assertEquals(1420, settings.mtu)

        val enabled = settings.copy(useSmallPackets = true)
        assertEquals(1350, enabled.mtu)
        assertEquals(1420, enabled.copy(useSmallPackets = false).mtu)
    }

    @Test
    fun `toggled settings keep their mtu after being persisted and reloaded`() {
        val wireGuard = roundTrip(WireGuardSettings().copy(useSmallPackets = true))
        val openVpn = roundTrip(OpenVpnSettings().copy(useSmallPackets = true))
        val automatic = roundTrip(AutomaticSettings().copy(useSmallPackets = true))

        assertEquals(1280, wireGuard.mtu)
        assertEquals(1350, openVpn.mtu)
        assertEquals(1350, automatic.mtu)
    }

    @Test
    fun `mtu is not persisted`() {
        val stored = protocolSettingsJson.encodeToString(WireGuardSettings().copy(useSmallPackets = true))

        assertFalse(stored.contains("mtu"), stored)
    }

    @Test
    fun `legacy stored settings with stale mtu are decoded using the toggle`() {
        // What the previous version wrote to disk after the user enabled Small Packets.
        val wireGuard = protocolSettingsJson.decodeFromString<WireGuardSettings>("""{"useSmallPackets":true,"mtu":1420}""")
        val openVpn = protocolSettingsJson.decodeFromString<OpenVpnSettings>("""{"useSmallPackets":true,"mtu":1420}""")
        val automatic = protocolSettingsJson.decodeFromString<AutomaticSettings>("""{"useSmallPackets":true,"mtu":1420}""")

        assertTrue(wireGuard.useSmallPackets)
        assertEquals(1280, wireGuard.mtu)
        assertEquals(1350, openVpn.mtu)
        assertEquals(1350, automatic.mtu)
    }

    @Test
    fun `legacy stored settings keep their other values`() {
        val wireGuard =
            protocolSettingsJson.decodeFromString<WireGuardSettings>("""{"port":"53","useSmallPackets":false,"mtu":1420}""")

        assertEquals("53", wireGuard.port)
        assertEquals(1420, wireGuard.mtu)
    }

    private inline fun <reified T> roundTrip(settings: T): T =
        protocolSettingsJson.decodeFromString(protocolSettingsJson.encodeToString(settings))
}