package com.kape.vpnlauncher.di

import android.content.Context
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnconnect.domain.StartConnectionUseCase
import com.kape.vpnconnect.domain.StopConnectionUseCase
import com.kape.vpnlauncher.VpnLauncher
import com.kape.vpnregions.utils.RegionListProvider
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class VpnLauncherModule {

    @Singleton
    fun provideVpnLauncher(
        context: Context,
        connectionPrefs: ConnectionPrefs,
        settingsPrefs: SettingsPrefs,
        regionListProvider: RegionListProvider,
        startConnectionUseCase: StartConnectionUseCase,
        stopConnectionUseCase: StopConnectionUseCase,
    ): VpnLauncher = VpnLauncher(
        context,
        connectionPrefs,
        settingsPrefs,
        regionListProvider,
        startConnectionUseCase,
        stopConnectionUseCase,
    )
}