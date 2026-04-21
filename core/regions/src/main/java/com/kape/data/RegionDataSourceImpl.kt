package com.kape.data

import com.kape.shadowsocksregions.domain.ShadowsocksRegionDataSource
import com.kape.vpnregions.domain.VpnRegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.ShadowsocksRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

@Singleton([VpnRegionDataSource::class, ShadowsocksRegionDataSource::class])
class RegionDataSourceImpl(
    private val api: RegionsAPI,
) : VpnRegionDataSource,
    ShadowsocksRegionDataSource,
    KoinComponent {
    // region VpnRegionDataSource
    override suspend fun fetchVpnRegions(locale: String): VpnRegionsResponse? =
        suspendCancellableCoroutine { cont ->
            api.fetchVpnRegions(locale) { response, error ->
                if (error != null) {
                    cont.resume(null)
                    return@fetchVpnRegions
                }
                cont.resume(response)
            }
        }

    override suspend fun pingRequests(): List<RegionLowerLatencyInformation>? =
        suspendCancellableCoroutine { cont ->
            api.pingRequests { response, error ->
                if (error != null) {
                    cont.resume(null)
                    return@pingRequests
                }
                cont.resume(response)
            }
        }
    // endregion

    // region ShadowsocksRegionDataSource
    override suspend fun fetchShadowsocksRegions(locale: String): List<ShadowsocksRegionsResponse> =
        suspendCancellableCoroutine { cont ->
            api.fetchShadowsocksRegions(locale) { response, error ->
                if (error != null) {
                    cont.resume(emptyList())
                    return@fetchShadowsocksRegions
                }
                cont.resume(response)
            }
        }
    // endregion
}