package com.kape.shadowsocksregions.domain

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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

    fun getSelectedShadowsocksServer(): Flow<ShadowsocksServer> =
        shadowsocksRegionPrefs.selectedShadowsocksServer
            .map { selectedServer ->
                val matchedServer =
                    getShadowsocksServers().firstOrNull {
                        it.host == selectedServer?.host
                    }

                if (matchedServer != null) {
                    matchedServer to false
                } else {
                    getShadowsocksServers().first() to true
                }
            }.onEach { (server, shouldPersist) ->
                if (shouldPersist) {
                    setShadowsocksRegionsUseCase.setSelectShadowsocksServer(server)
                }
            }.map { it.first }

    fun getShadowsocksServers(): List<ShadowsocksServer> =
        // If there are no servers persisted. Let's use the initial set of servers we are
        // shipping the application with while we perform a request for an updated version.
        shadowsocksRegionPrefs.shadowsocksServers.value.ifEmpty {
            readShadowsocksRegionsDetailsUseCase.readShadowsocksRegionsDetailsFromAssetsFolder()
        }
}