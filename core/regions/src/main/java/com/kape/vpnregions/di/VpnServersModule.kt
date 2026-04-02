package com.kape.vpnregions.di

import com.kape.utils.DI
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.kape.vpnregions", "com.kape.data")
class VpnServersModule {

    @Singleton
    fun provideRegionListProvider(
        regionRepository: VpnRegionRepository,
        readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): RegionListProvider =
        RegionListProvider(regionRepository, readVpnRegionsDetailsUseCase, ioDispatcher)
}