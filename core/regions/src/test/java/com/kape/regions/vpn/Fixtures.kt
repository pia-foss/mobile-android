package com.kape.regions.vpn

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import com.privateinternetaccess.regions.RegionsProtocol

internal fun vpnServer(
    key: String,
    latency: String? = null,
    autoRegion: Boolean = true,
    isGeo: Boolean = false,
    isOffline: Boolean = false,
    metaIp: String = "meta-$key",
) = VpnServer(
    name = "Name $key",
    iso = "XX",
    dns = "$key.example",
    latency = latency,
    endpoints =
        mapOf(
            ServerGroup.META to listOf(ServerEndpointDetails(metaIp, "cn-$key")),
            ServerGroup.WIREGUARD to listOf(ServerEndpointDetails("10.0.0.1:1337", "cn-$key")),
            ServerGroup.OPENVPN_UDP to listOf(ServerEndpointDetails("10.0.0.2", "cn-$key")),
        ),
    key = key,
    latitude = null,
    longitude = null,
    isGeo = isGeo,
    isOffline = isOffline,
    allowsPortForwarding = false,
    autoRegion = autoRegion,
    dipToken = null,
    dedicatedIp = null,
)

internal fun dedicatedIp(
    regionKey: String,
    ip: String = "203.0.113.7",
) = DedicatedIPInformationResponse.DedicatedIPInformation(
    regionKey,
    ip,
    "dip-cn",
    emptyList(),
    null,
    "dip-token",
    DedicatedIPInformationResponse.Status.active,
    false,
    mapOf(RegionsProtocol.OPENVPN_UDP.protocol to listOf(8080)),
)