package com.kape.automation.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import com.kape.location.data.LocationPermissionManager
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.router.AutomationAddRule
import com.kape.router.AutomationBackgroundLocation
import com.kape.router.AutomationLocation
import com.kape.router.AutomationMain
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.utils.AutomationManager
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent

class AutomationViewModel(
    private val router: Router,
    private val locationPermissionManager: LocationPermissionManager,
    private val settingsPrefs: SettingsPrefs,
    private val networkRulesManager: NetworkRulesManager,
    private val networkConnectionListener: NetworkConnectionListener,
    private val broadcastIntent: Intent,
    private val automationManager: AutomationManager,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<AutomationState>(
        AutomationState(
            isEnabled = settingsPrefs.isAutomationEnabled(),
            locationPermissionGranted = locationPermissionManager.isFineLocationPermissionGranted(),
            backgroundLocationPermissionGranted = locationPermissionManager.isBackgroundLocationPermissionGranted(),
            rules = networkRulesManager.getRules(),
        ),
    )
    val automationState = _state.asStateFlow()

    val availableNetwork = networkConnectionListener.currentSSID

    fun onLocationPermissionGranted() {
        _state.update { it.copy(locationPermissionGranted = true) }
    }

    fun onBackgroundLocationPermissionGranted(context: Context) {
        settingsPrefs.setAutomationEnabled(true)
        automationManager.startAutomationService()
        sendBroadcast(context)
        _state.update {
            it.copy(
                isEnabled = true,
                locationPermissionGranted = true,
                backgroundLocationPermissionGranted = true,
            )
        }
        navigateToAutomationMain()
    }

    fun onAutomationToggled(context: Context) {
        if (settingsPrefs.isAutomationEnabled()) {
            disableAutomation()
            _state.update { it.copy(isEnabled = false) }
        } else {
            if (areLocationPermissionsGranted()) {
                settingsPrefs.setAutomationEnabled(true)
                automationManager.startAutomationService()
                sendBroadcast(context)
                _state.update {
                    it.copy(
                        isEnabled = true,
                        locationPermissionGranted = true,
                        backgroundLocationPermissionGranted = true,
                    )
                }
            } else {
                navigateToLocationPermissionRequests()
            }
        }
    }

    fun sendBroadcast(context: Context) = context.sendBroadcast(broadcastIntent)

    private fun disableAutomation() {
        settingsPrefs.setAutomationEnabled(false)
        automationManager.stopAutomationService()
    }

    private fun areLocationPermissionsGranted() =
        locationPermissionManager.isFineLocationPermissionGranted() &&
                locationPermissionManager.isBackgroundLocationPermissionGranted()

    fun scanNetworks() = networkConnectionListener.triggerUpdate()

    fun updateRule(rule: NetworkItem, behavior: NetworkBehavior) {
        networkRulesManager.updateRule(rule, behavior)
        _state.update { it.copy(rules = networkRulesManager.getRules()) }
    }

    fun addRule(ssid: String, behavior: NetworkBehavior) {
        networkRulesManager.addRule(ssid, behavior)
        _state.update { it.copy(rules = networkRulesManager.getRules()) }
    }

    fun removeRule(rule: NetworkItem) {
        networkRulesManager.removeRule(rule)
        _state.update { it.copy(rules = networkRulesManager.getRules()) }
    }

    private fun navigateToAutomationLocation() = router.updateDestination(AutomationLocation)
    private fun navigateToAutomationBackgroundLocation() = router.updateDestination(
        AutomationBackgroundLocation,
    )

    private fun navigateToAutomationMain() = router.updateDestination(AutomationMain)
    fun navigateToAutomationAddNewRule() = router.updateDestination(AutomationAddRule)
    private fun navigateToLocationPermissionRequests() {
        if (!locationPermissionManager.isFineLocationPermissionGranted()) {
            router.updateDestination(AutomationLocation)
        } else {
            router.updateDestination(AutomationBackgroundLocation)
        }
    }

    fun navigateToNextScreen() {
        if (areLocationPermissionsGranted()) {
            router.updateDestination(AutomationMain)
        } else {
            navigateToLocationPermissionRequests()
        }
    }
}

data class AutomationState(
    val isEnabled: Boolean,
    val locationPermissionGranted: Boolean,
    val backgroundLocationPermissionGranted: Boolean,
    val rules: List<NetworkItem>,
)