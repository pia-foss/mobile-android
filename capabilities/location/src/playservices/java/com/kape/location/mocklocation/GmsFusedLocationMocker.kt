package com.kape.location.mocklocation

import android.content.Context
import android.location.Location
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices

class GmsFusedLocationMocker(
    context: Context,
) : FusedLocationMocker {
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