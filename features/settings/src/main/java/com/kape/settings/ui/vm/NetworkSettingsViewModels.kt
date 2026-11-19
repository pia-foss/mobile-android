package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.KillSwitchSettings
import com.kape.localprefs.api.SettingsStore
import com.kape.location.mocklocation.MockLocationManager
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DnsOptions
import com.kape.settings.domain.IsNumericIpAddress
import com.kape.settings.domain.usecase.ReconnectPrompt
import com.kape.settings.domain.usecase.SetAllowLocalNetworkUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class NetworkSettingsViewModel(
    private val prefs: SettingsStore,
    private val setAllowLocalNetwork: SetAllowLocalNetworkUseCase,
    private val isNumericIp: IsNumericIpAddress,
    val reconnectPrompt: ReconnectPrompt,
) : ViewModel() {
    val customDns: StateFlow<CustomDns> = prefs.customDns
    val selectedDnsOption: StateFlow<DnsOptions> = prefs.selectedDnsOption
    val isAllowLocalTrafficEnabled: StateFlow<Boolean> = prefs.isAllowLocalTrafficEnabled
    val maceEnabled: StateFlow<Boolean> = prefs.isMaceEnabled

    fun isPortForwardingEnabled(): StateFlow<Boolean> = prefs.isPortForwardingEnabled

    fun isNumericIpAddress(ipAddress: String): Boolean = isNumericIp(ipAddress)

    fun setCustomDns(customDns: CustomDns) = save { prefs.setCustomDns(customDns) }

    fun setSelectedDnsOption(dnsOptions: DnsOptions) = save { prefs.setSelectedDnsOption(dnsOptions) }

    fun toggleEnablePortForwarding(enabled: Boolean) = save { prefs.setEnablePortForwarding(enabled) }

    fun toggleAllowLocalNetwork(enabled: Boolean) = save { setAllowLocalNetwork(enabled) }

    fun toggleMace(enabled: Boolean) = save { prefs.setMaceEnabled(enabled) }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

@KoinViewModel
class PrivacySettingsViewModel(
    private val prefs: SettingsStore,
    private val router: Router,
    val reconnectPrompt: ReconnectPrompt,
    mockLocationManager: MockLocationManager,
) : ViewModel() {
    val selectedDnsOption: StateFlow<DnsOptions> = prefs.selectedDnsOption
    val maceEnabled: StateFlow<Boolean> = prefs.isMaceEnabled
    val isMockLocationAvailable: Boolean = mockLocationManager.isMockLocationSupported

    fun setSelectedDnsOption(dnsOptions: DnsOptions) {
        viewModelScope.launch { prefs.setSelectedDnsOption(dnsOptions) }
    }

    fun toggleMace(enabled: Boolean) {
        viewModelScope.launch { prefs.setMaceEnabled(enabled) }
    }

    fun navigateToKillSwitch() = router.updateDestination(KillSwitchSettings)
}

@KoinViewModel
class KillSwitchViewModel(
    val reconnectPrompt: ReconnectPrompt,
) : ViewModel()