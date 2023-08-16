package com.kape.connection.di

import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.connection.data.ConnectionDataSourceImpl
import com.kape.connection.domain.ConnectionDataSource
import com.kape.connection.domain.ConnectionUseCase
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.connection.utils.ConnectionPrefs
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val connectionModule = module {
    single { ConnectionPrefs(get()) }
    single<ConnectionDataSource> { ConnectionDataSourceImpl() }
    viewModel { AppBarViewModel() }
    single { ConnectionUseCase(get()) }
    viewModel { ConnectionViewModel(get(), get(), get(), get(), get()) }
}