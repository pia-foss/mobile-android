package com.kape.shadowsocksregions.domain

import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetShadowsocksRegionsUseCase(
    private val shadowsocksRegionRepository: ShadowsocksRegionRepository,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
) {

    fun fetchShadowsocksServers(locale: String): Flow<List<ShadowsocksServer>> = flow {
        shadowsocksRegionRepository.fetchShadowsocksServers(locale).collect {
            emit(it)
        }
    }
    fun getSelectedShadowsocksServer(): ShadowsocksServer? =
        shadowsocksRegionPrefs.getSelectedShadowsocksServer()

    fun getShadowsocksServers() =
        shadowsocksRegionPrefs.getShadowsocksServers()
}