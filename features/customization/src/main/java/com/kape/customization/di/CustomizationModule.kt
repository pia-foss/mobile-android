package com.kape.customization.di

import com.kape.contracts.Router
import com.kape.customization.CustomizationViewModel
import com.kape.localprefs.prefs.CustomizationPrefs
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class CustomizationModule {

    @KoinViewModel
    fun provideCustomizationViewModel(
        prefs: CustomizationPrefs,
        router: Router,
    ): CustomizationViewModel = CustomizationViewModel(prefs, router)
}