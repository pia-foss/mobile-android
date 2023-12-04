package com.kape.vpnregions.domain

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.data.VpnRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetVpnRegionsUseCase(
    private val repo: VpnRegionRepository,
    private val prefs: VpnRegionPrefs,
) {

    fun loadVpnServers(locale: String): Flow<List<VpnServer>> = flow {
        repo.fetchVpnRegions(locale).collect {
            emit(it)
        }
    }

    fun getFavoriteVpnServers(): List<String> = prefs.getFavoriteVpnServers()

    fun getSelectedVpnServerKey() = prefs.getSelectedVpnServerKey()

    fun selectVpnServer(key: String) = prefs.selectVpnServer(key)

    fun getVpnServers() = prefs.getVpnServers()
}