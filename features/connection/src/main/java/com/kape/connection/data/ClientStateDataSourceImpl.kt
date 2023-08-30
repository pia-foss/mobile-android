package com.kape.connection.data

import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.connection.domain.ClientStateDataSource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientStateDataSourceImpl(
    private val accountAPI: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
) : ClientStateDataSource {
    override fun getClientStatus(): Flow<Boolean> = callbackFlow {
        accountAPI.clientStatus { status: ClientStatusInformation?, error: List<AccountRequestError> ->
            status?.let {
                if (status.connected) {
                    connectionPrefs.setClientVpnIp(status.ip)
                } else {
                    connectionPrefs.setClientIp(status.ip)
                    connectionPrefs.setClientVpnIp(NO_IP)
                }
                trySend(true)
            } ?: run {
                connectionPrefs.setClientVpnIp(NO_IP)
                trySend(false)
            }
        }
        awaitClose { channel.close() }
    }

    override fun resetVpnIp() {
        connectionPrefs.setClientVpnIp(NO_IP)
    }
}