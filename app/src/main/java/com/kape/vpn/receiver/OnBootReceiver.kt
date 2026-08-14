package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.data.DI
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@Singleton
class OnBootReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val settingsPrefs: SettingsPrefs by inject()
    private val vpnLauncher: VpnLauncher by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named(DI.IO_DISPATCHER))

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                if (settingsPrefs.isLaunchOnStartupEnabled.first()) {
                    vpnLauncher.launchVpn()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}