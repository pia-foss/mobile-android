package com.kape.vpn.shortcuts

import com.kape.contracts.ConnectionStatusSource
import com.kape.data.ConnectionStatus
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
class RecordShortcutActionUseCase(
    private val repository: ShortcutRequestRepository,
) {
    suspend operator fun invoke(action: ShortcutAction) = repository.record(action)
}

@Factory
class KeepAppShortcutsUpdatedUseCase(
    private val shortcuts: AppShortcuts,
    private val connectionStatus: ConnectionStatusSource,
) {
    suspend operator fun invoke() {
        connectionStatus.status
            .map { it == ConnectionStatus.CONNECTED }
            .distinctUntilChanged()
            .collect { shortcuts.publish(isConnected = it) }
    }
}