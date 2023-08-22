package com.kape.regionselection.data

import com.kape.regionselection.domain.RegionDataSource
import com.kape.regionselection.utils.adaptServers
import com.kape.regionselection.utils.adaptServersInfo
import com.kape.utils.server.Server
import com.kape.utils.server.ServerInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RegionRepository(private val source: RegionDataSource) {

    private var serverMap: Map<String, Server> = hashMapOf()
    private var serverInfo: ServerInfo = ServerInfo()

    fun fetchRegions(locale: String): Flow<List<Server>> = flow {
        source.fetchRegions(locale).collect {
            if (it == null) {
                emit(emptyList())
            } else {
                serverMap = adaptServers(it)
                serverInfo = adaptServersInfo(it)
                emit(serverMap.values.toList())
            }
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