package com.kape.shadowsocksregions.domain

import com.kape.data.DI
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton
class GetShadowsocksRegionsUseCase(
    private val shadowsocksRegionRepository: ShadowsocksRegionRepository,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val readShadowsocksRegionsDetailsUseCase: ReadShadowsocksRegionsDetailsUseCase,
    private val setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase,
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) {
    suspend fun fetchShadowsocksServers(locale: String): List<ShadowsocksServer> =
        shadowsocksRegionRepository.fetchShadowsocksServers(locale)

    fun getSelectedShadowsocksServer(): Flow<ShadowsocksServer> =
        getShadowsocksServers().combine(shadowsocksRegionPrefs.selectedShadowsocksServer) { servers, selectedServer ->
            servers.firstOrNull { it.host == selectedServer?.host }
                ?: run {
                    val defaultShadowsocksServer = servers.first()
                    ioScope.launch {
                        setShadowsocksRegionsUseCase.setSelectShadowsocksServer(
                            defaultShadowsocksServer,
                        )
                    }
                    defaultShadowsocksServer
                }
        }

    fun getShadowsocksServers(): Flow<List<ShadowsocksServer>> =
        shadowsocksRegionPrefs.shadowsocksServers.map { servers ->
            servers.ifEmpty {
                readShadowsocksRegionsDetailsUseCase.readShadowsocksRegionsDetailsFromAssetsFolder()
            }
        }
}