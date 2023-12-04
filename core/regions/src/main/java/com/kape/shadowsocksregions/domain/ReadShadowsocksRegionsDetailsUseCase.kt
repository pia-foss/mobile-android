package com.kape.shadowsocksregions.domain

import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.kape.shadowsocksregions.utils.adaptShadowsocksServers
import com.kape.utils.shadowsocksserver.ShadowsocksServer

class ReadShadowsocksRegionsDetailsUseCase(
    private val regionInputStream: RegionInputStream,
    private val regionSerialization: RegionSerialization,
) {

    companion object {
        private const val SHADOWSOCKS_REGIONS_FILENAME = "shadowsocks-regions.json"
    }

    fun readShadowsocksRegionsDetailsFromAssetsFolder(): List<ShadowsocksServer> {
        val servers = regionInputStream.readAssetsFile(filename = SHADOWSOCKS_REGIONS_FILENAME)
        val shadowSocksRegionsResponse = regionSerialization.decodeShadowsocksRegionsFromString(
            servers.split("\n\n").first(),
        )
        return adaptShadowsocksServers(shadowSocksRegionsResponse)
    }
}