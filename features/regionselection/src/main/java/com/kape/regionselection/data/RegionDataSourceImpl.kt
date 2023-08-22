package com.kape.regionselection.data

import com.kape.regionselection.domain.RegionDataSource
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.RegionsAPI
import com.privateinternetaccess.regions.model.RegionsResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

class RegionDataSourceImpl(private val api: RegionsAPI) : RegionDataSource, KoinComponent {

    override fun fetchRegions(locale: String): Flow<RegionsResponse?> = callbackFlow {
        api.fetchRegions(locale) { response, error ->
            if (error.isNotEmpty()) {
                trySend(null)
                return@fetchRegions
            }
            trySend(response)
        }
        awaitClose { channel.close() }
    }

    override fun pingRequests(): Flow<List<RegionLowerLatencyInformation>> = callbackFlow {
        api.pingRequests { response, error ->
            if (error.isNotEmpty()) {
                trySend(emptyList())
                return@pingRequests
            }
            trySend(response)
        }
        awaitClose { channel.close() }
    }
}