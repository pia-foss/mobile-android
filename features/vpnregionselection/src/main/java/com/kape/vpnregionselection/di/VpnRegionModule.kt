package com.kape.vpnregionselection.di

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class VpnRegionModule {
    @KoinViewModel
    fun provideVpnRegionSelectionViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        vpnRegionPrefs: VpnRegionPrefs,
        settingsPrefs: SettingsPrefs,
        connectionInfoProvider: ConnectionInfoProvider,
        connectionManager: ConnectionManager,
    ): VpnRegionSelectionViewModel =
        VpnRegionSelectionViewModel(
            router,
            regionListProvider,
            vpnRegionPrefs,
            settingsPrefs,
            connectionInfoProvider,
            connectionManager,
        )
}