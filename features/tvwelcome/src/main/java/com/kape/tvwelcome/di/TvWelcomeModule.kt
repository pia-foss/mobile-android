package com.kape.tvwelcome.di

import com.kape.tvwelcome.ui.vm.TvWelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val tvWelcomeModule = module {
    viewModel { TvWelcomeViewModel(get(), get()) }
}