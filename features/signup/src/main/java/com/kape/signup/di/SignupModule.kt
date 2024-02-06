package com.kape.signup.di

import com.kape.signup.ConsentPrefs
import com.kape.signup.data.EmailDataSourceImpl
import com.kape.signup.data.SignupDataSourceImpl
import com.kape.signup.domain.ConsentUseCase
import com.kape.signup.domain.EmailDataSource
import com.kape.signup.domain.SetEmailUseCase
import com.kape.signup.domain.SignupDataSource
import com.kape.signup.domain.SignupUseCase
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.PriceFormatter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun signupModule(appModule: Module) = module {
    includes(appModule, localSignupModule)
}

private val localSignupModule = module {
    single<SignupDataSource> { SignupDataSourceImpl(get()) }
    single<EmailDataSource> { EmailDataSourceImpl(get()) }
    single { SignupUseCase(get(), get(), get(), get()) }
    single { SetEmailUseCase(get()) }
    single { PriceFormatter(get()) }
    single { ConsentPrefs(get()) }
    single { ConsentUseCase(get()) }
    viewModel { SignupViewModel(get(), get(), get(), get(), get(), get(), get()) }
}