package com.kape.contracts

import com.kape.data.vpnserver.VpnServer
import kotlinx.coroutines.Job

interface ConnectionManager {
    fun connectionInProgress(): Boolean
    suspend fun connect(server: VpnServer, isManual: Boolean)
    suspend fun disconnect()
    suspend fun reconnect(server: VpnServer)
}