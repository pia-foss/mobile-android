package com.kape.dedicatedip.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val DIP_SIGNUP_MOCKED_RESPONSE = "{\"status\":\"success\",\"available_products\":[{\"id\":\"monthly_pia_1\",\"plan\":\"monthly\",\"price\":\"6.95\",\"legacy\":true},{\"id\":\"yearly_pia_1\",\"plan\":\"yearly\",\"price\":\"39.95\",\"legacy\":true}]}"

@Serializable
data class DedicatedIpSignupPlans(
    @SerialName("available_products")
    val availableProducts: List<AvailableProduct>,
    @SerialName("status")
    val status: String,
) {
    @Serializable
    data class AvailableProduct(
        @SerialName("id")
        val id: String,
        @SerialName("legacy")
        val legacy: Boolean,
        @SerialName("plan")
        val plan: String,
        @SerialName("price")
        val price: String,
    )
}