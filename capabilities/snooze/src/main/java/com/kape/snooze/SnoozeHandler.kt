package com.kape.snooze

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.CountDownTimer
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.kape.connection.ConnectionPrefs
import com.kape.vpnlauncher.VpnLauncher
import java.util.Calendar

private const val MINUTE = 60 * 1000L

class SnoozeHandler(
    private val alarmManager: AlarmManager,
    private val setSnoozePendingIntent: PendingIntent,
    private val cancelSnoozePendingIntent: PendingIntent,
    private val connectionPrefs: ConnectionPrefs,
    private val vpnLauncher: VpnLauncher,
) {

    val isSnoozeActive = mutableStateOf(false)
    val timeUntilResume = mutableIntStateOf(0)
    private var countDownTimer: CountDownTimer? = null

    fun setSnooze(interval: Int) {
        isSnoozeActive.value = true
        val nowInMillis = Calendar.getInstance().timeInMillis
        val end = nowInMillis + interval
        setCountdownTimer(end)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, end, setSnoozePendingIntent)
        connectionPrefs.setLastSnoozeEndTime(end)
        vpnLauncher.stopVpn()
    }

    fun cancelSnooze() {
        isSnoozeActive.value = false
        alarmManager.cancel(cancelSnoozePendingIntent)
        connectionPrefs.setLastSnoozeEndTime(0)
        countDownTimer?.cancel()
    }

    private fun setCountdownTimer(end: Long) {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(end, MINUTE) {
                override fun onTick(millisUntilFinished: Long) {
                    timeUntilResume.intValue =
                        (((connectionPrefs.getLastSnoozeEndTime() - Calendar.getInstance().timeInMillis) / MINUTE) + 1).toInt()
                }

                override fun onFinish() {
                    // no-op
                }
            }
        }
        (countDownTimer as CountDownTimer).start()
    }
}