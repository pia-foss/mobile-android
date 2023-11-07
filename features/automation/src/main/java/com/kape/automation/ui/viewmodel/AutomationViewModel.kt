package com.kape.automation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.automation.utils.AutomationStep
import com.kape.location.data.LocationPermissionManager
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
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow<AutomationStep>(AutomationStep.LocationPermission)
    val state: StateFlow<AutomationStep> = _state

    init {
        navigateToNextScreen()
    }

    fun exitAutomation() = router.handleFlow(ExitFlow.Automation)

    fun navigateUp() = router.handleFlow(Back)

    fun navigateToNextScreen() = viewModelScope.launch {
        viewModelScope.launch {
            if (locationPermissionManager.isFineLocationPermissionGranted()
                && locationPermissionManager.isBackgroundLocationPermissionGranted()
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
}