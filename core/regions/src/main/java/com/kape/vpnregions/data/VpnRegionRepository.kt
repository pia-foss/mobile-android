package com.kape.vpnregions.data

import com.kape.dip.DipPrefs
import com.kape.utils.server.VpnServer
import com.kape.utils.server.VpnServerInfo
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.kape.vpnregions.utils.adaptServers
import com.kape.vpnregions.utils.adaptServersInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val UPDATE_INTERVAL_MS = 30000 // (30 seconds)

class VpnRegionRepository(
    private val source: VpnRegionDataSource,
    private val dipPrefs: DipPrefs,
) {

    private var serverMap: Map<String, VpnServer> = hashMapOf()
    private var serverInfo: VpnServerInfo = VpnServerInfo()
    private var lastUpdate: Long = 0

    fun fetchRegions(locale: String): Flow<List<VpnServer>> = flow {
        if (serverMap.isEmpty() or (System.currentTimeMillis() - lastUpdate >= UPDATE_INTERVAL_MS)) {
            source.fetchRegions(locale).collect {
                lastUpdate = System.currentTimeMillis()
                if (it == null) {
                    emit(emptyList())
                } else {
                    serverMap = adaptServers(it, dipPrefs)
                    serverInfo = adaptServersInfo(it)
                    emit(serverMap.values.toList())
                }
            }
        } else {
            emit(serverMap.values.toList())
        }
    }

    fun fetchLatencies(): Flow<List<VpnServer>> = flow {
        source.pingRequests().collect {
            if (it == null) {
                emit(emptyList())
            } else {
                for (info in it) {
                    serverMap[info.region]?.latency = info.latency.toString()
                }
                emit(serverMap.values.toList())
            }
        }
    }

    fun getUdpPorts() = serverInfo.udpPorts ?: emptyList()

    fun getTcpPorts() = serverInfo.tcpPorts ?: emptyList()
}