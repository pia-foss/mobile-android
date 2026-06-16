package com.kape.vpnregionselection.di

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.utils.UpdateAvailableManager
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregionselection.ui.vm.VpnRegionSelectionViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class VpnRegionModule {
    @KoinViewModel
    fun provideVpnRegionSelectionViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        vpnRegionPrefs: VpnRegionPrefs,
        settingsPrefs: SettingsPrefs,
        connectionPrefs: ConnectionPrefs,
        connectionInfoProvider: ConnectionInfoProvider,
        connectionManager: ConnectionManager,
        updateAvailableManager: UpdateAvailableManager,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
    ): VpnRegionSelectionViewModel =
        VpnRegionSelectionViewModel(
            router,
            regionListProvider,
            vpnRegionPrefs,
            settingsPrefs,
            connectionPrefs,
            connectionInfoProvider,
            connectionManager,
            updateAvailableManager,
            ioDispatcher,
        )
}