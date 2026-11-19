package com.kape.vpnconnect.di

import android.content.Context
import androidx.work.WorkManager
import com.kape.contracts.NetworkStatus
import com.kape.contracts.UsageProvider
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.prefs.AutoProtocolNudgePrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnconnect.data.VpnStatusTracker
import com.kape.vpnconnect.domain.ConnectionProblemDetector
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.kape.vpnconnect")
class VpnConnectModule {
    @Singleton
    fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)

    @Singleton
    fun provideConnectionProblemDetector(
        statusTracker: VpnStatusTracker,
        settingsPrefs: SettingsPrefs,
        usage: UsageProvider,
        networkStatus: NetworkStatus,
        nudgePrefs: AutoProtocolNudgePrefs,
        @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    ): ConnectionProblemDetector =
        ConnectionProblemDetector(
            connectionStatusProvider = statusTracker,
            hasTunnelError = { statusTracker.lastTunnelError.value != null },
            settingsPrefs = settingsPrefs,
            usageProvider = usage,
            networkStatus = networkStatus,
            nudgePrefs = nudgePrefs,
            scope = appScope,
        )
}