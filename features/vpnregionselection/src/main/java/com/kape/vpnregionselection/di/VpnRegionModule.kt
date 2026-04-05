package com.kape.vpnregionselection.di

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.Router
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.vpnconnect.domain.ReconnectUseCase
import com.kape.vpnconnect.utils.ConnectionInfoProviderImpl
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
        vpnRegionPrefs: VpnRegionPrefs,
        settingsPrefs: SettingsPrefs,
        connectionInfoProvider: ConnectionInfoProvider,
        reconnectUseCase: ReconnectUseCase,
    ): VpnRegionSelectionViewModel = VpnRegionSelectionViewModel(
        router,
        regionListProvider,
        vpnRegionPrefs,
        settingsPrefs,
        connectionInfoProvider,
        reconnectUseCase,
    )
}