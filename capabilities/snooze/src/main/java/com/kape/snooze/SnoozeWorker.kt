package com.kape.snooze

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kape.vpnlauncher.VpnLauncher
import org.koin.android.annotation.KoinWorker

@KoinWorker
class SnoozeWorker(
    context: Context,
    params: WorkerParameters,
    private val snoozeHandler: SnoozeHandler,
    private val vpnLauncher: VpnLauncher,
) : Worker(context, params) {

    override fun doWork(): Result {
        snoozeHandler.cancelSnooze()
        vpnLauncher.launchVpn()
        return Result.success()
    }
}