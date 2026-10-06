package com.kape.vpnregions.utils

import com.kape.data.vpnserver.VpnServer
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VpnRegionResponseHandlerTest {
    @Test
    fun `getServerForDip builds separate wireguard and amnezia endpoints`() {
        val server =
            VpnServer(
                "test",
                "us",
                "",
                null,
                mapOf(
                    VpnServer.ServerGroup.WIREGUARD to
                        listOf(
                            VpnServer.ServerEndpointDetails("1.2.3.4:1337", "wg.privateinternetaccess.com"),
                        ),
                    VpnServer.ServerGroup.AMNEZIA to
                        listOf(
                            VpnServer.ServerEndpointDetails(
                                "1.2.3.5",
                                "awg.privateinternetaccess.com",
                                1339,
                            ),
                        ),
                ),
                "id",
                null,
                null,
                false,
                false,
                false,
                false,
                null,
                null,
            )
        val dip =
            DedicatedIPInformationResponse.DedicatedIPInformation(
                id = "id",
                ip = "9.9.9.9",
                cn = "dip.privateinternetaccess.com",
                groups = null,
                dip_expire = null,
                dipToken = "token",
                status = DedicatedIPInformationResponse.Status.active,
            )

        val actual = getServerForDip(server, dip)

        val wireguardEndpoint = actual.endpoints[VpnServer.ServerGroup.WIREGUARD]?.first()
        assertEquals("9.9.9.9:1337", wireguardEndpoint?.ip)
        assertEquals("dip.privateinternetaccess.com", wireguardEndpoint?.cn)
        assertEquals(null, wireguardEndpoint?.port)

        val amneziaEndpoint = actual.endpoints[VpnServer.ServerGroup.AMNEZIA]?.first()
        assertEquals("9.9.9.9", amneziaEndpoint?.ip)
        assertEquals("dip.privateinternetaccess.com", amneziaEndpoint?.cn)
        assertEquals(1339, amneziaEndpoint?.port)
    }

    @Test
    fun `getServerForDip falls back to default amnezia port when none provided`() {
        val server =
            VpnServer(
                "test",
                "us",
                "",
                null,
                mapOf(
                    VpnServer.ServerGroup.AMNEZIA to
                        listOf(
                            VpnServer.ServerEndpointDetails("1.2.3.5", "awg.privateinternetaccess.com"),
                        ),
                ),
                "id",
                null,
                null,
                false,
                false,
                false,
                false,
                null,
                null,
            )
        val dip =
            DedicatedIPInformationResponse.DedicatedIPInformation(
                id = "id",
                ip = "9.9.9.9",
                cn = "dip.privateinternetaccess.com",
                groups = null,
                dip_expire = null,
                dipToken = "token",
                status = DedicatedIPInformationResponse.Status.active,
            )

        val actual = getServerForDip(server, dip)

        val amneziaEndpoint = actual.endpoints[VpnServer.ServerGroup.AMNEZIA]?.first()
        assertEquals("9.9.9.9", amneziaEndpoint?.ip)
        assertEquals(1338, amneziaEndpoint?.port)
    }

    @Test
    fun `getDipOpenVpnPorts returns udp ports for openvpn udp`() {
        val dip = dipWithPorts(mapOf("ovpnudp" to listOf(1197, 8080), "ovpntcp" to listOf(501)))

        assertEquals(listOf(1197, 8080), getDipOpenVpnPorts(dip, VpnServer.ServerGroup.OPENVPN_UDP))
    }

    @Test
    fun `getDipOpenVpnPorts returns tcp ports for openvpn tcp`() {
        val dip = dipWithPorts(mapOf("ovpnudp" to listOf(1197), "ovpntcp" to listOf(501)))

        assertEquals(listOf(501), getDipOpenVpnPorts(dip, VpnServer.ServerGroup.OPENVPN_TCP))
    }

    @Test
    fun `getDipOpenVpnPorts returns empty list when dip has no ports`() {
        val dip = dipWithPorts(emptyMap())

        assertEquals(emptyList<Int>(), getDipOpenVpnPorts(dip, VpnServer.ServerGroup.OPENVPN_UDP))
        assertEquals(emptyList<Int>(), getDipOpenVpnPorts(dip, VpnServer.ServerGroup.OPENVPN_TCP))
    }

    @Test
    fun `getDipOpenVpnPorts returns empty list for non openvpn groups`() {
        val dip =
            dipWithPorts(
                mapOf(
                    "ovpnudp" to listOf(1197),
                    "ovpntcp" to listOf(501),
                    "wg" to listOf(1337),
                    "meta" to listOf(443),
                    "awg" to listOf(1338),
                ),
            )

        assertEquals(emptyList<Int>(), getDipOpenVpnPorts(dip, VpnServer.ServerGroup.WIREGUARD))
        assertEquals(emptyList<Int>(), getDipOpenVpnPorts(dip, VpnServer.ServerGroup.META))
        assertEquals(emptyList<Int>(), getDipOpenVpnPorts(dip, VpnServer.ServerGroup.AMNEZIA))
    }

    private fun dipWithPorts(ports: Map<String, List<Int>>) =
        DedicatedIPInformationResponse.DedicatedIPInformation(
            id = "id",
            ip = "9.9.9.9",
            cn = "dip.privateinternetaccess.com",
            groups = null,
            dip_expire = null,
            dipToken = "token",
            status = DedicatedIPInformationResponse.Status.active,
            ports = ports,
        )
}