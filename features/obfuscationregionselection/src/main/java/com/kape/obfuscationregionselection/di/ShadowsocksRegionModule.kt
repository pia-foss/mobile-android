package com.kape.obfuscationregionselection.di

import com.kape.contracts.Router
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscationregionselection.ui.vm.ShadowsocksRegionSelectionViewModel
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class ShadowsocksRegionModule {
    @KoinViewModel
    fun provideShadowsocksRegionSelectionViewModel(
        router: Router,
        getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
        shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    ): ShadowsocksRegionSelectionViewModel =
        ShadowsocksRegionSelectionViewModel(
            router,
            getShadowsocksRegionsUseCase,
            shadowsocksRegionPrefs,
        )
}