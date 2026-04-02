package com.kape.shadowsocksregions.data

import com.kape.shadowsocksregions.domain.ShadowsocksRegionDataSource
import com.kape.shadowsocksregions.utils.adaptShadowsocksServers
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import org.koin.core.annotation.Singleton

@Singleton
class ShadowsocksRegionRepository(
    private val shadowsocksRegionDataSource: ShadowsocksRegionDataSource,
) {

    private var response: List<ShadowsocksServer> = emptyList()
    private var lastUpdate: Long = 0

    companion object {
        private const val UPDATE_INTERVAL_MS = 30000 // (30 seconds)
    }

    suspend fun fetchShadowsocksServers(locale: String): List<ShadowsocksServer> {
        return if (response.isEmpty() || System.currentTimeMillis() - lastUpdate >= UPDATE_INTERVAL_MS) {
            val regions = shadowsocksRegionDataSource.fetchShadowsocksRegions(locale)
            lastUpdate = System.currentTimeMillis()
            if (regions.isEmpty()) {
                emptyList()
            } else {
                response = adaptShadowsocksServers(regions)
                response
            }
        } else {
            response
        }
    }
}