package com.kape.vpnconnect.domain

import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.presenters.VPNManagerCallback
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.CoroutineScope

interface ConnectionDataSource {
    suspend fun startConnection(
        clientConfiguration: ClientConfiguration,
        scope: CoroutineScope
    ): Boolean

    fun stopConnection()
    fun getVpnToken(): String
    fun startPortForwarding()
    fun stopPortForwarding()
    suspend fun getDebugLogs(): List<String>
    suspend fun updateConfigurationServers(servers: ServerList): Boolean

}