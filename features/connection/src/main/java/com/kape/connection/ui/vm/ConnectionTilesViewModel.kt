package com.kape.connection.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.domain.usecase.DisplayedServer
import com.kape.connection.domain.usecase.ObserveDisplayedServerUseCase
import com.kape.connection.domain.usecase.ObserveProtocolSettingsUseCase
import com.kape.connection.domain.usecase.ObserveQuickConnectServersUseCase
import com.kape.connection.domain.usecase.QuickConnectItem
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.UsageProvider
import com.kape.data.ConnectionStatus
import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.api.SettingsStore
import com.kape.regions.shadowsocks.domain.usecase.ObserveSelectedShadowsocksServerUseCase
import com.kape.settings.data.ProtocolSettings
import com.kape.snooze.SnoozeHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ConnectionTilesViewModel(
    observeDisplayedServer: ObserveDisplayedServerUseCase,
    observeQuickConnectServers: ObserveQuickConnectServersUseCase,
    observeSelectedShadowsocksServer: ObserveSelectedShadowsocksServerUseCase,
    observeProtocolSettings: ObserveProtocolSettingsUseCase,
    snooze: SnoozeHandler,
    connectionInfo: ConnectionInfoProvider,
    usage: UsageProvider,
    settingsPrefs: SettingsStore,
) : ViewModel() {
    val status: StateFlow<ConnectionStatus> = connectionInfo.status
    val displayedServer: StateFlow<DisplayedServer?> = observeDisplayedServer().state(null)
    val quickConnect: StateFlow<List<QuickConnectItem>> = observeQuickConnectServers().state(emptyList())
    val shadowsocksServer: StateFlow<ShadowsocksServer?> = observeSelectedShadowsocksServer().state(null)
    val protocolSettings: StateFlow<ProtocolSettings?> = observeProtocolSettings().state(null)

    val isSnoozeActive: StateFlow<Boolean> = snooze.isSnoozeActive
    val minutesUntilResume: StateFlow<Int> = snooze.minutesUntilResume

    val publicIp: StateFlow<String> = connectionInfo.publicIp
    val vpnIp: StateFlow<String> = connectionInfo.vpnIp
    val isPortForwardingEnabled: StateFlow<Boolean> = settingsPrefs.isPortForwardingEnabled
    val portForwardingStatus: StateFlow<PortForwardingStatus> = connectionInfo.portForwardingStatus
    val port: StateFlow<String> = connectionInfo.port

    val download: StateFlow<String> = usage.download
    val upload: StateFlow<String> = usage.upload

    private fun <T> Flow<T>.state(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initial)
}

private const val STOP_TIMEOUT_MS = 5_000L