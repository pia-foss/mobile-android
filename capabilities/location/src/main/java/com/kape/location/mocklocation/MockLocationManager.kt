package com.kape.location.mocklocation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationManager
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

    val mockLocationAppSelected: StateFlow<Boolean> = _mockLocationAppSelected

    val isMockLocationSupported: Boolean
        get() = locationManager.allProviders.any { it != LocationManager.PASSIVE_PROVIDER && it != CHECK_PROVIDER }

    fun refreshMockLocationAppSelected(): Boolean = checkMockLocationAppSelected().also { _mockLocationAppSelected.value = it }

    fun isLocationPermissionGranted(): Boolean = locationPermissionManager.isFineLocationPermissionGranted()

    fun start(
        latitude: Double,
        longitude: Double,
    ) {
        if (!isMockLocationSupported || !isLocationPermissionGranted() || !mockLocationAppSelected.value) return
        val intent =
            Intent(context, MockLocationService::class.java)
                .putExtra(MockLocationService.EXTRA_LATITUDE, latitude)
                .putExtra(MockLocationService.EXTRA_LONGITUDE, longitude)
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (_: IllegalStateException) {
        } catch (_: SecurityException) {
        }
    }

    fun stop() {
        context.stopService(Intent(context, MockLocationService::class.java))
    }

    // Adding a test provider throws a SecurityException unless PIA is the selected mock location
    // app. A dedicated provider name is used so a running MockLocationService isn't disturbed.
    @SuppressLint("WrongConstant")
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
                Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE,
            )
            locationManager.removeTestProvider(CHECK_PROVIDER)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            locationManager.removeTestProvider(CHECK_PROVIDER)
            true
        }

    private companion object {
        const val CHECK_PROVIDER = "pia_mock_location_check"
    }
}