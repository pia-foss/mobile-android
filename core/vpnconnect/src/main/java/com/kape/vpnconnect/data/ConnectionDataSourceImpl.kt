package com.kape.vpnconnect.data

import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConnectionDataSourceImpl : ConnectionDataSource, KoinComponent {

    private val connectionApi: VPNManagerAPI by inject()
    private val accountApi: AndroidAccountAPI by inject()

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

    override fun getVpnToken(): String {
        return accountApi.vpnToken() ?: ""
    }
}