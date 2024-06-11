package com.kape.payments.ui

import android.content.Context
import com.kape.payments.data.PurchaseData

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

    override fun purchaseProduct(
        productId: String,
        callback: (result: Result<PurchaseData>) -> Unit,
    ) {
        callback(Result.failure(IllegalStateException("Unsupported")))
    }
    // endregion
}