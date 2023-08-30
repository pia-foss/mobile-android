package com.kape.connection.domain

import kotlinx.coroutines.flow.Flow

interface ClientStateDataSource {

    fun getClientStatus(): Flow<Boolean>

    fun resetVpnIp()
}