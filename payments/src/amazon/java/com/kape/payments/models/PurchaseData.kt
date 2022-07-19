package com.kape.payments.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PurchaseData(val userId: String, val receiptId: String) {

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}