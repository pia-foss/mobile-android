package com.kape.localprefs.api

import com.kape.data.vpnserver.VpnServer
import com.kape.regions.data.ServerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VpnRegionStore {
    suspend fun addToFavorites(serverData: ServerData)

    val favoriteVpnServers: StateFlow<List<ServerData>>

    fun isFavorite(serverData: ServerData): Flow<Boolean>

    suspend fun removeFromFavorites(serverData: ServerData)

    suspend fun selectVpnServer(vpnServer: VpnServer)
}