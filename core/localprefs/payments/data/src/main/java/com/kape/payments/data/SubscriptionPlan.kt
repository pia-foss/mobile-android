package com.kape.payments.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SubscriptionPlan(
    val id: String,
    val priceInMicros: Long,
    val formattedPrice: String,
    val billingPeriod: String,
    val freeTrialDuration: String?,
    val plan: String,
) {

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}