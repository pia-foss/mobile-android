package com.kape.vpnregions.data

import com.kape.dip.DipPrefs
import com.kape.utils.vpnserver.VpnServer
import com.kape.utils.vpnserver.VpnServerInfo
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.kape.vpnregions.utils.adaptServersInfo
import com.kape.vpnregions.utils.adaptVpnServers
import com.kape.vpnregions.utils.getServerForDip
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class VpnRegionRepository(
    private val source: VpnRegionDataSource,
    private val dipPrefs: DipPrefs,
) {

    private var serverMap: Map<String, VpnServer> = hashMapOf()
    private var serverInfo: VpnServerInfo = VpnServerInfo()
    private var latencyInfo: List<RegionLowerLatencyInformation> = emptyList()
    private var lastUpdate: Long = 0

    companion object {
        private const val UPDATE_INTERVAL_MS = 30000 // (30 seconds)
    }

    fun fetchVpnRegions(locale: String): Flow<List<VpnServer>> = flow {
        val serverList = mutableListOf<VpnServer>()
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
                emit(addDipToServerList(serverList))
            }
        } else {
            serverList.addAll(serverMap.values.toList())
            emit(addDipToServerList(serverList))
        }
    }

    fun fetchLatencies(useCachedLatencies: Boolean): Flow<List<VpnServer>> = flow {
        if (useCachedLatencies) {
            populateLatencies()
            emit(addDipToServerList(serverMap.values.toList()))
        } else {
            source.pingRequests().collect {
                if (it == null) {
                    emit(emptyList())
                } else {
                    latencyInfo = it
                    populateLatencies()
                    emit(addDipToServerList(serverMap.values.toList()))
                }
            }
        }
    }

    fun getUdpPorts() = serverInfo.udpPorts ?: emptyList()

    fun getTcpPorts() = serverInfo.tcpPorts ?: emptyList()

    fun getServers(): List<VpnServer> = addDipToServerList(serverMap.values.toList())

    private fun addDipToServerList(servers: List<VpnServer>): List<VpnServer> {
        val updatedList = mutableListOf<VpnServer>()
        updatedList.addAll(servers)
        for (dip in dipPrefs.getDedicatedIps()) {
            servers.firstOrNull { it.key == dip.id }?.let {
                updatedList.add(getServerForDip(it, dip))
            }
        }
        return updatedList
    }

    private fun populateLatencies() {
        for (info in latencyInfo) {
            serverMap[info.region]?.latency = info.latency.toString()
        }
    }
}