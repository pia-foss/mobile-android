package com.kape.vpnconnect.domain

interface ConnectionDataSource {
//    suspend fun startConnection(
//        clientConfiguration: ClientConfiguration,
//        connectionStatusProvider: ConnectionStatusProvider,
//    ): Result<Unit>
//
//    suspend fun stopConnection(): Result<Unit>

    fun getVpnToken(): String

    fun startPortForwarding()

    fun stopPortForwarding()

    suspend fun getDebugLogs(): List<String>

//    suspend fun updateConfigurationServers(servers: ServerList): Boolean
}