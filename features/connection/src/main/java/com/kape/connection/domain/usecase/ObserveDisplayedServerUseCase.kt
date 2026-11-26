package com.kape.connection.domain.usecase

import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.ConnectionStore
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import com.kape.regions.vpn.domain.usecase.ObserveIsDefaultVpnServerListUseCase
import com.kape.regions.vpn.domain.usecase.ObserveVpnServersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.annotation.Factory

data class DisplayedServer(
    val server: VpnServer,
    val isOptimal: Boolean,
    val showOptimalLocationInfo: Boolean,
)

@Factory
class ObserveDisplayedServerUseCase(
    private val connectionPrefs: ConnectionStore,
    private val observeVpnServers: ObserveVpnServersUseCase,
    private val observeIsDefaultList: ObserveIsDefaultVpnServerListUseCase,
    private val getOptimalVpnServer: GetOptimalVpnServerUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DisplayedServer> {
        val optimal = observeVpnServers().filter { it.isNotEmpty() }.mapLatest { getOptimalVpnServer() }
        return combine(connectionPrefs.observeSelectedVpnServer(), optimal, observeIsDefaultList()) { selected, optimal, isDefaultList ->
            if (selected != null) {
                DisplayedServer(selected, isOptimal = false, showOptimalLocationInfo = false)
            } else {
                DisplayedServer(optimal, isOptimal = true, showOptimalLocationInfo = isDefaultList)
            }
        }
    }
}