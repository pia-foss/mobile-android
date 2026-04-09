package com.kape.contracts

import com.kape.data.vpnserver.VpnServer

interface ConnectionManager {
    fun connect(server: VpnServer, isManual: Boolean)
    fun disconnect()
    fun reconnect(server: VpnServer)
}