package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Singleton
class OnBootReceiver : BroadcastReceiver(), KoinComponent {

    private val settingsPrefs: SettingsPrefs by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (settingsPrefs.isLaunchOnStartupEnabled()) {
            vpnLauncher.launchVpn()
        }
    }
}