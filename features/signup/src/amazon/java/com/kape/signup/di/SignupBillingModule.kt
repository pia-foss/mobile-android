package com.kape.signup.di

import com.kape.signup.data.NoOpSignupBillingHandler
import com.kape.signup.data.NoOpSignupHandlerImpl
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.domain.SignupHandler
import com.kape.signup.utils.AMAZON_LOGIN
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class SignupBillingModule {
    @Singleton(binds = [SignupBillingHandler::class])
    fun provideSignupBillingHandler(): SignupBillingHandler = NoOpSignupBillingHandler(AMAZON_LOGIN)

    @Singleton(binds = [SignupHandler::class])
    fun provideSignupHandler(): SignupHandler = NoOpSignupHandlerImpl()
}