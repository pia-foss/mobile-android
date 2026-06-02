package com.kape.snooze.di

import androidx.work.WorkManager
import com.kape.data.DI
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.snooze.SnoozeHandler
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("com.kape.snooze")
class SnoozeModule {
    @Singleton
    fun provideSnoozeHandler(
        workManager: WorkManager,
        connectionPrefs: ConnectionPrefs,
        vpnLauncher: VpnLauncher,
        @Named(DI.IO_SCOPE) ioScope: CoroutineScope,
    ): SnoozeHandler = SnoozeHandler(workManager, connectionPrefs, vpnLauncher, ioScope)
}