package com.kape.vpnregionselection.domain.usecase

import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.data.ServerData
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
class ToggleFavoriteVpnRegionUseCase(
    private val vpnRegionPrefs: VpnRegionStore,
) {
    suspend operator fun invoke(server: VpnServer) {
        val favorite = ServerData(server.name, server.isDedicatedIp)
        if (vpnRegionPrefs.isFavorite(favorite).first()) {
            vpnRegionPrefs.removeFromFavorites(favorite)
        } else {
            vpnRegionPrefs.addToFavorites(favorite)
        }
    }
}