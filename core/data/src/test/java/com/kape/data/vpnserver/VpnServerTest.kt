package com.kape.data.vpnserver

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal fun vpnServer(
    key: String = "us_east",
    iso: String = "US",
    name: String = "US East",
    latency: String? = null,
    dedicatedIp: String? = null,
    allowsPortForwarding: Boolean = true,
) = VpnServer(
    name = name,
    iso = iso,
    dns = "10.0.0.243",
    latency = latency,
    endpoints = mapOf(VpnServer.ServerGroup.WIREGUARD to listOf(VpnServer.ServerEndpointDetails("1.2.3.4", "cn"))),
    key = key,
    latitude = null,
    longitude = null,
    isGeo = false,
    isOffline = false,
    allowsPortForwarding = allowsPortForwarding,
    autoRegion = true,
    dipToken = null,
    dedicatedIp = dedicatedIp,
)

internal class VpnServerTest {
    @Test
    fun `a server is dedicated only when it has a dedicated ip`() {
        assertFalse(vpnServer().isDedicatedIp)
        assertFalse(vpnServer(dedicatedIp = "").isDedicatedIp)
        assertTrue(vpnServer(dedicatedIp = "5.6.7.8").isDedicatedIp)
    }

    @Test
    fun `servers with the same key, country and dedicated ip status are equal`() {
        assertEquals(vpnServer(name = "a", latency = "10"), vpnServer(name = "b", latency = "200"))
    }

    @Test
    fun `equal servers have equal hash codes, whatever their latency`() {
        val fast = vpnServer(name = "a", latency = "10")
        val slow = vpnServer(name = "b", latency = "200")

        assertEquals(fast.hashCode(), slow.hashCode())
        assertEquals(1, setOf(fast, slow).size)
        assertEquals(listOf(fast), listOf(fast, slow).distinct())
    }

    @Test
    fun `a server's hash code survives a latency update`() {
        val server = vpnServer(latency = "10")
        val servers = hashSetOf(server)

        server.latency = "500"

        assertTrue(server in servers)
    }

    @Test
    fun `two dedicated IPs in the same region are different servers`() {
        assertNotEquals(vpnServer(dedicatedIp = "5.6.7.8"), vpnServer(dedicatedIp = "9.9.9.9"))
        assertEquals(vpnServer(dedicatedIp = "5.6.7.8"), vpnServer(dedicatedIp = "5.6.7.8", latency = "42"))
    }

    @Test
    fun `servers differing in key, country or dedicated ip status are not equal`() {
        assertNotEquals(vpnServer(), vpnServer(key = "us_west"))
        assertNotEquals(vpnServer(), vpnServer(iso = "CA"))
        assertNotEquals(vpnServer(), vpnServer(dedicatedIp = "5.6.7.8"))
        assertEquals(vpnServer(), vpnServer(dedicatedIp = ""))
        assertFalse(vpnServer().equals("us_east"))
        assertFalse(vpnServer().equals(null))
    }

    @Test
    fun `server groups print their api names`() {
        assertEquals(
            listOf("openvpn_tcp", "openvpn_udp", "wireguard", "meta", "awg"),
            VpnServer.ServerGroup.entries.map { it.toString() },
        )
    }

    @Test
    fun `port forwarding is stored under its legacy name`() {
        val json = Json.encodeToJsonElement(VpnServer.serializer(), vpnServer(allowsPortForwarding = false)).jsonObject

        assertFalse(json.getValue("isAllowsPF").jsonPrimitive.boolean)
        assertEquals(vpnServer(allowsPortForwarding = false), Json.decodeFromJsonElement(VpnServer.serializer(), json))
    }
}