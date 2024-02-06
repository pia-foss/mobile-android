package com.kape.vpnregions.domain

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateLatencyUseCase(private val repo: VpnRegionRepository) {

    fun updateLatencies(useCachedLatencies: Boolean): Flow<List<VpnServer>> = flow {
        repo.fetchLatencies(useCachedLatencies).collect {
            emit(it)
        }
    }
}