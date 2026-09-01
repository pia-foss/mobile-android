package com.kape.tvwelcome.di

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.signup.domain.SignupBillingHandler
import com.kape.tvwelcome.ui.vm.TvWelcomeViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class TvWelcomeModule {
    @KoinViewModel
    fun provideTvWelcomeViewModel(
        router: Router,
        billingHandler: SignupBillingHandler,
        buildConfigProvider: BuildConfigProvider,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
        @Named(DI.MAIN_DISPATCHER) mainDispatcher: CoroutineDispatcher,
    ): TvWelcomeViewModel = TvWelcomeViewModel(router, billingHandler, buildConfigProvider, ioDispatcher, mainDispatcher)
}