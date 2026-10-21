package com.kape.vpn.location

import android.content.Context
import android.location.Location
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.kape.location.mocklocation.FusedLocationMocker

class GmsFusedLocationMocker(
    context: Context,
) : FusedLocationMocker {
    // Null on devices without Play Services, where there is no fused provider to mock.
    private val client =
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            LocationServices.getFusedLocationProviderClient(context)
        } else {
            null
        }

    override fun setMockMode(enabled: Boolean) {
        client?.setMockMode(enabled)
    }

    override fun setMockLocation(location: Location) {
        client?.setMockLocation(location)
    }
}