package com.kape.signup.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [StoreModule::class])
@ComponentScan("com.kape.signup")
class SignupModule