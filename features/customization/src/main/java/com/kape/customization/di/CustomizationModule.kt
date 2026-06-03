package com.kape.customization.di

import com.kape.contracts.Router
import com.kape.customization.CustomizationViewModel
import com.kape.data.DI
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class CustomizationModule {
    @KoinViewModel
    fun provideCustomizationViewModel(
        prefs: CustomizationPrefs,
        settingsPrefs: SettingsPrefs,
        router: Router,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): CustomizationViewModel = CustomizationViewModel(prefs, settingsPrefs, router, ioDispatcher)
}