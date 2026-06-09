package com.kape.automation.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.AutomationAddRule
import com.kape.data.AutomationBackgroundLocation
import com.kape.data.AutomationLocation
import com.kape.data.AutomationMain
import com.kape.data.DI
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.location.data.LocationPermissionManager
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkRulesManager
import com.kape.utils.AutomationManager
import com.kape.utils.NetworkConnectionListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class AutomationViewModel(
    private val router: Router,
    private val locationPermissionManager: LocationPermissionManager,
    private val settingsPrefs: SettingsPrefs,
    private val networkRulesManager: NetworkRulesManager,
    private val networkConnectionListener: NetworkConnectionListener,
    @Named(DI.RULES_UPDATED_INTENT) private val broadcastIntent: Intent,
    private val automationManager: AutomationManager,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val isAutomationEnabled = settingsPrefs.isAutomationEnabled

    private val _state =
        MutableStateFlow(
            AutomationState(
                locationPermissionGranted = locationPermissionManager.isFineLocationPermissionGranted(),
                backgroundLocationPermissionGranted = locationPermissionManager.isBackgroundLocationPermissionGranted(),
            ),
        )
    val state = _state.asStateFlow()
    val availableNetwork = networkConnectionListener.ssid
    val rules = networkRulesManager.getRules()

    fun onLocationPermissionGranted() {
        _state.update { it.copy(locationPermissionGranted = true) }
        navigateToAutomationBackgroundLocation()
    }

    fun onBackgroundLocationPermissionGranted(context: Context) {
        viewModelScope.launch(ioDispatcher) {
            settingsPrefs.setAutomationEnabled(true)
            automationManager.startAutomationService()
            sendBroadcast(context)
            _state.update {
                it.copy(
                    locationPermissionGranted = true,
                    backgroundLocationPermissionGranted = true,
                )
            }
            navigateToAutomationMain()
        }
    }

    fun onAutomationToggled(context: Context) {
        if (settingsPrefs.isAutomationEnabled.value) {
            disableAutomation()
        } else {
            viewModelScope.launch(ioDispatcher) {
                if (areLocationPermissionsGranted()) {
                    settingsPrefs.setAutomationEnabled(true)
                    automationManager.startAutomationService()
                    sendBroadcast(context)
                    _state.update {
                        it.copy(
                            locationPermissionGranted = true,
                            backgroundLocationPermissionGranted = true,
                        )
                    }
                } else {
                    navigateToLocationPermissionRequests()
                }
            }
        }
    }

    fun sendBroadcast(context: Context) = context.sendBroadcast(broadcastIntent)

    private fun disableAutomation() =
        viewModelScope.launch(ioDispatcher) {
            settingsPrefs.setAutomationEnabled(false)
            automationManager.stopAutomationService()
        }

    private fun areLocationPermissionsGranted() =
        locationPermissionManager.isFineLocationPermissionGranted() &&
            locationPermissionManager.isBackgroundLocationPermissionGranted()

    fun scanNetworks() = networkConnectionListener.triggerUpdate()

    fun updateRule(
        rule: NetworkItem,
        behavior: NetworkBehavior,
    ) = viewModelScope.launch(ioDispatcher) {
        networkRulesManager.updateRule(rule, behavior)
    }

    fun addRule(
        ssid: String,
        behavior: NetworkBehavior,
    ) = viewModelScope.launch(ioDispatcher) {
        networkRulesManager.addRule(ssid, behavior)
    }

    fun removeRule(rule: NetworkItem) =
        viewModelScope.launch(ioDispatcher) {
            networkRulesManager.removeRule(rule)
        }

    private fun navigateToAutomationBackgroundLocation() =
        router.updateDestination(
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
    val locationPermissionGranted: Boolean,
    val backgroundLocationPermissionGranted: Boolean,
)