package com.kape.vpn.service

import android.content.Intent
import android.net.VpnService
import com.kape.login.domain.AuthenticationDataSource
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * This service is only used to detect when the 'Always ON' option from device's network settings is selected,
 * so that the app can start a connection using the vpn manager.
 */
class VpnAlwaysOnService : VpnService(), KoinComponent {

    private val authenticationDataSource: AuthenticationDataSource by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (authenticationDataSource.isUserLoggedIn()) {
            vpnLauncher.launchVpn()
        }
        return super.onStartCommand(intent, flags, startId)
    }
}