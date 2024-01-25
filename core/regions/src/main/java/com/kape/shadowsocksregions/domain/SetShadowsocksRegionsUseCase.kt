package com.kape.shadowsocksregions.domain

import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.utils.shadowsocksserver.ShadowsocksServer

class SetShadowsocksRegionsUseCase(
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
) {

    fun setSelectShadowsocksServer(shadowsocksServer: ShadowsocksServer) =
        shadowsocksRegionPrefs.setSelectShadowsocksServer(shadowsocksServer)

    fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) =
        shadowsocksRegionPrefs.setShadowsocksServers(shadowsocksServers)
}