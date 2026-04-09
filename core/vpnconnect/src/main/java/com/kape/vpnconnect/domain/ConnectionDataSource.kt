package com.kape.vpnconnect.domain

import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.presenters.VPNManagerCallback
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener

interface ConnectionDataSource {
    suspend fun startConnection(
        clientConfiguration: ClientConfiguration,
        listener: VPNManagerConnectionListener,
    ): Boolean

    suspend fun stopConnection(): Boolean
    fun getVpnToken(): String
    fun startPortForwarding()
    fun stopPortForwarding()
    suspend fun getDebugLogs(): List<String>
    suspend fun updateConfigurationServers(servers: ServerList): Boolean

}