package com.kape.data

import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import kotlinx.serialization.json.Json

class RegionSerialization {

    fun decodeVpnRegionsFromString(
        vpnRegionsResponse: String,
    ): VpnRegionsResponse =
        Json.decodeFromString(vpnRegionsResponse)

    fun decodeShadowsocksRegionsFromString(
        shadowsocksRegionsResponse: String,
    ): List<ShadowsocksRegionsResponse> =
        Json.decodeFromString(shadowsocksRegionsResponse)
}