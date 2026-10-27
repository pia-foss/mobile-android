package com.kape.location.mocklocation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MockLocationProvider(
    context: Context,
    private val fusedLocationMocker: FusedLocationMocker,
    private val scope: CoroutineScope,
) {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    @Volatile
    private var target: Coordinates? = null
    private var pumpJob: Job? = null
    private var registeredProviders: List<String> = emptyList()

    /**
     * Sets the mock location. On first call, registers the test providers and starts pushing.
     * Returns false if no provider could be registered (caller should stop the service).
     */
    @Synchronized
    fun push(
        latitude: Double,
        longitude: Double,
    ): Boolean {
        if (registeredProviders.isEmpty()) {
            registeredProviders = registerProviders()
            if (registeredProviders.isEmpty()) return false
            fusedLocationMocker.setMockMode(true)
        }
        target = Coordinates(latitude, longitude)
        if (pumpJob?.isActive != true) {
            // Hand the pump its own snapshot so it never reads registeredProviders off the lock.
            val providers = registeredProviders
            pumpJob = scope.launch { pump(providers) }
        }
        return true
    }

    @Synchronized
    fun stop() {
        pumpJob?.cancel()
        pumpJob = null
        target = null
        if (registeredProviders.isEmpty()) return
        registeredProviders.forEach { name ->
            runCatching { locationManager.setTestProviderEnabled(name, false) }
            runCatching { locationManager.removeTestProvider(name) }
        }
        registeredProviders = emptyList()
        fusedLocationMocker.setMockMode(false)
    }

    private suspend fun pump(providers: List<String>) {
        var lastDispatched: Coordinates? = null
        var burstRemaining = 0
        while (true) {
            // Read the target once per round so every provider receives the same fix.
            val coordinates = target
            if (coordinates != null) {
                // Push aggressively after every target change to overwrite cached locations in the
                // fused provider, then settle into the steady cadence once the mock has taken hold.
                if (coordinates != lastDispatched) {
                    burstRemaining = BURST_ITERATIONS
                    lastDispatched = coordinates
                }
                providers.forEach { name -> dispatch(name, coordinates) }
                fusedLocationMocker.setMockLocation(buildLocation(FUSED_PROVIDER_NAME, coordinates))
            }
            val interval =
                if (burstRemaining > 0) {
                    burstRemaining--
                    BURST_INTERVAL_MS
                } else {
                    PUSH_INTERVAL_MS
                }
            delay(interval.milliseconds) // this delay avoids "blocked - too fast" rejections from the fused provider
        }
    }

    private fun dispatch(
        name: String,
        coordinates: Coordinates,
    ) {
        runCatching {
            locationManager.setTestProviderLocation(
                name,
                buildLocation(name, coordinates),
            )
        }
    }

    private fun registerProviders(): List<String> {
        val deviceProviders = locationManager.allProviders
        return TARGET_PROVIDERS
            .filter { name -> name !in DEVICE_SPECIFIC_PROVIDERS || name in deviceProviders }
            .filter { name -> registerProvider(name) }
    }

    private fun registerProvider(name: String): Boolean {
        // Drop any provider left behind by a previous session so it's re-added with known properties.
        runCatching { locationManager.removeTestProvider(name) }
        return try {
            addTestProvider(name)
            locationManager.setTestProviderEnabled(name, true)
            true
        } catch (_: SecurityException) {
            // PIA is no longer the selected mock location app in Developer options.
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    // Criteria constants match the ProviderProperties ones S+ annotates these params with.
    @SuppressLint("WrongConstant")
    private fun addTestProvider(name: String) {
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
                Criteria.POWER_LOW,
                Criteria.ACCURACY_FINE,
            )
        }
    }

    private fun buildLocation(
        provider: String,
        coordinates: Coordinates,
    ) = Location(provider).apply {
        latitude = coordinates.latitude
        longitude = coordinates.longitude
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

    private data class Coordinates(
        val latitude: Double,
        val longitude: Double,
    )

    private companion object {
        const val PUSH_INTERVAL_MS = 1_000L

        // For the first 3s (60 * 50ms) after each location change, we push a burst of mock locations
        // at a faster rate to ensure overwriting potentially cached locations.
        const val BURST_ITERATIONS = 60
        const val BURST_INTERVAL_MS = 50L

        // LocationManager.FUSED_PROVIDER is API 31+; the value is the same on all versions.
        const val FUSED_PROVIDER_NAME = "fused"

        const val GPS_HARDWARE_PROVIDER =
            "gps_hardware" // Works on some devices (e.g. Huawei P9 Lite)

        // addTestProvider accepts any name, so these are only registered when the device actually has them.
        val DEVICE_SPECIFIC_PROVIDERS = setOf(GPS_HARDWARE_PROVIDER)

        val TARGET_PROVIDERS =
            buildList {
                add(LocationManager.GPS_PROVIDER)
                add(GPS_HARDWARE_PROVIDER)
                add(LocationManager.NETWORK_PROVIDER)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            }
    }
}