package com.kape.vpnconnect.domain

import com.kape.vpnconnect.utils.ConnectionStatus

interface ClientStateDataSource {

    suspend fun getClientStatus(connectionStatus: ConnectionStatus): Boolean

    fun resetVpnIp()
}