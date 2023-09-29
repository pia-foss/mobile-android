package com.kape.regionselection.di

import com.kape.regionselection.ui.vm.RegionSelectionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun regionSelectionModule(appModule: Module) = module {
    includes(appModule, localRegionSelectionModule)
}

val localRegionSelectionModule = module {
    viewModel { RegionSelectionViewModel(get(), get(), get(), get()) }
}