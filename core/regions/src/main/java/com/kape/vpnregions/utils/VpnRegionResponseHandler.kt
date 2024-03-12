package com.kape.vpnregions.utils

import com.kape.utils.vpnserver.VpnServer
import com.kape.utils.vpnserver.VpnServerInfo
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.regions.RegionsProtocol
import com.privateinternetaccess.regions.model.VpnRegionsResponse

fun adaptVpnServers(vpnRegionsResponse: VpnRegionsResponse): Map<String, VpnServer> {
    val servers = mutableMapOf<String, VpnServer>()
    for (region in vpnRegionsResponse.regions) {
        val wireguardEndpoints = region.servers[RegionsProtocol.WIREGUARD.protocol]
        val ovpnTcpEndpoints = region.servers[RegionsProtocol.OPENVPN_TCP.protocol]
        val ovpnUdpEndpoints = region.servers[RegionsProtocol.OPENVPN_UDP.protocol]
        val metaEndpoints = region.servers[RegionsProtocol.META.protocol]

        val regionEndpoints =
            mutableMapOf<VpnServer.ServerGroup, List<VpnServer.ServerEndpointDetails>>()

        vpnRegionsResponse.groups[RegionsProtocol.WIREGUARD.protocol]?.let { group ->
            val port = group.first().ports.first().toString()
            wireguardEndpoints?.let {
                val mappedEndpoints = mutableListOf<VpnServer.ServerEndpointDetails>()
                for (wireguardEndpoint in it) {
                    // Application does not support the user option to choose wg ports and
                    // expect the format `endpoint:port`, as it is not aware of wg ports.
                    mappedEndpoints.add(
                        VpnServer.ServerEndpointDetails(
                            "${wireguardEndpoint.ip}:$port",
                            wireguardEndpoint.cn,
                        ),
                    )
                }
                regionEndpoints[VpnServer.ServerGroup.WIREGUARD] = mappedEndpoints
            }
        }

        ovpnTcpEndpoints?.let {
            val mappedEndpoints = mutableListOf<VpnServer.ServerEndpointDetails>()
            for (ovpnTcpEndpoint in it) {
                mappedEndpoints.add(
                    VpnServer.ServerEndpointDetails(
                        ovpnTcpEndpoint.ip,
                        ovpnTcpEndpoint.cn,
                    ),
                )
            }
            regionEndpoints[VpnServer.ServerGroup.OPENVPN_TCP] = mappedEndpoints
        }

        ovpnUdpEndpoints?.let {
            val mappedEndpoints = mutableListOf<VpnServer.ServerEndpointDetails>()
            for (ovpnUdpEndpoint in it) {
                mappedEndpoints.add(
                    VpnServer.ServerEndpointDetails(
                        ovpnUdpEndpoint.ip,
                        ovpnUdpEndpoint.cn,
                    ),
                )
            }
            regionEndpoints[VpnServer.ServerGroup.OPENVPN_UDP] = mappedEndpoints
        }

        metaEndpoints?.let {
            val mappedEndpoints = mutableListOf<VpnServer.ServerEndpointDetails>()
            for (metaEndpoint in it) {
                mappedEndpoints.add(
                    VpnServer.ServerEndpointDetails(
                        metaEndpoint.ip,
                        metaEndpoint.cn,
                    ),
                )
            }
            regionEndpoints[VpnServer.ServerGroup.META] = mappedEndpoints
        }

        val server = VpnServer(
            region.name,
            region.country,
            region.dns,
            null,
            regionEndpoints,
            region.id,
            region.latitude,
            region.longitude,
            region.geo,
            region.offline,
            region.portForward,
            null,
            null,
        )
        servers[region.id] = server
    }
    return servers
}

fun adaptServersInfo(vpnRegionsResponse: VpnRegionsResponse): VpnServerInfo {
    val autoRegions = mutableListOf<String>()
    vpnRegionsResponse.regions.filter { it.autoRegion }.forEach { region ->
        autoRegions.add(region.id)
    }
    val ovpntcp = mutableListOf<Int>()
    vpnRegionsResponse.groups[RegionsProtocol.OPENVPN_TCP.protocol]?.forEach { protocolPorts ->
        ovpntcp.addAll(protocolPorts.ports)
    }
    val ovpnudp = mutableListOf<Int>()
    vpnRegionsResponse.groups[RegionsProtocol.OPENVPN_UDP.protocol]?.forEach { protocolPorts ->
        ovpnudp.addAll(protocolPorts.ports)
    }
    return VpnServerInfo(autoRegions, ovpnudp, ovpntcp)
}

fun getServerForDip(
    server: VpnServer,
    dip: DedicatedIPInformationResponse.DedicatedIPInformation,
): VpnServer {
    val updatedEndpointsPerProtocol =
        mutableMapOf<VpnServer.ServerGroup, List<VpnServer.ServerEndpointDetails>>()
    server.endpoints.forEach { (serverGroup, serverEndpointDetails) ->
        val updatedEndpointDetails =
            mutableListOf<VpnServer.ServerEndpointDetails>()
        when (serverGroup) {
            VpnServer.ServerGroup.OPENVPN_TCP,
            VpnServer.ServerGroup.OPENVPN_UDP,
            VpnServer.ServerGroup.META,
            -> {
                dip.ip?.let { ip ->
                    dip.cn?.let { cn ->
                        updatedEndpointDetails.add(
                            VpnServer.ServerEndpointDetails(
                                ip,
                                cn,
                            ),
                        )
                    }
                }
            }

            VpnServer.ServerGroup.WIREGUARD -> {
                val port: String =
                    serverEndpointDetails[0].ip.split(":")[1]
                dip.cn?.let { cn ->
                    updatedEndpointDetails.add(
                        VpnServer.ServerEndpointDetails(
                            dip.ip + ":" + port,
                            cn,
                        ),
                    )
                }
            }
        }
        updatedEndpointsPerProtocol[serverGroup] = updatedEndpointDetails
    }

    return VpnServer(
        server.name,
        server.iso,
        server.dns,
        server.latency,
        updatedEndpointsPerProtocol,
        server.key,
        server.latitude,
        server.longitude,
        server.isGeo,
        server.isOffline,
        server.allowsPortForwarding,
        dip.dipToken,
        dip.ip,
    )
}