package com.kape.vpnregions.di

import com.kape.data.RegionDataSourceImpl
import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.VpnRegionDataSource
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
    single { ReadVpnRegionsDetailsUseCase(get(), get()) }
}