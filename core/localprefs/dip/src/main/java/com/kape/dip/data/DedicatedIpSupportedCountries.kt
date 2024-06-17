package com.kape.dip.data

import com.privateinternetaccess.account.model.response.DipCountriesResponse
import kotlinx.serialization.Serializable

@Serializable
data class DedicatedIpSupportedCountries(
    val persistedTimestamp: Long,
    val supportedCountries: DipCountriesResponse,
)