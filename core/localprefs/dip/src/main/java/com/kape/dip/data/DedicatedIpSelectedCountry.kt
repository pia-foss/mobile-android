package com.kape.dip.data

import kotlinx.serialization.Serializable

@Serializable
data class DedicatedIpSelectedCountry(
    val countryCode: String,
    val countryName: String,
    val regionName: String,
)