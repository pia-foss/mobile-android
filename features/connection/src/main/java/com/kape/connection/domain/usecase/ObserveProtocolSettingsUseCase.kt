package com.kape.connection.domain.usecase

import com.kape.localprefs.api.SettingsStore
import com.kape.settings.data.ProtocolSettings
import com.kape.settings.data.VpnProtocols
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.annotation.Factory

@Factory
class ObserveProtocolSettingsUseCase(
    private val settingsPrefs: SettingsStore,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<ProtocolSettings> =
        settingsPrefs.selectedProtocol.flatMapLatest { protocol ->
            when (protocol) {
                VpnProtocols.WireGuard -> settingsPrefs.wireGuardSettings
                VpnProtocols.OpenVPN -> settingsPrefs.openVpnSettings
                VpnProtocols.Automatic -> settingsPrefs.autoSettings
            }
        }
}