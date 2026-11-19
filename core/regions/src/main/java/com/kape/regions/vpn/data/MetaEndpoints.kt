package com.kape.regions.vpn.data

import com.kape.contracts.MetaEndpointsProvider
import com.kape.data.GenericEndpoint
import com.kape.data.vpnserver.VpnServer
import com.kape.regions.vpn.data.local.SelectedVpnServerDataSource
import org.koin.core.annotation.Singleton
import kotlin.random.Random

private const val MAX_META_ENDPOINTS = 2

internal fun selectMetaEndpoints(
    servers: List<VpnServer>,
    selected: VpnServer?,
    random: Random,
): List<GenericEndpoint> {
    val byLatency =
        servers
            .filter { (it.latency?.toLongOrNull() ?: 0) > 0 }
            .sortedBy { it.latency!!.toLong() }
    val candidates = listOfNotNull(selected) + byLatency + servers.shuffled(random)
    return candidates
        .distinctBy { it.key }
        .mapNotNull { it.endpoints[VpnServer.ServerGroup.META]?.firstOrNull() }
        .take(MAX_META_ENDPOINTS)
        .map {
            GenericEndpoint(
                endpoint = it.ip,
                isProxy = true,
                usePinnedCertificate = true,
                certificateCommonName = it.cn,
            )
        }
}

@Singleton(binds = [MetaEndpointsProvider::class])
internal class VpnMetaEndpointsProvider(
    private val store: VpnRegionStore,
    private val selectedServer: SelectedVpnServerDataSource,
) : MetaEndpointsProvider {
    override fun metaEndpoints(): List<GenericEndpoint> =
        selectMetaEndpoints(store.currentServers(), selectedServer.selectedServer(), Random.Default)
}