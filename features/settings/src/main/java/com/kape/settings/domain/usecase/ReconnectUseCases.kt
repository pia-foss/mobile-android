package com.kape.settings.domain.usecase

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.di.Qualifiers
import com.kape.localprefs.api.ConnectionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

/** Runs in the app scope, so leaving the screen doesn't stop it. */
@Factory
class ReconnectUseCase(
    private val connectionPrefs: ConnectionStore,
    private val connectionManager: ConnectionManager,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    operator fun invoke() =
        appScope.launch {
            val server = connectionPrefs.getSelectedVpnServerNow() ?: return@launch
            connectionManager.reconnect(server, stopCallback = { appScope.launch { connectionManager.disconnect() } })
        }
}

@Factory
class ReconnectPrompt(
    private val connectionInfo: ConnectionInfoProvider,
    private val reconnect: ReconnectUseCase,
) {
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    fun onSettingChanged(): Boolean {
        if (connectionInfo.isConnected()) _isVisible.value = true
        return _isVisible.value
    }

    fun onReconnect() {
        _isVisible.value = false
        reconnect()
    }

    fun onLater() {
        _isVisible.value = false
    }
}