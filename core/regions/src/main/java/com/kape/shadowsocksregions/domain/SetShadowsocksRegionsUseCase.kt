package com.kape.shadowsocksregions.domain

import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.utils.shadowsocksserver.ShadowsocksServer

class SetShadowsocksRegionsUseCase(
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
) {

    fun setSelectShadowsocksServer(shadowsocksServerName: String) =
        shadowsocksRegionPrefs.setSelectShadowsocksServer(shadowsocksServerName)

    fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) =
        shadowsocksRegionPrefs.setShadowsocksServers(shadowsocksServers)
}