package com.kape.vpnregions.domain

import com.kape.utils.server.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateLatencyUseCase(private val repo: VpnRegionRepository) {

    fun updateLatencies(): Flow<List<VpnServer>> = flow {
        repo.fetchLatencies().collect {
            emit(it)
        }
    }
}