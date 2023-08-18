package com.kape.appbar.di

import com.kape.appbar.viewmodel.AppBarViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appBarModule = module {
    viewModel { AppBarViewModel(get()) }
}