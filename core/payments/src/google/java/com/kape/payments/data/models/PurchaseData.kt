package com.kape.payments.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PurchaseData(val token: String, val productId: String, val orderId: String) {
    
    override fun toString(): String {
        return Json.encodeToString(this)
    }
}