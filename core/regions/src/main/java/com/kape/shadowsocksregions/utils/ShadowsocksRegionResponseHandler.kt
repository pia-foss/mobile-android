package com.kape.shadowsocksregions.utils

import com.kape.utils.shadowsocksserver.ShadowsocksServer
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse

fun adaptShadowsocksServers(
    shadowsocksRegionsResponse: List<ShadowsocksRegionsResponse>,
): List<ShadowsocksServer> {
    return shadowsocksRegionsResponse.map {
        ShadowsocksServer(
            iso = it.iso,
            region = it.region,
            host = it.host,
            port = it.port,
            key = it.key,
            cipher = it.cipher,
        )
    }
}