package com.kape.contracts

import com.kape.data.vpnserver.VpnServer
import kotlinx.coroutines.Job

interface ConnectionManager {
    fun connect(server: VpnServer, isManual: Boolean): Job
    fun disconnect(): Job
    fun reconnect(server: VpnServer): Job
}