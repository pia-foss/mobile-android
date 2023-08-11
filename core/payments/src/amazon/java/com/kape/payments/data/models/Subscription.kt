package com.kape.payments.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Subscription(val id: String, val legacy: Boolean, val plan: String, val price: Double, var formattedPrice: String? = null) {

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}