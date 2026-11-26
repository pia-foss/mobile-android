package com.kape.vpnregionselection.domain.usecase

import com.kape.contracts.ConnectionManager
import com.kape.contracts.SnoozeStatus
import com.kape.contracts.di.Qualifiers
import com.kape.data.vpnserver.VpnServer
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

/** Runs in the app scope, so it carries on after the region screen closes. */
@Factory
class ConnectToVpnRegionUseCase(
    private val connectionManager: ConnectionManager,
    private val snooze: SnoozeStatus,
    private val getOptimalVpnServer: GetOptimalVpnServerUseCase,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    operator fun invoke(region: VpnServer): Job =
        appScope.launch {
            val server = if (region.endpoints.isEmpty()) getOptimalVpnServer() else region
            if (connectionManager.isConnectionInProgress()) {
                connectionManager.disconnect()
            }
            snooze.cancelSnooze()
            connectionManager.connect(
                server = server,
                isManual = true,
                stopCallback = { appScope.launch { connectionManager.disconnect() } },
                showDialog = {},
            )
        }
}