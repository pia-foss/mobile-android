package com.kape.payments.ui

import android.app.Activity
import com.kape.payments.data.DipPurchaseData

interface DipSubscriptionPaymentProvider {

    /**
     * It queries the productIds against those known by the billing library and returns the known
     * ones, along with their formatted price.
     *
     * The first parameter of the pair is the product id.
     * The second parameter is the formatted price.
     *
     * @param productIds
     * @param callback
     */
    fun productsDetails(
        productIds: List<String>,
        callback: (result: Result<List<Pair<String, String>>>) -> Unit,
    )

    /**
     * It triggers the purchase flow for given product id.
     *
     * @param activity
     * @param productId
     * @param callback
     */
    fun purchaseProduct(
        activity: Activity,
        productId: String,
        callback: (result: Result<DipPurchaseData>) -> Unit,
    )
}