package com.kape.regionselection.di

import com.kape.regions.RegionPrefs
import com.kape.regionselection.data.RegionDataSourceImpl
import com.kape.regionselection.data.RegionRepository
import com.kape.regionselection.domain.GetRegionsUseCase
import com.kape.regionselection.domain.RegionDataSource
import com.kape.regionselection.domain.UpdateLatencyUseCase
import com.kape.regionselection.ui.vm.RegionSelectionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun regionModule(appModule: Module) = module {
    includes(appModule, localRegionModule)
}

val localRegionModule = module {
    single { RegionPrefs(get()) }
    single<RegionDataSource> { RegionDataSourceImpl(get()) }
    single { RegionRepository(get()) }
    single { GetRegionsUseCase(get(), get()) }
    single { UpdateLatencyUseCase(get()) }
    viewModel { RegionSelectionViewModel(get(), get(), get(), get()) }
}