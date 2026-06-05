package com.kape.login.domain.mobile

import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionManager
import com.kape.contracts.LogoutUseCase
import com.kape.data.auth.ApiResult
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.login.domain.LogoutHandler
import org.koin.core.annotation.Singleton

@Singleton
class LogoutUseCaseImpl(
    private val source: AuthenticationDataSource,
    private val connectionPrefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val connectionManager: ConnectionManager,
    private val logoutHandler: LogoutHandler,
) : LogoutUseCase {
    override suspend fun logout(): Boolean {
        if (settingsPrefs.isAutomationEnabled.value) {
            connectionPrefs.setDisconnectedByUser(true)
        }
        if (connectionManager.isConnectionInProgress()) {
            connectionManager.disconnect().getOrNull()
        }
        return performLogout()
    }

    private suspend fun performLogout(): Boolean {
        clearPrefs()
        return when (source.logout()) {
            ApiResult.Success -> true
            is ApiResult.Error -> false
        }
    }

    private suspend fun clearPrefs() {
        logoutHandler.clearLocalStorage()
    }
}