package com.kape.data

import com.kape.vpnmanager.api.VPNManagerConnectionStatus

data class VpnConnectionStatus(
    val status: ConnectionStatus,
    val title: String,
    val vpnManagerConnectionStatus: VPNManagerConnectionStatus? = null,
)