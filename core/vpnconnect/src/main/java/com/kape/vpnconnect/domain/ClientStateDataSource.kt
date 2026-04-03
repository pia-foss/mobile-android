package com.kape.vpnconnect.domain

import com.kape.vpnconnect.utils.ConnectionStatus

interface ClientStateDataSource {
    suspend fun getPublicIp(): String
    suspend fun getVpnIp(): String
}