package com.kape.shadowsocksregions.domain

import com.kape.data.DI
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import kotlinx.coroutines.CoroutineScope
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

    fun getSelectedShadowsocksServer(): ShadowsocksServer =
        getShadowsocksServers().firstOrNull {
            it.host == shadowsocksRegionPrefs.selectedShadowsocksServer.value?.host
        } ?: run {
            val defaultShadowsocksServer = getShadowsocksServers().first()
            ioScope.launch {
                setShadowsocksRegionsUseCase.setSelectShadowsocksServer(defaultShadowsocksServer)
            }
            defaultShadowsocksServer
        }

    fun getShadowsocksServers(): List<ShadowsocksServer> =
        // If there are no servers persisted. Let's use the initial set of servers we are
        // shipping the application with while we perform a request for an updated version.
        shadowsocksRegionPrefs.shadowsocksServers.value.ifEmpty {
            readShadowsocksRegionsDetailsUseCase.readShadowsocksRegionsDetailsFromAssetsFolder()
        }
}