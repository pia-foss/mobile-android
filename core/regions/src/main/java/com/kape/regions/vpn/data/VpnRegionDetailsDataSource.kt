package com.kape.regions.vpn.data

import com.kape.regions.common.data.RegionDetails
import com.kape.regions.common.data.RegionDetailsDataSource
import com.kape.regions.vpn.domain.VpnRegionRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Singleton

@Singleton(binds = [RegionDetailsDataSource::class])
internal class VpnRegionDetailsDataSource(
    private val repository: VpnRegionRepository,
) : RegionDetailsDataSource {
    override suspend fun getRegionDetails(): Map<String, RegionDetails> =
        repository
            .observeServers()
            .first()
            .associate { it.key.lowercase() to RegionDetails(name = it.name, iso = it.iso) }
}