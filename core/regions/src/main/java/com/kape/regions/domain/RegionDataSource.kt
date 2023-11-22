package com.kape.regions.domain

import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import kotlinx.coroutines.flow.Flow

interface RegionDataSource {

    fun fetchRegions(locale: String): Flow<VpnRegionsResponse?>

    fun pingRequests(): Flow<List<RegionLowerLatencyInformation>?>
}