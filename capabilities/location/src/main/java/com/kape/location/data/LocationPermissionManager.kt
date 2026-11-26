package com.kape.location.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.kape.contracts.LocationPermissionStatus
import org.koin.core.annotation.Singleton

// Also bound as itself for MockLocationManager.
@Singleton(binds = [LocationPermissionManager::class, LocationPermissionStatus::class])
class LocationPermissionManager(
    private val context: Context,
) : LocationPermissionStatus {
    override fun isFineLocationPermissionGranted() =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    override fun isBackgroundLocationPermissionGranted() =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
}