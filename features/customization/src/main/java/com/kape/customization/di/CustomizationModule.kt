package com.kape.customization.di

import com.kape.contracts.Router
import com.kape.customization.CustomizationViewModel
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class CustomizationModule {
    @KoinViewModel
    fun provideCustomizationViewModel(
        prefs: CustomizationPrefs,
        settingsPrefs: SettingsPrefs,
        router: Router,
    ): CustomizationViewModel = CustomizationViewModel(prefs, settingsPrefs, router)
}