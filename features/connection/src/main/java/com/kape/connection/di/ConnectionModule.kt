package com.kape.connection.di

import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.dedicatedip.domain.DipPurchaseHandler
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShortcutPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.permissions.domain.IsVpnProfileInstalledUseCase
import com.kape.rating.utils.RatingTool
import com.kape.snooze.SnoozeHandler
import com.kape.utils.NetworkConnectionListener
import com.kape.utils.UpdateAvailableManager
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregions.utils.ShadowsocksListProvider
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class ConnectionModule {
    @KoinViewModel
    fun provideConnectionViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        shadowsocksListProvider: ShadowsocksListProvider,
        connectionManager: ConnectionManager,
        connectionInfoProvider: ConnectionInfoProvider,
        prefs: ConnectionPrefs,
        settingsPrefs: SettingsPrefs,
        snoozeHandler: SnoozeHandler,
        dipPrefs: DipPrefs,
        customizationPrefs: CustomizationPrefs,
        vpnRegionPrefs: VpnRegionPrefs,
        ratingTool: RatingTool,
        shortcutPrefs: ShortcutPrefs,
        buildConfigProvider: BuildConfigProvider,
        isVpnProfileInstalledUseCase: IsVpnProfileInstalledUseCase,
        dipPurchaseHandler: DipPurchaseHandler,
        updateAvailableManager: UpdateAvailableManager,
        @Named(DI.IO_DISPATCHER) ioDispatcher: CoroutineDispatcher,
        networkConnectionListener: NetworkConnectionListener,
    ): ConnectionViewModel =
        ConnectionViewModel(
            router,
            regionListProvider,
            shadowsocksListProvider,
            connectionManager,
            prefs,
            settingsPrefs,
            snoozeHandler,
            dipPrefs,
            customizationPrefs,
            vpnRegionPrefs,
            ratingTool,
            shortcutPrefs,
            buildConfigProvider,
            isVpnProfileInstalledUseCase,
            dipPurchaseHandler,
            updateAvailableManager,
            ioDispatcher,
            connectionInfoProvider,
            networkConnectionListener,
        )
}