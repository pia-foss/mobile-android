package com.kape.shadowsocksregions.domain

import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetShadowsocksRegionsUseCase(
    private val shadowsocksRegionRepository: ShadowsocksRegionRepository,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val readShadowsocksRegionsDetailsUseCase: ReadShadowsocksRegionsDetailsUseCase,
) {

    fun fetchShadowsocksServers(locale: String): Flow<List<ShadowsocksServer>> = flow {
        shadowsocksRegionRepository.fetchShadowsocksServers(locale).collect {
            emit(it)
        }
    }
    fun getSelectedShadowsocksServer(): ShadowsocksServer =
        getShadowsocksServers().firstOrNull {
            it.host == shadowsocksRegionPrefs.getSelectedShadowsocksServer()?.host
        } ?: getShadowsocksServers().first()

    fun getShadowsocksServers(): List<ShadowsocksServer> =
        // If there are no servers persisted. Let's use the initial set of servers we are
        // shipping the application with while we perform a request for an updated version.
        shadowsocksRegionPrefs.getShadowsocksServers().ifEmpty {
            readShadowsocksRegionsDetailsUseCase.readShadowsocksRegionsDetailsFromAssetsFolder()
        }
}