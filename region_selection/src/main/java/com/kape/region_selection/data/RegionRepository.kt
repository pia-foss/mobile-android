package com.kape.region_selection.data

import com.kape.region_selection.domain.RegionDataSource
import com.kape.region_selection.server.Server
import com.kape.region_selection.server.ServerInfo
import com.kape.region_selection.utils.adaptServers
import com.kape.region_selection.utils.adaptServersInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RegionRepository(private val source: RegionDataSource) {

    private var serverMap: Map<String, Server> = hashMapOf()
    private var serverInfo: ServerInfo = ServerInfo()

    suspend fun fetchRegions(locale: String): Flow<List<Server>> = flow {
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

    suspend fun fetchLatencies(): Flow<List<Server>> = flow {
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
}