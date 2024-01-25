package com.kape.obfuscationregionselection.di

import com.kape.obfuscationregionselection.ui.vm.ShadowsocksRegionSelectionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun shadowsocksSelectionModule(appModule: Module) = module {
    includes(appModule, localRegionSelectionModule)
}

val localRegionSelectionModule = module {
    viewModel { ShadowsocksRegionSelectionViewModel(get(), get(), get()) }
}