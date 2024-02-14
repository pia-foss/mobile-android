package com.kape.shadowsocksregions.di

import com.kape.data.RegionDataSourceImpl
import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.data.ShadowsocksRegionRepository
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import com.kape.shadowsocksregions.domain.ReadShadowsocksRegionsDetailsUseCase
import com.kape.shadowsocksregions.domain.SetShadowsocksRegionsUseCase
import com.kape.shadowsocksregions.domain.ShadowsocksRegionDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

fun shadowsocksRegionsModule(appModule: Module) = module {
    includes(appModule, localShadowsocksRegionsModule)
}

val localShadowsocksRegionsModule = module {
    single { RegionSerialization() }
    single { RegionInputStream(get()) }
    single { ShadowsocksRegionPrefs(get()) }
    single<ShadowsocksRegionDataSource> { RegionDataSourceImpl(get()) }
    single { ShadowsocksRegionRepository(get()) }
    single { GetShadowsocksRegionsUseCase(get(), get(), get()) }
    single { ReadShadowsocksRegionsDetailsUseCase(get(), get(), get()) }
    single { SetShadowsocksRegionsUseCase(get()) }
}