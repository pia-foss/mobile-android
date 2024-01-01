package com.kape.login.di

import com.kape.login.data.AuthenticationDataSourceImpl
import com.kape.login.domain.AuthenticationDataSource
import com.kape.login.domain.GetUserLoggedInUseCase
import com.kape.login.domain.LoginUseCase
import com.kape.login.domain.LogoutUseCase
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.LoginWithEmailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun loginModule(appModule: Module) = module {
    includes(appModule, localLoginModule)
}

private val localLoginModule = module {
    single<AuthenticationDataSource> { AuthenticationDataSourceImpl(get()) }
    single { LoginUseCase(get()) }
    single {
        LogoutUseCase(
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
    single { GetUserLoggedInUseCase(get()) }
    viewModel { LoginViewModel(get(), get(), get(), get()) }
    viewModel { LoginWithEmailViewModel(get()) }
}