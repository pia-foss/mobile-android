package com.kape.vpnlauncher.di

import android.content.Context
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnconnect.domain.ConnectionUseCase
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
        connectionUseCase: ConnectionUseCase,
        settingsPrefs: SettingsPrefs,
        regionListProvider: RegionListProvider,
    ): VpnLauncher = VpnLauncher(context, connectionPrefs, connectionUseCase, settingsPrefs, regionListProvider)
}