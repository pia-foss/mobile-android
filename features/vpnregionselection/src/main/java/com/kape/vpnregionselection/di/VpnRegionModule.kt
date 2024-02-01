package com.kape.vpnregionselection.di

import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun regionSelectionModule(appModule: Module) = module {
    includes(appModule, localRegionSelectionModule)
}

val localRegionSelectionModule = module {
    viewModel { VpnRegionSelectionViewModel(get(), get(), get(), get(), get(), get()) }
}