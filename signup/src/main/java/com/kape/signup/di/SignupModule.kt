package com.kape.signup.di

import com.kape.signup.data.EmailDataSourceImpl
import com.kape.signup.data.SignupDataSourceImpl
import com.kape.signup.domain.EmailDataSource
import com.kape.signup.domain.SetEmailUseCase
import com.kape.signup.domain.SignupDataSource
import com.kape.signup.domain.SignupUseCase
import com.kape.signup.ui.vm.SubscribeViewModel
import com.kape.signup.utils.PriceFormatter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val signupModule = module {
    single<SignupDataSource> { SignupDataSourceImpl() }
    single<EmailDataSource> { EmailDataSourceImpl() }
    single { SignupUseCase(get(), get()) }
    single { SetEmailUseCase(get()) }
    single { PriceFormatter(get()) }
    viewModel { SubscribeViewModel(get(), get()) }
}