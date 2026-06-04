package com.kape.contracts

import com.kape.data.ConnectionStatus
import com.kape.vpnmanager.api.VPNManagerConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface ConnectionStatusProvider {
    val status: StateFlow<ConnectionStatus>
    val title: StateFlow<String>
    val vpnManagerConnectionStatus: StateFlow<VPNManagerConnectionStatus?>
}