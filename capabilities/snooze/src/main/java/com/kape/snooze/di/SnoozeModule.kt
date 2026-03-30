package com.kape.snooze.di

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.snooze.OnSnoozeReceiver
import com.kape.snooze.SnoozeHandler
import com.kape.utils.DI
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
class SnoozeModule {
    @Singleton
    @Named(DI.SET_SNOOZE_PENDING_INTENT)
    fun provideSetSnoozePendingIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, OnSnoozeReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
        )
    }

    @Singleton
    @Named(DI.CANCEL_SNOOZE_PENDING_INTENT)
    fun provideCancelSnoozePendingIntent(context: Context): PendingIntent? {
        return PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, OnSnoozeReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
        )
    }

    @Singleton
    fun provideSnoozeHandler(
        @Named(DI.ALARM_MANAGER) alarmManager: AlarmManager,
        @Named(DI.SET_SNOOZE_PENDING_INTENT) setSnooze: PendingIntent,
        @Named(DI.CANCEL_SNOOZE_PENDING_INTENT) cancelSnooze: PendingIntent,
        connectionPrefs: ConnectionPrefs,
        vpnLauncher: VpnLauncher,
    ): SnoozeHandler = SnoozeHandler(
        alarmManager, setSnooze, cancelSnooze, connectionPrefs, vpnLauncher,
    )

}