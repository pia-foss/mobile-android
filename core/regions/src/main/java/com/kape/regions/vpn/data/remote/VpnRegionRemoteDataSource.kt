package com.kape.regions.vpn.data.remote

import com.kape.regions.vpn.data.VpnRegions
import com.kape.regions.vpn.data.toVpnRegions
import com.privateinternetaccess.regions.RegionsAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

internal interface VpnRegionRemoteDataSource {
    suspend fun fetchRegions(locale: String): VpnRegions?

    suspend fun pingRegions(): Map<String, Long>?
}

@Singleton(binds = [VpnRegionRemoteDataSource::class])
internal class RegionsApiVpnRegionRemoteDataSource(
    private val api: RegionsAPI,
) : VpnRegionRemoteDataSource {
    override suspend fun fetchRegions(locale: String): VpnRegions? =
        suspendCancellableCoroutine { continuation ->
            api.fetchVpnRegions(locale) { response, error ->
                continuation.resume(if (error != null) null else response?.toVpnRegions(isBundled = false))
            }
        }

    override suspend fun pingRegions(): Map<String, Long>? =
        suspendCancellableCoroutine { continuation ->
            api.pingRequests { response, error ->
                continuation.resume(if (error != null) null else response?.associate { it.region to it.latency })
            }
        }
}