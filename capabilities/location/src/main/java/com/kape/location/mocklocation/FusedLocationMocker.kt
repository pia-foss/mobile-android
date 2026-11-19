package com.kape.location.mocklocation

import android.location.Location

interface FusedLocationMocker {
    fun setMockMode(enabled: Boolean)

    fun setMockLocation(location: Location)
}

class NoOpFusedLocationMocker : FusedLocationMocker {
    override fun setMockMode(enabled: Boolean) = Unit

    override fun setMockLocation(location: Location) = Unit
}