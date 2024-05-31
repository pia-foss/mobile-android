package com.kape.shadowsocksregions.domain

import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import kotlinx.coroutines.flow.Flow

interface ShadowsocksRegionDataSource {

    fun fetchShadowsocksRegions(locale: String): Flow<List<ShadowsocksRegionsResponse>>
}