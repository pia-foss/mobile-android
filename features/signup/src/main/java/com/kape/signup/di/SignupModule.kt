package com.kape.signup.di

import com.kape.signup.data.EmailDataSourceImpl
import com.kape.signup.data.SignupDataSourceImpl
import com.kape.signup.domain.*
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.ConsentPrefs
import com.kape.signup.utils.PriceFormatter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val signupModule = module {
    single<SignupDataSource> { SignupDataSourceImpl() }
    single<EmailDataSource> { EmailDataSourceImpl() }
    single { SignupUseCase(get(), get(), get(), get()) }
    single { SetEmailUseCase(get()) }
    single { PriceFormatter(get()) }
    single { ConsentPrefs(get()) }
    single { ConsentUseCase(get()) }
    viewModel { SignupViewModel(get(), get(), get(), get(), get()) }
}