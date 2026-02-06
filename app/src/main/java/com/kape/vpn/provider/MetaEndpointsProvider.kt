package com.kape.vpn.provider

import com.kape.connection.ConnectionPrefs
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.utils.RegionListProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

private const val MAX_META_ENDPOINTS = 2

class MetaEndpointsProvider : KoinComponent {

    private val connectionPrefs: ConnectionPrefs by inject()
    private val regionsListProvider: RegionListProvider by inject()

    fun metaEndpoints(): List<GenericEndpoint> {
        val endpoints = mutableListOf<GenericEndpoint>()

        val selectedRegion = connectionPrefs.getSelectedVpnServer()

        // Get the list of known regions sorted by latency.
        val sortedLatencyRegions =
            regionsListProvider.servers.value.sortedBy { it.latency?.toInt() }

        // Filter out invalid latencies. e.g. nil, zero, etc.
        val regionsWithValidLatency = sortedLatencyRegions.filterNot {
            it.latency.isNullOrEmpty() || it.latency == "0"
        }.toMutableList()

        // If there were no regions with valid latencies yet or less than what we need to. Pick random.
        if (regionsWithValidLatency.isEmpty() || regionsWithValidLatency.size < MAX_META_ENDPOINTS) {
            for (i in 2..MAX_META_ENDPOINTS) {
                if (sortedLatencyRegions.isNotEmpty()) {
                    val region = sortedLatencyRegions[Random.nextInt(0, sortedLatencyRegions.size)]
                    regionsWithValidLatency.add(region)
                }
            }
        }

        // Add the selected region.
        selectedRegion?.let {
            regionsWithValidLatency.add(0, it)
        }

        // Add the MAX_META_ENDPOINTS regions with the lowest latencies.
        for (region in regionsWithValidLatency.subList(0, MAX_META_ENDPOINTS)) {
            // We want different meta regions. Provide just one meta per region region.
            val selectedEndpoint = region.endpoints[VpnServer.ServerGroup.META]?.firstOrNull()
            if (selectedEndpoint != null) {
                endpoints.add(
                    GenericEndpoint(
                        selectedEndpoint.ip,
                        isProxy = true,
                        usePinnedCertificate = true,
                        certificateCommonName = selectedEndpoint.cn,
                    ),
                )
            }
        }
        return endpoints
    }
}