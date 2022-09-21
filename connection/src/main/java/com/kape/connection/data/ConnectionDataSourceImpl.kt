package com.kape.connection.data

import com.kape.connection.domain.ConnectionDataSource
import com.privateinternetaccess.kapevpnmanager.models.ClientConfiguration
import com.privateinternetaccess.kapevpnmanager.presenters.VPNManagerAPI
import com.privateinternetaccess.kapevpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConnectionDataSourceImpl : ConnectionDataSource, KoinComponent {

    private val connectionApi: VPNManagerAPI by inject()

    override fun startConnection(
        clientConfiguration: ClientConfiguration,
        listener: VPNManagerConnectionListener
    ): Flow<Boolean> =
        callbackFlow {
            connectionApi.addConnectionListener(listener) {}
            connectionApi.startConnection(clientConfiguration) {
                trySend(it.isSuccess)
            }
            awaitClose { channel.close() }
        }

    override fun stopConnection(): Flow<Boolean> = callbackFlow {
        connectionApi.stopConnection {
            trySend(it.isSuccess)
        }
        awaitClose { channel.close() }
    }
}