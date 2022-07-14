package com.kape.region_selection.di

import com.kape.region_selection.data.RegionDataSourceImpl
import com.kape.region_selection.data.RegionRepository
import com.kape.region_selection.domain.GetRegionsUseCase
import com.kape.region_selection.domain.RegionDataSource
import com.kape.region_selection.domain.UpdateLatencyUseCase
import com.kape.region_selection.ui.vm.RegionSelectionViewModel
import com.kape.region_selection.utils.RegionPrefs
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val regionModule = module {
    single { RegionPrefs(get()) }
    single<RegionDataSource> { RegionDataSourceImpl() }
    single { RegionRepository(get()) }
    single { GetRegionsUseCase(get()) }
    single { UpdateLatencyUseCase(get()) }
    viewModel { RegionSelectionViewModel(get(), get(), get()) }
}