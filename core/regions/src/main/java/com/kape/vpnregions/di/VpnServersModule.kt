package com.kape.vpnregions.di

import com.kape.data.DI
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregions.utils.ShadowsocksListProvider
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
    ): RegionListProvider = RegionListProvider(regionRepository, readVpnRegionsDetailsUseCase, ioDispatcher)

    @Singleton
    fun provideShadowsocksListProvider(
        getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): ShadowsocksListProvider = ShadowsocksListProvider(getShadowsocksRegionsUseCase, ioDispatcher)
}