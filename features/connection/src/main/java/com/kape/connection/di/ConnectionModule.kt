package com.kape.connection.di

import android.app.AlarmManager
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.contracts.Router
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShortcutPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.rating.utils.RatingTool
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import com.kape.shadowsocksregions.domain.SetShadowsocksRegionsUseCase
import com.kape.snooze.SnoozeHandler
import com.kape.utils.DI
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnconnect.domain.StartConnectionUseCase
import com.kape.vpnconnect.domain.StopConnectionUseCase
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnconnect.utils.ConnectionInfoProvider
import com.kape.vpnregions.utils.RegionListProvider
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
class ConnectionModule {

    @KoinViewModel
    fun provideConnectionViewModel(
        router: Router,
        regionListProvider: RegionListProvider,
        startConnectionUseCase: StartConnectionUseCase,
        stopConnectionUseCase: StopConnectionUseCase,
        connectionInfoProvider: ConnectionInfoProvider,
        prefs: ConnectionPrefs,
        settingsPrefs: SettingsPrefs,
        snoozeHandler: SnoozeHandler,
        usageProvider: UsageProvider,
        dipPrefs: DipPrefs,
        renewDipUseCase: RenewDipUseCase,
        customizationPrefs: CustomizationPrefs,
        vpnRegionPrefs: VpnRegionPrefs,
        ratingTool: RatingTool,
        shortcutPrefs: ShortcutPrefs,
        buildConfigProvider: BuildConfigProvider,
        networkConnectionListener: NetworkConnectionListener,
    ): ConnectionViewModel = ConnectionViewModel(
        router, regionListProvider, startConnectionUseCase, stopConnectionUseCase,
        prefs, settingsPrefs, snoozeHandler, usageProvider, dipPrefs,
        renewDipUseCase, customizationPrefs, vpnRegionPrefs, ratingTool,
        shortcutPrefs, buildConfigProvider, connectionInfoProvider, networkConnectionListener,
    )
}