package com.kape.vpnregions.domain

import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.VpnRegionsResponse

interface VpnRegionDataSource {

    suspend fun fetchVpnRegions(locale: String): VpnRegionsResponse?

    suspend fun pingRequests(): List<RegionLowerLatencyInformation>?
}