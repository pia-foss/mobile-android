package com.kape.shadowsocksregions.domain

import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse

interface ShadowsocksRegionDataSource {

    suspend fun fetchShadowsocksRegions(locale: String): List<ShadowsocksRegionsResponse>
}