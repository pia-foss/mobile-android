package com.kape.vpnregionselection.di

import com.kape.contracts.Router
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import com.kape.vpnregions.utils.RegionListProvider
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class VpnRegionModule {

    @KoinViewModel
    fun provideVpnRegionSelectionViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        connectionUseCase: ConnectionUseCase,
        vpnRegionPrefs: VpnRegionPrefs,
        settingsPrefs: SettingsPrefs,
    ): VpnRegionSelectionViewModel = VpnRegionSelectionViewModel(
        router, regionListProvider, connectionUseCase, vpnRegionPrefs, settingsPrefs,
    )
}