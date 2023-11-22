package com.kape.regions.data

import com.kape.regions.domain.RegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

class RegionDataSourceImpl(private val api: RegionsAPI) : RegionDataSource, KoinComponent {

    override fun fetchRegions(locale: String): Flow<VpnRegionsResponse?> = callbackFlow {
        api.fetchVpnRegions(locale) { response, error ->
            if (error != null) {
                trySend(null)
                return@fetchVpnRegions
            }
            trySend(response)
        }
        awaitClose { channel.close() }
    }

    override fun pingRequests(): Flow<List<RegionLowerLatencyInformation>> = callbackFlow {
        api.pingRequests { response, error ->
            if (error != null) {
                trySend(emptyList())
                return@pingRequests
            }
            trySend(response)
        }
        awaitClose { channel.close() }
    }
}