package com.kape.signup.di

import com.kape.signup.SignupDataSourceImpl
import com.kape.signup.data.EmailDataSourceImpl
import com.kape.signup.domain.EmailDataSource
import com.kape.signup.domain.SignupDataSource
import org.koin.dsl.module

val signupModule = module {
    single<SignupDataSource> { SignupDataSourceImpl() }
    single<EmailDataSource> { EmailDataSourceImpl() }
}