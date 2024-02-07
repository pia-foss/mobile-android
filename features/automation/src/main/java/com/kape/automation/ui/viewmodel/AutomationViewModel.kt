package com.kape.automation.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.automation.utils.AutomationStep
import com.kape.location.data.LocationPermissionManager
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class AutomationViewModel(
    private val router: Router,
    private val locationPermissionManager: LocationPermissionManager,
    private val settingsPrefs: SettingsPrefs,
    private val networkRulesManager: NetworkRulesManager,
    private val networkConnectionListener: NetworkConnectionListener,
    val broadcastIntent: Intent,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<AutomationStep>(AutomationStep.LocationPermission)
    val state: StateFlow<AutomationStep> = _state

    val availableNetwork = networkConnectionListener.currentSSID

    init {
        navigateToNextScreen()
    }

    fun exitAutomation() = router.handleFlow(ExitFlow.Automation)

    fun navigateUp() = router.handleFlow(Back)

    fun navigateToNextScreen() = viewModelScope.launch {
        viewModelScope.launch {
            if (locationPermissionManager.isFineLocationPermissionGranted() &&
                locationPermissionManager.isBackgroundLocationPermissionGranted()
            ) {
                if (settingsPrefs.isAutomationEnabled().not()) {
                    settingsPrefs.setAutomationEnabled(true)
                    _state.emit(AutomationStep.MainSet)
                } else {
                    _state.emit(AutomationStep.MainUpdate)
                }
            } else {
                if (!locationPermissionManager.isFineLocationPermissionGranted()) {
                    _state.emit(AutomationStep.LocationPermission)
                } else {
                    _state.emit(AutomationStep.EnableBackgroundLocation)
                }
            }
        }
    }

    fun navigateToAddNewRule() = viewModelScope.launch {
        _state.emit(AutomationStep.AddRule)
    }

    fun scanNetworks() = networkConnectionListener.triggerUpdate()

    fun getNetworkItems() = networkRulesManager.getRules()

    fun updateRule(rule: NetworkItem, behavior: NetworkBehavior) {
        networkRulesManager.updateRule(rule, behavior)
    }

    fun addRule(ssid: String, behavior: NetworkBehavior) {
        networkRulesManager.addRule(ssid, behavior)
    }

    fun removeRule(rule: NetworkItem) {
        networkRulesManager.removeRule(rule)
    }
}