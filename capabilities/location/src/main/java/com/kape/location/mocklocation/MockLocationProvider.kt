package com.kape.location.mocklocation

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock

class MockLocationProvider(
    context: Context,
    private val fusedLocationMocker: FusedLocationMocker,
) {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    private val providers =
        buildList {
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
        }

    fun start() {
        providers.forEach { name ->
            try {
                locationManager.removeTestProvider(name)
            } catch (_: Exception) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val props =
                    ProviderProperties
                        .Builder()
                        .setHasAltitudeSupport(true)
                        .setHasSpeedSupport(true)
                        .setHasBearingSupport(true)
                        .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                        .setAccuracy(ProviderProperties.ACCURACY_FINE)
                        .build()
                locationManager.addTestProvider(name, props)
            } else {
                @Suppress("DEPRECATION")
                locationManager.addTestProvider(
                    name,
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
            }
            locationManager.setTestProviderEnabled(name, true)
        }
        fusedLocationMocker.setMockMode(true)
    }

    fun push(
        lat: Double,
        lng: Double,
    ) {
        providers.forEach { name ->
            locationManager.setTestProviderLocation(name, buildLocation(name, lat, lng))
        }
        fusedLocationMocker.setMockLocation(buildLocation(FUSED_PROVIDER_NAME, lat, lng))
    }

    fun stop() {
        providers.forEach { name ->
            try {
                locationManager.setTestProviderEnabled(name, false)
                locationManager.removeTestProvider(name)
            } catch (_: Exception) {
            }
        }
        fusedLocationMocker.setMockMode(false)
    }

    private fun buildLocation(
        provider: String,
        lat: Double,
        lng: Double,
    ) = Location(provider).apply {
        latitude = lat
        longitude = lng
        altitude = 0.0
        accuracy = 3f
        speed = 0f
        bearing = 0f
        time = System.currentTimeMillis()
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            verticalAccuracyMeters = 3f
            speedAccuracyMetersPerSecond = 0.1f
            bearingAccuracyDegrees = 0.1f
        }
    }

    companion object {
        // LocationManager.FUSED_PROVIDER is API 31+; the value is the same on all versions.
        private const val FUSED_PROVIDER_NAME = "fused"
    }
}