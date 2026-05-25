package com.kape.vpnconnect.data

import com.kape.data.NO_IP
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CsiPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.utils.DELAY_BETWEEN_RETRY
import com.privateinternetaccess.account.AndroidAccountAPI
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
    override suspend fun getPublicIp(): String {
        repeat(3) { _ ->
            val ip = getPublicIpOnce()
            println("--- get public IP: $ip")
            if (ip != NO_IP) {
                connectionPrefs.setClientIp(ip)
                return ip
            }

            delay(DELAY_BETWEEN_RETRY)
        }
        return NO_IP
    }

    override suspend fun getVpnIp(): String {
        repeat(5) { _ ->
            val vpnIp = getVpnIpOnce()
            println("--- get vpnIp: $vpnIp")
            if (vpnIp != NO_IP && vpnIp != connectionPrefs.clientIp.value) {
                connectionPrefs.setVpnIp(vpnIp)
                return vpnIp
            }

            delay(DELAY_BETWEEN_RETRY)
        }
        return NO_IP
    }

    private suspend fun getVpnIpOnce(): String =
        suspendCancellableCoroutine { continuation ->
            accountAPI.clientStatus { clientStatusInfo, errors ->
                val ip = clientStatusInfo?.ip ?: NO_IP
                if (clientStatusInfo?.connected == true) {
                    connectionPrefs.setVpnIp(ip)
                }
                continuation.resume(ip)
            }
        }

    private suspend fun getPublicIpOnce(): String =
        suspendCancellableCoroutine { continuation ->
            accountAPI.clientStatus { clientStatusInfo, errors ->
                val ip = clientStatusInfo?.ip ?: NO_IP
                if (clientStatusInfo?.connected == false) {
                    connectionPrefs.setClientIp(ip)
                    connectionPrefs.setVpnIp(NO_IP)
                }
                continuation.resume(ip)
            }
        }
}