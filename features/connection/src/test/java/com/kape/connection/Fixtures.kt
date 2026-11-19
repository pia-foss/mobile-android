package com.kape.connection

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerEndpointDetails
import com.kape.data.vpnserver.VpnServer.ServerGroup

internal fun vpnServer(
    key: String,
    dedicatedIp: String? = null,
    hasEndpoints: Boolean = true,
) = VpnServer(
    name = key.replaceFirstChar { it.uppercase() },
    iso = key.take(2).uppercase(),
    dns = "",
    latency = "50",
    endpoints = if (hasEndpoints) mapOf(ServerGroup.WIREGUARD to listOf(ServerEndpointDetails(ip = "10.0.0.1", cn = key))) else emptyMap(),
    key = key,
    latitude = null,
    longitude = null,
    isGeo = false,
    isOffline = false,
    allowsPortForwarding = true,
    autoRegion = true,
    dipToken = null,
    dedicatedIp = dedicatedIp,
)