package com.kape.regions.di

import com.kape.regions.RegionPrefs
import com.kape.regions.data.RegionDataSourceImpl
import com.kape.regions.data.RegionRepository
import com.kape.regions.domain.GetRegionsUseCase
import com.kape.regions.domain.RegionDataSource
import com.kape.regions.domain.UpdateLatencyUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

fun regionsModule(appModule: Module) = module {
    includes(appModule, localRegionsModule)
}

val localRegionsModule = module {
    single { RegionPrefs(get()) }
    single<RegionDataSource> { RegionDataSourceImpl(get()) }
    single { RegionRepository(get(), get()) }
    single { GetRegionsUseCase(get(), get()) }
    single { UpdateLatencyUseCase(get()) }
}