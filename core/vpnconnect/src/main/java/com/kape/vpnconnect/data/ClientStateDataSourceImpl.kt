package com.kape.vpnconnect.data

import com.kape.data.NO_IP
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.utils.DELAY_BETWEEN_RETRY
import com.kape.vpnconnect.utils.SHORT_DELAY
import com.kape.vpnconnect.utils.STATUS_REQUEST_LONG_TIMEOUT
import com.privateinternetaccess.account.AccountRequestError
import com.privateinternetaccess.account.AndroidAccountAPI
import com.privateinternetaccess.account.model.response.ClientStatusInformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [ClientStateDataSource::class])
class ClientStateDataSourceImpl(
    private val accountAPI: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
    private val csiPrefs: CsiPrefs,
    private val settingsPrefs: SettingsPrefs,
) : ClientStateDataSource {

    override suspend fun getPublicIp(): String = suspendCancellableCoroutine { continuation ->
        accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT) { clientStatusInfo, errors ->
            clientStatusInfo?.let {
                if (!it.connected) {
                    connectionPrefs.setClientIp(it.ip)
                    connectionPrefs.setVpnIp(NO_IP)
                }
                continuation.resume(it.ip)
            } ?: continuation.resume(NO_IP)
        }
    }

    override suspend fun getVpnIp(): String {
        repeat(3) { attempt ->
            val vpnIp = getVpnIpOnce()
            if (vpnIp != NO_IP) return vpnIp

            delay(DELAY_BETWEEN_RETRY)
        }
        return NO_IP
    }

    private suspend fun getVpnIpOnce(): String =
        suspendCancellableCoroutine { continuation ->
            accountAPI.clientStatus(STATUS_REQUEST_LONG_TIMEOUT) { clientStatusInfo, errors ->
                val ip = clientStatusInfo?.ip ?: NO_IP
                if (clientStatusInfo?.connected == true) {
                    connectionPrefs.setVpnIp(ip)
                }
                continuation.resume(ip)
            }
        }
}