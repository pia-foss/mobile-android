package com.kape.localprefs.prefs

import com.kape.settings.data.WireGuardSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The "Use Amnezia" toggle is persisted as part of the WireGuard settings JSON, so it must survive
 * a round trip and default to off for settings stored before the toggle existed.
 */
class WireGuardAmneziaSettingsTest {
    @Test
    fun `amnezia is off by default`() {
        assertFalse(WireGuardSettings().useAmnezia)
    }

    @Test
    fun `enabled amnezia is kept after being persisted and reloaded`() {
        val reloaded = roundTrip(WireGuardSettings().copy(useAmnezia = true))

        assertTrue(reloaded.useAmnezia)
    }

    @Test
    fun `disabled amnezia is kept after being persisted and reloaded`() {
        val reloaded = roundTrip(WireGuardSettings().copy(useAmnezia = true).copy(useAmnezia = false))

        assertFalse(reloaded.useAmnezia)
    }

    @Test
    fun `toggling amnezia keeps the other wireguard settings`() {
        val reloaded = roundTrip(WireGuardSettings(port = "53", useSmallPackets = true).copy(useAmnezia = true))

        assertTrue(reloaded.useAmnezia)
        assertEquals("53", reloaded.port)
        assertTrue(reloaded.useSmallPackets)
        assertEquals(1280, reloaded.mtu)
    }

    @Test
    fun `settings stored before the toggle existed decode with amnezia off`() {
        val legacy = protocolSettingsJson.decodeFromString<WireGuardSettings>("""{"port":"53","useSmallPackets":true}""")

        assertFalse(legacy.useAmnezia)
        assertEquals("53", legacy.port)
        assertTrue(legacy.useSmallPackets)
    }

    private fun roundTrip(settings: WireGuardSettings): WireGuardSettings =
        protocolSettingsJson.decodeFromString(protocolSettingsJson.encodeToString(settings))
}