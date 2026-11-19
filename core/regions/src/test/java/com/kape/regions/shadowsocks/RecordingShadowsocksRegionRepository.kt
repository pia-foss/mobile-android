package com.kape.regions.shadowsocks

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import kotlinx.coroutines.flow.MutableStateFlow

internal class RecordingShadowsocksRegionRepository : ShadowsocksRegionRepository {
    val servers = MutableStateFlow<List<ShadowsocksServer>>(emptyList())
    val selected = MutableStateFlow<ShadowsocksServer?>(null)
    val refreshedLocales = mutableListOf<String>()

    override fun observeServers() = servers

    override suspend fun refreshServers(locale: String) {
        refreshedLocales += locale
    }

    override fun observeSelectedServer() = selected

    override suspend fun selectServer(server: ShadowsocksServer) {
        selected.value = server
    }
}