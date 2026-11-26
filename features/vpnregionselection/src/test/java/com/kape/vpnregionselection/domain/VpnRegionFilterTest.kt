package com.kape.vpnregionselection.domain

import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.vpnregionselection.vpnServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VpnRegionFilterTest {
    @Test
    fun `each protocol needs its own server group`() {
        assertEquals(ServerGroup.WIREGUARD, serverGroupFor(VpnProtocols.WireGuard, Transport.TCP))
        assertEquals(ServerGroup.OPENVPN_TCP, serverGroupFor(VpnProtocols.OpenVPN, Transport.TCP))
        assertEquals(ServerGroup.OPENVPN_UDP, serverGroupFor(VpnProtocols.OpenVPN, Transport.UDP))
        assertNull(serverGroupFor(VpnProtocols.Automatic, Transport.UDP))
    }

    @Test
    fun `a region without endpoints for the protocol is not listed`() {
        val udpOnly = vpnServer("de", groups = setOf(ServerGroup.OPENVPN_UDP))

        assertTrue(udpOnly.isListed(ServerGroup.OPENVPN_UDP, showGeoLocated = true))
        assertFalse(udpOnly.isListed(ServerGroup.WIREGUARD, showGeoLocated = true))
        assertTrue(udpOnly.isListed(null, showGeoLocated = true))
    }

    @Test
    fun `geo-located regions are listed only when the user wants them`() {
        val geo = vpnServer("bs", isGeo = true)

        assertTrue(geo.isListed(null, showGeoLocated = true))
        assertFalse(geo.isListed(null, showGeoLocated = false))
    }
}