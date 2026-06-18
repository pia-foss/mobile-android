package com.kape.customization.di

import com.kape.contracts.Router
import com.kape.contracts.ScreenElementProvider
import com.kape.customization.CustomizationViewModel
import com.kape.customization.ScreenElementProviderImpl
import com.kape.data.DI
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class CustomizationModule {
    @Singleton(binds = [ScreenElementProvider::class])
    fun provideScreenElementProvider(
        customizationPrefs: CustomizationPrefs,
        settingsPrefs: SettingsPrefs,
        ioScope: CoroutineScope,
    ): ScreenElementProvider = ScreenElementProviderImpl(customizationPrefs, settingsPrefs, ioScope)

    @KoinViewModel
    fun provideCustomizationViewModel(
        prefs: CustomizationPrefs,
        screenElementProvider: ScreenElementProvider,
        router: Router,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): CustomizationViewModel = CustomizationViewModel(prefs, screenElementProvider, router, ioDispatcher)
}