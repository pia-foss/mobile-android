package com.kape.obfuscationregionselection.di

import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscationregionselection.ui.vm.ShadowsocksRegionSelectionViewModel
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class ShadowsocksRegionModule {
    @KoinViewModel
    fun provideShadowsocksRegionSelectionViewModel(
        router: Router,
        getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
        shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): ShadowsocksRegionSelectionViewModel =
        ShadowsocksRegionSelectionViewModel(
            router,
            getShadowsocksRegionsUseCase,
            shadowsocksRegionPrefs,
            ioDispatcher,
        )
}