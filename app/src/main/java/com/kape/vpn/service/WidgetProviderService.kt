package com.kape.vpn.service

import android.content.Intent
import android.net.VpnService
import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionInfoProvider
import com.kape.vpnconnect.utils.ConnectionInfoProviderImpl
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@Singleton
class WidgetProviderService : VpnService(), KoinComponent {

    private val authenticationDataSource: AuthenticationDataSource by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val vpnLauncher: VpnLauncher by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (authenticationDataSource.isUserLoggedIn()) {
            if (connectionInfoProvider.isConnected()) {
                vpnLauncher.stopVpn()
            } else {
                vpnLauncher.launchVpn()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}