package com.kape.shadowsocksregions.domain

import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import org.koin.core.annotation.Singleton

@Singleton
class GetShadowsocksRegionsUseCase(
    private val shadowsocksRegionRepository: ShadowsocksRegionRepository,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val readShadowsocksRegionsDetailsUseCase: ReadShadowsocksRegionsDetailsUseCase,
    private val setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase,
) {

    suspend fun fetchShadowsocksServers(locale: String): List<ShadowsocksServer> =
        shadowsocksRegionRepository.fetchShadowsocksServers(locale)

    fun getSelectedShadowsocksServer(): ShadowsocksServer =
        getShadowsocksServers().firstOrNull {
            it.host == shadowsocksRegionPrefs.getSelectedShadowsocksServer()?.host
        } ?: run {
            val defaultShadowsocksServer = getShadowsocksServers().first()
            setShadowsocksRegionsUseCase.setSelectShadowsocksServer(defaultShadowsocksServer)
            defaultShadowsocksServer
        }

    fun getShadowsocksServers(): List<ShadowsocksServer> =
        // If there are no servers persisted. Let's use the initial set of servers we are
        // shipping the application with while we perform a request for an updated version.
        shadowsocksRegionPrefs.getShadowsocksServers().ifEmpty {
            readShadowsocksRegionsDetailsUseCase.readShadowsocksRegionsDetailsFromAssetsFolder()
        }
}