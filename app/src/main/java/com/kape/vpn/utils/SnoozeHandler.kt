package com.kape.vpn.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.kape.connection.ConnectionPrefs
import org.koin.core.component.KoinComponent

const val SNOOZE_REQUEST_CODE = 24

class SnoozeHandler(
    private val alarmManager: AlarmManager,
    private val snoozePendingIntent: PendingIntent?,
    private val connectionPrefs: ConnectionPrefs,
    private val vpnLauncher: VpnLauncher,
) : KoinComponent {

    fun resumeVpn() {
        if (snoozePendingIntent != null) {
            alarmManager.cancel(snoozePendingIntent)
        }
        connectionPrefs.setLastSnoozeEndTime(0)
        vpnLauncher.launchVpn()
    }
}