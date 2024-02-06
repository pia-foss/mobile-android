package com.kape.appbar.di

import com.kape.appbar.viewmodel.AppBarViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun appBarModule(appModule: Module) = module {
    includes(appModule, localAppBarModule)
}

private val localAppBarModule = module {
    viewModel { AppBarViewModel(get(), get()) }
}