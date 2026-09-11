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
}