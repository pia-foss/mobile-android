package com.kape.data.vpnserver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VpnServerOutdatedTest {
    private val endpoints =
        mapOf(VpnServer.ServerGroup.OPENVPN_UDP to listOf(VpnServer.ServerEndpointDetails("1.2.3.4", "cn", 8080)))

    private fun outdated(dedicatedIp: String? = null) =
        VpnServerOutdated(
            name = "Germany",
            iso = "DE",
            dns = "10.0.0.243",
            latency = "42",
            endpoints = endpoints,
            key = "de",
            latitude = "52.5",
            longitude = "13.4",
            isGeo = true,
            isOffline = true,
            allowsPortForwarding = true,
            dipToken = "token",
            dedicatedIp = dedicatedIp,
        )

    @Test
    fun `converts every field to a current server`() {
        val server = outdated().toVpnServer()

        assertEquals("Germany", server.name)
        assertEquals("DE", server.iso)
        assertEquals("10.0.0.243", server.dns)
        assertEquals("42", server.latency)
        assertEquals(endpoints, server.endpoints)
        assertEquals("de", server.key)
        assertEquals("52.5", server.latitude)
        assertEquals("13.4", server.longitude)
        assertTrue(server.isGeo)
        assertTrue(server.isOffline)
        assertTrue(server.allowsPortForwarding)
        assertEquals("token", server.dipToken)
    }

    @Test
    fun `an outdated server is never an auto region`() {
        assertFalse(outdated().toVpnServer().autoRegion)
    }

    @Test
    fun `keeps the dedicated ip status`() {
        assertFalse(outdated().toVpnServer().isDedicatedIp)
        val dedicated = outdated(dedicatedIp = "5.6.7.8").toVpnServer()
        assertTrue(dedicated.isDedicatedIp)
        assertEquals("5.6.7.8", dedicated.dedicatedIp)
    }
}