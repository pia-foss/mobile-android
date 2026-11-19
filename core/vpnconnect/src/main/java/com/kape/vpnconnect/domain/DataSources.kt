package com.kape.vpnconnect.domain

interface ConnectionDataSource {
    fun getVpnToken(): String

    fun startPortForwarding()

    fun stopPortForwarding()

    suspend fun getDebugLogs(): List<String>

    suspend fun clearDebugLogs()
}

interface ClientStateDataSource {
    suspend fun getPublicIp(): String

    suspend fun getVpnIp(): String
}