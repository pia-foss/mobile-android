package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.settings.SettingsPrefs
import com.kape.vpn.utils.VpnLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OnBootReceiver : BroadcastReceiver(), KoinComponent {

    private val settingsPrefs: SettingsPrefs by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (settingsPrefs.isLaunchOnStartupEnabled()) {
            vpnLauncher.launchVpn()
        }
    }
}