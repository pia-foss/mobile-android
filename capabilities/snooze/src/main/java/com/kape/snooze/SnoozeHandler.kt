package com.kape.snooze

import android.os.CountDownTimer
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kape.data.WorkerTags
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnlauncher.VpnLauncher
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val MINUTE = 60 * 1000L

class SnoozeHandler(
    private val workManager: WorkManager,
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
        val workRequest = OneTimeWorkRequestBuilder<SnoozeWorker>()
            .setInitialDelay(interval.toLong(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            WorkerTags.SNOOZE_WORKER,
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
        connectionPrefs.setLastSnoozeEndTime(end)
        vpnLauncher.stopVpn()
    }

    fun cancelSnooze() {
        isSnoozeActive.value = false
        workManager.cancelUniqueWork(WorkerTags.SNOOZE_WORKER)
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