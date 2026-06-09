package com.kape.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DipPurchaseData(
    val token: String,
    val productId: String,
    val orderId: String,
)