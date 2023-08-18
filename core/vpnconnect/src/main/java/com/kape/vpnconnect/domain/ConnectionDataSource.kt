package com.kape.vpnconnect.domain

import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.flow.Flow

interface ConnectionDataSource {

    fun startConnection(
        clientConfiguration: ClientConfiguration,
        listener: VPNManagerConnectionListener
    ): Flow<Boolean>

    fun stopConnection(): Flow<Boolean>

    fun getVpnToken(): String
}