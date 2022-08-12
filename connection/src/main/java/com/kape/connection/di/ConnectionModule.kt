package com.kape.connection.di

import com.kape.connection.ui.vm.ConnectionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val connectionModule = module {
    viewModel { ConnectionViewModel(get()) }
}