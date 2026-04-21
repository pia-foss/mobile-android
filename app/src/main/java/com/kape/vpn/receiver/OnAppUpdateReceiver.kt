package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Singleton
class OnAppUpdateReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val settingsPrefs: SettingsPrefs by inject()
    private val userLoggedInUseCase: IsUserLoggedInUseCase by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().removeAllCookies(null)

        if (settingsPrefs.isConnectOnAppUpdateEnabled() && userLoggedInUseCase.invoke()) {
            vpnLauncher.launchVpn()
        }
    }
}