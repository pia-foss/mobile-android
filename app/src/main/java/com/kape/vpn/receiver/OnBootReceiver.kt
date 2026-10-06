package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.data.DI
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.Calendar

@Singleton
class OnBootReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val settingsPrefs: SettingsPrefs by inject()
    private val connectionPrefs: ConnectionPrefs by inject()
    private val vpnLauncher: VpnLauncher by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named(DI.IO_DISPATCHER))

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                // An active snooze survives a reboot (the SnoozeWorker stays scheduled and resumes the
                // VPN when it ends), so launch-on-boot must not cut it short.
                val isSnoozed = connectionPrefs.getLastSnoozeEndTimeNow() > Calendar.getInstance().timeInMillis
                if (settingsPrefs.isLaunchOnBootEnabledNow() && !isSnoozed) {
                    vpnLauncher.launchVpn()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}