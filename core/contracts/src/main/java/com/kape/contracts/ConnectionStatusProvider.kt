package com.kape.contracts

import com.kape.data.ConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface ConnectionStatusProvider {
    val status: StateFlow<ConnectionStatus>
    val title: StateFlow<String>
    val vpnManagerConnectionStatus: StateFlow<KapeVPNConnectionStatus?>

    fun handleConnectionStatusChange(status: KapeVPNConnectionStatus)
}