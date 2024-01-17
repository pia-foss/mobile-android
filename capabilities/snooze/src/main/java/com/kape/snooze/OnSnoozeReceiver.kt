package com.kape.snooze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OnSnoozeReceiver : BroadcastReceiver(), KoinComponent {

    private val snoozeHandler: SnoozeHandler by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onReceive(context: Context, intent: Intent) {
        snoozeHandler.cancelSnooze()
        vpnLauncher.launchVpn()
    }
}