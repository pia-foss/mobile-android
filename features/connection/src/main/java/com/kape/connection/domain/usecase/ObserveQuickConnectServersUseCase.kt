package com.kape.connection.domain.usecase

import com.kape.connection.model.QuickConnectServer
import com.kape.data.QUICK_CONNECT_MAX_SERVERS
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.data.ServerData
import com.kape.regions.vpn.domain.usecase.ObserveVpnServersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

data class QuickConnectItem(
    val server: VpnServer,
    val isFavorite: Boolean,
)

@Factory
class ObserveQuickConnectServersUseCase(
    private val connectionPrefs: ConnectionStore,
    private val vpnRegionPrefs: VpnRegionStore,
    private val observeVpnServers: ObserveVpnServersUseCase,
) {
    operator fun invoke(): Flow<List<QuickConnectItem>> =
        combine(connectionPrefs.quickConnectServers, vpnRegionPrefs.favoriteVpnServers, observeVpnServers(), ::quickConnectItems)
}

internal fun quickConnectItems(
    recent: List<QuickConnectServer>,
    favorites: List<ServerData>,
    servers: List<VpnServer>,
): List<QuickConnectItem> {
    val favoriteServers =
        favorites.mapNotNull { favorite ->
            servers.firstOrNull {
                it.name == favorite.name &&
                    it.isDedicatedIp == favorite.isDip
            }
        }
    val recentServers =
        recent.asReversed().mapNotNull { used ->
            servers.firstOrNull {
                it.key == used.serverKey &&
                    it.isDedicatedIp == used.isDip
            }
        }
    return (favoriteServers + recentServers)
        .distinctBy { it.key to it.isDedicatedIp }
        .take(QUICK_CONNECT_MAX_SERVERS)
        .map { QuickConnectItem(it, isFavorite = ServerData(it.name, it.isDedicatedIp) in favorites) }
}