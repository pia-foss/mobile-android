package com.kape.regions.vpn.data

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.vpn.domain.VPN_PING_TIMEOUT_MS
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Singleton

/**
 * A plain class on purpose: [VpnServer.equals] compares only a few fields, so a data class would make
 * a refreshed list with changed endpoints look equal to the old one, and StateFlow would drop it.
 */
internal class VpnRegions(
    val servers: List<VpnServer>,
    val info: VpnServerInfo,
    val isBundled: Boolean,
)

@Singleton
internal class VpnRegionStore {
    val regions = MutableStateFlow<VpnRegions?>(null)

    val latencies = MutableStateFlow<Map<String, Long>?>(null)

    fun currentServers(): List<VpnServer> =
        regions.value
            ?.servers
            .orEmpty()
            .withLatencies(latencies.value)
}

internal fun List<VpnServer>.withLatencies(latencies: Map<String, Long>?): List<VpnServer> =
    if (latencies == null) {
        map { it.copy(latency = null) }
    } else {
        map { it.copy(latency = (latencies[it.key] ?: VPN_PING_TIMEOUT_MS).toString()) }
    }