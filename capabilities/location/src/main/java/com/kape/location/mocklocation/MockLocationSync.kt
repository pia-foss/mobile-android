package com.kape.location.mocklocation

import com.kape.contracts.ConnectionStatusSource
import com.kape.contracts.di.Qualifiers
import com.kape.data.ConnectionStatus
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Singleton
class MockLocationSync(
    private val settingsPrefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val connectionStatus: ConnectionStatusSource,
    private val mockLocationManager: MockLocationManager,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    fun start() {
        appScope.launch {
            combine(
                settingsPrefs.isMockLocationEnabled,
                mockLocationManager.mockLocationAppSelected,
                connectionStatus.status,
                connectionPrefs.selectedVpnServer,
            ) { enabled, appSelected, status, server ->
                val latitude = server?.latitude?.toDoubleOrNull()
                val longitude = server?.longitude?.toDoubleOrNull()
                if (enabled && appSelected && status == ConnectionStatus.CONNECTED && latitude != null && longitude != null) {
                    latitude to longitude
                } else {
                    null
                }
            }.distinctUntilChanged()
                .collect { coordinates ->
                    if (coordinates !=
                        null
                    ) {
                        mockLocationManager.start(coordinates.first, coordinates.second)
                    } else {
                        mockLocationManager.stop()
                    }
                }
        }
    }
}