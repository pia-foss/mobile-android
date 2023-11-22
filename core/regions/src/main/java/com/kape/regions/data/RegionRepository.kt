package com.kape.regions.data

import com.kape.dip.DipPrefs
import com.kape.regions.domain.RegionDataSource
import com.kape.regions.utils.adaptServers
import com.kape.regions.utils.adaptServersInfo
import com.kape.utils.server.Server
import com.kape.utils.server.ServerInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val UPDATE_INTERVAL_MS = 30000 // (30 seconds)

class RegionRepository(
    private val source: RegionDataSource,
    private val dipPrefs: DipPrefs
) {

    private var serverMap: Map<String, Server> = hashMapOf()
    private var serverInfo: ServerInfo = ServerInfo()
    private var lastUpdate: Long = 0

    fun fetchRegions(locale: String): Flow<List<Server>> = flow {
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

    fun fetchLatencies(): Flow<List<Server>> = flow {
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