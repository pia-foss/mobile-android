package com.kape.vpnconnect.data

import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.csi.CsiPrefs
import com.kape.settings.SettingsPrefs
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ClientStateDataSourceImpl(
    private val accountAPI: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
    private val csiPrefs: CsiPrefs,
    private val settingsPrefs: SettingsPrefs,
) : ClientStateDataSource {

    override fun getClientStatus(): Flow<Boolean> = callbackFlow {
        fun processClientStatus(
            status: ClientStatusInformation?,
            error: List<AccountRequestError>,
        ) {
            csiPrefs.addCustomDebugLogs(
                "getClientStatusErrors: $error",
                settingsPrefs.isDebugLoggingEnabled(),
            )
            status?.let {
                if (status.connected) {
                    connectionPrefs.setVpnIp(status.ip)
                } else {
                    connectionPrefs.setClientIp(status.ip)
                    connectionPrefs.setVpnIp(NO_IP)
                }
                trySend(true)
            } ?: run {
                connectionPrefs.setVpnIp(NO_IP)
                trySend(false)
            }
        }

        accountAPI.clientStatus { status: ClientStatusInformation?, error: List<AccountRequestError> ->
            processClientStatus(status, error)
            // Sometimes the API will timeout while the tunnel is being started. If that happens we retry once
            if (error.isNotEmpty()) {
                accountAPI.clientStatus(20000) { status: ClientStatusInformation?, error: List<AccountRequestError> ->
                    processClientStatus(status, error)
                }
            }
        }

        awaitClose { channel.close() }
    }

    override fun resetVpnIp() {
        connectionPrefs.setVpnIp(NO_IP)
    }
}