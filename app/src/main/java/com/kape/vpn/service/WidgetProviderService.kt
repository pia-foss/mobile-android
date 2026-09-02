package com.kape.vpn.service

import android.content.Intent
import android.net.VpnService
import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionInfoProvider
import com.kape.vpnlauncher.VpnLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.getValue

@Singleton
class WidgetProviderService :
    VpnService(),
    KoinComponent,
    CoroutineScope {
    private val authenticationDataSource: AuthenticationDataSource by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val vpnLauncher: VpnLauncher by inject()

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        launch {
            if (authenticationDataSource.isUserLoggedIn()) {
                if (connectionInfoProvider.isConnected()) {
                    vpnLauncher.stopVpn()
                } else {
                    vpnLauncher.launchVpn()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}