package com.kape.vpnconnect.domain

import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.portforwarding.domain.PortForwardingUseCase
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class StartPortForwardingUseCase(
    private val connectionDataSource: ConnectionDataSource,
    private val portForwardingUseCase: PortForwardingUseCase,
    private val settingsPrefs: SettingsPrefs,
) {
    operator suspend fun invoke(): Boolean {
        if (!settingsPrefs.isPortForwardingEnabled()) return false
        val context = currentCoroutineContext()
        context.ensureActive()
        portForwardingUseCase.bindPort(connectionDataSource.getVpnToken())
        context.ensureActive() // 👈 critical checkpoint
        connectionDataSource.startPortForwarding()
        return true
    }
}