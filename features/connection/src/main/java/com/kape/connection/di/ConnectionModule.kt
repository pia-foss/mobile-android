package com.kape.connection.di

import com.kape.connection.data.ClientStateDataSourceImpl
import com.kape.connection.domain.ClientStateDataSource
import com.kape.connection.ui.vm.ConnectionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun connectionModule(appModule: Module) = module {
    includes(appModule, localConnectionModule)
}

private val localConnectionModule = module {
    single<ClientStateDataSource> { ClientStateDataSourceImpl(get(), get()) }
    viewModel { ConnectionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}