package com.kape.contracts

import com.kape.data.vpnserver.VpnServer

interface ConnectionManager {
    suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    )

    suspend fun connectToLastKnownOrOptimalServer()

    suspend fun disconnect()

    suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    )

    fun isConnected(): Boolean

    fun isConnectionInProgress(): Boolean
}