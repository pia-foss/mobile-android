package com.kape.payments.ui

import android.app.Activity
import android.content.Context
import com.kape.payments.data.DipPurchaseData

class DipSubscriptionPaymentProviderImpl(
    private val context: Context,
) : DipSubscriptionPaymentProvider {

    // region DipSubscriptionPaymentProvider
    override fun productsDetails(
        productIds: List<String>,
        callback: (result: Result<List<Pair<String, String>>>) -> Unit,
    ) {
        callback(Result.failure(IllegalStateException("Unsupported")))
    }

    override fun unacknowledgedProductIds(
        productIds: List<String>,
        callback: (result: Result<List<String>>) -> Unit,
    ) {
        callback(Result.failure(IllegalStateException("Unsupported")))
    }

    override fun purchaseProduct(
        activity: Activity,
        productId: String,
        callback: (result: Result<DipPurchaseData>) -> Unit,
    ) {
        callback(Result.failure(IllegalStateException("Unsupported")))
    }
    // endregion
}