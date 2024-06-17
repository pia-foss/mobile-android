package com.kape.payments.data

import kotlinx.serialization.Serializable

@Serializable
data class DipPurchaseData(val token: String, val productId: String, val orderId: String)