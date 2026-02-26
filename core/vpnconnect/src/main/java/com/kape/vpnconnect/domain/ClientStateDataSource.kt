package com.kape.vpnconnect.domain

import com.kape.vpnconnect.utils.ConnectionStatus
import kotlinx.coroutines.flow.Flow

interface ClientStateDataSource {

    fun getClientStatus(connectionStatus: ConnectionStatus): Flow<Boolean>

    fun resetVpnIp()
}