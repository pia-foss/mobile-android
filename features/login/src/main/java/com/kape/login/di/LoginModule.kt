package com.kape.login.di

import com.kape.login.data.AuthenticationDataSourceImpl
import com.kape.login.domain.AuthenticationDataSource
import com.kape.login.domain.GetUserLoggedInUseCase
import com.kape.login.domain.LoginUseCase
import com.kape.login.domain.LogoutUseCase
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.LoginWithEmailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    single<AuthenticationDataSource> { AuthenticationDataSourceImpl() }
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { GetUserLoggedInUseCase(get()) }
    viewModel { LoginViewModel(get(), get(), get(), get()) }
    viewModel { LoginWithEmailViewModel(get()) }
}