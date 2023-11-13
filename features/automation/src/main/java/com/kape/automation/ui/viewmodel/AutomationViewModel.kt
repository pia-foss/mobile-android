package com.kape.automation.ui.viewmodel

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.automation.utils.AutomationStep
import com.kape.location.data.LocationPermissionManager
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.networkmanagement.data.NetworkScanner
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class AutomationViewModel(
    private val router: Router,
    private val locationPermissionManager: LocationPermissionManager,
    private val settingsPrefs: SettingsPrefs,
    private val networkRulesManager: NetworkRulesManager,
    private val networkScanner: NetworkScanner,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<AutomationStep>(AutomationStep.LocationPermission)
    val state: StateFlow<AutomationStep> = _state

    val availableNetworks = networkScanner.scanResults

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
                settingsPrefs.setAutomationEnabled(true)
                _state.emit(AutomationStep.Main)
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

    fun scanNetworks() = networkScanner.scanNetworks()

    fun getNetworkItems() = networkRulesManager.getRules()

    fun updateRule(rule: NetworkItem, behavior: NetworkBehavior) {
        networkRulesManager.updateRule(rule, behavior)
    }

    fun addRule(scanResult: ScanResult, behavior: NetworkBehavior) {
        networkRulesManager.addRule(scanResult, behavior)
    }

    fun removeRule(rule: NetworkItem) {
        networkRulesManager.removeRule(rule)
    }
}