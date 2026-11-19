package com.kape.vpnregionselection

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup

internal fun vpnServer(
    key: String,
    latency: String? = null,
    groups: Set<ServerGroup> = ServerGroup.entries.toSet(),
    isGeo: Boolean = false,
    dedicatedIp: String? = null,
    autoRegion: Boolean = true,
) = VpnServer(
    name = key.replaceFirstChar { it.uppercase() },
    iso = key.take(2).uppercase(),
    dns = "",
    latency = latency,
    endpoints = groups.associateWith { listOf(ServerEndpointDetails(ip = "10.0.0.1", cn = key)) },
    key = key,
    latitude = null,
    longitude = null,
    isGeo = isGeo,
    isOffline = false,
    allowsPortForwarding = true,
    autoRegion = autoRegion,
    dipToken = null,
    dedicatedIp = dedicatedIp,
)