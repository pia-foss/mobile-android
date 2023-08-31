package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.kape.connection.ConnectionPrefs
import com.kape.settings.SettingsPrefs
import com.kape.vpn.utils.VpnLauncher
import com.kape.vpnconnect.domain.ConnectionUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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