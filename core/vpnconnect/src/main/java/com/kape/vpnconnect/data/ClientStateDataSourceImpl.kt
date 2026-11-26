package com.kape.vpnconnect.data

import com.kape.contracts.NetworkStatus
import com.kape.contracts.di.Qualifiers
import com.kape.data.NO_IP
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnconnect.domain.ClientStateDataSource
import com.kape.vpnconnect.utils.DELAY_BETWEEN_RETRY
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

@Singleton(binds = [ClientStateDataSource::class])
class ClientStateDataSourceImpl(
    private val accountAPI: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
    private val networkStatus: NetworkStatus,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) : ClientStateDataSource {
    override suspend fun getPublicIp(): String {
        repeat(3) { _ ->
            val ip = getPublicIpOnce()
            if (ip != NO_IP) {
                return ip
            }
            if (isOffline()) return NO_IP
            delay(DELAY_BETWEEN_RETRY.milliseconds)
        }
        return NO_IP
    }

    override suspend fun getVpnIp(): String {
        repeat(5) { _ ->
            val vpnIp = getVpnIpOnce()
            if (vpnIp != NO_IP && vpnIp != connectionPrefs.getClientIpNow()) {
                return vpnIp
            }
            if (isOffline()) return NO_IP
            delay(DELAY_BETWEEN_RETRY.milliseconds)
        }
        return NO_IP
    }

    private fun isOffline(): Boolean = !networkStatus.isOnline.value

    private suspend fun getVpnIpOnce(): String =
        suspendCancellableCoroutine { continuation ->
            accountAPI.clientStatus { clientStatusInfo, errors ->
                appScope.launch {
                    val ip = clientStatusInfo?.ip ?: NO_IP
                    if (clientStatusInfo?.connected == true) {
                        connectionPrefs.setVpnIp(ip)
                    }
                    continuation.resume(ip)
                }
            }
        }

    private suspend fun getPublicIpOnce(): String =
        suspendCancellableCoroutine { continuation ->
            accountAPI.clientStatus { clientStatusInfo, errors ->
                appScope.launch {
                    val ip = clientStatusInfo?.ip ?: NO_IP
                    if (clientStatusInfo?.connected == false) {
                        connectionPrefs.setClientIp(ip)
                        connectionPrefs.setVpnIp(NO_IP)
                    }
                    continuation.resume(ip)
                }
            }
        }
}