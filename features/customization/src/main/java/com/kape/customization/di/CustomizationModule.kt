package com.kape.customization.di

import com.kape.customization.CustomizationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun customizationModule(appModule: Module) = module {
    includes(appModule, localCustomizationModule)
}

private val localCustomizationModule = module {
    viewModel { CustomizationViewModel(get(), get()) }
}