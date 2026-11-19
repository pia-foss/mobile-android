package com.kape.regions.vpn.data

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.data.vpnserver.VpnServerInfo
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import com.privateinternetaccess.regions.RegionsProtocol
import com.privateinternetaccess.regions.model.VpnRegionsResponse

private const val DEFAULT_AMNEZIA_PORT = 1338

internal fun VpnRegionsResponse.toVpnRegions(isBundled: Boolean) =
    VpnRegions(
        servers = regions.map { it.toVpnServer(groups) },
        info = toServerInfo(),
        isBundled = isBundled,
    )

private fun VpnRegionsResponse.Region.toVpnServer(groups: Map<String, List<VpnRegionsResponse.ProtocolDetails>>): VpnServer {
    fun endpoints(protocol: RegionsProtocol) = servers[protocol.protocol]

    val endpoints =
        buildMap {
            val wireGuardPort = groups[RegionsProtocol.WIREGUARD.protocol]?.firstOrNull()?.ports?.firstOrNull()
            if (wireGuardPort != null) {
                endpoints(RegionsProtocol.WIREGUARD)?.let { list ->
                    put(ServerGroup.WIREGUARD, list.map { ServerEndpointDetails("${it.ip}:$wireGuardPort", it.cn) })
                }
            }
            endpoints(RegionsProtocol.OPENVPN_TCP)?.let { list ->
                put(ServerGroup.OPENVPN_TCP, list.map { ServerEndpointDetails(it.ip, it.cn) })
            }
            endpoints(RegionsProtocol.OPENVPN_UDP)?.let { list ->
                put(ServerGroup.OPENVPN_UDP, list.map { ServerEndpointDetails(it.ip, it.cn) })
            }
            endpoints(RegionsProtocol.META)?.let { list ->
                put(ServerGroup.META, list.map { ServerEndpointDetails(it.ip, it.cn) })
            }
            endpoints(RegionsProtocol.AMNEZIA)?.let { list ->
                put(ServerGroup.AMNEZIA, list.map { ServerEndpointDetails(it.ip, it.cn, it.port) })
            }
        }

    return VpnServer(
        name = name,
        iso = country,
        dns = dns,
        latency = null,
        endpoints = endpoints,
        key = id,
        latitude = latitude,
        longitude = longitude,
        isGeo = geo,
        isOffline = offline,
        allowsPortForwarding = portForward,
        autoRegion = autoRegion,
        dipToken = null,
        dedicatedIp = null,
    )
}

private fun VpnRegionsResponse.toServerInfo() =
    VpnServerInfo(
        autoRegions = regions.filter { it.autoRegion }.map { it.id },
        udpPorts = groups[RegionsProtocol.OPENVPN_UDP.protocol]?.flatMap { it.ports }.orEmpty(),
        tcpPorts = groups[RegionsProtocol.OPENVPN_TCP.protocol]?.flatMap { it.ports }.orEmpty(),
    )

internal fun List<VpnServer>.withDedicatedIps(dedicatedIps: List<DedicatedIPInformation>): List<VpnServer> {
    val dipServers = dedicatedIps.mapNotNull { dip -> firstOrNull { it.key == dip.id }?.withDedicatedIp(dip) }
    return (this + dipServers).distinct()
}

internal fun VpnServer.withDedicatedIp(dip: DedicatedIPInformation): VpnServer {
    val ip = dip.ip
    val cn = dip.cn
    val dipEndpoints =
        endpoints.mapValues { (group, regionEndpoints) ->
            if (ip == null || cn == null) return@mapValues emptyList()
            when (group) {
                ServerGroup.META -> listOf(ServerEndpointDetails(ip, cn))
                ServerGroup.OPENVPN_TCP, ServerGroup.OPENVPN_UDP ->
                    listOf(ServerEndpointDetails(ip, cn, dip.openVpnPorts(group).firstOrNull()))
                ServerGroup.WIREGUARD ->
                    regionEndpoints
                        .firstOrNull()
                        ?.ip
                        ?.substringAfter(':', missingDelimiterValue = "")
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { port -> listOf(ServerEndpointDetails("$ip:$port", cn)) }
                        .orEmpty()
                ServerGroup.AMNEZIA ->
                    listOf(ServerEndpointDetails(ip, cn, regionEndpoints.firstOrNull()?.port ?: DEFAULT_AMNEZIA_PORT))
            }
        }
    return copy(endpoints = dipEndpoints, dipToken = dip.dipToken, dedicatedIp = ip, isDedicatedIp = !ip.isNullOrEmpty())
}

private fun DedicatedIPInformation.openVpnPorts(group: ServerGroup): List<Int> {
    val protocol =
        when (group) {
            ServerGroup.OPENVPN_UDP -> RegionsProtocol.OPENVPN_UDP
            ServerGroup.OPENVPN_TCP -> RegionsProtocol.OPENVPN_TCP
            else -> return emptyList()
        }
    return ports[protocol.protocol].orEmpty()
}