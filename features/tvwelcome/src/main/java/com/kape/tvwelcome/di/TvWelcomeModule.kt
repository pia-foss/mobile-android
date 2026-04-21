package com.kape.tvwelcome.di

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.contracts.Router
import com.kape.tvwelcome.ui.vm.TvWelcomeViewModel
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class TvWelcomeModule {
    @KoinViewModel
    fun provideTvWelcomeViewModel(
        router: Router,
        buildConfigProvider: BuildConfigProvider,
    ): TvWelcomeViewModel = TvWelcomeViewModel(router, buildConfigProvider)
}