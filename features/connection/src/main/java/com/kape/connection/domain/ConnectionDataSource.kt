package com.kape.connection.domain

import com.privateinternetaccess.kapevpnmanager.models.ClientConfiguration
import com.privateinternetaccess.kapevpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.flow.Flow

interface ConnectionDataSource {

    fun startConnection(
        clientConfiguration: ClientConfiguration,
        listener: VPNManagerConnectionListener
    ): Flow<Boolean>

    fun stopConnection(): Flow<Boolean>

    fun getVpnToken(): String
}