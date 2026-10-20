package com.kape.location.mocklocation

import android.location.Location

/**
 * Mocks the Google Play Services fused location provider.
 *
 * The Play Services implementation lives in the app's google flavor so the dependency
 * never ships in the amazon, noinapp, meta or fdroid builds.
 */
interface FusedLocationMocker {
    fun setMockMode(enabled: Boolean)

    fun setMockLocation(location: Location)
}

class NoOpFusedLocationMocker : FusedLocationMocker {
    override fun setMockMode(enabled: Boolean) = Unit

    override fun setMockLocation(location: Location) = Unit
}