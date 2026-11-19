package com.kape.regions.shadowsocks.data.remote

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

internal interface ShadowsocksRegionRemoteDataSource {
    suspend fun fetchServers(locale: String): List<ShadowsocksServer>
}

@Singleton(binds = [ShadowsocksRegionRemoteDataSource::class])
internal class RegionsApiShadowsocksRemoteDataSource(
    private val api: RegionsAPI,
) : ShadowsocksRegionRemoteDataSource {
    override suspend fun fetchServers(locale: String): List<ShadowsocksServer> =
        suspendCancellableCoroutine { continuation ->
            api.fetchShadowsocksRegions(locale) { response, error ->
                continuation.resume(if (error != null) emptyList() else response.map { it.toDomain() })
            }
        }

    private fun ShadowsocksRegionsResponse.toDomain() =
        ShadowsocksServer(
            iso = iso,
            region = region,
            host = host,
            port = port,
            key = key,
            cipher = cipher,
        )
}