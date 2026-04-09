package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionManager
import com.kape.data.vpnserver.VpnServer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConnectionManagerImpl : ConnectionManager, KoinComponent {
    private val connectionSource: ConnectionDataSource by inject()

    override fun connect(server: VpnServer, isManual: Boolean) {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun reconnect(server: VpnServer) {
        TODO("Not yet implemented")
    }

}