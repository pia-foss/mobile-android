package com.kape.contracts

import com.kape.data.ConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVpnTunnelError
import kotlinx.coroutines.flow.StateFlow

interface ConnectionStatusProvider {
    val status: StateFlow<ConnectionStatus>
    val title: StateFlow<String>
    val vpnManagerConnectionStatus: StateFlow<KapeVPNConnectionStatus?>
    val lastTunnelError: StateFlow<KapeVpnTunnelError?>

    fun handleConnectionStatusChange(status: KapeVPNConnectionStatus)

    fun handleTunnelError(error: KapeVpnTunnelError?)
}