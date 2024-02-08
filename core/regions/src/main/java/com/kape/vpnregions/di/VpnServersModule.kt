package com.kape.vpnregions.di

import com.kape.data.RegionDataSourceImpl
import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.GetVpnRegionsUseCase
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.SetVpnRegionsUseCase
import com.kape.vpnregions.domain.UpdateLatencyUseCase
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.kape.vpnregions.utils.RegionListProvider
import org.koin.core.module.Module
import org.koin.dsl.module

fun vpnRegionsModule(appModule: Module) = module {
    includes(appModule, localVpnRegionsModule)
}

val localVpnRegionsModule = module {
    single { VpnRegionPrefs(get()) }
    single<VpnRegionDataSource> { RegionDataSourceImpl(get()) }
    single { VpnRegionRepository(get(), get()) }
    single { RegionInputStream(get()) }
    single { RegionSerialization() }
    single { GetVpnRegionsUseCase(get()) }
    single { ReadVpnRegionsDetailsUseCase(get(), get()) }
    single { SetVpnRegionsUseCase(get()) }
    single { UpdateLatencyUseCase(get()) }
    single { RegionListProvider(get(), get(), get()) }
}