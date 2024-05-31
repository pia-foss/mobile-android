package com.kape.dedicatedip.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val MOCKED_RESPONSE = "{\"dedicatedIpCountriesAvailable\":[{\"country_code\":\"au\",\"name\":\"Australia\",\"regions\":[\"Melbourne\",\"Sydney\"],\"new_regions\":[]},{\"country_code\":\"be\",\"name\":\"Belgium\",\"regions\":[],\"new_regions\":[\"Brussels\"]},{\"country_code\":\"ca\",\"name\":\"Canada\",\"regions\":[\"Montreal\",\"Toronto\",\"Vancouver\"],\"new_regions\":[]},{\"country_code\":\"ch\",\"name\":\"Switzerland\",\"regions\":[\"Switzerland\"],\"new_regions\":[]},{\"country_code\":\"de\",\"name\":\"Germany\",\"regions\":[\"Berlin\",\"Frankfurt\"],\"new_regions\":[]},{\"country_code\":\"gb\",\"name\":\"UnitedKingdom\",\"regions\":[\"London\"],\"new_regions\":[]},{\"country_code\":\"jp\",\"name\":\"Japan\",\"regions\":[\"Tokyo\"],\"new_regions\":[]},{\"country_code\":\"se\",\"name\":\"Sweden\",\"regions\":[],\"new_regions\":[\"Stockholm\"]},{\"country_code\":\"sg\",\"name\":\"Singapore\",\"regions\":[\"Singapore\"],\"new_regions\":[]},{\"country_code\":\"us\",\"name\":\"UnitedStates\",\"regions\":[\"Atlanta\",\"California\",\"Chicago\",\"Denver\",\"East\",\"Florida\",\"LasVegas\",\"NewYork\",\"Texas\",\"WashingtonDC\",\"West\"],\"new_regions\":[\"Houston\",\"SiliconValley\"]}]}"

@Serializable
data class SupportedCountries(
    @SerialName("dedicatedIpCountriesAvailable")
    val dedicatedIpCountriesAvailable: List<DedicatedIpCountriesAvailable>,
) {
    @Serializable
    data class DedicatedIpCountriesAvailable(
        @SerialName("country_code")
        val countryCode: String,
        @SerialName("name")
        val name: String,
        @SerialName("new_regions")
        val newRegions: List<String>,
        @SerialName("regions")
        val regions: List<String>,
    )
}