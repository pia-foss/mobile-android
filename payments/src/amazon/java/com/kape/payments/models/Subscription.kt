package com.kape.payments.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Subscription(val id: String, val legacy: Boolean, val plan: String, val price: Double) {

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}