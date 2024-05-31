package com.kape.vpnconnect.domain

import kotlinx.coroutines.flow.Flow

interface ClientStateDataSource {

    fun getClientStatus(): Flow<Boolean>

    fun resetVpnIp()
}