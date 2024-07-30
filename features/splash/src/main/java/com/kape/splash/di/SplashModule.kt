package com.kape.splash.di

import com.kape.splash.ui.vm.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val splashModule = module {
    viewModel { SplashViewModel(get(), get(), get(), get(), get(named("update-url"))) }
}