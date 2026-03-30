package com.kape.payments.data

import kotlinx.serialization.Serializable

@Serializable
class Subscription(val id: String, val legacy: Boolean, val plan: String, val price: String, var formattedPrice: String?) {

    override fun toString(): String {
        return ""
    }
}