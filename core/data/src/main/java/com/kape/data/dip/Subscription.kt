package com.kape.data.dip

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Subscription(
    val id: String,
    val legacy: Boolean,
    val plan: String,
    val price: String,
    var formattedPrice: String?,
) {
    override fun toString(): String = Json.encodeToString(this)
}