package com.kape.region_selection.domain

import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.RegionsResponse
import kotlinx.coroutines.flow.Flow

interface RegionDataSource {

    fun fetchRegions(locale: String): Flow<RegionsResponse?>

    fun pingRequests(): Flow<List<RegionLowerLatencyInformation>?>

}