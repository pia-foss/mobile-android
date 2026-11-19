package com.kape.regions.vpn.domain

import com.kape.data.vpnserver.VpnServer
import kotlin.random.Random

internal fun selectOptimalServer(
    servers: List<VpnServer>,
    random: Random,
): VpnServer {
    require(servers.isNotEmpty()) { "No servers to choose from" }
    val candidates = servers.filter { it.autoRegion && !it.isGeo && !it.isOffline && !it.isDedicatedIp }
    return candidates
        .filter { it.latency?.toLongOrNull() != null }
        .minByOrNull { it.latency!!.toLong() }
        ?: candidates.randomOrNull(random)
        ?: servers.first()
}