package com.kape.contracts

interface LocationPermissionStatus {
    fun isFineLocationPermissionGranted(): Boolean

    fun isBackgroundLocationPermissionGranted(): Boolean
}