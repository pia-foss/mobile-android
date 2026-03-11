package com.kape.connection.di

import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.vpnconnect.domain.ClientStateDataSource
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun connectionModule(appModule: Module) = module {
    includes(appModule, localConnectionModule)
}

private val localConnectionModule = module {
    viewModel {
        ConnectionViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}