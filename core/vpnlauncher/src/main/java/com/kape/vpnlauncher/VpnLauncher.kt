package com.kape.vpnlauncher

import android.content.Context
import android.net.VpnService
import com.kape.contracts.ConnectionManager
import com.kape.contracts.di.Qualifiers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton
class VpnLauncher(
    private val context: Context,
    private val connectionManager: ConnectionManager,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    fun launchVpn() {
        if (VpnService.prepare(context) != null) return
        appScope.launch { connectionManager.connectToLastKnownOrOptimalServer() }
    }

    fun stopVpn() {
        appScope.launch { connectionManager.disconnect() }
    }
}