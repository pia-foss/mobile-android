package com.kape.vpnregions.domain

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetVpnRegionsUseCase(private val repo: VpnRegionRepository) {

    fun loadVpnServers(locale: String): Flow<List<VpnServer>> = flow {
        repo.fetchVpnRegions(locale).collect {
            emit(it)
        }
    }
}