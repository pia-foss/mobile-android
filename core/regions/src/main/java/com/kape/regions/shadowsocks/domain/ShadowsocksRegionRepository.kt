package com.kape.regions.shadowsocks.domain

import com.kape.data.shadowsocksserver.ShadowsocksServer
import kotlinx.coroutines.flow.Flow

interface ShadowsocksRegionRepository {
    fun observeServers(): Flow<List<ShadowsocksServer>>

    suspend fun refreshServers(locale: String)

    fun observeSelectedServer(): Flow<ShadowsocksServer?>

    suspend fun selectServer(server: ShadowsocksServer)
}