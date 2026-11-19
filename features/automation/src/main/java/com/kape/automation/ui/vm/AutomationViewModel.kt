package com.kape.automation.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.automation.domain.AutomationToggleResult
import com.kape.automation.domain.EditNetworkRulesUseCase
import com.kape.automation.domain.EnableAutomationUseCase
import com.kape.automation.domain.NetworkObserver
import com.kape.automation.domain.ObserveAutomationEnabledUseCase
import com.kape.automation.domain.ObserveNetworkRulesUseCase
import com.kape.automation.domain.ToggleAutomationUseCase
import com.kape.contracts.LocationPermissionStatus
import com.kape.contracts.Router
import com.kape.data.AutomationAddRule
import com.kape.data.AutomationBackgroundLocation
import com.kape.data.AutomationLocation
import com.kape.data.AutomationMain
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class AutomationState(
    val locationPermissionGranted: Boolean,
    val backgroundLocationPermissionGranted: Boolean,
)

/** Automation needs location access, including in the background, since Android only gives Wi-Fi names to apps that have it. */
@KoinViewModel
class AutomationViewModel(
    private val router: Router,
    private val locationPermissions: LocationPermissionStatus,
    private val networkObserver: NetworkObserver,
    private val enableAutomation: EnableAutomationUseCase,
    private val toggleAutomation: ToggleAutomationUseCase,
    private val editRules: EditNetworkRulesUseCase,
    observeAutomationEnabled: ObserveAutomationEnabledUseCase,
    observeRules: ObserveNetworkRulesUseCase,
) : ViewModel() {
    val isAutomationEnabled: StateFlow<Boolean> = observeAutomationEnabled()

    private val _state =
        MutableStateFlow(
            AutomationState(
                locationPermissionGranted = locationPermissions.isFineLocationPermissionGranted(),
                backgroundLocationPermissionGranted = locationPermissions.isBackgroundLocationPermissionGranted(),
            ),
        )
    val state: StateFlow<AutomationState> = _state.asStateFlow()

    val availableNetwork: StateFlow<String?> = networkObserver.ssid
    val rules: Flow<List<NetworkItem>> = observeRules()

    fun onLocationPermissionGranted() {
        _state.value = _state.value.copy(locationPermissionGranted = true)
        router.updateDestination(AutomationBackgroundLocation)
    }

    fun onBackgroundLocationPermissionGranted() {
        viewModelScope.launch {
            enableAutomation()
            onAutomationEnabled()
            router.updateDestination(AutomationMain)
        }
    }

    fun onAutomationToggled() {
        viewModelScope.launch {
            when (toggleAutomation()) {
                AutomationToggleResult.Enabled -> onAutomationEnabled()
                AutomationToggleResult.Disabled -> Unit
                AutomationToggleResult.MissingLocationPermission -> navigateToMissingPermission()
            }
        }
    }

    fun updateRule(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    ) {
        viewModelScope.launch { editRules.update(rule, behavior) }
    }

    fun addRule(
        ssid: String,
        behavior: NetworkBehavior,
    ) {
        viewModelScope.launch { editRules.add(ssid, behavior) }
    }

    fun removeRule(rule: NetworkItem) {
        viewModelScope.launch { editRules.remove(rule) }
    }

    fun scanNetworks() = networkObserver.reapplyRules()

    fun navigateToAutomationAddNewRule() = router.updateDestination(AutomationAddRule)

    fun navigateToNextScreen() {
        if (arePermissionsGranted()) router.updateDestination(AutomationMain) else navigateToMissingPermission()
    }

    private fun onAutomationEnabled() {
        _state.value = AutomationState(locationPermissionGranted = true, backgroundLocationPermissionGranted = true)
    }

    private fun arePermissionsGranted() =
        locationPermissions.isFineLocationPermissionGranted() && locationPermissions.isBackgroundLocationPermissionGranted()

    private fun navigateToMissingPermission() {
        router.updateDestination(
            if (locationPermissions.isFineLocationPermissionGranted()) AutomationBackgroundLocation else AutomationLocation,
        )
    }
}