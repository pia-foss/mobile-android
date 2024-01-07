package com.kape.snooze

import android.app.AlarmManager
import android.app.PendingIntent
import androidx.compose.runtime.mutableStateOf
import com.kape.connection.ConnectionPrefs
import com.kape.vpnlauncher.VpnLauncher
import java.util.Calendar

class SnoozeHandler(
    private val alarmManager: AlarmManager,
    private val setSnoozePendingIntent: PendingIntent,
    private val cancelSnoozePendingIntent: PendingIntent,
    private val connectionPrefs: ConnectionPrefs,
    private val vpnLauncher: VpnLauncher,
) {

    val isSnoozeActive = mutableStateOf(false)

    fun setSnooze(interval: Int) {
        isSnoozeActive.value = true
        val nowInMillis = Calendar.getInstance().timeInMillis
        val end = nowInMillis + interval
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, end, setSnoozePendingIntent)
        connectionPrefs.setLastSnoozeEndTime(end)
        vpnLauncher.stopVpn()
    }

    fun cancelSnooze() {
        isSnoozeActive.value = false
        alarmManager.cancel(cancelSnoozePendingIntent)
        connectionPrefs.setLastSnoozeEndTime(0)
        vpnLauncher.launchVpn()
    }
}