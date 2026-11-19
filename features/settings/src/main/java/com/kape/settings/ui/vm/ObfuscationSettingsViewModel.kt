package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.ExternalAppList
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.SettingsStore
import com.kape.settings.data.CustomObfuscation
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.Transport
import com.kape.settings.domain.IsNumericIpAddress
import com.kape.settings.domain.usecase.ReconnectPrompt
import com.kape.settings.domain.usecase.SetAllowLocalNetworkUseCase
import com.kape.settings.domain.usecase.SetTransportUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

private const val MAX_PORT = 65_535

@KoinViewModel
class ObfuscationSettingsViewModel(
    private val prefs: SettingsStore,
    private val connectionPrefs: ConnectionStore,
    private val setTransportUseCase: SetTransportUseCase,
    private val setAllowLocalNetwork: SetAllowLocalNetworkUseCase,
    private val isNumericIp: IsNumericIpAddress,
    private val router: Router,
    val reconnectPrompt: ReconnectPrompt,
) : ViewModel() {
    val customObfuscation: StateFlow<CustomObfuscation?> = prefs.customObfuscation
    val externalProxyAppEnabled: StateFlow<Boolean> = prefs.isExternalProxyAppEnabled
    val externalProxyAppPackageName: StateFlow<String> = prefs.externalProxyAppPackageName
    val externalProxyAppPort: StateFlow<String> = connectionPrefs.proxyPort
    val shadowsocksObfuscationEnabled: StateFlow<Boolean> = prefs.isShadowsocksObfuscationEnabled
    val isAllowLocalTrafficEnabled: StateFlow<Boolean> = prefs.isAllowLocalTrafficEnabled
    val selectedObfuscationOption: StateFlow<ObfuscationOptions> = prefs.selectedObfuscationOption

    val transport: StateFlow<Transport> =
        prefs.openVpnSettings.map { it.transport }.stateIn(viewModelScope, SharingStarted.Eagerly, Transport.UDP)

    val showExternalProxyTcpPrompt: StateFlow<Boolean> =
        combine(externalProxyAppEnabled, prefs.openVpnSettings) { enabled, settings -> enabled && settings.transport != Transport.TCP }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    fun isNumericIpAddress(ipAddress: String): Boolean = isNumericIp(ipAddress)

    fun isPortValid(port: String): Boolean = port.toIntOrNull() in 1..MAX_PORT

    fun navigateToExternalAppList() = router.updateDestination(ExternalAppList)

    fun setCustomObfuscation(customObfuscation: CustomObfuscation) = save { prefs.setCustomObfuscation(customObfuscation) }

    fun setSelectedObfuscationOption(option: ObfuscationOptions) = save { prefs.setSelectedObfuscationOption(option) }

    fun setExternalProxyPort(port: String?) = save { connectionPrefs.setProxyPort(port) }

    fun setTransport(transport: Transport) =
        save {
            if (setTransportUseCase(transport)) reconnectPrompt.onSettingChanged()
        }

    fun toggleExternalProxyApp(enabled: Boolean) = save { prefs.setExternalProxyAppEnabled(enabled) }

    fun toggleAllowLocalNetwork(enabled: Boolean) = save { setAllowLocalNetwork(enabled) }

    fun toggleShadowsocksObfuscation(enabled: Boolean) = save { prefs.setShadowsocksObfuscationEnabled(enabled) }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

private const val STOP_TIMEOUT_MS = 5_000L