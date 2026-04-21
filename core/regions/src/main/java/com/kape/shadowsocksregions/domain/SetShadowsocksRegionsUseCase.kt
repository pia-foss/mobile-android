package com.kape.shadowsocksregions.domain

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import org.koin.core.annotation.Singleton

@Singleton
class SetShadowsocksRegionsUseCase(
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
) {
    fun setSelectShadowsocksServer(shadowsocksServer: ShadowsocksServer) =
        shadowsocksRegionPrefs.setSelectShadowsocksServer(shadowsocksServer)

    fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) =
        shadowsocksRegionPrefs.setShadowsocksServers(shadowsocksServers)
}