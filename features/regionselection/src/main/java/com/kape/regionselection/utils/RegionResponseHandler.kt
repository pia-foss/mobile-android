package com.kape.regionselection.utils

import com.kape.utils.server.Server
import com.kape.utils.server.ServerInfo
import com.privateinternetaccess.regions.model.RegionsResponse

private var openVpnTcpEndpoints = mutableListOf<RegionsResponse.Region.Server>()
private var openVpnUdpEndpoints = mutableListOf<RegionsResponse.Region.Server>()

fun adaptServers(regionsResponse: RegionsResponse): Map<String, Server> {
    val servers = mutableMapOf<String, Server>()
    for (region in regionsResponse.regions) {
        val wireguardEndpoints =
            region.servers.filter { it.services.any { service -> service.service == Server.ServerGroup.WIREGUARD.toString() } }
        openVpnTcpEndpoints.addAll(region.servers.filter { it.services.any { service -> service.service == Server.ServerGroup.OPENVPN_TCP.toString() } })
        openVpnUdpEndpoints.addAll(region.servers.filter { it.services.any { service -> service.service == Server.ServerGroup.OPENVPN_UDP.toString() } })

        val metaEndpoints =
            region.servers.filter { it.services.any { service -> service.service == Server.ServerGroup.META.toString() } }

        val regionEndpoints =
            mutableMapOf<Server.ServerGroup, List<Server.ServerEndpointDetails>>()

        var mappedEndpoints = mutableListOf<Server.ServerEndpointDetails>()
        for (wireguardEndpoint in wireguardEndpoints) {
            // Application does not support the user option to choose wg ports and
            // expect the format `endpoint:port`, as it is not aware of wg ports.
            mappedEndpoints.add(
                Server.ServerEndpointDetails(
                    "${wireguardEndpoint.ip}:${wireguardEndpoint.services.first().ports.first()}",
                    wireguardEndpoint.cn,
                ),
            )
        }
        regionEndpoints[Server.ServerGroup.WIREGUARD] = mappedEndpoints

        mappedEndpoints = mutableListOf()
        for (ovpnTcpEndpoint in openVpnTcpEndpoints) {
            mappedEndpoints.add(
                Server.ServerEndpointDetails(
                    ovpnTcpEndpoint.ip,
                    ovpnTcpEndpoint.cn,
                ),
            )
        }
        regionEndpoints[Server.ServerGroup.OPENVPN_TCP] = mappedEndpoints

        mappedEndpoints = mutableListOf()
        for (ovpnUdpEndpoint in openVpnUdpEndpoints) {
            mappedEndpoints.add(
                Server.ServerEndpointDetails(
                    ovpnUdpEndpoint.ip,
                    ovpnUdpEndpoint.cn,
                ),
            )
        }
        regionEndpoints[Server.ServerGroup.OPENVPN_UDP] = mappedEndpoints

        mappedEndpoints = mutableListOf()
        for (metaEndpoint in metaEndpoints) {
            mappedEndpoints.add(
                Server.ServerEndpointDetails(
                    metaEndpoint.ip,
                    metaEndpoint.cn,
                ),
            )
        }
        regionEndpoints[Server.ServerGroup.META] = mappedEndpoints

        servers[region.id] = Server(
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
            null,
        )
    }
    return servers
}

fun adaptServersInfo(regionsResponse: RegionsResponse): ServerInfo {
    val autoRegions = mutableListOf<String>()
    regionsResponse.regions.filter { it.autoRegion }.forEach { region ->
        autoRegions.add(region.id)
    }
    val openVpnTcp = mutableListOf<Int>()
    openVpnTcpEndpoints.forEach {
        it.services.forEach { service ->
            openVpnTcp.addAll(service.ports)
        }
    }
    val openVpnUdp = mutableListOf<Int>()
    openVpnUdpEndpoints.forEach {
        it.services.forEach { service ->
            openVpnUdp.addAll(service.ports)
        }
    }
    return ServerInfo(autoRegions, openVpnUdp, openVpnTcp)
}