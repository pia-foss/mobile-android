package com.kape.vpnconnect.domain

import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.portforwarding.domain.PortForwardingUseCase

class StartPortForwardingUseCase(
    private val connectionDataSource: ConnectionDataSource,
    private val portForwardingUseCase: PortForwardingUseCase,
    private val settingsPrefs: SettingsPrefs,
) {
    operator suspend fun invoke(): Boolean {
        return if (settingsPrefs.isPortForwardingEnabled()) {
            portForwardingUseCase.bindPort(connectionDataSource.getVpnToken())
            connectionDataSource.startPortForwarding()
            true
        } else {
            false
        }
    }
}