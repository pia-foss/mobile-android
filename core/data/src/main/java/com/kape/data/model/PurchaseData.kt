package com.kape.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PurchaseData(
    val token: String,
    val productId: String,
    val orderId: String,
) {
    override fun toString(): String = Json.encodeToString(this)
}