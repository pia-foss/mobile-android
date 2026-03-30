package com.kape.vpnregions.di

import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.utils.RegionListProvider
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.kape.vpnregions", "com.kape.data")
class VpnServersModule {

    @Singleton
    fun provideRegionListProvider(
        regionRepository: VpnRegionRepository,
        readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
    ): RegionListProvider = RegionListProvider(regionRepository, readVpnRegionsDetailsUseCase)
}