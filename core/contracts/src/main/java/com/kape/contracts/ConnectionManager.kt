package com.kape.contracts

import com.kape.data.vpnserver.VpnServer
import kotlinx.coroutines.Job

interface ConnectionManager {
    var connectJob: Job?

    suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    )

    suspend fun disconnect()

    suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    )

    fun isConnectionInProgress(): Boolean
}