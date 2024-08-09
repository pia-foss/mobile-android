package com.kape.login.di

import com.kape.login.data.AuthenticationDataSourceImpl
import com.kape.login.domain.mobile.AuthenticationDataSource
import com.kape.login.domain.mobile.GetUserLoggedInUseCase
import com.kape.login.domain.mobile.LoginUseCase
import com.kape.login.domain.mobile.LogoutUseCase
import com.kape.login.domain.tv.LoginUsernameUseCase
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.mobile.LoginWithEmailViewModel
import com.kape.login.ui.vm.tv.LoginPasswordViewModel
import com.kape.login.ui.vm.tv.LoginUsernameViewModel
import com.kape.login.utils.TokenAuthenticationUtil
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun loginModule(appModule: Module) = module {
    includes(appModule, localLoginModule)
}

private val localLoginModule = module {
    single<AuthenticationDataSource> { AuthenticationDataSourceImpl(get()) }
    single { TokenAuthenticationUtil(get(), get()) }
    single { LoginUsernameUseCase() }
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
            get(),
            get(),
        )
    }
    single { GetUserLoggedInUseCase(get()) }
    viewModel { LoginUsernameViewModel(get(), get()) }
    viewModel { LoginPasswordViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { LoginWithEmailViewModel(get(), get()) }
}