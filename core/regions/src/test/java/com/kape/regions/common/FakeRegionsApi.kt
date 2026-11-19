package com.kape.regions.common

import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse

internal class FakeRegionsApi : RegionsAPI {
    var vpnRegions: VpnRegionsResponse? = null
    var shadowsocksRegions: List<ShadowsocksRegionsResponse> = emptyList()
    var pings: List<RegionLowerLatencyInformation> = emptyList()
    var error: Error? = null
    val requestedLocales = mutableListOf<String>()

    override fun fetchVpnRegions(
        locale: String,
        callback: (response: VpnRegionsResponse?, error: Error?) -> Unit,
    ) {
        requestedLocales += locale
        callback(vpnRegions, error)
    }

    override fun fetchShadowsocksRegions(
        locale: String,
        callback: (response: List<ShadowsocksRegionsResponse>, error: Error?) -> Unit,
    ) {
        requestedLocales += locale
        callback(shadowsocksRegions, error)
    }

    override fun pingRequests(callback: (response: List<RegionLowerLatencyInformation>, error: Error?) -> Unit) {
        callback(pings, error)
    }
}