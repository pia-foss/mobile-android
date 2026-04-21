package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionStatusProvider
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerList

interface ConnectionDataSource {
    suspend fun startConnection(
        clientConfiguration: ClientConfiguration,
        connectionStatusProvider: ConnectionStatusProvider,
    ): Result<Unit>

    suspend fun stopConnection(): Result<Unit>

    fun getVpnToken(): String

    fun startPortForwarding()

    fun stopPortForwarding()

    suspend fun getDebugLogs(): List<String>

    suspend fun updateConfigurationServers(servers: ServerList): Boolean
}