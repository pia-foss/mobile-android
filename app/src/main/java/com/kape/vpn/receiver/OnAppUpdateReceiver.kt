package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.data.DI
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@Singleton
class OnAppUpdateReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val settingsPrefs: SettingsPrefs by inject()
    private val userLoggedInUseCase: IsUserLoggedInUseCase by inject()
    private val vpnLauncher: VpnLauncher by inject()
    private val ioScope: CoroutineScope by inject(named(DI.IO_SCOPE))

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().removeAllCookies(null)

        ioScope.launch {
            if (settingsPrefs.isConnectOnAppUpdateEnabledNow() && userLoggedInUseCase.invoke()) {
                vpnLauncher.launchVpn()
            }
        }
    }
}