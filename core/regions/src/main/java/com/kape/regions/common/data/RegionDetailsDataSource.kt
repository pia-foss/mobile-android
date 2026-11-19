package com.kape.regions.common.data

internal data class RegionDetails(
    val name: String,
    val iso: String,
)

internal interface RegionDetailsDataSource {
    suspend fun getRegionDetails(): Map<String, RegionDetails>
}