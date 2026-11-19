package com.kape.payments.domain

import android.app.Activity
import com.kape.data.model.DipPurchaseData

data class ProductPrice(
    val productId: String,
    val formattedPrice: String,
    val priceInMicros: Long,
    val currencyCode: String,
)

interface DipSubscriptionBilling {
    suspend fun prices(productIds: List<String>): List<ProductPrice>?

    suspend fun unacknowledgedProducts(productIds: List<String>): List<String>?

    suspend fun purchase(
        activity: Activity,
        productId: String,
    ): Result<DipPurchaseData>

    suspend fun pendingPurchase(): DipPurchaseData?

    suspend fun storePendingPurchase(purchase: DipPurchaseData)

    suspend fun clearPendingPurchase()
}