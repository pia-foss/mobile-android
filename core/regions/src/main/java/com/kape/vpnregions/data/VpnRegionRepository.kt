package com.kape.vpnregions.data

import com.kape.dip.DipPrefs
import com.kape.utils.vpnserver.VpnServer
import com.kape.utils.vpnserver.VpnServerInfo
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.kape.vpnregions.utils.adaptServersInfo
import com.kape.vpnregions.utils.adaptVpnServers
import com.kape.vpnregions.utils.getServerForDip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VpnRegionRepository(
    private val source: VpnRegionDataSource,
    private val dipPrefs: DipPrefs,
) {

    private var serverMap: Map<String, VpnServer> = hashMapOf()
    private var serverInfo: VpnServerInfo = VpnServerInfo()
    private var lastUpdate: Long = 0

    companion object {
        private const val UPDATE_INTERVAL_MS = 30000 // (30 seconds)
    }

    fun fetchVpnRegions(locale: String): Flow<List<VpnServer>> = flow {
        var serverList = mutableListOf<VpnServer>()
        if (serverMap.isEmpty() or (System.currentTimeMillis() - lastUpdate >= UPDATE_INTERVAL_MS)) {
            source.fetchVpnRegions(locale).collect {
                lastUpdate = System.currentTimeMillis()
                if (it == null) {
                    serverList.clear()
                } else {
                    serverMap = adaptVpnServers(it)
                    serverInfo = adaptServersInfo(it)
                    serverList.addAll(serverMap.values.toList())
                }
            }
        } else {
            serverList.addAll(serverMap.values.toList())
        }

        for (dip in dipPrefs.getDedicatedIps()) {
            serverList.firstOrNull { it.iso.lowercase() == dip.id }?.let {
                serverList.add(getServerForDip(it, dip))
            }
        }
        emit(serverList)
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