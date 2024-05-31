package com.kape.shadowsocksregions.data

import com.kape.shadowsocksregions.domain.ShadowsocksRegionDataSource
import com.kape.shadowsocksregions.utils.adaptShadowsocksServers
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ShadowsocksRegionRepository(
    private val shadowsocksRegionDataSource: ShadowsocksRegionDataSource,
) {

    private var response: List<ShadowsocksServer> = emptyList()
    private var lastUpdate: Long = 0

    companion object {
        private const val UPDATE_INTERVAL_MS = 30000 // (30 seconds)
    }

    fun fetchShadowsocksServers(locale: String): Flow<List<ShadowsocksServer>> = flow {
        if (response.isEmpty() or (System.currentTimeMillis() - lastUpdate >= UPDATE_INTERVAL_MS)) {
            shadowsocksRegionDataSource.fetchShadowsocksRegions(locale).collect {
                lastUpdate = System.currentTimeMillis()
                if (it.isEmpty()) {
                    emit(emptyList())
                } else {
                    response = adaptShadowsocksServers(it)
                }
            }
        } else {
            emit(response)
        }
    }
}