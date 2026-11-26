package com.kape.snooze

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kape.contracts.SnoozeStatus
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.util.concurrent.TimeUnit

private const val MINUTE_MS = 60_000L
private const val SNOOZE_WORK = "snooze"

@Singleton(binds = [SnoozeStatus::class])
class SnoozeHandler(
    private val workManager: WorkManager,
    private val connectionPrefs: ConnectionPrefs,
    private val vpnLauncher: VpnLauncher,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : SnoozeStatus {
    private val _snoozeEndTime = MutableStateFlow(0L)
    override val snoozeEndTime: StateFlow<Long> = _snoozeEndTime.asStateFlow()

    private val _isSnoozeActive = MutableStateFlow(false)
    val isSnoozeActive: StateFlow<Boolean> = _isSnoozeActive.asStateFlow()

    private val _minutesUntilResume = MutableStateFlow(0)

    val minutesUntilResume: StateFlow<Int> = _minutesUntilResume.asStateFlow()

    private var countdownJob: Job? = null

    init {
        appScope.launch {
            val end = connectionPrefs.getLastSnoozeEndTimeNow()
            if (end > System.currentTimeMillis() && _snoozeEndTime.value == 0L) startCountdown(end)
        }
    }

    fun setSnooze(intervalMillis: Int) {
        appScope.launch {
            val end = System.currentTimeMillis() + intervalMillis
            startCountdown(end)
            workManager.enqueueUniqueWork(
                SNOOZE_WORK,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SnoozeWorker>().setInitialDelay(intervalMillis.toLong(), TimeUnit.MILLISECONDS).build(),
            )
            connectionPrefs.setLastSnoozeEndTime(end)
            vpnLauncher.stopVpn()
        }
    }

    override fun cancelSnooze() {
        appScope.launch {
            countdownJob?.cancel()
            countdownJob = null
            _isSnoozeActive.value = false
            _snoozeEndTime.value = 0L
            workManager.cancelUniqueWork(SNOOZE_WORK)
            connectionPrefs.setLastSnoozeEndTime(0)
        }
    }

    private fun startCountdown(end: Long) {
        _isSnoozeActive.value = true
        _snoozeEndTime.value = end
        countdownJob?.cancel()
        countdownJob =
            appScope.launch {
                while (isActive) {
                    _minutesUntilResume.value = minutesUntil(end, System.currentTimeMillis())
                    delay(MINUTE_MS)
                }
            }
    }
}

internal fun minutesUntil(
    end: Long,
    now: Long,
): Int = ((end - now + MINUTE_MS - 1) / MINUTE_MS).toInt().coerceAtLeast(0)