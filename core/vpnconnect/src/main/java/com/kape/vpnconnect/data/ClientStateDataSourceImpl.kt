package com.kape.vpnconnect.data

import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.csi.CsiPrefs
import com.kape.settings.SettingsPrefs
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnconnect.utils.STATUS_REQUEST_LONG_TIMEOUT
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

    override fun getClientStatus(connectionStatus: ConnectionStatus): Flow<Boolean> = callbackFlow {
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

        fun conditionsMatch(status: ClientStatusInformation?, error: List<AccountRequestError>): Boolean {
            if (error.isNotEmpty() || status == null) return false
            return (status.connected && connectionStatus is ConnectionStatus.CONNECTED) ||
                    (!status.connected && connectionStatus is ConnectionStatus.DISCONNECTED)
        }

        accountAPI.clientStatus { status: ClientStatusInformation?, error: List<AccountRequestError> ->
            if (conditionsMatch(status, error)) {
                processClientStatus(status, error)
            } else {
                // Retry with longer timeout if conditions don't match or there are errors
                accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT) { retryStatus: ClientStatusInformation?, retryError: List<AccountRequestError> ->
                    processClientStatus(retryStatus, retryError)
                }
            }
        }

        awaitClose { channel.close() }
    }

    override fun resetVpnIp() {
        connectionPrefs.setVpnIp(NO_IP)
    }
}