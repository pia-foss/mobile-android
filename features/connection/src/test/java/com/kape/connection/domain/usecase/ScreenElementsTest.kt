package com.kape.connection.domain.usecase

import com.kape.customization.data.Element
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ScreenElementsTest {
    private val elements =
        listOf(
            ScreenElement(Element.VpnRegionSelection, "region"),
            ScreenElement(Element.ShadowsocksRegionSelection, "shadowsocks"),
            ScreenElement(Element.Traffic, "traffic", isVisible = false),
        )

    @Test
    fun `shadowsocks needs PIA obfuscation over OpenVPN`() {
        assertTrue(isShadowsocksAvailable(true, ObfuscationOptions.PIA, VpnProtocols.OpenVPN))
        assertFalse(isShadowsocksAvailable(false, ObfuscationOptions.PIA, VpnProtocols.OpenVPN))
        assertFalse(isShadowsocksAvailable(true, ObfuscationOptions.PIA, VpnProtocols.WireGuard))
    }

    @Test
    fun `the connection screen shows visible tiles, and shadowsocks only when available`() {
        val shown = screenElements(elements, isShadowsocksAvailable = false).connection.map { it.shouldDisplayElement }

        assertEquals(listOf(true, false, false), shown)
    }

    @Test
    fun `customization lists the shadowsocks tile whenever it is available, even if hidden`() {
        val hiddenShadowsocks = elements.map { if (it.element == Element.ShadowsocksRegionSelection) it.copy(isVisible = false) else it }

        val shown = screenElements(hiddenShadowsocks, isShadowsocksAvailable = true).customization.map { it.shouldDisplayElement }

        assertEquals(listOf(true, true, false), shown)
    }
}