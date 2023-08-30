package com.kape.connection.di

import com.kape.connection.ConnectionPrefs
import com.kape.connection.data.ClientStateDataSourceImpl
import com.kape.connection.domain.ClientStateDataSource
import com.kape.connection.ui.vm.ConnectionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val connectionModule = module {
    single { ConnectionPrefs(get()) }
    single<ClientStateDataSource> { ClientStateDataSourceImpl(get(), get()) }
    viewModel { ConnectionViewModel(get(), get(), get(), get(), get(), get()) }
}