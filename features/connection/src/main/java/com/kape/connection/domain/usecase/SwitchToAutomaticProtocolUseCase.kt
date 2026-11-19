package com.kape.connection.domain.usecase

import com.kape.contracts.ConnectionManager
import com.kape.contracts.di.Qualifiers
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.domain.ConnectionProblemDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class SwitchToAutomaticProtocolUseCase(
    private val problemDetector: ConnectionProblemDetector,
    private val settingsPrefs: SettingsStore,
    private val connectionManager: ConnectionManager,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    operator fun invoke(server: VpnServer?) =
        appScope.launch {
            problemDetector.onSwitchToAutomaticAccepted()
            settingsPrefs.setSelectedProtocol(VpnProtocols.Automatic)
            server?.let { connectionManager.reconnect(it, stopCallback = { appScope.launch { connectionManager.disconnect() } }) }
        }
}