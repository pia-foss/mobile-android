package com.kape.settings.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.MockLocationSettings
import com.kape.localprefs.api.SettingsStore
import com.kape.location.mocklocation.MockLocationManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MockLocationViewModel(
    private val prefs: SettingsStore,
    private val mockLocationManager: MockLocationManager,
    private val router: Router,
) : ViewModel() {
    val mockLocationEnabled: StateFlow<Boolean> = prefs.isMockLocationEnabled
    val mockLocationAppSelected: StateFlow<Boolean> = mockLocationManager.mockLocationAppSelected

    fun isLocationPermissionGranted(): Boolean = mockLocationManager.isLocationPermissionGranted()

    fun refreshMockLocationAppSelected(): Boolean = mockLocationManager.refreshMockLocationAppSelected()

    fun toggleMockLocation(enabled: Boolean) {
        viewModelScope.launch { prefs.setMockLocationEnabled(enabled) }
    }

    fun navigateToMockLocationSettings() = router.updateDestination(MockLocationSettings)

    fun onMockLocationAppSelected() {
        toggleMockLocation(true)
        router.navigateBack()
    }
}