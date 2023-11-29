package com.kape.vpnregions.domain

import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import kotlinx.coroutines.flow.Flow

interface VpnRegionDataSource {

    fun fetchRegions(locale: String): Flow<VpnRegionsResponse?>

    fun pingRequests(): Flow<List<RegionLowerLatencyInformation>?>
}