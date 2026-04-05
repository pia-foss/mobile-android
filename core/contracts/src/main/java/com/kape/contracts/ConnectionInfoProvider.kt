package com.kape.contracts

import com.kape.data.VpnConnectionInfo
import com.kape.data.VpnConnectionStatus
import kotlinx.coroutines.flow.StateFlow

interface ConnectionInfoProvider {
    val connectionState: StateFlow<VpnConnectionStatus>
    val state: StateFlow<VpnConnectionInfo>
    fun isConnected(): Boolean
    fun isInConnectState(): Boolean
    fun isNotDisconnected(): Boolean
    fun updateInfo(name: String, iso: String, isManual: Boolean)
    fun resetConnectionInfo()
}