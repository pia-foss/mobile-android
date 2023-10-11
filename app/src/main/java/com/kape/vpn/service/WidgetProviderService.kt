package com.kape.vpn.service

import android.content.Intent
import android.net.VpnService
import com.kape.login.domain.AuthenticationDataSource
import com.kape.vpn.utils.VpnLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WidgetProviderService : VpnService(), KoinComponent {

    private val authenticationDataSource: AuthenticationDataSource by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (authenticationDataSource.isUserLoggedIn()) {
            if (vpnLauncher.isVpnConnected()) {
                vpnLauncher.stopVpn()
            } else {
                vpnLauncher.launchVpn()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}