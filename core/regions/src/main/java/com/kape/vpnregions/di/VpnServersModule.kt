package com.kape.vpnregions.di

import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.data.VpnRegionDataSourceImpl
import com.kape.vpnregions.data.VpnRegionInputStream
import com.kape.vpnregions.data.VpnRegionRepository
import com.kape.vpnregions.domain.GetVpnRegionsUseCase
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.SetVpnRegionsUseCase
import com.kape.vpnregions.domain.UpdateLatencyUseCase
import com.kape.vpnregions.domain.VpnRegionDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

fun regionsModule(appModule: Module) = module {
    includes(appModule, localRegionsModule)
}

val localRegionsModule = module {
    single { VpnRegionPrefs(get()) }
    single<VpnRegionDataSource> { VpnRegionDataSourceImpl(get()) }
    single { VpnRegionRepository(get(), get()) }
    single { VpnRegionInputStream(get()) }
    single { GetVpnRegionsUseCase(get(), get()) }
    single { ReadVpnRegionsDetailsUseCase(get(), get()) }
    single { SetVpnRegionsUseCase(get()) }
    single { UpdateLatencyUseCase(get()) }
}