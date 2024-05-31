package com.kape.about.di

import com.kape.about.vm.AboutViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun aboutModule(appModule: Module) = module {
    includes(appModule, localAboutModule)
}

private val localAboutModule = module {
    viewModel { AboutViewModel(get(), get(named("licences"))) }
}