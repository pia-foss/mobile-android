package com.kape.location.mocklocation

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.location.provider.ProviderProperties
import androidx.core.content.ContextCompat
import com.kape.location.data.LocationPermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Singleton

@Singleton
class MockLocationManager(
    private val context: Context,
    private val locationPermissionManager: LocationPermissionManager,
) {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    private val _mockLocationAppSelected = MutableStateFlow(checkMockLocationAppSelected())

    /**
     * Whether PIA is selected as the mock location app in Developer options. This can change
     * while the app is in the background, so call [refreshMockLocationAppSelected] on resume.
     */
    val mockLocationAppSelected: StateFlow<Boolean> = _mockLocationAppSelected

    fun refreshMockLocationAppSelected(): Boolean = checkMockLocationAppSelected().also { _mockLocationAppSelected.value = it }

    fun isLocationPermissionGranted(): Boolean = locationPermissionManager.isFineLocationPermissionGranted()

    fun start(
        latitude: Double,
        longitude: Double,
    ) {
        if (!isLocationPermissionGranted() || !mockLocationAppSelected.value) return
        val intent =
            Intent(context, MockLocationService::class.java)
                .putExtra(MockLocationService.EXTRA_LATITUDE, latitude)
                .putExtra(MockLocationService.EXTRA_LONGITUDE, longitude)
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (_: IllegalStateException) {
            // Foreground service start not allowed from the background (Android 12+).
        } catch (_: SecurityException) {
            // Location foreground service type requires the location permission (Android 14+).
        }
    }

    fun stop() {
        context.stopService(Intent(context, MockLocationService::class.java))
    }

    // Adding a test provider throws a SecurityException unless PIA is the selected mock location
    // app. A dedicated provider name is used so a running MockLocationService isn't disturbed.
    private fun checkMockLocationAppSelected(): Boolean =
        try {
            @Suppress("DEPRECATION")
            locationManager.addTestProvider(
                CHECK_PROVIDER,
                false,
                false,
                false,
                false,
                true,
                true,
                true,
                ProviderProperties.POWER_USAGE_LOW,
                ProviderProperties.ACCURACY_FINE,
            )
            locationManager.removeTestProvider(CHECK_PROVIDER)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            // A previous check left the provider registered, which also means we're allowed.
            locationManager.removeTestProvider(CHECK_PROVIDER)
            true
        }

    private companion object {
        const val CHECK_PROVIDER = "pia_mock_location_check"
    }
}