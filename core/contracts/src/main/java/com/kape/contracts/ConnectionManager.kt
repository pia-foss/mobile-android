package com.kape.contracts

import com.kape.data.vpnserver.VpnServer
import kotlinx.coroutines.Job

interface ConnectionManager {
    var connectJob: Job?
    suspend fun connect(server: VpnServer, isManual: Boolean, stopCallback: () -> Unit)
    suspend fun disconnect(): Result<Unit>
}