package com.kape.signup.di

import com.kape.signup.data.NoOpSignupBillingHandler
import com.kape.signup.domain.SignupBillingHandler
import com.kape.signup.utils.NO_IN_APP_SUBSCRIPTIONS
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class SignupBillingModule {
    @Singleton(binds = [SignupBillingHandler::class])
    fun provideSignupBillingHandler(): SignupBillingHandler = NoOpSignupBillingHandler(NO_IN_APP_SUBSCRIPTIONS)
}