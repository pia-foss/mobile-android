package com.kape.regions.utils

import com.kape.dip.DipPrefs
import com.kape.utils.server.Server
import com.kape.utils.server.ServerInfo
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.regions.RegionsProtocol
import com.privateinternetaccess.regions.model.RegionsResponse

fun adaptServers(regionsResponse: RegionsResponse, dipPrefs: DipPrefs): Map<String, Server> {
    val servers = mutableMapOf<String, Server>()
    for (region in regionsResponse.regions) {
        val wireguardEndpoints = region.servers[RegionsProtocol.WIREGUARD.protocol]
        val ovpnTcpEndpoints = region.servers[RegionsProtocol.OPENVPN_TCP.protocol]
        val ovpnUdpEndpoints = region.servers[RegionsProtocol.OPENVPN_UDP.protocol]
        val metaEndpoints = region.servers[RegionsProtocol.META.protocol]

        val regionEndpoints =
            mutableMapOf<Server.ServerGroup, List<Server.ServerEndpointDetails>>()

        regionsResponse.groups[RegionsProtocol.WIREGUARD.protocol]?.let { group ->
            val port = group.first().ports.first().toString()
            wireguardEndpoints?.let {
                val mappedEndpoints = mutableListOf<Server.ServerEndpointDetails>()
                for (wireguardEndpoint in it) {
                    // Application does not support the user option to choose wg ports and
                    // expect the format `endpoint:port`, as it is not aware of wg ports.
                    mappedEndpoints.add(
                        Server.ServerEndpointDetails(
                            "${wireguardEndpoint.ip}:$port",
                            wireguardEndpoint.cn,
                        )
                    )
                }
                regionEndpoints[Server.ServerGroup.WIREGUARD] = mappedEndpoints
            }
        }

        ovpnTcpEndpoints?.let {
            val mappedEndpoints = mutableListOf<Server.ServerEndpointDetails>()
            for (ovpnTcpEndpoint in it) {
                mappedEndpoints.add(
                    Server.ServerEndpointDetails(
                        ovpnTcpEndpoint.ip,
                        ovpnTcpEndpoint.cn,
                    )
                )
            }
            regionEndpoints[Server.ServerGroup.OPENVPN_TCP] = mappedEndpoints
        }

        ovpnUdpEndpoints?.let {
            val mappedEndpoints = mutableListOf<Server.ServerEndpointDetails>()
            for (ovpnUdpEndpoint in it) {
                mappedEndpoints.add(
                    Server.ServerEndpointDetails(
                        ovpnUdpEndpoint.ip,
                        ovpnUdpEndpoint.cn,
                    )
                )
            }
            regionEndpoints[Server.ServerGroup.OPENVPN_UDP] = mappedEndpoints
        }

        metaEndpoints?.let {
            val mappedEndpoints = mutableListOf<Server.ServerEndpointDetails>()
            for (metaEndpoint in it) {
                mappedEndpoints.add(
                    Server.ServerEndpointDetails(
                        metaEndpoint.ip,
                        metaEndpoint.cn,
                    )
                )
            }
            regionEndpoints[Server.ServerGroup.META] = mappedEndpoints
        }

        val server = Server(
            region.name,
            region.country,
            null,
            regionEndpoints,
            region.id,
            region.latitude,
            region.longitude,
            region.geo,
            region.offline,
            region.portForward,
            null,
            null
        )
        servers[region.id] = dipPrefs.getDedicatedIps().firstOrNull { it.id == region.id }?.let {
            getServerForDip(server, it)
        } ?: server
    }
    return servers
}

fun adaptServersInfo(regionsResponse: RegionsResponse): ServerInfo {
    val autoRegions = mutableListOf<String>()
    regionsResponse.regions.filter { it.autoRegion }.forEach { region ->
        autoRegions.add(region.id)
    }
    val ovpntcp = mutableListOf<Int>()
    regionsResponse.groups[RegionsProtocol.OPENVPN_TCP.protocol]?.forEach { protocolPorts ->
        ovpntcp.addAll(protocolPorts.ports)
    }
    val ovpnudp = mutableListOf<Int>()
    regionsResponse.groups[RegionsProtocol.OPENVPN_UDP.protocol]?.forEach { protocolPorts ->
        ovpnudp.addAll(protocolPorts.ports)
    }
    return ServerInfo(autoRegions, ovpnudp, ovpntcp)
}

private fun getServerForDip(
    server: Server,
    dip: DedicatedIPInformationResponse.DedicatedIPInformation,
): Server {
    val updatedEndpointsPerProtocol =
        mutableMapOf<Server.ServerGroup, List<Server.ServerEndpointDetails>>()
    server.endpoints.forEach { (serverGroup, serverEndpointDetails) ->
        val updatedEndpointDetails =
            mutableListOf<Server.ServerEndpointDetails>()
        when (serverGroup) {
            Server.ServerGroup.OPENVPN_TCP,
            Server.ServerGroup.OPENVPN_UDP,
            Server.ServerGroup.META,
            -> {
                dip.ip?.let { ip ->
                    dip.cn?.let { cn ->
                        updatedEndpointDetails.add(
                            Server.ServerEndpointDetails(
                                ip,
                                cn,
                            ),
                        )
                    }
                }
            }

            Server.ServerGroup.WIREGUARD -> {
                val port: String =
                    serverEndpointDetails[0].ip.split(":")[1]
                dip.cn?.let { cn ->
                    updatedEndpointDetails.add(
                        Server.ServerEndpointDetails(
                            dip.ip + ":" + port,
                            cn,
                        ),
                    )
                }
            }
        }
        updatedEndpointsPerProtocol[serverGroup] = updatedEndpointDetails
    }

    return Server(
        server.name,
        server.iso,
        server.latency,
        updatedEndpointsPerProtocol,
        server.key,
        server.latitude,
        server.longitude,
        server.isGeo,
        server.isOffline,
        server.isAllowsPF,
        dip.dipToken,
        dip.ip,
    )
}