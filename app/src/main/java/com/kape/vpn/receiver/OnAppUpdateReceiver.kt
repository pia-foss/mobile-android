package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.kape.login.domain.GetUserLoggedInUseCase
import com.kape.settings.SettingsPrefs
import com.kape.vpn.utils.VpnLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OnAppUpdatedReceiver : BroadcastReceiver(), KoinComponent {

    private val settingsPrefs: SettingsPrefs by inject()
    private val userLoggedInUseCase: GetUserLoggedInUseCase by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onReceive(context: Context, intent: Intent) {
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().removeAllCookies(null)

        if (settingsPrefs.isConnectOnAppUpdateEnabled() && userLoggedInUseCase.isUserLoggedIn()) {
            vpnLauncher.launchVpn()
        }
    }
}