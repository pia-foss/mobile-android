package com.kape.regions.vpn.data

import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.regions.vpn.dedicatedIp
import com.kape.regions.vpn.vpnServer
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import com.privateinternetaccess.regions.RegionsProtocol
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse.ProtocolDetails
import com.privateinternetaccess.regions.model.VpnRegionsResponse.Region
import com.privateinternetaccess.regions.model.VpnRegionsResponse.Region.ServerDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VpnRegionMappersTest {
    private val wg = RegionsProtocol.WIREGUARD.protocol
    private val udp = RegionsProtocol.OPENVPN_UDP.protocol
    private val tcp = RegionsProtocol.OPENVPN_TCP.protocol
    private val meta = RegionsProtocol.META.protocol
    private val awg = RegionsProtocol.AMNEZIA.protocol

    private val allProtocols =
        mapOf(
            wg to listOf(ServerDetails("1.0.0.1", "wg-cn", false, null)),
            udp to listOf(ServerDetails("1.0.0.2", "udp-cn", true, null)),
            tcp to listOf(ServerDetails("1.0.0.3", "tcp-cn", true, null)),
            meta to listOf(ServerDetails("1.0.0.4", "meta-cn", false, null)),
            awg to listOf(ServerDetails("1.0.0.5", "awg-cn", false, 51820)),
        )

    @Test
    fun `maps a region's fields to a server without latency or dedicated IP`() {
        val server =
            response(regions = listOf(region(id = "de_berlin", autoRegion = false, geo = true, offline = true, portForward = true)))
                .toVpnRegions(isBundled = false)
                .servers
                .single()

        assertEquals("de_berlin", server.key)
        assertEquals("Name de_berlin", server.name)
        assertEquals("DE", server.iso)
        assertEquals("de_berlin.example", server.dns)
        assertEquals("52.5", server.latitude)
        assertEquals("13.4", server.longitude)
        assertTrue(server.isGeo)
        assertTrue(server.isOffline)
        assertTrue(server.allowsPortForwarding)
        assertFalse(server.autoRegion)
        assertNull(server.latency)
        assertNull(server.dipToken)
        assertNull(server.dedicatedIp)
        assertFalse(server.isDedicatedIp)
    }

    @Test
    fun `maps every protocol's endpoints, appending the WireGuard group port and keeping the Amnezia port`() {
        val endpoints =
            response(regions = listOf(region(servers = allProtocols)))
                .toVpnRegions(isBundled = false)
                .servers
                .single()
                .endpoints

        assertEquals(
            mapOf(
                ServerGroup.WIREGUARD to listOf(ServerEndpointDetails("1.0.0.1:1337", "wg-cn")),
                ServerGroup.OPENVPN_UDP to listOf(ServerEndpointDetails("1.0.0.2", "udp-cn")),
                ServerGroup.OPENVPN_TCP to listOf(ServerEndpointDetails("1.0.0.3", "tcp-cn")),
                ServerGroup.META to listOf(ServerEndpointDetails("1.0.0.4", "meta-cn")),
                ServerGroup.AMNEZIA to listOf(ServerEndpointDetails("1.0.0.5", "awg-cn", 51820)),
            ),
            endpoints,
        )
    }

    @Test
    fun `drops WireGuard endpoints when the WireGuard group has no port`() {
        val withoutGroup = response(groups = emptyMap(), regions = listOf(region(servers = allProtocols)))
        val withoutPorts = response(groups = mapOf(wg to listOf(ProtocolDetails("wireguard", emptyList()))), regions = withoutGroup.regions)

        listOf(withoutGroup, withoutPorts).forEach {
            val endpoints =
                it
                    .toVpnRegions(isBundled = false)
                    .servers
                    .single()
                    .endpoints
            assertFalse(ServerGroup.WIREGUARD in endpoints)
            assertTrue(ServerGroup.META in endpoints)
        }
    }

    @Test
    fun `leaves out protocols a region has no servers for`() {
        val endpoints =
            response(regions = listOf(region(servers = mapOf(meta to allProtocols.getValue(meta)))))
                .toVpnRegions(isBundled = false)
                .servers
                .single()
                .endpoints

        assertEquals(setOf(ServerGroup.META), endpoints.keys)
    }

    @Test
    fun `builds the server info from auto regions and the OpenVPN group ports`() {
        val regions =
            response(
                groups =
                    mapOf(
                        udp to listOf(ProtocolDetails("openvpn_udp", listOf(8080, 53)), ProtocolDetails("openvpn_udp", listOf(1194))),
                        tcp to listOf(ProtocolDetails("openvpn_tcp", listOf(443))),
                    ),
                regions = listOf(region("auto1"), region("manual", autoRegion = false), region("auto2")),
            ).toVpnRegions(isBundled = true)

        assertTrue(regions.isBundled)
        assertEquals(listOf("auto1", "auto2"), regions.info.autoRegions)
        assertEquals(listOf(8080, 53, 1194), regions.info.udpPorts)
        assertEquals(listOf(443), regions.info.tcpPorts)
    }

    @Test
    fun `server info has empty port lists when the OpenVPN groups are missing`() {
        val info = response(groups = emptyMap(), regions = listOf(region("a"))).toVpnRegions(isBundled = false).info

        assertEquals(emptyList<Int>(), info.udpPorts)
        assertEquals(emptyList<Int>(), info.tcpPorts)
        assertFalse(info.isValid())
    }

    @Test
    fun `adds a dedicated IP copy of the matching region next to the regular servers`() {
        val servers = listOf(vpnServer("de"), vpnServer("fr"))

        val result = servers.withDedicatedIps(listOf(dedicatedIp("fr")))

        assertEquals(listOf("de" to false, "fr" to false, "fr" to true), result.map { it.key to it.isDedicatedIp })
        assertEquals("203.0.113.7", result.last().dedicatedIp)
    }

    @Test
    fun `keeps every dedicated IP in the same region`() {
        val servers = listOf(vpnServer("fr"))

        val result = servers.withDedicatedIps(listOf(dedicatedIp("fr", ip = "203.0.113.7"), dedicatedIp("fr", ip = "203.0.113.8")))

        assertEquals(listOf(null, "203.0.113.7", "203.0.113.8"), result.map { it.dedicatedIp })
    }

    @Test
    fun `ignores dedicated IPs whose region is not in the list`() {
        val servers = listOf(vpnServer("de"))

        assertEquals(servers, servers.withDedicatedIps(listOf(dedicatedIp("mars"))))
    }

    @Test
    fun `points every protocol of a dedicated IP server at the dedicated IP`() {
        val region =
            vpnServer("de").copy(
                endpoints =
                    mapOf(
                        ServerGroup.META to listOf(ServerEndpointDetails("10.0.0.4", "meta-cn")),
                        ServerGroup.OPENVPN_UDP to listOf(ServerEndpointDetails("10.0.0.2", "udp-cn")),
                        ServerGroup.OPENVPN_TCP to listOf(ServerEndpointDetails("10.0.0.3", "tcp-cn")),
                        ServerGroup.WIREGUARD to listOf(ServerEndpointDetails("10.0.0.1:1337", "wg-cn")),
                        ServerGroup.AMNEZIA to listOf(ServerEndpointDetails("10.0.0.5", "awg-cn", 51820)),
                    ),
            )

        val dip = region.withDedicatedIp(dedicatedIp("de", ip = "203.0.113.7"))

        assertEquals(
            mapOf(
                ServerGroup.META to listOf(ServerEndpointDetails("203.0.113.7", "dip-cn")),
                ServerGroup.OPENVPN_UDP to listOf(ServerEndpointDetails("203.0.113.7", "dip-cn", 8080)),
                ServerGroup.OPENVPN_TCP to listOf(ServerEndpointDetails("203.0.113.7", "dip-cn", null)),
                ServerGroup.WIREGUARD to listOf(ServerEndpointDetails("203.0.113.7:1337", "dip-cn")),
                ServerGroup.AMNEZIA to listOf(ServerEndpointDetails("203.0.113.7", "dip-cn", 51820)),
            ),
            dip.endpoints,
        )
        assertEquals("dip-token", dip.dipToken)
        assertEquals("203.0.113.7", dip.dedicatedIp)
        assertTrue(dip.isDedicatedIp)
    }

    @Test
    fun `falls back to the default Amnezia port and drops WireGuard without a region port`() {
        val region =
            vpnServer("de").copy(
                endpoints =
                    mapOf(
                        ServerGroup.WIREGUARD to listOf(ServerEndpointDetails("10.0.0.1", "wg-cn")),
                        ServerGroup.AMNEZIA to listOf(ServerEndpointDetails("10.0.0.5", "awg-cn")),
                    ),
            )

        val endpoints = region.withDedicatedIp(dedicatedIp("de")).endpoints

        assertEquals(emptyList<ServerEndpointDetails>(), endpoints[ServerGroup.WIREGUARD])
        assertEquals(listOf(ServerEndpointDetails("203.0.113.7", "dip-cn", 1338)), endpoints[ServerGroup.AMNEZIA])
    }

    @Test
    fun `a dedicated IP without an address or common name gets no endpoints and is not marked as dedicated`() {
        listOf(dip(ip = null, cn = "dip-cn"), dip(ip = "203.0.113.7", cn = null)).forEach { info ->
            val server = vpnServer("de").withDedicatedIp(info)

            assertTrue(server.endpoints.values.all { it.isEmpty() })
            assertEquals(vpnServer("de").endpoints.keys, server.endpoints.keys)
        }
        assertFalse(vpnServer("de").withDedicatedIp(dip(ip = null, cn = "dip-cn")).isDedicatedIp)
    }

    private fun dip(
        ip: String?,
        cn: String?,
    ) = DedicatedIPInformation(
        "de",
        ip,
        cn,
        emptyList(),
        null,
        "dip-token",
        DedicatedIPInformationResponse.Status.active,
        false,
        emptyMap(),
    )

    private fun response(
        groups: Map<String, List<ProtocolDetails>> = mapOf(wg to listOf(ProtocolDetails("wireguard", listOf(1337, 51820)))),
        regions: List<Region>,
    ) = VpnRegionsResponse(groups = groups, regions = regions)

    private fun region(
        id: String = "de_berlin",
        autoRegion: Boolean = true,
        geo: Boolean = false,
        offline: Boolean = false,
        portForward: Boolean = false,
        servers: Map<String, List<ServerDetails>> = allProtocols,
    ) = Region(
        id = id,
        name = "Name $id",
        country = "DE",
        dns = "$id.example",
        geo = geo,
        offline = offline,
        latitude = "52.5",
        longitude = "13.4",
        autoRegion = autoRegion,
        portForward = portForward,
        proxy = emptyList(),
        servers = servers,
    )
}