package com.kape.login.domain.usecase

import com.kape.account.domain.AuthenticationRepository
import com.kape.contracts.DebugLogCleaner
import com.kape.contracts.LogoutUseCase
import com.kape.contracts.VpnConnectionControl
import com.kape.login.domain.LocalSessionRepository
import org.koin.core.annotation.Factory

@Factory(binds = [LogoutUseCase::class])
class LogoutUseCaseImpl(
    private val repository: AuthenticationRepository,
    private val localSession: LocalSessionRepository,
    private val vpnConnection: VpnConnectionControl,
    private val debugLogs: DebugLogCleaner,
) : LogoutUseCase {
    override suspend fun logout(): Boolean {
        if (localSession.isAutomationEnabled()) {
            localSession.markDisconnectedByUser()
        }
        if (vpnConnection.isConnected() || vpnConnection.isConnectionInProgress()) {
            debugLogs.scheduleClearOnSessionEnd()
            vpnConnection.disconnect()
        }
        localSession.clear()
        debugLogs.clearDebugLogs()
        return repository.logout()
    }
}